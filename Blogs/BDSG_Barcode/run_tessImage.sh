#
# script to compile and run the Tesseract OCR example


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
export TESS4J_HOME=$CUR_DIR_NAME/lib



echo Compiling
cd TessImage

javac -cp ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar:${TESS4J_HOME}/*:/usr/lib/hadoop/* TessImage.java


echo Running

export TESSDATA_PREFIX=/usr/share/tesseract


hadoop fs -rm -r bdsg_mma_output
hadoop jar ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar -libjars ${CUR_DIR_NAME}/TessImage/TessImage.class,${TESS4J_HOME}/jai-imageio-core-1.3.1.jar,${TESS4J_HOME}/jna-4.2.1.jar,${TESS4J_HOME}/tess4j-3.0.0.jar,${TESS4J_HOME}/slf4j-api-1.7.7.jar,${TESS4J_HOME}/jul-to-slf4j-1.7.13.jar,${TESS4J_HOME}/log4j-over-slf4j-1.7.13.jar,${TESS4J_HOME}/jcl-over-slf4j-1.7.13.jar,${TESS4J_HOME}/lept4j-1.0.1.jar -conf ${CUR_DIR_NAME}/TessImage/tessImage.xml bdsg_mma_input_ocr bdsg_mma_output


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

