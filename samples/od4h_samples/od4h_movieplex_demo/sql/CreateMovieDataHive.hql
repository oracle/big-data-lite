--
-- $Header: hadoop/demo/od4h_samples/od4h_movieplex_demo/sql/CreateMovieDataHive.hql ratiwary_od4h_demos/1 2018/01/11 01:40:05 ratiwary Exp $
--
-- CreateMovieDataHive.hql
--
-- Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
