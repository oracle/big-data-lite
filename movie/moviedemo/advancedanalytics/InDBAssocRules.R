#####################################################
##
## Oracle R Connector for Hadoop
## Big Data Appliance Movie Demo
## ORE Association Rules Example
## 10/10/2012
##
#####################################################
# Load ORE
library(ORE)

# Connects to the Oracle Database 11.2.0.3 as moviedemo
ore.connect("moviedemo","orcl","bigdatalite", "welcome1",all=TRUE)

genMovieRules <- function() {
  # Load ORE library
  library(ORE)
  
  # Connects to the Oracle Database 11.2.0.3 as moviework  
  ore.connect("moviedemo","orcl","bigdatalite", "welcome1",all=TRUE)
  ore.sync("MOVIEDEMO","MOVIE_FACT")
  
  # Creates a Transaction Database that adds the Movie Titles
  # We are joining the Fact Table of Activities with the Movie Metadata
  # to capture the name of each Movie (instead of its ID)
  # we will remove duplications of Customer/Movie combinations
  # since a Customer can watch the same Movie more than once
  
  #Select the CUST_ID, MOVIE_ID and ACTIVITY_ID from the MOVIE_FACT 
  MF <- ore.get("MOVIE_FACT", schema = "MOVIEDEMO")[c("CUST_ID","MOVIE_ID","ACTIVITY_ID")]
  
  #Select the MOVIE_ID TITLE from the MOVIE
  MV <- MOVIE[,c("MOVIE_ID","TITLE")]
  
  transData <- merge(MF[MF$ACTIVITY_ID==2,], MV,
                    by="MOVIE_ID")[,c("CUST_ID","TITLE")]
  
  
  server.transData <- ore.pull(transData)
  server.transData <- data.frame(CUST_ID=as.factor(server.transData$CUST_ID),
                                TITLE=as.factor(server.transData$TITLE))
  idx_dup <- which(duplicated(data.frame(server.transData$CUST_ID,
                                         server.transData$TITLE))) 
  
  server.transData <- server.transData[-idx_dup,]
  
  # Loads the library that can calculate Association Rules
  # and is required to create the transactions file  
  library(arules)  
  
  
  # Converts the data frame into a special data type TRANSACTIONS
  # required for Association Analysis
  trans.movie <- as(split(server.transData[,"TITLE"],server.transData[,"CUST_ID"]),
                    "transactions")
  
  # Uses the traditional algorithm "A Priori" to generate association rules
  # Parameters defined for rules: past movie watched => movie to be offered
  # At least two movies are present in the Rule (minlen)
  # At least 3% of total customers watched the movie to be offered (support)
  # At least 25% of customers that watched the past movies, also watched 
  # the movie offered (confidence)
  # We expect to generate 4,341 rules with these settings
  assoc <- apriori(trans.movie, 
                   parameter=list(minlen=2,
                                  maxlen=2,
                                  support=0.05, 
                                  confidence=0.1))

}

# Execute the function in the database
assocRules <- ore.doEval(genMovieRules)

# Pull the results of the analysis locally for reporting
local.assocRules = assocRules <- ore.pull(assocRules)

# List the top 25 rules based on support 
inspect(sort(local.assocRules, by="support")[1:25])

