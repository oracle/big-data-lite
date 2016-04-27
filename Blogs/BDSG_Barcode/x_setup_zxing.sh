#
# script to download zxing libraries

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

mkdir zxing_lib
cd zxing_lib
rm *.jar

wget http://repo1.maven.org/maven2/com/google/zxing/javase/3.2.1/javase-3.2.1.jar
wget http://repo1.maven.org/maven2/com/google/zxing/core/3.2.1/core-3.2.1.jar
wget http://central.maven.org/maven2/com/beust/jcommander/1.48/jcommander-1.48.jar

echo setup_zxing.sh Script Complete.

