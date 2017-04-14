package jp.co.future.uroborosql.sample;

import java.time.LocalDate;

import jp.co.future.uroborosql.sample.type.Gender;

/**
 * Entity that can be mapped to employee table
 */
public class Employee {
	private long empNo;
	private String firstName;
	private String lastName;
	private LocalDate birthDate;
	private Gender gender;

	public long getEmpNo() {
		return this.empNo;
	}

	public void setEmpNo(long empNo) {
		this.empNo = empNo;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public Gender getGender() {
		return this.gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Override
	public String toString() {
		return "Employee [empNo=" + this.empNo + ", firstName=" + this.firstName + ", lastName=" + this.lastName
				+ ", birthDate=" + this.birthDate + ", gender=" + this.gender + "]";
	}
}
