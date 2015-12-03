echo Generate MOVIEDEMO.SESSIONS_HDFS_EXT_TAB locator file 

hadoop jar $OSCH_HOME/jlib/orahdfs.jar oracle.hadoop.exttab.ExternalTable \
-conf /home/oracle/movie/moviedemo/osch/common.xml \
-D oracle.hadoop.exttab.tableName=SESSIONS_HDFS_EXT_TAB \
-D oracle.hadoop.exttab.dataPaths=/user/oracle/moviedemo/session/p* \
-publish
