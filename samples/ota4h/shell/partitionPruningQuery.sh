#!/usr/bin/env bash
#
# $Header: hadoop/demo/osh/shell/partitionPruningQuery.sh /main/1 2015/03/20 13:52:02 ratiwary Exp $
#
# partitionPruningQueryHive.sh
#
# Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      partitionPruningQueryHive.sh
#
#    DESCRIPTION
#      Execute a query where filter is applied over partition key column
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
cd ${DEMO_DIR}/sql

QUERY="select First_Name, Last_Name, salary from EmployeeDataPartitioned 
where salary > 72000 and salary < 78000"
if [ $choice -eq 1 ]; then
  hive --auxpath ${HIVE_AUXPATH} -i hive_init.hql -e "${QUERY}"
elif [ $choice -eq 2 ]; then
  echo "sqlContext.sql(\""${QUERY}"\").show();System.exit(0);" > query.scala
  spark-shell --jars /opt/oracle/ota4h/jlib/osh.jar,/opt/oracle/ota4h/jlib/ojdbc7.jar,/opt/oracle/ota4h/jlib/ucp.jar -i query.scala
#  rm query.scala
else
  echo "Invalid input"
fi
