--
-- $Header: hadoop/demo/osh/sql/CreateEmployeeBonusHive.hql /main/2 2015/06/18 14:29:57 ratiwary Exp $
--
-- CreateEmployeeBonusHive.hql
--
-- Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
--
--    NAME
--      CreateEmployeeBonusHive.hql
--
--    DESCRIPTION
--      Creates a managed hive table to be used for join with external table
--
--    NOTES
--
--    MODIFIED   (MM/DD/YY)
--    ratiwary    03/20/15 - Created
--

drop table EmployeeBonus;
create table EmployeeBonus(Emp_ID string, Bonus int) row format delimited fields terminated by ',' stored as textfile;
LOAD DATA LOCAL INPATH '${hiveconf:csv}' OVERWRITE INTO TABLE EmployeeBonus;
