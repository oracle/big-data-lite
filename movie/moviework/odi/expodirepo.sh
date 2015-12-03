mv dev_odi_repo.dmp dev_odi_repo.dmp.bak
echo "create directory ODI_PUMP_DIR as '/home/oracle/movie/moviework/odi';"| sqlplus -s system/welcome1@orcl
expdp system/welcome1@orcl directory=ODI_PUMP_DIR schemas=dev_odi_repo dumpfile=dev_odi_repo.dmp
