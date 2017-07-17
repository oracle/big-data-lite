#!/bin/bash
#
# rstudio  Start/Stop RStudio
#
# chkconfig: 5 90 60

# If anything goes wrong, fail
exit_err () {
  echo "There was an error, please review the output above and, if appropriate, report a bug on https://github.com/oracle/big-data-lite with the full output of the script."
  echo ""
  echo "If you are having connection difficulties, ensure you set the https_proxy before running this script."
  echo "  Example:"
  echo "  export https_proxy=myproxy.company.com:80"
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
rstudio_root=/opt/rstudio
rstudio_inst_name=rstudio-server-1.0.136-1.x86_64
rstudio_pkg_url=https://download2.rstudio.org/rstudio-server-rhel-1.0.136-x86_64.rpm

cd $dir

if [ "$1" == "install" ]; then
  echo "Setting up RStudio..."
  sudo mkdir -p $rstudio_root
  sudo chmod 755 $rstudio_root
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
  a=""
  while [ "$a" != "y" ] && [ "$a" != "n" ]; do
    echo "Add to system services? (works on Oracle Linux, requires sudo) [y/n]"
    read a
  done
  if [ "$a" == "y" ]; then
    sudo rm -f /etc/init.d/rstudio
    sudo ln -s "$(readlink -f $0)" /etc/init.d/rstudio
    sudo chkconfig --add rstudio

    if ! grep -q 'RStudio,rstudio,RStudio server,1' /opt/bin/services.prop; then
      sudo echo 'RStudio,rstudio,RStudio server,1' >> /opt/bin/services.prop
    fi
  fi
  )

  echo "Done. You can run $0 install_extra to grab extra R packages."

  exit 0
elif [ "$1" == "install_extra" ]; then

  echo "Installing extra R packages.."
  Rscript --verbose <(cat <<EOC
# List of packages
pkgs <-c("DBI", "gtools", "gdata", "whisker", "xtable", "digest", "doParallel", "gridBase", "pkgmaker", "rngtools", "registry", "stringi", "magrittr", "stringr", "irlba", "scatterplot3d", "lmtest", "vcd", "TSP", "qap", "gclus", "dendextend", "bitops", "caTools", "gplots", "seriation", "quadprog", "zoo", "reshape2", "gtable", "dichromat", "plyr", "munsell", "labeling", "scales", "ggplot2", "RColorBrewer", "NMF", "igraph", "arulesViz", "arules", "tseries", "fracdiff", "nnet", "colorspace", "timeDate", "sandwich", "knitr", "Sparkr")
install.packages(pkgs, dependencies=TRUE, repos="http://cran.fhcrc.org", lib="/u01/app/oracle/product/12.1.0.2/dbhome_1/R/library", type="source")

# RcppArmadillo requires gcc 4.6 or greater and Big Data Lite contains gcc 4.4-7. Install an older version of RcppArmadillo  as workaround.
install.packages("http://cran.fhcrc.org/src/contrib/Archive/RcppArmadillo/RcppArmadillo_0.6.200.2.0.tar.gz", repos=NULL, lib="/u01/app/oracle/product/12.1.0.2/dbhome_1/R/library", type="source") 

# forecast depends on RcppArmadillo
install.packages("forecast", repos="http://cran.fhcrc.org")
EOC)

  exit 0
elif [ "$1" == "uninstall" ]; then
  sudo yum  --disablerepo='*' --disableplugin='*' remove ${rstudio_inst_name}

  exit 0
elif [ "$1" == "start" ]; then
  (sudo setsid /usr/lib/rstudio-server/bin/rstudio-server start) || echo "Failed to start RStudio!"

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

echo "Usage: $0 {install | install_extra | uninstall | start | stop | status}"
exit -1

