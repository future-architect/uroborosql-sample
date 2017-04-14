package jp.co.future.uroborosql.sample;

/**
 * Entity that can be mapped to department table
 */
public class Department {
	private long deptNo;
	private String deptName;

	public long getDeptNo() {
		return this.deptNo;
	}

	public void setDeptNo(long deptNo) {
		this.deptNo = deptNo;
	}

	public String getDeptName() {
		return this.deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	@Override
	public String toString() {
		return "Department [deptNo=" + this.deptNo + ", deptName=" + this.deptName + "]";
	}
}
