
export HADOOP_CONF_DIR=/etc/hadoop/conf

export HADOOP_HOME=/usr/lib/hadoop
export HADOOP_VERSION=2.6.0
export CDH_VERSION=5.5.1

export CP=${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar:${MMA_HOME}/opencv_3.0.0/opencv-300.jar:${HADOOP_HOME}/hadoop-common-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar:${HADOOP_HOME}/hadoop-auth-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar:${HADOOP_HOME}/lib/commons-lang-2.6.jar:${HADOOP_HOME}/lib/commons-logging-1.1.3.jar:${HADOOP_HOME}/lib/commons-configuration-1.6.jar:${HADOOP_HOME}/lib/commons-collections-3.2.2.jar:${HADOOP_HOME}/lib/guava-11.0.2.jar:${HADOOP_HOME}/lib/slf4j-api-1.7.5.jar:${HADOOP_HOME}/lib/slf4j-log4j12.jar:${HADOOP_HOME}/lib/log4j-1.2.17.jar:${HADOOP_HOME}/lib/commons-cli-1.2.jar:${HADOOP_HOME}/lib/protobuf-java-2.5.0.jar:${HADOOP_HOME}/lib/avro.jar:${HADOOP_HOME}/lib/servlet-api-2.5.jar:/usr/lib/hadoop-hdfs/hadoop-hdfs-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar:${HADOOP_HOME}/lib/htrace-core4-4.0.1-incubating.jar:${HADOOP_HOME}/hadoop-mapreduce-client-core-${HADOOP_VERSION}-cdh${CDH_VERSION}.jar

echo $CP

# command to use for running java programs (for convenience) 
#export JAVACMD=/usr/bin/java -classpath ${CP} -Xms2048m -Xmx2048m

#${JAVACMD} oracle.ord.hadoop.demo.OrdPlayImages -hadoop_conf_dir ${HADOOP_CONF_DIR} -image_file_dir voutput_image

/usr/bin/java -classpath ${CP} -Xms2048m -Xmx2048m oracle.ord.hadoop.demo.OrdPlayImages -hadoop_conf_dir ${HADOOP_CONF_DIR} -image_file_dir voutput_image
