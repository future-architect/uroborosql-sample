package jp.co.future.uroborosql.sample;

import java.nio.file.Paths;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Arrays;

import jp.co.future.uroborosql.SqlAgentProviderImpl;
import jp.co.future.uroborosql.UroboroSQL;
import jp.co.future.uroborosql.config.SqlConfig;
import jp.co.future.uroborosql.context.ExecutionContextProviderImpl;
import jp.co.future.uroborosql.event.EventListenerHolder;
import jp.co.future.uroborosql.event.subscriber.DumpResultEventSubscriber;
import jp.co.future.uroborosql.exception.DataNonUniqueException;
import jp.co.future.uroborosql.sample.type.Gender;
import jp.co.future.uroborosql.store.SqlResourceManagerImpl;
import jp.co.future.uroborosql.utils.CaseFormat;

/**
 * uroboroSQL SQLFile API Sample
 *
 * @author H.Sugimoto
 */
public class SqlFileApiSample extends AbstractApiSample {
	private final SqlConfig config;

	public SqlFileApiSample() {
		// create SqlConfig
		config = UroboroSQL
				.builder("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
				// ExecutionContextProviderの設定（Enum定数パッケージ設定の追加）
				.setExecutionContextProvider(
						new ExecutionContextProviderImpl()
								.setEnumConstantPackageNames(Arrays.asList(Gender.class.getPackage().getName()))
								.setDefaultResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE))
				// SqlAgentProviderの設定（Queryの戻り値のMapのキー文字列のデフォルトCaseFormat設定の追加）
				.setSqlAgentProvider(new SqlAgentProviderImpl()
						.setDefaultMapKeyCaseFormat(CaseFormat.CAMEL_CASE)
						.setForceUpdateWithinTransaction(true))
				.setSqlResourceManager(new SqlResourceManagerImpl())
				.setEventListenerHolder(new EventListenerHolder().addEventSubscriber(new DumpResultEventSubscriber()))
				.build();
	}

	public void run() throws Exception {

		log("SqlFileApiSample start.");

		setupTableAndData();

		collect();

		findFirst();

		findOne();

		stream();

		queryWith();

		transaction();

		update();

		updateWith();

		batchInsert();

		log("SqlFileApiSample end.");
	}

	/**
	 * setup tables and initial data
	 *
	 * @param agent SqlAgent
	 */
	private void setupTableAndData() {
		try (var agent = config.agent()) {
			agent.required(() -> {
				// create table :  テーブル作成
				var createCount = agent.update("ddl/create_tables").count();
				log("ddl/create_tables count={}", createCount);

				// setup data : 初期データ挿入
				var setupCount = agent.update("setup/insert_data").count();
				log("setup/insert_data count={}", setupCount);
			});
		}
	}

	/**
	 * query#collect() method sample
	 */
	private void collect() {
		try (var agent = config.agent()) {
			// select department data (collect) : 部署データ検索（collect API）
			log("select department data (collect)");
			// no parameter : バインドパラメータ指定なしで検索
			var deps = agent.query("department/select_department").collect();
			deps.forEach(data -> log(toS(data)));

			// add bind parameter : バインドパラメータを設定して検索
			log("select department data (collect) set param(dept_no=1)");
			agent.query("department/select_department").param("deptNo", 1).collect().forEach(data -> log(toS(data)));
		}
	}

	/**
	 * query#findFirst() method sample
	 */
	private void findFirst() {
		try (var agent = config.agent()) {
			// find first : findFirstを使用した先頭1件検索
			log("select first employee data.(Optional)");
			agent.query("employee/select_employee").findFirst().ifPresent(m -> log(toS(m)));
		}
	}

	/**
	 * query#findFirst() method sample
	 */
	private void findOne() {
		try (var agent = config.agent()) {
			// find one : findOneを使用した先頭1件検索
			log("select first employee data with single row validation. (Optional)");

			try {
				agent.query("employee/select_employee").findOne();
			} catch (DataNonUniqueException ex) {
				log("findOne throw DataNonUniqueException when selected data size > 1.");
			}
			agent.query("employee/select_employee").param("empNo", 1).findOne().ifPresent(m -> log(toS(m)));
		}
	}

	/**
	 * query#stream() method sample
	 */
	private void stream() {
		try (var agent = config.agent()) {
			// select employee data (stream) : 従業員データ検索（stream API）
			log("select employee data (stream)");
			// no parameter : バインドパラメータ指定なしで検索（BEGIN-ENDで囲まれた範囲内のIF条件がすべてfalseのため、BEGIN-ENDの中が削除される）
			agent.query("employee/select_employee").stream().forEach(m -> log(toS(m)));

			// add bind date parameter : バインドパラメータ（日付型）を指定して検索
			agent.query("employee/select_employee").param("birthDateFrom", LocalDate.of(1990, 1, 1)).stream()
					.forEach(m -> log(toS(m)));

			// add bind list parameter : バインドパラメータ（IN句用）を指定して検索
			agent.query("employee/select_employee").param("genderList", Arrays.asList(Gender.FEMALE)).stream()
					.forEach(m -> log(toS(m)));

			// use sql enum constant : SQL上でEnum定数を使用した検索
			agent.query("employee/select_employee").param("female", true).stream().forEach(m -> log(toS(m)));
		}
	}

	/**
	 * agent#queryWith() method sample
	 */
	private void queryWith() {
		try (var agent = config.agent()) {
			// Use SQL literal instead of SQL file : sqlファイルの代わりにSQL文字列を使用した検索
			log("select employee data.");
			agent.queryWith("select * from employee").collect().forEach(m -> log(toS(m)));

			log("select employee data with param.");
			agent.queryWith("select * from employee where emp_no = /*empNo*/0").param("empNo", 1).collect()
					.forEach(m -> log(toS(m)));
		}
	}

	/**
	 * transaction api sample
	 */
	private void transaction() {
		try (var agent = config.agent()) {
			// select with bind parameter : バインドパラメータを設定して検索
			log("select employee data (collect) set param(emp_no=1)");
			var empNo = 1L;
			var beforeEmp = agent.query("employee/select_employee").param("empNo", empNo).first();
			log(toS(beforeEmp));

			agent.required(() -> {
				agent.update("employee/update_employee").param("empNo", empNo)
						.param("birthDate", LocalDate.of(1971, 12, 1)).count();

				var updateEmp = agent.query("employee/select_employee").param("empNo", empNo).first();
				log(toS(updateEmp));

				agent.setRollbackOnly();
			});
			var afterEmp = agent.query("employee/select_employee").param("empNo", empNo).first();
			log(toS(afterEmp));
		}
	}

	/**
	 * update api sample
	 */
	private void update() {
		try (var agent = config.agent()) {
			log("select department data (stream)");
			agent.query("department/select_department").stream().forEach(e -> log(toS(e)));

			agent.required(() -> {
				agent.update("department/insert_department")
						.param("deptName", "production")
						.count();

				log("select department data (stream)");
				agent.query("department/select_department").stream().forEach(e -> log(toS(e)));

				// rollback insert data. Keep the data unchanged in other tests.
				agent.setRollbackOnly();
			});
		}
	}

	/**
	 * updateWith api sample
	 */
	private void updateWith() {
		try (var agent = config.agent()) {
			log("select department data (stream)");
			agent.query("department/select_department").stream().forEach(e -> log(toS(e)));

			agent.required(() -> {
				agent.updateWith("delete from department where dept_no = /*deptNo*/1")
						.param("deptNo", 1)
						.count();

				log("select department data (stream)");
				agent.query("department/select_department").stream().forEach(e -> log(toS(e)));

				// rollback insert data. Keep the data unchanged in other tests.
				agent.setRollbackOnly();
			});
		}
	}

	/**
	 * batchInsert api sample
	 */
	private void batchInsert() {
		try (var agent = config.agent()) {
			agent.required(() -> {
				log("delete tables with sql literal");
				// update with sql literal
				log("delete department count={}", agent.updateWith("delete from department").count());
				log("delete employee count={}", agent.updateWith("delete from employee").count());
				log("delete dept_emp count={}", agent.updateWith("delete from dept_emp").count());

				agent.query("department/select_department").stream().forEach(r -> log(toS(r)));
				agent.query("employee/select_employee").stream().forEach(r -> log(toS(r)));
				agent.query("relation/select_dept_emp").stream().forEach(r -> log(toS(r)));

				// batch insert (since v0.5.0)
				log("department/insert_department batch insert.");
				// department
				var deptCount = agent.batch("department/insert_department")
						.paramStream(getDataByFile(Paths.get("src/main/resources/data/department.tsv"))).count();
				log("department/insert_department count={}", deptCount);

				log("employee/insert_employee batch insert.");
				// employee
				// execute by 2 rows
				var empCount = agent.batch("employee/insert_employee")
						.paramStream(getDataByFile(Paths.get("src/main/resources/data/employee.tsv")))
						.by((ctx, row) -> ctx.batchCount() == 2).count();
				log("employee/insert_employee count={}", empCount);

				log("relation/insert_dept_emp batch insert.");
				// dept_emp
				// log message when batch execute.
				var deptEmpCount = agent.batch("relation/insert_dept_emp")
						.paramStream(getDataByFile(Paths.get("src/main/resources/data/dept_emp.tsv")))
						.batchWhen((agt, ctx) -> log("batch execute.")).count();
				log("relation/insert_dept_emp count={}", deptEmpCount);

				agent.query("department/select_department").stream().forEach(r -> log(toS(r)));
				agent.query("employee/select_employee").stream().forEach(r -> log(toS(r)));
				agent.query("relation/select_dept_emp").stream().forEach(r -> log(toS(r)));

				// rollback insert data. Keep the data unchanged in other tests.
				agent.setRollbackOnly();

			});
		}
	}

}
