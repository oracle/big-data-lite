#
# script to copy files into hdfs

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

export MMA_HOME=/opt/oracle/oracle-spatial-graph/multimedia

echo Copying files into hdfs bdsg_mma directories
hadoop fs -mkdir bdsg_mma_input
hadoop fs -put ${MMA_HOME}/example/video/bigdata.mp4 bdsg_mma_input
hadoop fs -mkdir bdsg_mma_input_qr
hadoop fs -put qr.mov bdsg_mma_input_qr
hadoop fs -mkdir bdsg_mma_input_ocr
hadoop fs -put ocr.mov bdsg_mma_input_ocr





echo setup_hdfs Script Complete.

