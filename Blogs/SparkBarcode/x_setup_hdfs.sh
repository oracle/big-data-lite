#
# script to copy files into hdfs

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

echo Copying files into hdfs barcode/images directory
hadoop fs -mkdir barcode
hadoop fs -mkdir barcode/images
hadoop fs -rm barcode/images/*
hadoop fs -put images/* barcode/images


echo setup_hdfs Script Complete.

