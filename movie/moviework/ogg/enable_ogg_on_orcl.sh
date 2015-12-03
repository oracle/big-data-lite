sqlplus sys/welcome1 as sysdba @enable_ogg_on_orcl.sql
echo "alter table moviedemo.movie add supplemental log data(all) columns;" | sqlplus system/welcome1@orcl
