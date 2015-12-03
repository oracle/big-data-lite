# Load ORE
library(ORE)
# Loads the library that can calculate Association Rules
# and is required to create the transactions file
library(arules)

# Connects to the Oracle Database 12c as moviedemo
ore.connect("moviedemo","orcl","bigdatalite", "welcome1",all=TRUE)

# Synchronizes the Table from the SCHEMA MOVIEDEMO, our Source
ore.sync("MOVIEDEMO","MOVIE_FACT")

# List the DB Table available in the DB schema
ore.ls("MOVIEDEMO")

# Creates a Transaction Database that adds the Movie Titles
# We are joining the Fact Table of Activities with the Movie Metadata
# to capture the name of each Movie (instead of its ID)
# we will remove duplications of Customer/Movie combinations
# since a Customer can watch the same Movie more than once

#Select the CUST_ID, MOVIE_ID and ACTIVITY_ID from the MOVIE_FACT 
MF <- ore.get("MOVIE_FACT", schema = "MOVIEDEMO")[c("CUST_ID","MOVIE_ID","ACTIVITY_ID")]

#Select the MOVIE_ID and TITLE from the MOVIE table
MV <-MOVIE[,c("MOVIE_ID","TITLE")]
head(MV)

#We can investigate that the file is an ORE.FRAME and not a DATA.FRAME
# class(MV)

#Now we join both DBs by MOVIE_ID, filtering by movies watched (ACTIVITY_ID=2)
#We only keep CUST_ID and TITLE

assocData <- merge(MF[MF$ACTIVITY_ID==2,], MV,
                  by="MOVIE_ID")[,c("CUST_ID","TITLE")]

# head(assocData)

# Brings the file for Local processing
local.assocData <- ore.pull(assocData)

# Converts both CUST_ID and TITLE into Factors as
# required by the Association Analysis
local.assocData <- data.frame(CUST_ID=as.factor(local.assocData$CUST_ID),
                              TITLE=as.factor(local.assocData$TITLE))

# Identify Duplicates
idx_dup <- which(duplicated(data.frame(local.assocData$CUST_ID,
                                       local.assocData$TITLE))) 

# Remove duplicates
local.assocData <- local.assocData[-idx_dup,]


# Converts the data frame into a special data type TRANSACTIONS
# required for Association Analysis
trans.movie <- as(split(local.assocData[,"TITLE"],
                        local.assocData[,"CUST_ID"]),
                        "transactions")

#Verify Transactions in the frame
# summary(trans.movie)
