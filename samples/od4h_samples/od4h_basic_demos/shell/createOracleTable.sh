#!/usr/bin/env bash
#
# $Header: hadoop/demo/osh/shell/createOracleTable.sh /main/1 2015/03/20 13:52:02 ratiwary Exp $
#
# createOracleTable.sh
#
# Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      createOracleTable.sh
#
#    DESCRIPTION
#      Creates Oracle Tables for the demo
#
#    NOTES
#
#    MODIFIED   (MM/DD/YY)
#    ratiwary    03/20/15 - Creation
#

echo "Enter schema name where EmployeeData will be loaded"
echo "Press Enter to use default [scott]:"
read username

if [ "X"$username = "X" ]; then
username=scott
fi

SHELL_DIR=`dirname "$0"`
DEMO_DIR=`cd "../"; pwd`
cd ${DEMO_DIR}/sql

echo "Enter password for the User"
sqlplus -S $username/ @CreateEmployeeData.sql;

cd ${DEMO_DIR}/data
#Use sqlldr to read data into the table from csv file.
echo "Enter Username and password specified above when prompted to load data"
sqlldr CONTROL=EmployeeData.ctl LOG=EmployeeData.log BAD=EmployeeData.bad skip=1
