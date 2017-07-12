#!/bin/bash

echo "*** Start Weblogic Server ***"
nohup /u01/Middleware/user_projects/domains/movieplex_domain/bin/startWebLogic.sh > /u01/Middleware/logs/movieplex.out &

echo "*** Untar KVROOT  ***"
unzip /u02/kvroot*.zip -d /u02/

echo "*** Start Oracle NoSQL Server ***"
nohup java -jar $KVHOME/lib/kvstore.jar kvlite -root $KVROOT > /u01/nosql/kvstd.out &

echo -n "Enter kvstore hostname name and press [ENTER]: "
read hostname

echo -n "Enter kvstore port and press [ENTER]: "
read port

echo -n "Enter kvstore name and press [ENTER]: "
read kvstore

echo "*** Create NoSQL DB Tables ***" 
nohup java -jar $KVHOME/lib/kvstore.jar runadmin -port $port -host $hostname - store $kvstore  load -file /home/oracle/movie/moviedemo/nosqldb/table_ddl/table.txt

