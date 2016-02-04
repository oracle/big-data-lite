#
# script to run the Spark Scala ZXing Barcode App 

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

cd ScalaBarcodeApp



echo " "
echo " "
echo "While running, view the progress in YARN at http://localhost:8088/cluster"
echo "               view the progress in HUE at http://localhost:8888/jobbrowser/"
echo "               view the progress in SPARKUI at http://localhost:4040/"
echo " "
echo Running
echo " "
echo " "

spark-submit --class "BarcodeApp" --jars ../SimpleJavaApp/lib/core-3.2.1.jar,../SimpleJavaApp/lib/javase-3.2.1.jar target/scala-2.10/scala-zxing-barcode-application_2.10-1.0.jar barcode/images/test.jpg


echo " "
echo " "

echo " "
echo "View Hue at http://localhost:8888/jobbrowser/"
echo "View YARN at http://localhost:8088/cluster"
echo "View Spark History at http://localhost:18088/"
echo " "


echo Script Complete.

