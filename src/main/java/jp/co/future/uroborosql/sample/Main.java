package jp.co.future.uroborosql.sample;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jp.co.future.uroborosql.SqlAgent;
import jp.co.future.uroborosql.SqlAgentFactoryImpl;
import jp.co.future.uroborosql.UroboroSQL;
import jp.co.future.uroborosql.config.SqlConfig;
import jp.co.future.uroborosql.context.SqlContextFactoryImpl;
import jp.co.future.uroborosql.sample.entity.Department;
import jp.co.future.uroborosql.sample.entity.Employee;
import jp.co.future.uroborosql.sample.type.Gender;
import jp.co.future.uroborosql.utils.CaseFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * uroboroSQL Sample Application
 *
 * @author H.Sugimoto
 */
public class Main {
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(final String... args) throws Exception {
		// create SqlConfig
		SqlConfig config = UroboroSQL
				.builder("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
				.setSqlContextFactory(
						new SqlContextFactoryImpl().setEnumConstantPackageNames(Arrays.asList(Gender.class.getPackage()
								.getName())))
				.setSqlAgentFactory(new SqlAgentFactoryImpl().setDefaultMapKeyCaseFormat(CaseFormat.CAMEL_CASE))
				.build();

		// create SqlAgent. SqlAgent implements AutoClosable.
		try (SqlAgent agent = config.agent()) {

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
			log("select department data (collect) set param(dept_no=1001)");
			departments = agent.query("department/select_department").param("dept_no", 1001).collect();
			departments.forEach(data -> log(toS(data)));

			// with Entity : エンティティによる部署データ検索
			log("select department data with Entity (collect)");
			// no parameter : 条件指定なしで検索
			List<Department> departmentEntities = agent.query(Department.class).collect();
			departmentEntities.forEach(data -> log(toS(data)));

			// add bind parameter : 条件を設定して検索
			log("select department data with Entity (collect) set param(deptNo=1001)");
			departmentEntities = agent.query(Department.class).param("deptNo"/* camelCase */, 1001).collect();
			departmentEntities.forEach(data -> log(toS(data)));

			// find department by dept_no key : キー（dept_no）を指定して、部署データ１件取得（find API）
			log("select department data by key (find)");
			Department department = agent.find(Department.class, 1002).orElse(null);
			log(toS(department));

			// find first : findFirstを使用した先頭1件検索
			log("select first employee data.(Optional)");
			agent.query("employee/select_employee").findFirst().ifPresent(m -> log(toS(m)));

			// select employee data (stream) : 従業員データ検索（stream API）
			log("select employee data (stream)");
			// no parameter : バインドパラメータ指定なしで検索（BEGIN-ENDで囲まれた範囲内のIF条件がすべてfalseのため、BEGIN-ENDの中が削除される）
			agent.query("employee/select_employee").stream().forEachOrdered(m -> log(toS(m)));

			// add bind date parameter : バインドパラメータ（日付型）を指定して検索
			agent.query("employee/select_employee").paramList("birth_date_from", LocalDate.of(1990, 1, 1)).stream()
					.forEachOrdered(m -> log(toS(m)));

			// add bind list parameter : バインドパラメータ（IN句用）を指定して検索
			agent.query("employee/select_employee").paramList("gender_list", Gender.FEMALE).stream()
					.forEachOrdered(m -> log(toS(m)));

			// use sql enum constant : SQL上でEnum定数を使用した検索
			agent.query("employee/select_employee").param("female", true).stream().forEachOrdered(m -> log(toS(m)));

			// with Entity : エンティティによる従業員データ検索（stream API）
			log("select employee data with Entity (stream)");
			// no parameter : バインドパラメータ指定なしで検索
			agent.query(Employee.class).stream().forEachOrdered(m -> log(toS(m)));
			// use LocalDate parameter : LocalDateを使用した検索
			agent.query(Employee.class).param("birthDate", LocalDate.of(1970, 1, 2)).stream()
					.forEachOrdered(m -> log(toS(m)));
			// use enum parameter : Enumを使用した検索
			agent.query(Employee.class).param("gender", Gender.FEMALE).stream().forEachOrdered(m -> log(toS(m)));

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
				// batch insert (new v0.5.0)
				log("department/insert_department batch insert.");
				// department
				List<Map<String, Object>> deptList = getDataByFile(Paths.get("src/main/resources/data/department.tsv"));
				int deptCount = agent.batch("department/insert_department").paramStream(deptList.stream()).count();
				log("department/insert_department count={}", deptCount);

				log("employee/insert_employee batch insert.");
				// employee
				// execute by 2 rows
				List<Map<String, Object>> empList = getDataByFile(Paths.get("src/main/resources/data/employee.tsv"));
				int empCount = agent.batch("employee/insert_employee").paramStream(empList.stream())
						.by((ctx, row) -> ctx.batchCount() == 2).count();
				log("employee/insert_employee count={}", empCount);

				log("relation/insert_dept_emp batch insert.");
				// dept_emp
				// log message when batch execute.
				List<Map<String, Object>> deptEmpList = getDataByFile(Paths.get("src/main/resources/data/dept_emp.tsv"));
				int deptEmpCount = agent.batch("relation/insert_dept_emp").paramStream(deptEmpList.stream())
						.batchWhen((agt, ctx) -> log("batch execute.")).count();
				log("relation/insert_dept_emp count={}", deptEmpCount);

				log("employee/select_employee in transaction select");
				agent.query("employee/select_employee").stream().forEachOrdered(m -> log(toS(m)));

				// insert with Entity
				log("insert with Entity");
				Department informationDept = new Department();
				informationDept.setDeptNo(1005);
				informationDept.setDeptName("information");
				agent.insert(informationDept);

				log(toS(agent.find(Department.class, 1005).get()));

				// update with Entity
				log("update with Entity");
				informationDept.setDeptName("Information System");
				agent.update(informationDept);

				log(toS(agent.find(Department.class, 1005).get()));

				// delete with Entity
				log("delete with Entity");
				agent.delete(informationDept);

				if (!agent.find(Department.class, 1005).isPresent()) {
					log("deptNo = 1005 deleted!!");
				}

				log("transaction rollback!");
				agent.setRollbackOnly();
			});
			log("employee/select_employee after transaction select. employee is empty.");
			agent.query("employee/select_employee").stream().forEachOrdered(m -> log(toS(m)));
		}
	}

	/**
	 * Read TSV file data and convert to List Object.
	 *
	 * @param filePath TSV file path.
	 * @return Data List
	 */
	private static List<Map<String, Object>> getDataByFile(final Path filePath) {
		try {
			List<String> lines = Files.readAllLines(filePath);
			String[] header = lines.get(0).split("\\t");
			return lines.stream()
					.skip(1)
					.map(s -> s.split("\\t"))
					.map(data -> IntStream.range(0, header.length)
							.<Map<String, Object>> collect(HashMap::new, (row, i) -> row.put(header[i], data[i]),
									Map::putAll))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
			throw new UncheckedIOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static String toS(final Object obj) {
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			return map.entrySet().stream()
					.map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining(",", "{", "}"));
		} else {
			return obj.toString();
		}
	}

	private static void log(final String format, final Object... arguments) {
		log.info(format, arguments);
	}

}
