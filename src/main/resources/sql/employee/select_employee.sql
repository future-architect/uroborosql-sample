select /* _SQL_ID_ */
	emp.emp_no			as	emp_no
,	emp.first_name		as	first_name
,	emp.last_name		as	last_name
,	emp.birth_date		as	birth_date
,	emp.gender			as	gender
,	emp.lock_version	as	lock_version
from
	employee	emp
/*BEGIN*/
where
/*IF SF.isNotEmpty(empNo)*/
and	emp.emp_no		=	/*empNo*/1
/*END*/
/*IF SF.isNotEmpty(firstName)*/
and	emp.first_name	=	/*firstName*/'Bob'
/*END*/
/*IF SF.isNotEmpty(lastName)*/
and	emp.last_name	=	/*lastName*/'Smith'
/*END*/
/*IF birthDateFrom != null*/
and	emp.birth_date	>=	/*birthDateFrom*/'1990-10-10'
/*END*/
/*IF birthDateTo != null*/
and	emp.birth_date	<	/*birthDateTo*/'1990-10-10'
/*END*/
/*IF genderList != null*/
and	emp.gender		IN	/*genderList*/('M')
/*END*/
/*IF female != null and female*/
and	emp.gender		=	/*#CLS_GENDER_FEMALE*/'M'
/*END*/
/*END*/
