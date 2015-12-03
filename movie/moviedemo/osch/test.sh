echo Automatically create MOVIEDEMO.AUTO_MOVIE_FACT_HDFS_EXT_TAB over Hive table
# export HADOOP_CLASSPATH=$HIVE_HOME/lib/*:$HADOOP_CLASSPATH
export HADOOP_CLASSPATH=/usr/lib/hive/lib/*:/usr/lib/hive/conf:/u01/connectors/orahdfs-2.0.0/jlib/*

echo $HADOOP_CLASSPATH

hadoop jar $OSCH_HOME/jlib/orahdfs.jar oracle.hadoop.exttab.ExternalTable \
-conf /home/oracle/movie/moviedemo/odch/common.xml \
-D oracle.hadoop.exttab.tableName=AUTO_MOVIE_FACT_HDFS_EXT_TAB \
-D oracle.hadoop.exttab.sourceType=hive \
-D oracle.hadoop.exttab.hive.tableName=movieapp_log_stage \
-D oracle.hadoop.exttab.hive.databaseName=moviedemo \
-createTable 
