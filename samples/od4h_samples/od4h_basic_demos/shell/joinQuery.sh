#!/usr/bin/env bash
#
# $Header: hadoop/demo/od4h_samples/od4h_basic_demos/shell/joinQuery.sh ratiwary_od4h_demos/1 2018/01/11 01:40:05 ratiwary Exp $
#
# joinQueryHive.sh
#
# Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      joinQueryHive.sh
#
#    DESCRIPTION
#      Executes a join query between a hive managed table and an external table
#
#    NOTES
#
#    MODIFIED   (MM/DD/YY)
#    ratiwary    03/20/15 - Creation
#

DEMO_DIR=`cd "../"; pwd`
DATA_DIR=${DEMO_DIR}/data
cd ${DEMO_DIR}/sql

QUERY1="insert into EmployeeBonusReport select EmployeeDataSimple.First_Name, EmployeeDataSimple.Last_Name,
EmployeeBonus.bonus from EmployeeDataSimple join EmployeeBonus on (EmployeeDataSimple.Emp_ID=EmployeeBonus.Emp_ID) 
where salary > 70000 and bonus > 7000"
QUERY2="select * from EmployeeBonusReport"
hive -hiveconf csv=${DATA_DIR}/EmployeeBonus.csv -f CreateEmployeeBonusHive.hql
hive -i hive_init.hql -e "${QUERY1}"
hive -i hive_init.hql -e "${QUERY2}"
