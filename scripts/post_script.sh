#!/bin/bash

OD4H_PATCH_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Patching Big Data Lite"
echo ""
echo "Updating OD4H...."
echo "Dropping current OD4H - /u01/od4h
sudo rm -rf /u01/od4h

echo "Installing new OD4H into same location"
sudo unzip -d /u01 $OD4H_PATCH_DIR/od4h.zip 
sudo chown -R oracle:oinstall /u01/od4h

echo "Finished updating OD4H"
echo ""

echo "Updating JDeveloper desktop shortcut"
sed -i 's/Middleware/jdev/g' /home/oracle/.gnome2/panel2.d/default/launchers/jdev-*.desktop
echo ""
echo "JDeveloper desktop shortcut updated.  You may need to restart the VM to see the change."
echo "Updates complete."
