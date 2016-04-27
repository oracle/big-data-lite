#
# script to compile and run the Simple Standalone Java App for zxing

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

export MMA_HOME=/opt/oracle/oracle-spatial-graph/multimedia
export TESS4J_HOME=$CUR_DIR_NAME/lib


echo Compiling
cd TessText
javac -cp ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar:${TESS4J_HOME}/*:/usr/lib/hadoop/* Tess.java


echo Running

export TESSDATA_PREFIX=/usr/share/tesseract


hadoop fs -rm -r bdsg_mma_output
hadoop jar ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar -libjars ${CUR_DIR_NAME}/TessText/Tess.class,${TESS4J_HOME}/jai-imageio-core-1.3.1.jar,${TESS4J_HOME}/jna-4.2.1.jar,${TESS4J_HOME}/tess4j-3.0.0.jar,${TESS4J_HOME}/slf4j-api-1.7.7.jar,${TESS4J_HOME}/jul-to-slf4j-1.7.13.jar,${TESS4J_HOME}/log4j-over-slf4j-1.7.13.jar,${TESS4J_HOME}/jcl-over-slf4j-1.7.13.jar,${TESS4J_HOME}/lept4j-1.0.1.jar -conf ${CUR_DIR_NAME}/TessText/tess.xml bdsg_mma_input_ocr bdsg_mma_output


hadoop fs -ls bdsg_mma_output

echo
echo
echo Displaying output part-m-00000

hadoop fs -cat bdsg_mma_output/part-m-00000

echo Script Complete.

