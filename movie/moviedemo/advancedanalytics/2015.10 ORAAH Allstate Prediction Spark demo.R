#####################################################
##
## Demo Script
##
## Oracle R Advanced Analytics for Hadoop 2.5.1
##
## Regression and Classification with Spark-based algorithms
##
## Using AllState Claims Prediction Challenge Data from
## https://www.kaggle.com/c/ClaimPredictionChallenge/data
##
## (c)2015 Oracle - All Rights Reserved
##
#######################################################

# Load the libraries which are going to check for
# connectivity to the Big Data Environment
library(ORCH)

# First we need to make sure no other connection with Spark exists
spark.disconnect()


# Connecting to Spark using the YARN Client - After the connection is done,
# the ORCH Spark Job can be seen at http://localhost:8088/cluster
# Clicking the "Application Master" link under the column
# called "Tracking UI" will allow us to see the ORCH Spark Jobs UI
# The default memory is 2GB, and the minimum is 512Mb
spark.connect("yarn-client", memory="2G", dfs.namenode="bigdatalite.localdomain")

# Standalone Mode option. For this one to work, one has to start a terminal
# window, move to the folder /usr/lib/spark and issue the command
# sudo sbin/start-all.sh. The root password will be required for the Service to
# start.  You can review the Spark master locally at: http://localhost:8080/
# Make sure the Service is running and that one Worker is running
# spark.connect("spark://bigdatalite.localdomain:7077", memory="2G", dfs.namenode="bigdatalite.localdomain")

# Check the current HDFS folder
hdfs.pwd()

# Lists the files in the current folder
hdfs.ls()

# Attaches the AllState Claims Dataset from the Kaggle Competition
# containing 13.4 mi records
# You can download the training data from:
# https://www.kaggle.com/c/ClaimPredictionChallenge/data
#
# Using force=TRUE will overwrite all Metadata extracted or written before
all_ins <- hdfs.attach("/user/oracle/ALLSTATE_INSURANCE", force=TRUE)

# For all other times, one can use just the attach without force
# all_ins <- hdfs.attach("/user/oracle/ALLSTATE_INSURANCE")

# Reviews the types and names of the Columns in the Dataset
# We make use of the function hdfs.describe that contains much more
# information about the file and only print the variables and types
nam <- hdfs.describe(all_ins)
nam[4:5,2]

# We have 2 problems to solve.
# 1) Variable names are not present
# 2) the first column type is incorret - it is Claim Amount, so it can have any 
# numeric values, not only integers

# If this is the first time loading this data, then the Metadata very likely does
# not contain the Variable names. This is common because files in HDFS tend to be
# split for performance and each piece cannot contain headers (given they are appended
# automatically).

# The command hdfs.meta() allows to change the metadata, including add column names
hdfs.meta(all_ins,names=c("claim_amount","var2","var4","var8","cat2",
                          "cat5","cat6","cat7","nvcat","veh_age"))

# This next command fixes the issue with Claim Amount, giving it the type numeric
hdfs.meta(all_ins,types=c("numeric", "numeric", "numeric", "numeric", 
                          "character", "character", "character", "character", 
                          "character", "integer"))

# We can check again the description
nam <- hdfs.describe(all_ins)
nam[4:5,2]

# We will now chwck the size of the Dataset.
# This will require a Map-Reduce process, and we will have to accept the request
hdfs.dim(all_ins)


# Checks the first 5 records of the dataset
hdfs.head(all_ins,5)



# Establish a Regression formula for use with the Linear Neural Model
# We are going to try to estimate the Claim Total based on 
# all other available columns, We are also going to specify 
# Vehicle Age as Factor to avoid a linear relationship per
# year

form_reg_allstate <- claim_amount ~ var2 + var4 + var8 + cat2 +
  cat5 + cat6 + cat7 + nvcat + veh_age

# NEURAL NETWORKS in 3 STEPS
# STEP 1 - Compute the Levels of all variables of the X Matrix (all predictors)
# It will use Spark for computing X Levels if a connection is available, or
# Map-Reduce if Spark is not available
system.time({xlev <- orch.getXlevels(form_reg_allstate,all_ins)
xlev <- lapply(xlev, sort)})


# STEP 2 - Compute the Model Matrix and automatically cache it as a Spark RDD
system.time(all_ins_Mat <- orch.prepare.model.matrix(form_reg_allstate,all_ins,
                                                     xlev=xlev))

# STEP 3 - Compute the Final Model directly off the cached RDD Model Matrix
system.time(mod_neu <- orch.neural(form_reg_allstate,all_ins_Mat,
                                   xlev=xlev,trace=TRUE))
# Reviews the Model
mod_neu 
# Checks how many Objective evaluations the Model did, and how many updates
mod_neu$nObjEvaluations 
mod_neu$nUpdates
# Checks the number of coefficients computed by the model
mod_neu$nWeights 
# Checks the Objective Value of the Solution
mod_neu$objValue


# STEP 3B - If we wanted to build a more complex Non-linear Model, we can use
# the same algorithm to build a Multi-Layer Perceptron Neural Network, which 
# requires just a few additional arguments like hiddenSizes for the number of
# neurons on each Layer, or activations for the activation functions at each Layer
# It is expected that the more complex the model, the more time it will take to solve
system.time(mod_neu_mlp <- orch.neural(form_reg_allstate,all_ins_Mat,
                                       hiddenSizes = c(10,5),
                                       activations    = c("tanh", "bSigmoid", "linear"),
                                       xlev=xlev,trace=TRUE))
# Reviews the Model
mod_neu_mlp 
# Checks how many Objective evaluations the Model did, and how many updates
mod_neu_mlp$nObjEvaluations 
mod_neu_mlp$nUpdates
# Checks the number of coefficients computed by the model
mod_neu_mlp$nWeights 
# Checks the Objective Value of the Solution
mod_neu_mlp$objValue



####  SECOND PART - WORKING WITH HIVE TO CREATE A NEW COLUMN ######
#
# Given that the data has a variable indicating the claim value, we might want
# to create a new binary value that is 1 when the customer has any Claims, or 0
# if they don't.  For this, we are going to use HIVE's engine to help us achieve that

# First make sure we don't have any previous connections 
ore.disconnect()
# Connect to HIVE using the database default
ore.hiveOptions(dbname='default')
ore.connect(type='HIVE')

# Pushes the table to HIVE to a new table called allstate
# and creates a pointer to it called all_ins_hive
# We will first drop the table just in case it already exists
ore.drop(table="allstate")
all_ins_hive <- hdfs.toHive(all_ins,table = 'ALLSTATE')

# Checks the first records of the HIVE table
head(all_ins_hive,10)

# Check size of the dataset
dim(all_ins_hive)

# Checks names of columns
names(all_ins_hive)

# Adds a new Binary column that is a function of claim_amount
all_ins_hive$any_claim <- 1*(all_ins_hive$claim_amount >0)  

# Checks that the new column exists
names(all_ins_hive)

# Checks how many records have Claims (0's and 1's) 
table(all_ins_hive$any_claim)

# Show a sample of the first records that have claims
head(all_ins_hive[all_ins_hive$claim_amount >0,],10)

# Create a final HIVE table with the new Binary column
# Let's drop the table if it already exists
ore.drop("allstate_bin")
ore.create(all_ins_hive,table="allstate_bin")

# Sync the newly created table, and attach it for use
ore.sync(table="allstate_bin")
ore.attach()

# List the available Tables we have synchronized from HIVE
ore.ls()

# FOr the final step, we will bring the data back to HDFS, or just update
# the Metadata directly on the HIVE data
all_bin_hdfs <- hdfs.fromHive(table="allstate_bin")


# Reviews the types and names of the Columns in the Dataset
# We should expect that with the original HIVE metadata, our dfs.id will
# already have column names correctly assigned
nam <- hdfs.describe(all_bin_hdfs)
nam[4:5,2]

# Now we establish the formula needed for the Classification Model, in this
# case the Spark-based Logistic Regression Model, using orch.glm2
form_cla_allstate <- any_claim ~ var2 + var4 + var8 + cat2 +
  cat5 + cat6 + cat7 + nvcat + veh_age

# Execute a Logistic Regression Model using the formula provided and the newly
# created dfs.id all_bin_hdfs
system.time(mod_glm <- orch.glm2(form_cla_allstate,all_bin_hdfs))

# Review the results of the Model
summary(mod_glm)
mod_glm$formula 
mod_glm$solutionStatus
mod_glm$nIterations
mod_glm$deviance
mod_glm


# We can now test the Prediction ability of the Model on the original Data
all_pred <- predict(mod_glm,newdata=all_bin_hdfs,
                    outpath = "/user/oracle/all_pred",
                    overwrite = TRUE)
hdfs.()

spark.disconnect()


