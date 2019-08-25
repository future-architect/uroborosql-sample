package jp.co.future.uroborosql.sample.entity;

import jp.co.future.uroborosql.mapping.annotations.Table;

/**
 * Entity that can be mapped to dept_emp table
 */
@Table(name = "dept_emp")
public class DeptEmp {
	private long deptNo;
	private long empNo;

	public long getDeptNo() {
		return deptNo;
	}

	public void setDeptNo(final long deptNo) {
		this.deptNo = deptNo;
	}

	public long getEmpNo() {
		return empNo;
	}

	public void setEmpNo(final long empNo) {
		this.empNo = empNo;
	}

	@Override
	public String toString() {
		return "DeptEmp [deptNo=" + deptNo + ", empNo=" + empNo + "]";
	}

}
