SELECT
/*
	_SQL_ID_
*/
	DEPT.DEPT_NO	AS	DEPT_NO
,	DEPT.DEPT_NAME	AS	DEPT_NAME
FROM
	DEPARTMENT	DEPT
WHERE
	1				=	1
/*IF SF.isNotEmpty(dept_no)*/
AND	DEPT.DEPT_NO	=	/*dept_no*/1
/*END*/
/*IF SF.isNotEmpty(dept_name)*/
AND	DEPT.DEPT_NAME	=	/*dept_name*/'sample'
/*END*/
