#!/bin/bash

echo "*** Stopping Weblogic Server ***"

/u01/Middleware/user_projects/domains/movieplex_domain/bin/stopWebLogic.sh 

echo "*** Deleting activity as well as weblogic console logs ***"
rm -rf /u01/Middleware/logs/*.*
rm /u01/nosql/kvstd.out

echo "*** Stopping Oracle NoSQL Server ***"
#ps -ef | grep kv- | grep -v grep | awk '{print $2}' | xargs -r kill -9 
java -jar $KVHOME/lib/kvstore.jar stop -root $KVROOT


echo "*** Deleting /u02/kvroot ***"
rm -rf $KVROOT


