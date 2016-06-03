
export HADOOP_CONF_DIR=/etc/hadoop/conf


export MMA_HOME=/opt/oracle/oracle-spatial-graph/multimedia


export CP=${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar:${MMA_HOME}/opencv_3.1.0/opencv-310.jar:${CLASSPATH}

java -classpath ${CP} -Xms2048m -Xmx2048m oracle.ord.hadoop.demo.OrdPlayImages -hadoop_conf_dir ${HADOOP_CONF_DIR} -image_file_dir $1 -image_file_number $2
