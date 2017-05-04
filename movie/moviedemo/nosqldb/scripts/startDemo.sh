#!/bin/bash

echo "*** Start Weblogic Server ***"
nohup /u01/Middleware/user_projects/domains/movieplex_domain/bin/startWebLogic.sh > /u01/Middleware/logs/movieplex.out &

echo "*** Untar KVROOT  ***"
unzip /u02/kvroot*.zip -d /u02/

echo "*** Start Oracle NoSQL Server ***"
nohup java -jar $KVHOME/lib/kvstore.jar kvlite -root $KVROOT > /u01/nosql/kvstd.out &
