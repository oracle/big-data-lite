# OGG reset

cd /u01/ogg
./ggsci < ./dirprm/reset_bigdata.oby

cp /u01/ogg/dirprm/pmov.properties.hive /u01/ogg/dirprm/pmov.properties

#ODI truncate target tables

hive -e 'truncate table session_stats'
echo "truncate table MOVIEDEMO.ODI_COUNTRY_SALES;" | sqlplus -s system/welcome1@orcl	
echo "truncate table MOVIEDEMO.ODI_MOVIE_RATING;" | sqlplus -s system/welcome1@orcl
echo "delete from MOVIEDEMO.MOVIE where movie_id<5;" | sqlplus -s system/welcome1@orcl
echo "create directory ODI_PUMP_DIR as '/home/oracle/movie/moviework/odi';"| sqlplus -s system/welcome1@orcl
hdfs dfs -rm /user/odi/hive/default/movie_updates/movie_updates*

#ODI reimport repository
# Created with
# expdp system/welcome1@orcl directory=ODI_PUMP_DIR schemas=dev_odi_repo dumpfile=dev_odi_repo.dmp

echo "drop user DEV_ODI_REPO CASCADE;" | sqlplus -s system/welcome1@orcl

impdp system/welcome1@orcl schemas=dev_odi_repo directory=ODI_PUMP_DIR dumpfile=dev_odi_repo.dmp

echo "drop directory ODI_PUMP_DIR;"| sqlplus -s system/welcome1@orcl
 
#ODI HOL 
