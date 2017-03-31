#!/bin/bash
#
# rstudio  Start/Stop RStudio
#
# chkconfig: 5 90 60

# If anything goes wrong, fail
exit_err () {
  echo "There was an error, please review the output above and, if appropriate, report a bug on https://github.com/oracle/big-data-lite with the full output of the script."
  exit 1
}
trap 'exit_err' ERR

dir=$(dirname "$(readlink -f $0)")
thirdparty_root=$dir/inst/
rstudio_root=$thirdparty_root/rstudio
rstudio_inst_name=rstudio-server-1.0.136-1.x86_64
rstudio_pkg_url=https://download2.rstudio.org/rstudio-server-rhel-1.0.136-x86_64.rpm

cd $dir

if [ "$1" == "install" ]; then
  echo "Setting up RStudio..."
  mkdir -p $rstudio_root
  rstudio_pkg=$rstudio_root/$(basename $rstudio_pkg_url)
  [ -f $rstudio_pkg ] ||
    curl $rstudio_pkg_url -o $rstudio_pkg

  # Install rstudio; we do a system installation, as there are hard-coded paths in the package
  (rpm -q ${rstudio_inst_name} > /dev/null) || (sudo rpm -i $rstudio_pkg)

  # Configure
  (sudo cp $dir/conf/rstudio/rserver.conf /etc/rstudio)
  (sudo cp $dir/conf/rstudio/SessionHelp.R /usr/lib/rstudio-server/R/modules/SessionHelp.R)
  (sudo chmod 644 /usr/lib/rstudio-server/R/modules/SessionHelp.R)

  (
  echo "Add to system services? (works on Oracle Linux, requires sudo) [Y/n]"
  read a
  a=""
  [ "$a" == "n" ] && exit 0
  sudo rm -f /etc/init.d/rstudio
  sudo ln -s "$(readlink -f $0)" /etc/init.d/rstudio
  sudo chkconfig --add rstudio
  )

  exit 0
elif [ "$1" == "uninstall" ]; then
  sudo yum  --disablerepo='*' --disableplugin='*' remove ${rstudio_inst_name}

  exit 0
elif [ "$1" == "start" ]; then
  (sudo /usr/lib/rstudio-server/bin/rstudio-server start) || echo "Failed to start RStudio!"

  exit 0
elif [ "$1" == "stop" ]; then
  (sudo /usr/lib/rstudio-server/bin/rstudio-server stop) || echo "Failed to stop RStudio!"

  exit 0
elif [ "$1" == "status" ]; then
  trap '' ERR

  curr_status="$(sudo /usr/lib/rstudio-server/bin/rstudio-server status)"
  if [ "$curr_status" == "rstudio-server stop/waiting" ]; then
   echo "$curr_status"
   exit 1
 else
   echo "$curr_status"
   exit 0
 fi
fi

echo "Usage: $0 {install | uninstall | start | stop | status}"
exit -1

