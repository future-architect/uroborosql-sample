update /* _SQL_ID_ */
	employee
set
/*IF firstName != null */
	first_name		=	/*firstName*/''			-- first_name
/*END*/
/*IF lastName != null */
,	last_name		=	/*lastName*/''			-- last_name
/*END*/
/*IF birthDate != null */
,	birth_date		=	/*birthDate*/''			-- birth_date
/*END*/
/*IF gender != null */
,	gender			=	/*gender*/''			-- gender        'F'emale/'M'ale/'O'ther
/*END*/
,	lock_version	=	lock_version	+	1	-- lock_version
where
	emp_no	=	/*empNo*/''