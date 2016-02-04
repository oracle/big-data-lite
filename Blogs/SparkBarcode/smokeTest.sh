#
# smoke test script to run all parts of demo
# this is used to easily validate that demo works between different versions of Big Data Lite VM

CUR_DIR=`dirname "$0"`
CUR_DIR_NAME=`cd "$CUR_DIR";pwd`
cd $CUR_DIR_NAME

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

message()
{
   echo "> ${1}"
}


echo " "
   askyn "Do you wish to run setup.sh [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./setup.sh
   fi

echo " "
echo " "
echo " "
echo " "
   askyn "Do you wish to run run_simple_java.sh [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./run_simple_java.sh
     echo " "
     message "Check for Success: Did the output find a QR code?"
     read -p "Press [Enter] to continue"
   fi


echo " "
echo " "
echo " "
echo " "
   askyn "Do you wish to build and run simple__scala [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./build_simple_scala.sh
     ./run_simple_scala.sh
     echo " "
     message "Check for Success: Did the Spark App finish without error?"
     message "Check for Success: Did the URLs to view Spark History, Yarn, and Hue work?"
     read -p "Press [Enter] to continue"
   fi

echo " "
echo " "
echo " "
echo " "
   askyn "Do you wish to build and run barcode [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./build_barcode.sh
     ./run_barcode_many.sh
     echo " "
     message "Check for Success: Did the Spark App finish without error?"
     message "Check for Success: Did output detect various QR and UPC codes?"
     message "Check for Success: Did the URLs to view Spark History, Yarn, and Hue work?"
     read -p "Press [Enter] to continue"
   fi

message "SmokeTest complete"
