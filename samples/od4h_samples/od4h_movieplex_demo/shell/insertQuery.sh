#!/usr/bin/env bash
#
# $Header: hadoop/demo/osh/shell/joinQuery.sh ratiwary_osh_newsplitters_outputformat/2 2016/03/05 06:41:50 ratiwary Exp $
#
# joinQueryHive.sh
#
# Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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

QUERY1="insert into MovieData SELECT
 m1.custid,
 m1.movieid,
 CASE WHEN m1.genreid > 0 THEN m1.genreid ELSE -1 END genreid,
 m1.time,
 CASE m1.recommended WHEN 'Y' THEN 1 ELSE 0 END
recommended,
 m1.activity,
 m1.rating
 FROM movieapp_log_avro m1
 JOIN
 (SELECT
 custid,
 movieid,
 CASE WHEN genreid > 0 THEN genreid ELSE -1 END genreid,
 MAX(time) max_time,
 activity
 FROM movieapp_log_avro
 GROUP BY custid,
 movieid,
 genreid,
 activity
 ) m2
 ON (
 m1.custid = m2.custid
 AND m1.movieid = m2.movieid
 AND m1.genreid = m2.genreid
 AND m1.time = m2.max_time
 AND m1.activity = 1
 AND m2.activity = 1
 ) LIMIT 25"
QUERY2="select * from MovieData"
hive -i hive_init.hql -e "${QUERY1}"
hive -i hive_init.hql -e "${QUERY2}"
