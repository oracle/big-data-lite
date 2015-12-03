#!/bin/bash
hadoop fs -rm moviework/applog_json/stream*
touch /u01/Middleware/logs/activity.out
chmod 777 /u01/Middleware/logs/activity.out
gnome-terminal -e "tail -f /u01/Middleware/logs/activity.out" --title=ApplicationLog &
gnome-terminal -e "./flume_movieagent.sh" --title=Flume &
