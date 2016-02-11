#!/usr/bin/env bash
#
# $Header: hadoop/demo/osh/shell/joinQuery.sh /main/2 2015/06/18 14:29:57 ratiwary Exp $
#
# joinQueryHive.sh
#
# Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

echo "Select an option from below"
echo "1. Execute query on hive (default)"
echo "2. Execute query on spark-sql"
echo "Enter number corresponding to your choice or press enter to use hive:"
read choice

if [ "X"$choice = "X" ]; then
    choice=1
fi

DEMO_DIR=`cd "../"; pwd`
DATA_DIR=${DEMO_DIR}/data
cd ${DEMO_DIR}/sql

QUERY="select EmployeeDataSimple.First_Name, EmployeeDataSimple.Last_Name,EmployeeBonus.bonus 
from EmployeeDataSimple join EmployeeBonus 
on (EmployeeDataSimple.Emp_ID=EmployeeBonus.Emp_ID) 
where salary > 70000 and bonus > 7000"
hive --auxpath ${HIVE_AUXPATH} -i hive_init.hql -hiveconf csv=${DATA_DIR}/EmployeeBonus.csv -f CreateEmployeeBonusHive.hql
if [ $choice -eq 1 ]; then
  hive --auxpath ${HIVE_AUXPATH} -i hive_init.hql -e "${QUERY}"
elif [ $choice -eq 2 ]; then
  echo "sqlContext.sql(\""${QUERY}"\").show();System.exit(0);" > query.scala
  spark-shell --executor-memory 3G --num-executors 4 --executor-cores 4 --jars /opt/oracle/ota4h/jlib/osh.jar,/opt/oracle/ota4h/jlib/ojdbc7.jar,/opt/oracle/ota4h/jlib/ucp.jar -i query.scala
  rm query.scala
else
  echo "Invalid input"
fi
