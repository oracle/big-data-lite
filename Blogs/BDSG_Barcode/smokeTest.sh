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
   askyn "Do you wish to run run_video2image.sh [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./run_video2image.sh
     echo " "
     message "Check for Success: Do see individual images?"
     read -p "Press [Enter] to continue"
   fi

echo " "
echo " "
echo " "
echo " "
   askyn "Do you wish to run run_sample.sh [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./run_sample.sh
     echo " "
     message "Check for Success: Do see individual images with faces detected?"
     read -p "Press [Enter] to continue"
   fi


echo " "
echo " "
echo " "
echo " "
   askyn "Do you wish to run run_QRImage.sh [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./run_QRImage.sh
     echo " "
     message "Check for Success: Do see individual images with qr codes?"
     read -p "Press [Enter] to continue"
   fi

echo " "
echo " "
echo " "
echo " "
   askyn "Do you wish to run run_tessImage.sh [Y]: " Y
   if [ "${YN}" = "Y" ]
   then
     ./run_tessImage.sh
     echo " "
     message "Check for Success: Do see individual images with ocr text?"
     read -p "Press [Enter] to continue"
   fi


message "SmokeTest complete"
