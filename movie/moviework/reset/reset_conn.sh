
# clean up HDFS directories used by Oracle Loader for Hadoop in 
# previous lab runs

hdfs dfs -rm -r temp_out_session
hdfs dfs -rm -r moviework/data_dp
hdfs dfs -rm moviework/data/part-00005

# clean up files from previous lab runs

rm /home/oracle/movie/moviework/osch/osch*

rm /home/oracle/movie/moviework/osch/*log
rm /home/oracle/movie/moviework/osch/*bad
rm /home/oracle/movie/moviework/olh/*log

sqlplus moviedemo@orcl/welcome1 <<ENDOFSQL

drop directory MOVIEWORKSHOP_DIR;
drop table MOVIE_FACT_DB_TAB;
drop table MOVIE_SESSIONS_TAB;
drop table MOVIE_FACT_EXT_TAB_HIVE;
drop table MOVIE_FACT_EXT_TAB_TEXT;
drop table MOVIE_FACT_EXT_TAB_DP;
drop table MOVIE_FACT_LOCAL;

create or replace directory MOVIEWORKSHOP_DIR as '/home/oracle/movie/moviework/osch';

create or replace directory OSCH_BIN_PATH as '/u01/connectors/osch/bin';

exit;
ENDOFSQL

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$HIVE_HOME/lib/*

hive -e "drop table moviedemo.movieapp_log_stage_1;"
hive -e "create table moviedemo.movieapp_log_stage_1 as select * from moviedemo.movieapp_log_stage where activity = 1;"

