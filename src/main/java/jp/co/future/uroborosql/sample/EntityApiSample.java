package jp.co.future.uroborosql.sample;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.future.uroborosql.SqlAgent;
import jp.co.future.uroborosql.SqlAgentFactoryImpl;
import jp.co.future.uroborosql.UroboroSQL;
import jp.co.future.uroborosql.config.SqlConfig;
import jp.co.future.uroborosql.context.SqlContextFactoryImpl;
import jp.co.future.uroborosql.enums.InsertsType;
import jp.co.future.uroborosql.exception.UroborosqlRuntimeException;
import jp.co.future.uroborosql.filter.DumpResultSqlFilter;
import jp.co.future.uroborosql.filter.SqlFilterManagerImpl;
import jp.co.future.uroborosql.sample.entity.Department;
import jp.co.future.uroborosql.sample.entity.DeptEmp;
import jp.co.future.uroborosql.sample.entity.Employee;
import jp.co.future.uroborosql.sample.type.Gender;
import jp.co.future.uroborosql.store.NioSqlManagerImpl;
import jp.co.future.uroborosql.utils.CaseFormat;

/**
 * uroboroSQL Entity API Sample
 *
 * @author H.Sugimoto
 */
public class EntityApiSample extends AbstractApiSample {
	private final SqlConfig config;

	public EntityApiSample() {
		super();
		// create SqlConfig
		config = UroboroSQL
				.builder("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
				// SqlContextFactoryの設定（Enum定数パッケージ設定の追加）
				.setSqlContextFactory(
						new SqlContextFactoryImpl()
								.setEnumConstantPackageNames(Arrays.asList(Gender.class.getPackage().getName()))
								.setDefaultResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE))
				// SqlAgentFactoryの設定（Queryの戻り値のMapのキー文字列のデフォルトCaseFormat設定の追加）
				.setSqlAgentFactory(new SqlAgentFactoryImpl()
						.setDefaultMapKeyCaseFormat(CaseFormat.CAMEL_CASE)
						.setForceUpdateWithinTransaction(true))
				.setSqlManager(new NioSqlManagerImpl(false))
				.setSqlFilterManager(new SqlFilterManagerImpl().addSqlFilter(new DumpResultSqlFilter()))
				.build();
	}

	public void run() throws Exception {

		log("EntityApiSample start.");

		setupTableAndData();

		collect();

		first();

		stream();

		transaction();

		update();

		batchInsert();

		bulkInsert();

		log("EntityApiSample end.");
	}

	/**
	 * setup tables and initial data
	 *
	 * @param agent SqlAgent
	 */
	private void setupTableAndData() {
		try (SqlAgent agent = config.agent()) {
			agent.required(() -> {
				// create table :  テーブル作成
				int createCount = agent.update("ddl/create_tables").count();
				log("ddl/create_tables count={}", createCount);

				// setup data : 初期データ挿入
				int setupCount = agent.update("setup/insert_data").count();
				log("setup/insert_data count={}", setupCount);
			});
		}
	}

	/**
	 * query#collect() method sample
	 */
	private void collect() {
		try (SqlAgent agent = config.agent()) {
			// select department data (collect) : 部署データ検索（collect API）
			log("select department data (collect)");
			// no parameter : バインドパラメータ指定なしで検索
			List<Department> deps = agent.query(Department.class).collect();
			deps.forEach(data -> log(toS(data)));

			// add bind parameter : バインドパラメータを設定して検索
			log("select department data (collect) set param(dept_no=1)");
			agent.query(Department.class).equal("deptNo", 1).collect().forEach(data -> log(toS(data)));
		}
	}

	/**
	 * query#first() method sample
	 */
	private void first() {
		try (SqlAgent agent = config.agent()) {
			// first : firstを使用した先頭1件検索
			log("select first employee data.(Optional)");
			agent.query(Employee.class).first().ifPresent(m -> log(toS(m)));
		}
	}

	/**
	 * query#stream() method sample
	 */
	private void stream() {
		try (SqlAgent agent = config.agent()) {
			// select employee data (stream) : 従業員データ検索（stream API）
			log("select employee data (stream)");
			// no parameter : バインドパラメータ指定なしで検索（BEGIN-ENDで囲まれた範囲内のIF条件がすべてfalseのため、BEGIN-ENDの中が削除される）
			agent.query(Employee.class).stream().forEach(m -> log(toS(m)));

			// add bind date parameter : バインドパラメータ（日付型）を指定して検索
			agent.query(Employee.class).greaterEqual("birthDate", LocalDate.of(1990, 1, 1)).stream()
					.forEach(m -> log(toS(m)));

			// add bind list parameter : バインドパラメータ（IN句用）を指定して検索
			agent.query(Employee.class).in("gender", Gender.FEMALE).stream().forEach(m -> log(toS(m)));
		}
	}

	/**
	 * transaction api sample
	 */
	private void transaction() {
		try (SqlAgent agent = config.agent()) {
			// select with bind parameter : バインドパラメータを設定して検索
			log("select employee data (collect) set param(emp_no=1)");
			long empNo = 1;
			Employee emp = agent.query(Employee.class).param("empNo", empNo).first()
					.orElseThrow(UroborosqlRuntimeException::new);
			log(toS(emp));

			agent.required(() -> {
				emp.setBirthDate(LocalDate.of(1971, 12, 1));
				int count = agent.update(emp);
				log("update employee count={}", count);

				agent.query(Employee.class).param("empNo", empNo).first().ifPresent(e -> log(toS(e)));

				agent.setRollbackOnly();
			});
			agent.query(Employee.class).param("empNo", empNo).first().ifPresent(e -> log(toS(e)));
		}
	}

	/**
	 * update api sample
	 */
	private void update() {
		try (SqlAgent agent = config.agent()) {
			log("select department data (stream)");
			agent.query(Department.class).stream().forEach(e -> log(toS(e)));

			agent.required(() -> {
				Department dept = new Department();
				dept.setDeptName("production");
				int count = agent.insert(dept);
				log("insert department count={}", count);

				log("select department data (stream)");
				agent.query(Department.class).stream().forEach(e -> log(toS(e)));

				// rollback insert data. Keep the data unchanged in other tests.
				agent.setRollbackOnly();
			});
		}
	}

	/**
	 * batchInsert api sample
	 */
	private void batchInsert() {
		try (SqlAgent agent = config.agent()) {
			agent.required(() -> {
				log("delete tables with sql literal");
				// update with sql literal
				log("delete department count={}", agent.updateWith("delete from department").count());
				log("delete employee count={}", agent.updateWith("delete from employee").count());
				log("delete dept_emp count={}", agent.updateWith("delete from dept_emp").count());

				agent.query(Department.class).stream().forEach(r -> log(toS(r)));
				agent.query(Employee.class).stream().forEach(r -> log(toS(r)));
				agent.query(DeptEmp.class).stream().forEach(r -> log(toS(r)));

				// batch insert (since v0.10.0)
				log("department batch insert.");
				// department
				List<Department> depts = new ArrayList<>();
				Department dept1 = new Department();
				dept1.setDeptName("sales");
				depts.add(dept1);
				Department dept2 = new Department();
				dept2.setDeptName("export");
				depts.add(dept2);
				Department dept3 = new Department();
				dept3.setDeptName("accounting");
				depts.add(dept3);
				Department dept4 = new Department();
				dept4.setDeptName("personnel");
				depts.add(dept4);
				Department dept5 = new Department();
				dept5.setDeptName("production");
				depts.add(dept5);

				int deptCount = agent.inserts(depts.stream(), InsertsType.BATCH);
				log("department count={}", deptCount);

				log("employee batch insert.");
				// employee
				List<Employee> emps = new ArrayList<>();
				Employee emp1 = new Employee();
				emp1.setFirstName("Bob");
				emp1.setLastName("Smith");
				emp1.setBirthDate(LocalDate.of(1970, 1, 2));
				emp1.setGender(Gender.MALE);
				emps.add(emp1);
				Employee emp2 = new Employee();
				emp2.setFirstName("Susan");
				emp2.setLastName("Davis");
				emp2.setBirthDate(LocalDate.of(1969, 2, 10));
				emp2.setGender(Gender.FEMALE);
				emps.add(emp2);
				Employee emp3 = new Employee();
				emp3.setFirstName("John");
				emp3.setLastName("Wilson");
				emp3.setBirthDate(LocalDate.of(1982, 5, 8));
				emp3.setGender(Gender.MALE);
				emps.add(emp3);

				// execute by 2 rows
				int empCount = agent.inserts(emps.stream(), (ctx, count, row) -> count == 2, InsertsType.BATCH);
				log("employee count={}", empCount);

				log("dept_emp batch insert.");
				List<DeptEmp> deptEmps = new ArrayList<>();
				DeptEmp deptEmp1 = new DeptEmp();
				deptEmp1.setDeptNo(dept1.getDeptNo());
				deptEmp1.setEmpNo(emp1.getEmpNo());
				deptEmps.add(deptEmp1);
				DeptEmp deptEmp2 = new DeptEmp();
				deptEmp2.setDeptNo(dept2.getDeptNo());
				deptEmp2.setEmpNo(emp1.getEmpNo());
				deptEmps.add(deptEmp2);
				DeptEmp deptEmp3 = new DeptEmp();
				deptEmp3.setDeptNo(dept3.getDeptNo());
				deptEmp3.setEmpNo(emp2.getEmpNo());
				deptEmps.add(deptEmp3);
				DeptEmp deptEmp4 = new DeptEmp();
				deptEmp4.setDeptNo(dept4.getDeptNo());
				deptEmp4.setEmpNo(emp2.getEmpNo());
				deptEmps.add(deptEmp4);
				DeptEmp deptEmp5 = new DeptEmp();
				deptEmp5.setDeptNo(dept5.getDeptNo());
				deptEmp5.setEmpNo(emp3.getEmpNo());
				deptEmps.add(deptEmp5);

				// dept_emp
				// log message when batch execute.
				int deptEmpCount = agent.inserts(deptEmps.stream(), InsertsType.BATCH);
				log("dept_emp count={}", deptEmpCount);

				agent.query(Department.class).stream().forEach(r -> log(toS(r)));
				agent.query(Employee.class).stream().forEach(r -> log(toS(r)));
				agent.query(DeptEmp.class).stream().forEach(r -> log(toS(r)));

				// rollback insert data. Keep the data unchanged in other tests.
				agent.setRollbackOnly();

			});
		}
	}

	/**
	 * bulkInsert api sample
	 */
	private void bulkInsert() {
		try (SqlAgent agent = config.agent()) {
			agent.required(() -> {
				log("delete tables with sql literal");
				// update with sql literal
				log("delete department count={}", agent.updateWith("delete from department").count());
				log("delete employee count={}", agent.updateWith("delete from employee").count());
				log("delete dept_emp count={}", agent.updateWith("delete from dept_emp").count());

				agent.query(Department.class).stream().forEach(r -> log(toS(r)));
				agent.query(Employee.class).stream().forEach(r -> log(toS(r)));
				agent.query(DeptEmp.class).stream().forEach(r -> log(toS(r)));

				// batch insert (since v0.10.0)
				log("department batch insert.");
				// department
				List<Department> depts = new ArrayList<>();
				Department dept1 = new Department();
				dept1.setDeptName("sales");
				depts.add(dept1);
				Department dept2 = new Department();
				dept2.setDeptName("export");
				depts.add(dept2);
				Department dept3 = new Department();
				dept3.setDeptName("accounting");
				depts.add(dept3);
				Department dept4 = new Department();
				dept4.setDeptName("personnel");
				depts.add(dept4);
				Department dept5 = new Department();
				dept5.setDeptName("production");
				depts.add(dept5);

				int deptCount = agent.inserts(depts.stream(), InsertsType.BULK);
				log("department count={}", deptCount);

				log("employee batch insert.");
				// employee
				List<Employee> emps = new ArrayList<>();
				Employee emp1 = new Employee();
				emp1.setFirstName("Bob");
				emp1.setLastName("Smith");
				emp1.setBirthDate(LocalDate.of(1970, 1, 2));
				emp1.setGender(Gender.MALE);
				emps.add(emp1);
				Employee emp2 = new Employee();
				emp2.setFirstName("Susan");
				emp2.setLastName("Davis");
				emp2.setBirthDate(LocalDate.of(1969, 2, 10));
				emp2.setGender(Gender.FEMALE);
				emps.add(emp2);
				Employee emp3 = new Employee();
				emp3.setFirstName("John");
				emp3.setLastName("Wilson");
				emp3.setBirthDate(LocalDate.of(1982, 5, 8));
				emp3.setGender(Gender.MALE);
				emps.add(emp3);

				// execute by 2 rows
				int empCount = agent.inserts(emps.stream(), (ctx, count, row) -> count == 2, InsertsType.BULK);
				log("employee count={}", empCount);

				log("dept_emp batch insert.");
				List<DeptEmp> deptEmps = new ArrayList<>();
				DeptEmp deptEmp1 = new DeptEmp();
				deptEmp1.setDeptNo(dept1.getDeptNo());
				deptEmp1.setEmpNo(emp1.getEmpNo());
				deptEmps.add(deptEmp1);
				DeptEmp deptEmp2 = new DeptEmp();
				deptEmp2.setDeptNo(dept2.getDeptNo());
				deptEmp2.setEmpNo(emp1.getEmpNo());
				deptEmps.add(deptEmp2);
				DeptEmp deptEmp3 = new DeptEmp();
				deptEmp3.setDeptNo(dept3.getDeptNo());
				deptEmp3.setEmpNo(emp2.getEmpNo());
				deptEmps.add(deptEmp3);
				DeptEmp deptEmp4 = new DeptEmp();
				deptEmp4.setDeptNo(dept4.getDeptNo());
				deptEmp4.setEmpNo(emp2.getEmpNo());
				deptEmps.add(deptEmp4);
				DeptEmp deptEmp5 = new DeptEmp();
				deptEmp5.setDeptNo(dept5.getDeptNo());
				deptEmp5.setEmpNo(emp3.getEmpNo());
				deptEmps.add(deptEmp5);

				// dept_emp
				// log message when batch execute.
				int deptEmpCount = agent.inserts(deptEmps.stream());
				log("dept_emp count={}", deptEmpCount);

				agent.query(Department.class).stream().forEach(r -> log(toS(r)));
				agent.query(Employee.class).stream().forEach(r -> log(toS(r)));
				agent.query(DeptEmp.class).stream().forEach(r -> log(toS(r)));

				// rollback insert data. Keep the data unchanged in other tests.
				agent.setRollbackOnly();

			});
		}
	}

}
