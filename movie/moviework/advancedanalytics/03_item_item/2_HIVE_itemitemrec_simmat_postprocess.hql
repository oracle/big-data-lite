#####################################################
##
## Oracle R Connector for Hadoop
## Big Data Appliance Movie Demo
## HIVE_itemitemrec_simmat_postprocess.R
## 7/25/2012
##
#####################################################

##############################################################
# Post-processing to remove the leading '\t' from the 
# similarity matrix output above using HIVE
##############################################################

# run the following in hive shell:

CREATE EXTERNAL TABLE tmp_table (mid1 string, mid2 string, cor string)
ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.RegexSerDe'
WITH SERDEPROPERTIES (
  "input.regex" = "\t([^ ]*),([^ ]*),([^ ]*)",
  "output.format.string" = "%1$s %2$s %3$s"
)
STORED AS TEXTFILE
LOCATION '<hdfs location pointed by movieMovieSimilarity call above>';

CREATE TABLE movie_similarity (mid1 int, mid2 int, cor double)
row format delimited fields terminated by ','
stored as textfile
location '/user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix';

insert overwrite table movie_similarity select * from tmp_table;

# You will see hdfs loc '/user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix' 
# has a single file called 000000_0 with the correct format of the data

# Rename this file to part-00000 so that ORCH can understand it.
# run the following in regular shell:
hadoop fs -mv /user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix/000000_0 \
              /user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix/part-00000

# Modify the metadata of similarity matrix output and store it in 
# /user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix 
# so that ORCH can use proper column names for hdfs.pull. Run the following:


hadoop fs -get /tmp/mdata <hdfs location pointed by movieMovieSimilarity call above>/__ORCHMETA__

# open /tmp/mdata and replace orch.names      "val1","val2","val3" with:
# orch.names      "mid1","mid2","cor"

# save the metadata file in 
# /user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix using:

hadoop fs -put /tmp/mdata/user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix/__ORCHMETA__

# DONE! the data located in 
# /user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix 
# is ready to be used by hdfs.pull in ORCH.


## Run the following in ORCH to load to Oracle Database:
#R
#library(ORCH)
## establish DB connection for sqoop
#orch.connect("localhost",schema,"orcl",password,secure=F)
## attach similarity matrix output above to ORCH
#res <- hdfs.id("/user/oracle/moviework/advancedanalytics/item_item/item_item_similarity_matrix")
##  move similairty matrix from HDFS to DB
#dbtab <- hdfs.pull(res, db.name=<similarity.matrix.table.name>)


