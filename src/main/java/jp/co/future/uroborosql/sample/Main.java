/**
 *
 */
package jp.co.future.uroborosql.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.future.uroborosql.SqlAgent;
import jp.co.future.uroborosql.config.DefaultSqlConfig;
import jp.co.future.uroborosql.config.SqlConfig;
import jp.co.future.uroborosql.context.SqlContextFactory;
import jp.co.future.uroborosql.fluent.SqlUpdate;
import jp.co.future.uroborosql.sample.type.Gender;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * uroboroSQL Sample Application
 * @author H.Sugimoto
 *
 */
public class Main {
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(final String... args) throws Exception {
		// create SqlConfig
		SqlConfig config = createSqlConfig();

		// create SqlAgent. SqlAgent implements AutoClosable.
		try (SqlAgent agent = config.createAgent()) {

			// create table :  テーブル作成
			int createCount = agent.update("ddl/create_tables").count();
			log("ddl/create_tables count={}", createCount);

			// setup data : 初期データ挿入
			int setupCount = agent.update("setup/insert_data").count();
			log("setup/insert_data count={}", setupCount);

			// select department data (collect) : 部署データ検索（collect API）
			log("select department data (collect)");
			// no parameter : バインドパラメータ指定なしで検索
			List<Map<String, Object>> departments = agent.query("department/select_department").collect();
			departments.forEach(data -> log(toS(data)));

			// add bind parameter : バインドパラメータを設定して検索
			departments = agent.query("department/select_department").param("dept_no", 1001).collect();
			departments.forEach(data -> log(toS(data)));

			// select employee data (stream) : 従業員データ検索（stream API）
			log("select employee data (stream)");
			// no parameter : バインドパラメータ指定なしで検索（BEGIN-ENDで囲まれた範囲内のIF条件がすべてfalseのため、BEGIN-ENDの中が削除される）
			agent.query("employee/select_employee").stream().forEachOrdered(m -> log(toS(m)));

			// add bind date parameter : バインドパラメータ（日付型）を指定して検索
			agent.query("employee/select_employee").paramList("birth_date_from", LocalDate.of(1990, 1, 1)).stream()
					.forEachOrdered(m -> log(toS(m)));

			// add bind list parameter : バインドパラメータ（IN句用）を指定して検索
			agent.query("employee/select_employee").paramList("gender_list", Gender.Female).stream()
					.forEachOrdered(m -> log(toS(m)));

			// use sql enum constant : SQL上でEnum定数を私用した検索
			agent.query("employee/select_employee").param("female", true).stream().forEachOrdered(m -> log(toS(m)));

			log("delete tables with sql literal");
			// update with sql literal
			log("delete dept_emp count={}", agent.updateWith("delete from dept_emp").count());
			log("delete department count={}", agent.updateWith("delete from department").count());
			log("delete employee count={}", agent.updateWith("delete from employee").count());

			log("commit!");
			agent.commit();

			// transaction (requiredNew)
			agent.requiresNew(() -> {

				log("create new transaction.");
				// batch insert
				log("department/insert_department batch insert.");
				// department
				List<Map<String, String>> deptList = getDataByFile(Paths.get("src/main/resources/data/department.tsv"));
				SqlUpdate deptUpdate = agent.update("department/insert_department");
				deptList.forEach(row -> {
					row.forEach(deptUpdate::param);
					deptUpdate.addBatch();
				});
				int[] deptCount = deptUpdate.batch();
				log("department/insert_department count={}",
						ToStringBuilder.reflectionToString(deptCount, ToStringStyle.SIMPLE_STYLE));

				log("employee/insert_employee batch insert.");
				// employee
				List<Map<String, String>> empList = getDataByFile(Paths.get("src/main/resources/data/employee.tsv"));
				SqlUpdate empUpdate = agent.update("employee/insert_employee");
				empList.forEach(row -> {
					row.forEach(empUpdate::param);
					empUpdate.addBatch();
				});
				int[] empCount = empUpdate.batch();
				log("employee/insert_employee count={}",
						ToStringBuilder.reflectionToString(empCount, ToStringStyle.SIMPLE_STYLE));

				log("relation/insert_dept_emp batch insert.");
				// dept_emp
				List<Map<String, String>> deptEmpList = getDataByFile(Paths.get("src/main/resources/data/dept_emp.tsv"));
				SqlUpdate deptEmpUpdate = agent.update("relation/insert_dept_emp");
				deptEmpList.forEach(row -> {
					row.forEach(deptEmpUpdate::param);
					deptEmpUpdate.addBatch();
				});
				int[] deptEmpCount = deptEmpUpdate.batch();
				log("relation/insert_dept_emp count=",
						ToStringBuilder.reflectionToString(deptEmpCount, ToStringStyle.SIMPLE_STYLE));

				log("employee/select_employee in transaction select");
				agent.query("employee/select_employee").stream().forEachOrdered(m -> log(toS(m)));

				log("transaction rollback!");
				agent.setRollbackOnly();
			});
			log("employee/select_employee after transaction select. employee is empty.");
			agent.query("employee/select_employee").stream().forEachOrdered(m -> log(toS(m)));
		}

	}

	/**
	 * Create and setting SqlConfig
	 * @return SqlConfig
	 */
	private static SqlConfig createSqlConfig() {
		SqlConfig config = DefaultSqlConfig.getConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");

		// set Enum Constant
		SqlContextFactory sqlContextFactory = config.getSqlContextFactory();
		sqlContextFactory.setEnumConstantPackageNames(Arrays.asList(Gender.class.getPackage().getName()));
		sqlContextFactory.initialize();

		return config;
	}

	/**
	 * Read TSV file data and convert to List Object.
	 *
	 * @param filePath TSV file path.
	 * @return Data List
	 */
	private static List<Map<String, String>> getDataByFile(final Path filePath) {
		List<Map<String, String>> ans = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(filePath);
			String[] header = lines.get(0).split("\\t");
			lines.remove(0);

			lines.forEach(s -> {
				String[] data = s.split("\\t");
				Map<String, String> row = new HashMap<>(data.length);
				for (int i = 0; i < header.length; i++) {
					row.put(header[i], data[i]);
				}
				ans.add(row);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ans;
	}

	@SuppressWarnings("unchecked")
	private static String toS(final Object obj) {
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			StringBuilder builder = new StringBuilder("{");
			map.forEach((k, v) -> builder.append(k).append("=").append(v).append(","));
			if (!map.isEmpty()) {
				builder.deleteCharAt(builder.length() - 1);
			}
			builder.append("}");
			return builder.toString();
		} else {
			return obj.toString();
		}
	}

	private static void log(final String format, final Object... arguments) {
		log.info(format, arguments);
	}

}
