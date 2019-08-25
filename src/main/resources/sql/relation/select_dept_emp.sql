select /* _SQL_ID_ */
	emp_no	as	emp_no	-- emp_no
,	dept_no	as	dept_no	-- dept_no
from
	dept_emp
/*BEGIN*/
where
/*IF empNo != null */
and	emp_no	=	/*empNo*/''
/*END*/
/*IF deptNo != null */
and	dept_no	=	/*deptNo*/''
/*END*/
/*END*/
