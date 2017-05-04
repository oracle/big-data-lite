# R script to install open source R packages for HOL excercises
# This script is called by /home/oracle/scripts/install_additional_packages.sh

# List of packages
 pkgs <-c("DBI",
	  "gtools",
          "gdata",
          "whisker",
          "xtable",
          "digest",
          "doParallel",
          "gridBase",
          "pkgmaker",
          "rngtools",
          "registry",
          "stringi",
          "magrittr",
          "stringr",
          "irlba",
          "scatterplot3d",
          "lmtest",
          "vcd",
          "TSP",
          "qap",
          "gclus",
          "dendextend",
          "bitops",
          "caTools",
          "gplots",
          "seriation",
          "quadprog",
          "zoo",
          "reshape2",
          "gtable",
          "dichromat",
          "plyr",
          "munsell",
          "labeling",
          "scales",
          "ggplot2",
          "RColorBrewer",
          "NMF",
          "igraph",
          "arulesViz",
          "arules",
          "tseries",
          "fracdiff",
          #"RcppArmadillo",
          "nnet",
          "colorspace",
          "timeDate",
          #"forecast",
          "sandwich")

install.packages(pkgs, dependencies=TRUE, 
                 repos="http://cran.fhcrc.org",
                 lib="/u01/app/oracle/product/12.1.0.2/dbhome_1/R/library",
                 type="source")

# RcppArmadillo requires gcc 4.6 or greater and Big Data Lite contains gcc 4.4-7. Install an older version of RcppArmadillo  as workaround.
install.packages("http://cran.fhcrc.org/src/contrib/Archive/RcppArmadillo/RcppArmadillo_0.6.200.2.0.tar.gz",
                 repos=NULL,
                 lib="/u01/app/oracle/product/12.1.0.2/dbhome_1/R/library",
                 type="source")

# forecast depends on RcppArmadillo
install.packages("forecast", repos="http://cran.fhcrc.org")
