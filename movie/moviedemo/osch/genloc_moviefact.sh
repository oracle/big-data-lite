echo MOVIEDEMO movie_fact_hdfs_ext_tab  location files
hadoop jar $OSCH_HOME/jlib/orahdfs.jar oracle.hadoop.exttab.ExternalTable \
-conf /home/oracle/movie/moviedemo/osch/common.xml \
-D oracle.hadoop.exttab.tableName=MOVIE_FACT_HDFS_EXT_TAB \
-D oracle.hadoop.exttab.dataPaths=/user/hive/warehouse/moviedemo.db/movieapp_log_stage/* \
-publish
