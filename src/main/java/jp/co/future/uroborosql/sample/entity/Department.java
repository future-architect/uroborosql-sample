package jp.co.future.uroborosql.sample.entity;

import jp.co.future.uroborosql.enums.GenerationType;
import jp.co.future.uroborosql.mapping.annotations.GeneratedValue;
import jp.co.future.uroborosql.mapping.annotations.Id;
import jp.co.future.uroborosql.mapping.annotations.Table;
import jp.co.future.uroborosql.mapping.annotations.Version;

/**
 * Entity that can be mapped to department table
 */
@Table(name = "department")
public class Department {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long deptNo;

	private String deptName;

	@Version
	private long lockVersion;

	public long getDeptNo() {
		return this.deptNo;
	}

	public void setDeptNo(final long deptNo) {
		this.deptNo = deptNo;
	}

	public String getDeptName() {
		return this.deptName;
	}

	public void setDeptName(final String deptName) {
		this.deptName = deptName;
	}

	public long getLockVersion() {
		return this.lockVersion;
	}

	public void setLockVersion(final long lockVersion) {
		this.lockVersion = lockVersion;
	}

	@Override
	public String toString() {
		return "Department [deptNo=" + this.deptNo + ", deptName=" + this.deptName + ", lockVersion="
				+ this.lockVersion + "]";
	}
}
