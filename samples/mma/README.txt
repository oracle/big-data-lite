Running the multimedia analytics example to recognize faces
-----------------------------------------------------------

This example uses sample data shipped with the product.

1. Training

Use face images in $MMA_HOME/example/facetrain/faces/bigdata 
and the labels in dirmap.txt to create a training model.

$ cd /home/oracle/src/samples/mma/facetrain
$ ./trainface_bdl.sh


2. Detect and identify faces

Use the training model from Step 1 to identify faces in the sample video
(available at $MMA_HOME/example/video/bigdata.mp4).

The video processing output will be images since the value 'image' was specified 
for the property oracle.ord.hadoop.outputtypes in 
config/oracle_multimedia_analysis_framework_bdl.xml.  See Step 3 to display 
the image output.

Change the value to 'json' to get JSON output.


$ cd /home/oracle/src/samples/mma/analytics
$ ./runjob_bdl.sh

3. Display image output.

Use the simple demo viewer available with the product to display the 
output images (video frames).

$ cd /home/oracle/src/samples/mma/analytics
$ ./playimage_bdl.sh

