#!/usr/bin/env bash
#
# $Header: hadoop/demo/od4h_samples/od4h_movieplex_demo/shell/createHiveTable.sh ratiwary_od4h_demos/1 2018/01/11 01:40:05 ratiwary Exp $
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

echo "Enter Oracle schema name where MovieData is loaded"
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

hive -i hive_init.hql -f CreateMovieDataHive.hql -hiveconf connection_string="$conn_string" -hiveconf oracle_user=$username -hiveconf oracle_pwd=$password
