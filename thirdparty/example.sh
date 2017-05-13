#!/bin/bash

# If anything goes wrong, fail
exit_err () {
  echo "There was an error, please review the output above and, if appropriate, report a bug on https://github.com/oracle/big-data-lite with the full output of the script."
  exit 1
}
trap 'exit_err' ERR

# If run as root or as service, rerun as owner
owner=$(stat -c '%U' $0)
if [ "$USER" != "$owner" ] && ( [ "$USER" == "root" ] || [ "$USER" == "" ] ); then
  sudo -E -u $owner bash -c "$0 $*"
  exit $?
fi

dir=$(dirname "$(readlink -f $0)")
thirdparty_root=$dir/inst/
# Put additional global variables here

cd $dir

if [ "$1" == "install" ]; then
  # Install script goes here

  exit 0
elif [ "$1" == "uninstall" ]; then
  # Uninstall script goes here

  exit 0
elif [ "$1" == "start" ]; then
  # Start script goes here

  exit 0
elif [ "$1" == "stop" ]; then
  # Stop script goes here

  exit 0
elif [ "$1" == "status" ]; then
  # Print status

  exit 0
fi

echo "Usage: $0 {install | uninstall | start | stop | status}"
exit -1

