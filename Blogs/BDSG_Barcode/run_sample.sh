#!/bin/sh

#
# script to compile and run the BDSG MMA Sample Java program

askyn() {
   while [ 1 ]
   do
      echo -n "${1}"
      read YN
      if [ "${YN}" = "n" -o "${YN}" = "N" ]
      then
         YN="N"
         break
      fi
      if [ "${YN}" = "y" -o "${YN}" = "Y" ]
      then
         YN="Y"
         break
      fi
      if [ "${YN}" = "" ]
      then
         YN="${2}"
         break
      fi
   done
}


CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME


export MMA_HOME=/opt/oracle/oracle-spatial-graph/multimedia

echo Compiling
cd OrdFaceDetection
javac -cp ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar:${MMA_HOME}/opencv_3.1.0/opencv-310.jar:/usr/lib/hadoop/* OrdFaceDetectionSample.java

echo Running

hadoop fs -rm -r bdsg_mma_output
hadoop jar ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar -libjars OrdFaceDetectionSample.class -conf ${CUR_DIR_NAME}/OrdFaceDetection/sample.xml bdsg_mma_input bdsg_mma_output


echo Showing output
cd ${CUR_DIR_NAME}

#askyn "Do you wish to launch the image viewer [Y]: " Y
#if [ "${YN}" = "Y" ]
#then
#   echo
#   echo Type CTRL-C in this window to quit the image viewer.
#   echo
#    ./play_images.sh bdsg_mma_output 4
#fi

askyn "Do you wish to view images [Y]: " Y
if [ "${YN}" = "Y" ]
then
   echo
   echo
    ./save_images.sh bdsg_mma_output 4
fi




echo Script Complete.
