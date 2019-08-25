package jp.co.future.uroborosql.sample.entity;

import java.time.LocalDate;

import jp.co.future.uroborosql.enums.GenerationType;
import jp.co.future.uroborosql.mapping.annotations.GeneratedValue;
import jp.co.future.uroborosql.mapping.annotations.Id;
import jp.co.future.uroborosql.mapping.annotations.Table;
import jp.co.future.uroborosql.mapping.annotations.Version;
import jp.co.future.uroborosql.sample.type.Gender;

/**
 * Entity that can be mapped to employee table
 */
@Table(name = "employee")
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long empNo;

	private String firstName;

	private String lastName;

	private LocalDate birthDate;

	private Gender gender;

	@Version
	private long lockVersion = 0;

	public long getEmpNo() {
		return this.empNo;
	}

	public void setEmpNo(final long empNo) {
		this.empNo = empNo;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public void setBirthDate(final LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public Gender getGender() {
		return this.gender;
	}

	public void setGender(final Gender gender) {
		this.gender = gender;
	}

	public long getLockVersion() {
		return this.lockVersion;
	}

	public void setLockVersion(final long lockVersion) {
		this.lockVersion = lockVersion;
	}

	@Override
	public String toString() {
		return "Employee [empNo=" + this.empNo + ", firstName=" + this.firstName + ", lastName=" + this.lastName
				+ ", birthDate=" + this.birthDate + ", gender=" + this.gender + ", lockVersion="
				+ this.lockVersion + "]";
	}
}
