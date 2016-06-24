#!/bin/bash

SG_HOME="/u01"
SHARED_FOLDER='/opt/shareddir'
HADOOP_LIB_PATH="/usr/lib/hadoop"
ALL_ACCESS_FOLDER=$SHARED_FOLDER/spatial
BDS_CONFIG_FILE='/u01/bigdatasql_config/bigdata.properties'
BDS_CONFIG_PARAM='bigdatasql.enabled=true'

echo "Post-samples update"


function updateSpatial {

    ######
    # install spatial image processing framework
    ######
    echo Shared folder:  $SHARED_FOLDER
    echo All Access Dir: $ALL_ACCESS_FOLDER


    echo "Copy gdal data to $SHARED_FOLDER"
    cp -R /opt/oracle/oracle-spatial-graph/spatial/raster/gdal/data $SHARED_FOLDER/


    echo "Create ALL_ACCESS_FOLDER: $ALL_ACCESS_FOLDER"
    sudo mkdir $ALL_ACCESS_FOLDER
    sudo chmod 777 $ALL_ACCESS_FOLDER

    echo "Copy data to ALL_ACCESS_FOLDER"
    sudo cp -R /opt/oracle/oracle-spatial-graph/spatial/raster/examples/data $ALL_ACCESS_FOLDER/
    sudo chmod -R 777 $ALL_ACCESS_FOLDER/data/xmls

    echo "update permisions on java examples"
    sudo chmod -R 777 /opt/oracle/oracle-spatial-graph/spatial/vector/examples/java

    echo "...Copy sample data"
    mkdir -p /opt/oracle/oracle-spatial-graph/spatial/demo/vector/
    cp /home/oracle/src/scripts/tweets.json /opt/oracle/oracle-spatial-graph/spatial/demo/vector/

}


echo "...Checking if Spatial Image Processng needs to be updated"

if [ "$(ls -A $SHARED_FOLDER)" ]; then
     echo "...Spatial is up to date."
else
    echo "...Updating Spatial"
    updateSpatial

fi

echo "...Checking if Big Data SQL Configuration needs to be updated"

if [ "$(grep $BDS_CONFIG_PARAM $BDS_CONFIG_FILE)" ]; then
     echo "...Big Data SQL config is up to date"
else
    echo "...Updating Big Data SQL Config"
    echo $BDS_CONFIG_PARAM >> $BDS_CONFIG_FILE
fi

echo "Post-sample update complete"

