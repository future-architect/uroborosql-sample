insert into department (dept_no, dept_name) values (1001, 'sales');
insert into department (dept_no, dept_name) values (1002, 'export');
insert into department (dept_no, dept_name) values (1003, 'accounting');
insert into department (dept_no, dept_name) values (1004, 'personnel');

insert into employee (emp_no, first_name, last_name, birth_date, gender) values (0001, 'Bob', 'Smith', '1970-01-02', 'M');
insert into employee (emp_no, first_name, last_name, birth_date, gender) values (0002, 'Susan', 'Davis', '1969-02-10', 'F');
insert into employee (emp_no, first_name, last_name, birth_date, gender) values (0003, 'John', 'Wilson', '1982-05-08', 'M');
insert into employee (emp_no, first_name, last_name, birth_date, gender) values (0004, 'Sharon', 'Johnson', '1990-01-20', 'F');
insert into employee (emp_no, first_name, last_name, birth_date, gender) values (0005, 'Stephen', 'Taylor', '2003-12-31', 'M');

insert into dept_emp (emp_no, dept_no) values (0001, '1001');
insert into dept_emp (emp_no, dept_no) values (0002, '1001');
insert into dept_emp (emp_no, dept_no) values (0003, '1002');
insert into dept_emp (emp_no, dept_no) values (0004, '1003');
insert into dept_emp (emp_no, dept_no) values (0005, '1004');
