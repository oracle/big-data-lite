#!/usr/bin/env bash
#
# $Header: hadoop/demo/osh/shell/partitionQuery.sh ratiwary_osh_newsplitters_outputformat/1 2016/03/05 06:41:50 ratiwary Exp $
#
# partitionQueryHive.sh
#
# Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      partitionQueryHive.sh
#
#    DESCRIPTION
#      Execute a query over external table created with partitioned splitter
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

QUERY="select First_Name, Last_Name from EmployeeDataPartitioned 
where Emp_ID=39332"
if [ $choice -eq 1 ]; then
  hive -i hive_init.hql -e "${QUERY}"
elif [ $choice -eq 2 ]; then
  echo "sqlContext.sql(\""${QUERY}"\").show();System.exit(0);" > query.scala
  spark-shell --jars /opt/oracle/od4h/jlib/osh.jar,/opt/oracle/od4h/jlib/ojdbc7.jar,/opt/oracle/od4h/jlib/ucp.jar -i query.scala
  rm query.scala
else
  echo "Invalid Input"
fi
