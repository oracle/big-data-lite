-- The *_hive_moviefact.sql scripts perform the following Hive DDL & Update tasks:
--   * creates database moviedemo
--   * creates MOVIEAPP_LOG_AVRO table over the movie applications AVRO log file 
--   * creates MOVIEAPP_LOG_STAGE table that will contain staging data used to load into the Oracle Database 
--   * shows example queries required to extract the data from the MOVIEAPP_LOG_AVRO table
--   * inserts data into staging table 

-- Create the moviework database
create database moviework;
use moviework;

-- Create table over AVRO source using the activity.avsc AVRO schema
CREATE EXTERNAL TABLE movieapp_log_avro
ROW FORMAT 
SERDE 'org.apache.hadoop.hive.serde2.avro.AvroSerDe'
WITH SERDEPROPERTIES ('avro.schema.url'='hdfs://bigdatalite.localdomain/user/oracle/moviework/schemas/activity.avsc')
STORED AS 
INPUTFORMAT 'org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.avro.AvroContainerOutputFormat'
LOCATION '/user/oracle/moviework/applog_avro'; 

-- Create staging table that will be used to populate Oracle DB
CREATE TABLE IF NOT EXISTS movieapp_log_stage (
  custId INT,
  movieId INT,
  genreId INT,
  time  STRING,
  recommended INT,
  activity INT,
  rating INT,
  sales FLOAT
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';
