#!/bin/sh
#

#
# script to compile and run the QR Detection (Zxing) Sample for BDSG MMA

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
export ZX_HOME=$CUR_DIR_NAME/zxing_lib


echo Compiling
cd QR
javac -cp ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar:${ZX_HOME}/*:/usr/lib/hadoop/* QRImage.java BarcodeProcessor.java

echo Running




hadoop fs -rm -r bdsg_mma_output
hadoop jar ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar -libjars QRImage.class,BarcodeProcessor.class,${ZX_HOME}/javase-3.2.1.jar,${ZX_HOME}/core-3.2.1.jar -conf ${CUR_DIR_NAME}/QR/QRimage.xml bdsg_mma_input_qr bdsg_mma_output


echo Showing output
cd ${CUR_DIR_NAME}


#askyn "Do you wish to launch the image viewer [Y]: " Y
#if [ "${YN}" = "Y" ]
#then
#   echo
#   echo Type CTRL-C in this window to quit the image viewer.
#   echo
#    ./play_images.sh bdsg_mma_output 2
#fi

askyn "Do you wish to view images [Y]: " Y
if [ "${YN}" = "Y" ]
then
   echo
   echo
    ./save_images.sh bdsg_mma_output 2
fi




echo Script Complete.
