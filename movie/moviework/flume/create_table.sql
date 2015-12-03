CREATE EXTERNAL TABLE moviework.movieapp_log_avro
ROW FORMAT 
SERDE 'org.apache.hadoop.hive.serde2.avro.AvroSerDe'
WITH SERDEPROPERTIES ('avro.schema.url'='hdfs://bigdatalite.localdomain/user/oracle/moviework/schemas/activity.avsc')
STORED AS 
INPUTFORMAT 'org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.avro.AvroContainerOutputFormat'
LOCATION '/user/oracle/moviework/applog_avro'; 
