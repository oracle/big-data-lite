shutdown immediate
startup mount
alter database noarchivelog;
alter database drop supplemental log data;
alter database no force logging;
alter system set ENABLE_GOLDENGATE_REPLICATION=FALSE SCOPE=BOTH;
alter database open;
exit
