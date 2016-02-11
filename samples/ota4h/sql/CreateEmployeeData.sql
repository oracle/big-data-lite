Rem
Rem $Header: hadoop/demo/osh/sql/CreateEmployeeData.sql /main/1 2015/03/20 13:52:02 ratiwary Exp $
Rem
Rem CreateEmployeeData.sql
Rem
Rem Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
Rem
Rem    NAME
Rem      CreateEmployeeData.sql
Rem
Rem    DESCRIPTION
Rem      Creates partitioned external table for OTA4H storage handler
Rem
Rem    NOTES
Rem
Rem    BEGIN SQL_FILE_METADATA 
Rem    SQL_SOURCE_FILE: hadoop/demo/osh/sql/CreateEmployeeData.sql 
Rem    SQL_SHIPPED_FILE: 
Rem    SQL_PHASE: 
Rem    SQL_STARTUP_MODE: NORMAL 
Rem    SQL_IGNORABLE_ERRORS: NONE 
Rem    SQL_CALLING_FILE: 
Rem    END SQL_FILE_METADATA
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    ratiwary    03/20/15 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

drop table EmployeeData;

CREATE TABLE EmployeeData ( Emp_ID NUMBER,
    First_Name VARCHAR2(20),
    Last_Name VARCHAR2(20),
    Job_Title VARCHAR2(40),
    Salary NUMBER)
PARTITION BY RANGE (Salary)
 ( PARTITION salary_1 VALUES LESS THAN (60000),
   PARTITION salary_2 VALUES LESS THAN (70000),
   PARTITION salary_3 VALUES LESS THAN (80000),
   PARTITION salary_4 VALUES LESS THAN (90000),
   PARTITION salary_5 VALUES LESS THAN (100000)
 );
quit;
