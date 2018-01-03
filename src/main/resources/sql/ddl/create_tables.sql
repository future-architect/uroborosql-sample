-- employee
drop table if exists employee cascade;

create table employee (
  emp_no NUMBER(11) not null
  , first_name VARCHAR(20) not null
  , last_name VARCHAR(20) not null
  , birth_date DATE not null
  , gender CHAR(1) not null
  , lock_version NUMBER(10) not null default 0
  , constraint employee_PKC primary key (emp_no)
) ;

-- dept_emp
drop table if exists dept_emp cascade;

create table dept_emp (
  emp_no NUMBER(11) not null
  , dept_no NUMBER(4) not null
  , constraint dept_emp_PKC primary key (emp_no,dept_no)
) ;

-- department
drop table if exists department cascade;

create table department (
  dept_no NUMBER(4) not null
  , dept_name VARCHAR(100) not null
  , lock_version NUMBER(10) not null default 0
  , constraint department_PKC primary key (dept_no)
) ;

comment on table employee is 'employee';
comment on column employee.emp_no is 'emp_no';
comment on column employee.first_name is 'first_name';
comment on column employee.last_name is 'last_name';
comment on column employee.birth_date is 'birth_date';
comment on column employee.gender is 'gender	 ''F''emale/''M''ale/''O''ther';
comment on column employee.lock_version is 'lock_version';

comment on table dept_emp is 'dept_emp';
comment on column dept_emp.emp_no is 'emp_no';
comment on column dept_emp.dept_no is 'dept_no';

comment on table department is 'department';
comment on column department.dept_no is 'dept_no';
comment on column department.dept_name is 'dept_name';
comment on column department.lock_version is 'lock_version';
