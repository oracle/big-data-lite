#####################################################
##
## Oracle R Connector for Hadoop
## Big Data Appliance Movie Demo
## ORE Association Rules Example
## 10/10/2012
##
#####################################################

genMovieRules <- function() {
  ore.connect("moviework","orcl","bigdatalite", "oracle",all=TRUE)
  ore.sync("MOVIEDEMO","MOVIE_FACT")
  MF <- ore.get("MOVIE_FACT", schema = "MOVIEDEMO")[c("CUST_ID","MOVIE_ID","ACTIVITY_ID")]
  MV <- MOVIE[,c("MOVIE_ID","TITLE")]
  
  transData <- merge(MF[MF$ACTIVITY_ID==2,], MV,
                    by="MOVIE_ID")[,c("CUST_ID","TITLE")]
  
  transData <- ore.pull(transData)
  transData <- data.frame(CUST_ID=as.factor(transData$CUST_ID),
                                TITLE=as.factor(transData$TITLE))
  idx_dup <- which(duplicated(data.frame(transData$CUST_ID,
                                         transData$TITLE))) 
  
  transData <- transData[-idx_dup,]
  
  library(arules)  
  trans.movie <- as(split(transData[,"TITLE"],transData[,"CUST_ID"]),
                    "transactions")
  assoc <- apriori(trans.movie, 
                   parameter=list(minlen=2,
                                  maxlen=2,
                                  support=0.05, 
                                  confidence=0.1))
  assoc
}
