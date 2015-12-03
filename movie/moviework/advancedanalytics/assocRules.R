#####################################################
##
## Oracle R Enterprise 1.3
## ORE Association Rules Example
## (c)2013 Oracle - All Rights Reserved
##
#####################################################

genMovieRules <- function() {
  ore.sync(table= c("MOVIE_FACT", "MOVIE"))
  MV <- ore.get("MOVIE")[,c("MOVIE_ID","TITLE")]
  MF <- ore.get("MOVIE_FACT")[c("CUST_ID","MOVIE_ID","ACTIVITY_ID")]
 
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
