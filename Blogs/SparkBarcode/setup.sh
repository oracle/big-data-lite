#
# master script to setup Big Data Lite VM for Barcode/Spark/Scala/ZXing blog demo

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

ask_proxy() {
   PROXY_LINE=""
   HTTP_PROXY="NONE"
   askyn "Do you wish to configure an HTTP proxy server to access the Internet [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
      while [ "${PROXY_LINE}" = "" ]
      do
         echo "Specify your HTTP proxy using the following format: \"host:port\" or \"proxyUsername:proxyPassword@host:port\"" 
         echo "For example: \"myproxyhost.us.oracle.com:80\" or \"proxyuser:proxypass@myproxyhost.us.oracle.com:8080\""
         echo -n ">  "
         read PROXY_LINE
      done
      HTTP_PROXY="http://${PROXY_LINE}"

   fi
}

message()
{
   echo "> ${1}"
}


CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

message "INFO: This script will need to download some additional libraries from the Internet."

ask_proxy

message "INFO: If there is no Internet connectivity, this command may hang."
if [ "${HTTP_PROXY}" != "NONE" ]
then
  message "INFO: Setting proxy to ${HTTP_PROXY}"

  export http_proxy=${HTTP_PROXY}
  export https_proxy=${HTTP_PROXY}
else
  message "INFO: Unsetting out proxies"

  unset http_proxy
  unset https_proxy

fi


echo Setup ZXing
./x_setup_zxing.sh

echo Setup SBT
./x_setup_sbt.sh

echo Setup HDFS sample files
./x_setup_hdfs.sh

echo Sanity Check: Make sure you see jar files with reasonable file sizes
ls -l SimpleJavaApp/lib
ls -l sbt*.jar
echo Sanity Check: Make sure you see jar files with reasonable file sizes
echo " "
echo oracle user part of setup.sh Script Complete.

#echo " "
#   askyn "Do you wish to run setup_root.sh to fix some SparkHistory config issues? [Y]: " Y
#   if [ "${YN}" = "Y" ]
#   then
#     sudo ./setup_root.sh
#   fi

echo " "
read -p "Press [Enter] to view readme for any known issues or extra setup steps"
less readme.MD
