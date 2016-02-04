#
# script to compile and run the Simple Standalone Java App for zxing

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

cd SimpleJavaApp

echo Compiling


javac -cp lib/javase-3.2.1.jar:lib/core-3.2.1.jar:lib/jcommander-1.48.jar barcodedemo/BarcodeDetector.java

echo Running

java -cp .:lib/javase-3.2.1.jar:lib/core-3.2.1.jar:lib/jcommander-1.48.jar barcodedemo/BarcodeDetector ../images/test.jpg

echo Script Complete.

