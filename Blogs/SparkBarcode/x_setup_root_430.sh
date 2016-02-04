#!/bin/sh

# script to fix issues with YARN history/log ports in BDL VM v 4.3.0.
# NOTE. BDL VM 4.3.1 has these fixes built-in


echo "Run this as root"


DATE=$(date +"%Y%m%d%H%M")

cp /etc/spark/conf/spark-defaults.conf /etc/spark/conf/spark-defaults.conf.$DATE
cp extras/spark-defaults.conf /etc/spark/conf/spark-defaults.conf

cp /etc/spark/conf/log4j.properties /etc/spark/conf/log4j.properties.$DATE
cp extras/spark_log4j.properties /etc/spark/conf/log4j.properties

cp /etc/hadoop/conf/yarn-site.xml /etc/hadoop/conf/yarn-site.xml.$DATE
cp extras/yarn-site.xml /etc/hadoop/conf/yarn-site.xml

cp /opt/bin/services.prop /opt/bin/services.prop.$DATE
rm /opt/bin/services.prop
cp extras/services.prop /opt/bin/services.prop
chown oracle:oracle /opt/bin/services.prop


service spark-history-server stop
service spark-history-server start

service hadoop-yarn-nodemanager stop
service hadoop-yarn-resourcemanager stop
service hadoop-yarn-resourcemanager start
service hadoop-yarn-nodemanager start

