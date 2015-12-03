echo Automatically create MOVIEDEMO.AUTO_MOVIE_FACT_HDFS_EXT_TAB over Hive table
export HADOOP_CLASSPATH=$HIVE_HOME/lib/*:$HADOOP_CLASSPATH

echo $HADOOP_CLASSPATH

hadoop jar $OSCH_HOME/jlib/orahdfs.jar oracle.hadoop.exttab.ExternalTable \
-conf /home/oracle/movie/moviedemo/osch/common.xml \
-D oracle.hadoop.exttab.tableName=AUTO_MOVIE_FACT_HDFS_EXT_TAB \
-D oracle.hadoop.exttab.sourceType=hive \
-D oracle.hadoop.exttab.hive.tableName=movieapp_log_stage \
-D oracle.hadoop.exttab.hive.databaseName=moviedemo \
-createTable
