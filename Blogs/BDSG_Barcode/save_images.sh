
export HADOOP_CONF_DIR=/etc/hadoop/conf


export MMA_HOME=/opt/oracle/oracle-spatial-graph/multimedia



echo Compiling SequenceFileRead

javac -cp ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar:/usr/lib/hadoop/* SequenceFileRead.java

jar cf Seq.jar SequenceFileRead.class


echo Reading the SequenceFile output and saving each entry as its own image file
mkdir temp_images
cd temp_images

rm *.jpg

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar
COUNTER=${2}
while [ $COUNTER -gt 0 ]; do
  let COUNTER=COUNTER-1
  echo
  echo
  echo Saving images from sequence file part-m-0000${COUNTER}
  hadoop jar ../Seq.jar SequenceFileRead ${1}/part-m-0000${COUNTER} $COUNTER
done


xdg-open image0_0.jpg

echo Script Complete.



