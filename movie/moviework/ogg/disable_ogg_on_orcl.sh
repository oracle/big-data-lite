sqlplus sys/welcome1 as sysdba @disable_ogg_on_orcl.sql
echo "alter table moviedemo.movie drop supplemental log data(all) columns;" | sqlplus system/welcome1@orcl
