#!/bin/bash

# This script does not initialize the KVStore to its initial state
# Will start much more quickly than startDemo.sh - but you will see your
# previous activity

echo "*** Start Oracle NoSQL Server ***"
nohup java -jar $KVHOME/lib/kvstore.jar kvlite -root $KVROOT > /u01/nosql/kvstd.out &
echo "*** Start Weblogic Server ***"
nohup /u01/Middleware/user_projects/domains/movieplex_domain/bin/startWebLogic.sh > /u01/Middleware/logs/movieplex.out &


