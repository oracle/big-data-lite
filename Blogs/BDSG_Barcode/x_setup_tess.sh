# script to install Tesseract OCR package
# assumes that http_proxy is already set (via setup.sh)


# install EPEL
rm epel-release-6-8.noarch.rpm

wget http://dl.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm

sudo rpm -Uvh epel-release-6-8.noarch.rpm
echo
echo


# if using proxy, need to setup yum.conf
if [ "X"$http_proxy = "X" ]; then
  echo Not using proxy.  Will assume original yum.conf has not been edited to add proxies.
else
  echo Will temporarily add proxy to yum.conf

DATE=$(date +"%Y%m%d%H%M")

sudo cp /etc/yum.conf /etc/yum.conf.$DATE
cat <<EOF | sudo tee /etc/yum.conf
[main]
cachedir=/var/cache/yum/\$basearch/\$releasever
keepcache=0
debuglevel=2
logfile=/var/log/yum.log
exactarch=1
obsoletes=1
gpgcheck=1
plugins=1
installonly_limit=3
proxy=$http_proxy
EOF
fi

echo
echo
echo The first time you run yum, it will take several minutes to update the repositories
echo
echo 

# install tesseract
sudo yum install -y --nogpgcheck tesseract
sudo yum install -y --nogpgcheck tesseract-osd

echo
echo

# restore original yum.conf if needed
if [ "X"$http_proxy = "X" ]; then
  echo 
else
  echo restoring yum.conf

  sudo cp /etc/yum.conf.$DATE /etc/yum.conf
fi

echo Tesseract setup script finished.  Type "tesseract" to test.
echo

