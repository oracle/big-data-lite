#
# script to copy directories to a working area

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

echo Copying Directories to working area /home/oracle directory

mkdir ~/BDSG_Barcode
cp -r * ~/BDSG_Barcode

#mkdir ~/src/Blogs
#mkdir ~/src/Blogs/BDSG_Barcode
#cp -r * ~/src/Blogs/BDSG_Barcode

echo Script Complete.

