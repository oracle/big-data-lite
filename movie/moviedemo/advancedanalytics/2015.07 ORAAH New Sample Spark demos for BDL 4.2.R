#####################################################
##
## ORE OTN Virtual Technology Days 2015
## Demo Script
##
## Oracle R Advanced Analytics for Hadoop 2.5.1
##
## (c)2015 Oracle - All Rights Reserved
##
#######################################################
# Getting started with
# Oracle R Advanced Analytics for Hadoop
#######################################################
library(ORCH)

# Testing by using the YARN Client - After the connection is done,
# the ORCH Spark Job can be seen at http://localhost:8088/cluster
# Clicking on the "Application Master" link under the column
# called "Tracking UI" will allow us to see the ORCH Spark Jobs UI
# The default memory is 2GB, and the minimum is 512Mb
spark.connect("yarn-client", memory="512m", dfs.namenode="bigdatalite.localdomain")

# Example using a Basic configuration of Neural Networks
system.time(example("orch.prepare.model.matrix"))

# Example using a Basic Configuration of Spark-based GLM
system.time(example("orch.glm2"))

# Disconnect from Spark
spark.disconnect()


# Connecting to Spark using the SPARK Standalone Mode,
# directly to the Spark Master, with UI at http://localhost:8080/
# the ORCH Jobs will show up there directly
# The default memory is 2GB, and the minimum is 512Mb
spark.connect("spark://bigdatalite.localdomain:7077",
              memory="512m",
              dfs.namenode="bigdatalite.localdomain")

# Example using a Basic configuration of Neural Networks
system.time(example("orch.prepare.model.matrix"))

# Example using a Basic Configuration of Spark-based GLM
system.time(example("orch.glm2"))

# Disconnect from Spark
spark.disconnect()

# Sample Map-Reduce Execution

demo("orch_kmeans",package="ORCH")
demo("mapred_modelbuild",package="ORCH")
demo("orch_sample")
