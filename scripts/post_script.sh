echo "Updating bdsg startup script in /etc/init.d"
sudo cp /home/oracle/src/scripts/bdsg /etc/init.d/
sudo chmod 755 /etc/init.d/bdsg
sudo chown root:root /etc/init.d/bdsg
