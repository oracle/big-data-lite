#!/bin/sh
#
# trainface.sh
#
# Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      trainface.sh - the scripts for the example of training face recognition model
#

HADOOP_HOME=/usr/lib/hadoop
HADOOP_VERSION=2.6.0
CDH_VERSION=5.5.1

CP=$MMA_HOME/lib/ordhadoop-multimedia-analytics.jar:$MMA_HOME/opencv_3.0.0/opencv-300.jar:${HADOOP_HOME}/hadoop-common-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar:${HADOOP_HOME}/hadoop-auth-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar:${HADOOP_HOME}/lib/commons-lang-2.6.jar:${HADOOP_HOME}/lib/commons-logging-1.1.3.jar:${HADOOP_HOME}/lib/commons-configuration-1.6.jar:${HADOOP_HOME}/lib/commons-collections-3.2.2.jar:${HADOOP_HOME}/lib/guava-11.0.2.jar:${HADOOP_HOME}/lib/slf4j-api-1.7.5.jar:${HADOOP_HOME}/lib/slf4j-log4j12-1.7.5.jar:${HADOOP_HOME}/lib/log4j-1.2.17.jar:${HADOOP_HOME}/lib/commons-cli-1.2.jar:${HADOOP_HOME}/lib/protobuf-java-2.5.0.jar:${HADOOP_HOME}/lib/avro.jar:${HADOOP_HOME}/hadoop-hdfs-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar:${HADOOP_HOME}/hadoop-mapreduce-client-core-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar

# command to use for running java programs {for convenience}
JAVACMD="java -classpath ${CP} -Xms2048m -Xmx2048m -Djava.library.path=$MMA_HOME/opencv_3.0.0/lib:../../lib"

${JAVACMD} oracle.ord.hadoop.recognizer.OrdFaceTrainer ./config/facetrain_bigdata.xml
