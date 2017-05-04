#!/bin/bash

echo -n "Enter kvstore hostname name and press [ENTER]: "
read hostname

echo -n "Enter kvstore port and press [ENTER]: "
read port

echo -n "Enter kvstore name and press [ENTER]: "
read kvstore

echo "*** Create NoSQL DB Tables ***" 
nohup java -jar $KVHOME/lib/kvstore.jar runadmin -port $port -host $hostname - store $kvstore  load -file /home/oracle/movie/moviedemo/nosqldb/table_ddl/table.txt
