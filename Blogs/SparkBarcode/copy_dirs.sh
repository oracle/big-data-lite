#
# script to copy directories to a working area

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

echo Copying Directories to working area /home/oracle directory

mkdir ~/SparkBarcode
cp -r * ~/SparkBarcode

#mkdir ~/src/Blogs
#mkdir ~/src/Blogs/SparkBarcode
#cp -r * ~/src/Blogs/SparkBarcode

echo Script Complete.

