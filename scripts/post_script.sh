echo Running post-script
echo Updating /usr/lib64/R/etc/Renviron.site
sudo cp /usr/lib64/R/etc/Renviron.site /usr/lib64/R/etc/Renviron.site.sav
sudo cp ~oracle/src/scripts/Renviron.site /usr/lib64/R/etc/
sudo chmod 644 /usr/lib64/R/etc/Renviron.site
sudo chown root:root /usr/lib64/R/etc/Renviron.site
echo Update complete.
