shutdown immediate
startup mount
alter database archivelog;
alter database add supplemental log data;
alter database force logging;
alter system set ENABLE_GOLDENGATE_REPLICATION=TRUE SCOPE=BOTH;
alter database open;
exit
