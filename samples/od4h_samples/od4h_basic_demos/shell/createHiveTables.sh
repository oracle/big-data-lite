#!/usr/bin/env bash
#
# $Header: hadoop/demo/od4h_samples/od4h_basic_demos/shell/createHiveTables.sh ratiwary_od4h_demos/2 2018/01/19 05:07:14 ratiwary Exp $
#
# createHiveTables.sh
#
# Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      createHiveTables.sh
#
#    DESCRIPTION
#      Creates hive tables for the demo
#
#    NOTES
#
#    MODIFIED   (MM/DD/YY)
#    ratiwary    03/20/15 - Creation
#

echo "Enter Oracle schema name where EmployeeData is loaded"
echo "Press Enter to use default [scott]:"
read username

if [ "X"$username = "X" ]; then
username=scott
fi

echo "Enter password"
stty -echo
read password
stty echo

echo "Enter connection string for oracle database"
echo "Press Enter to use default[jdbc:oracle:thin:@localhost:1521/orcl]"
read conn_string
if [ "X"$conn_string = "X" ]; then
conn_string=jdbc:oracle:thin:@localhost:1521/orcl
fi

SHELL_DIR=`dirname "$0"`
DEMO_DIR=`cd "../"; pwd`
cd ${DEMO_DIR}/sql

# Storing password in plain text is not recommended. OD4H recommends using
# strong password authentication like Kerberos
hive -i hive_init.hql -f CreateEmployeeDataHive.hql -hiveconf connection_string="$conn_string" -hiveconf oracle_user=$username -hiveconf oracle_pwd=$password
