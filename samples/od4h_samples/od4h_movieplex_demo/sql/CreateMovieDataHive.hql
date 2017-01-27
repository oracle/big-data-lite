--
-- $Header: hadoop/demo/osh/sql/CreateMovieDataHive.hql ratiwary_osh_newsplitters_outputformat/2 2016/01/19 05:57:36 ratiwary Exp $
--
-- CreateMovieDataHive.hql
--
-- Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
--
--    NAME
--      CreateMovieDataHive.hql
--
--    DESCRIPTION
--      Creates hive external tables using OracleStorageHandler
--
--    NOTES
--
--    MODIFIED   (MM/DD/YY)
--    ratiwary    03/20/15 - Created
--

DROP TABLE MovieData;
CREATE EXTERNAL TABLE MovieData (
 custid int,
 movieid int,
 genreid int,
 time string,
 recommended int,
 activity int,
 rating int
)
STORED BY 'oracle.hcat.osh.OracleStorageHandler'
WITH SERDEPROPERTIES (
     'oracle.hcat.osh.columns.mapping' = 'custid,movieid,genreid,time,recommended,activity,rating')
TBLPROPERTIES (
 'mapreduce.jdbc.url' = '${hiveconf:connection_string}',
 'mapreduce.jdbc.username' = '${hiveconf:oracle_user}',
 'mapreduce.jdbc.password' = '${hiveconf:oracle_pwd}',
 'mapreduce.jdbc.input.table.name' = 'MovieData'
);
