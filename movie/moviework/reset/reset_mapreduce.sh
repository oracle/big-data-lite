cd /home/oracle/movie/moviework/reset

echo Dropping files
hadoop fs -rm -r /user/oracle/my_stuff
hadoop fs -rm /user/oracle/moviework/applog_avro/*

echo Dropping Hive database moviework
hive -f ./reset_hive.sql
