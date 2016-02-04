README:
------------------------------
SparkBarcode Blog/Demo

By David Bayard
January 21, 2016

Find the blog post here: xxxxxxxxxx
You can also find a PDF of the blog in the BLOG_spark_barcode.pdf file.
Find the github here: https://github.com/oracle/BigDataLite/Blogs/SparkBarcode

This was originally built for Big Data Lite VM 4.3.

------------------------------

Refer to Blog_Html/SparkBarcode.html or the blog URL for instructions on how to run.

------------------------------

Warning: The /home/oracle/src directory gets deleted and re-written everytime you run "Refresh Samples".  Therefore, you will need to re-run /home/oracle/src/Blogs/SparkBarcode/setup.sh after running Refresh Samples.
Also, if you want to modify the source code, we suggest you copy the SparkBarcode directory to a location outside of /home/oracle/src so that you don't lose your changes later. You can use "copy_dirs.sh" to do so.

------------------------------

Issue: In Big Data Lite VM 4.3.0, Spark History webUI and YARN job history webUI are missing some configuration entries.  If hit any errors trying to access the Spark History webUI or viewing the History or Logs of a completed Spark job, be sure to run "x_setup_root_430.sh" as the root user.
Users of Big Data Lite VM 4.3.1 and higher should have the fixes already built-in and do not need to run x_setup_root_430.sh

------------------------------
