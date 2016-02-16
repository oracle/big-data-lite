CREATE EXTERNAL TABLE `movie_updates`(
  `op` string,
  `ts` timestamp,
  `tok` string,
  `movie_id` int, 
  `title` string, 
  `year` int, 
  `budget` int, 
  `gross` int, 
  `plot_summary` string 
  )
ROW FORMAT SERDE 
  'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe' 
STORED AS INPUTFORMAT 
  'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 
  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION
  'hdfs://bigdatalite.localdomain:8020/user/odi/hive/orcl.moviedemo.movie'
