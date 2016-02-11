load data 
infile 'EmployeeData.csv' "str '\n'"
append
into table EmployeeData
fields terminated by ','
OPTIONALLY ENCLOSED BY '"' AND '"'
trailing nullcols
           ( Emp_ID CHAR(40000),
             First_Name CHAR(40000),
             Last_Name CHAR(40000),
             Job_Title CHAR(40000),
             Salary CHAR(40000)
           )
