#
# script to build the Scala ZXing Barcode App 
#

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

cd ScalaBarcodeApp

echo "SBT may need to download dependencies from the internet."

ask_proxy

message "INFO: If there is no Internet connectivity, this command may hang."
if [ "${HTTP_PROXY}" != "NONE" ]
then
  message "INFO: Setting proxy to ${HTTP_PROXY}"

  export http_proxy=${HTTP_PROXY}
  export https_proxy=${HTTP_PROXY}

fi

echo Compiling

../sbt package



echo Script Complete.

