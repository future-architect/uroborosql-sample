SELECT /* _SQL_ID_ */
	DEPT.DEPT_NO		AS	DEPT_NO
,	DEPT.DEPT_NAME		AS	DEPT_NAME
,	DEPT.LOCK_VERSION	AS	LOCK_VERSION
FROM
	DEPARTMENT	DEPT
/*BEGIN*/
WHERE
/*IF SF.isNotEmpty(dept_no)*/
AND	DEPT.DEPT_NO	=	/*dept_no*/1
/*END*/
/*IF SF.isNotEmpty(dept_name)*/
AND	DEPT.DEPT_NAME	=	/*dept_name*/'sample'
/*END*/
/*END*/
