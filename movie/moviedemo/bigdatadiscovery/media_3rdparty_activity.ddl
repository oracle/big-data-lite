DROP TABLE media_demo_3rdparty_activity;
CREATE EXTERNAL TABLE media_demo_3rdparty_activity(
cust_id int,
in_market string,
predictors string,
demographics string
) row format delimited fields terminated by '\t' stored as textfile
 LOCATION '/user/oracle/mediademo/media_3rdparty_activity'
