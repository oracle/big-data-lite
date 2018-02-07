#!/usr/bin/env bash
#
# $Header: hadoop/demo/od4h_samples/od4h_basic_demos/shell/partitionPruningQuery.sh ratiwary_od4h_demos/2 2018/01/11 01:45:07 ratiwary Exp $
#
# partitionPruningQueryHive.sh
#
# Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
  hive -i hive_init.hql -e "${QUERY}"
elif [ $choice -eq 2 ]; then
  echo "sqlContext.sql(\""${QUERY}"\").show();System.exit(0);" > query.scala
  spark-shell --jars /opt/oracle/od4h/jlib/osh.jar,/opt/oracle/od4h/jlib/ojdbc8.jar,/opt/oracle/od4h/jlib/ucp.jar -i query.scala
  rm query.scala
else
  echo "Invalid input"
fi
