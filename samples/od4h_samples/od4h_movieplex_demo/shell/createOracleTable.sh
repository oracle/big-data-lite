#!/usr/bin/env bash
#
# $Header: hadoop/demo/od4h_samples/od4h_movieplex_demo/shell/createOracleTable.sh ratiwary_od4h_demos/2 2018/01/19 05:07:14 ratiwary Exp $
#
# createOracleTable.sh
#
# Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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

echo "Enter schema name where MovieData will be loaded"
echo "Press Enter to use default [scott]:"
read username

if [ "X"$username = "X" ]; then
username=scott
fi

SHELL_DIR=`dirname "$0"`
DEMO_DIR=`cd "../"; pwd`
cd ${DEMO_DIR}/sql

export TWO_TASK=orcl
echo "Enter password for the User"
sqlplus -S $username/ @CreateMovieData.sql;
