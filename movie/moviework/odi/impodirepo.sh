echo "create directory ODI_PUMP_DIR as '/home/oracle/movie/moviework/odi';"| sqlplus -s system/welcome1@orcl
echo "drop user DEV_ODI_REPO CASCADE;" | sqlplus -s system/welcome1@orcl
impdp system/welcome1@orcl schemas=dev_odi_repo directory=ODI_PUMP_DIR dumpfile=dev_odi_repo.dmp
echo "drop directory ODI_PUMP_DIR;"| sqlplus -s system/welcome1@orcl

