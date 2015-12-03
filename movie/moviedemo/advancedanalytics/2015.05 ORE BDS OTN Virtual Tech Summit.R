#####################################################
##
## ORE OTN Virtual Technology Days 2015
## Demo Script
##
## Oracle R Enterprise 1.5 with Oracle Big Data SQL
##
## (c)2015 Oracle - All Rights Reserved
##
#######################################################
# Getting started with Oracle R Enterprise using
# Oracle Database tables and also Big Data SQL external 
# tables with data residing in HIVE
#######################################################

# Load the ORE client packages
library(ORE)

# Connect to the Database Server on the VM (localhost)
# with user moviedemo. We could have added the option ,all=TRUE
# right after the password option to map metadata for 
# all tables, but we want to control which tables we are
# connecting to
ore.connect(user="moviedemo",sid="orcl",host="localhost",password="welcome1")
# Optimization required for Oracle R Enterprise on 12c
ore.exec('alter session set optimizer_dynamic_sampling=0')

# Sync 2 Tables and attach them for use with R:
# 1. a Table mapped via Oracle Big Data SQL, coming from HIVE
ore.sync(schema="MOVIEDEMO",table="MOVIEAPP_LOG_AVRO")

# 2. an Oracle Database Table of Customer Demographics
ore.sync(schema="MOVIEDEMO",table="CUSTOMER_V")

# Attaches the metadata of the tables to be used by R
# so we can call the tables by their names directly
ore.attach()

# Lists all available Tables for ORE
ore.ls()

# The Oracle Database Tables and Views in this case are 
# seen by R as an object of class "ore.frame", wheter it is
# an Oracle View like CUSTOMER_V, or an Oracle View mapping
# to an External Table MOVIEAPP_LOG_AVRO
class(CUSTOMER_V)
class(MOVIEAPP_LOG_AVRO)

########################################################
# Working with a Dataset stored in the Oracle Database #
########################################################

# Most R language commands that would work on a local "data.frame", 
# is going to work on an ore.frame. For example, we can use
# the command dim to check dataset size...
dim(CUSTOMER_V)

# ...or the command names to check column names
names(CUSTOMER_V)

# Basic Statistics transparently executed
summary(CUSTOMER_V[,c("GENDER","INCOME","AGE","EDUCATION")])

# Transparent crosstable table() function
cust.tab <- with(CUSTOMER_V, table(EDUCATION,MARITAL_STATUS))
cust.tab

# Histogram Plots with hist()
hist(CUSTOMER_V$INCOME,
     col="red",
     breaks=25,
     main="Customer Income Distribution",
     xlab="Income US$",
     ylab="Numer of Customers")

# Box Plots with the boxplot() function
boxplot(CUSTOMER_V$INCOME,xlab="Income US$",ylab="income",
        main="Distribution of Customer Income",
        col="red",notch=TRUE)

# Custom Splits for group-by data processing
cust.split <- with(CUSTOMER_V, split(INCOME,as.factor(EDUCATION)))

boxplot(cust.split, xlab="Education",ylab="Income",boxwex = 0.5,
        main="Distribution of Customer Income by Education",
        col="red",notch=TRUE)


# Using an alias to facilitate programming
# Creating a multi-faceted Chart
cust<- CUSTOMER_V 
row.names(cust) <- cust$CUST_ID
N <- nrow(cust)
s <- sample(1:N,N*0.2)
with(cust[s,],
     pairs(cbind(AGE, INCOME, YRS_CUSTOMER),
           panel=function(x,y) {
             points(x,y)
             abline(lm(y~x),lty="dashed",col="red",lwd=2)
             lines(lowess(x,y),col="blue",lwd=3)
           },
           diag.panel=function(x){
             par(new=TRUE)
             hist(x,main="",axes=FALSE, col="red")
           }
     ))

###################################################################
# Using the Transparency layer to process Oracle Big Data SQL Tables, 
# Defined in the Oracle Database as External HIVE Tables
###################################################################


# Now in this next command, because the table MOVIEAPP_LOG_AVRO
# is actually an external table mapped using the HIVE driver,
# the proper command is sent to HIVE to process and execute
# In this small VM a longer processing time is expected
dim(MOVIEAPP_LOG_AVRO)
names(MOVIEAPP_LOG_AVRO)

# Creates a new Temporary object that links to the Original
# HIVE data but filters out the Log transactions on anything 
# that was not about a Movie (customer login, logout, etc.)
mov <- MOVIEAPP_LOG_AVRO[MOVIEAPP_LOG_AVRO$MOVIEID != '0',]

# Checks the amount of records
dim(mov)

# Basic Statistics transparently executed
summary(mov[,c("RATING","ACTIVITY","RECOMMENDED","PRICE")])

# Transparent crosstable table() function on Acvitity 
# vs Recommended.
act.tab <- with(mov, table(ACTIVITY,RECOMMENDED))
act.tab

# Converts the numbers into column percentages
act.pct <- 100*round(prop.table(act.tab,2),4)
act.pct

# Plots the table on a Mosaic Plot. The plot() is going to 
# call mosaicplot() automatically because act.tab is
# of the R table class
plot(act.tab,col="red",main="Activity vs. Recommended")

# Generic statistics on the Data on HIVE, illustrated
# through the transparency of the function hist()
hist(mov$ACTIVITY,
     col="red",
     main="Dist. of Activity Codes",
     xlab="Activity Code",
     ylab="Numer of Transactions")

########################################################## #
#  Counting GENRE types by using R's JOIN (MERGE) function #
############################################################

# Capture Movie ID and Genre from the Transactions in HIVE
# Using the mov view
mov_sub <- mov[c("MOVIEID","GENREID")]

# Brings the Genre definitions from an Oracle Database Table
ore.sync(schema="MOVIEDEMO",table="GENRE")
ore.attach()
gen <- GENRE

# Join both datasets
combined  <- merge(mov_sub,gen, by.x="GENREID",by.y="GENRE_ID")

# Runs an R aggregate function to count the number of movies
# in each Genre, and list them
genre.cnts <- with (combined, aggregate(NAME, 
                                        by = list(NAME),
                                        FUN = length))
# Gives the Aggregated colum some labels
names(genre.cnts) <- c("Genre","Count")
# Prints out the Aggregate Result
genre.cnts

# Brings the table result locally for sorting and plotting
gcnts <- ore.pull(genre.cnts)
gcnts.sorted <- gcnts[order(gcnts$Count,decreasing=TRUE),]
barplot(height=gcnts.sorted$Count, names=gcnts.sorted$Genre,
        main="Barplot of Movie Counts by Genre",
        col="red",cex.names=0.7,las=2)


# Brings the Movie Information table from an Oracle Database Table
ore.sync(schema="MOVIEDEMO",table="MOVIE")
ore.attach()
mov.info <- MOVIE

# Joins the previous Table with the Movie Information Table
multi.join <- merge(mov.info,combined[,2:3],by.x="MOVIE_ID",by.y="MOVIEID")
# Checks the size of the dataset
dim(multi.join)
# Creates a split on Genre
multi.split <- split(multi.join$GROSS, multi.join$NAME)

# Generates a Box-Plot on Gross Earnings by Genre
boxplot(multi.split, ylab="Gross Earnings",xlab="Genre",col="green",
        main="Distribution of Movie Popularity by Genre",
        cex.axis=0.6, boxwex=.5, las=2)





#####################################################
## Associations with in-Database model and R graphics
#####################################################

ore.connect(user="odmuser",sid="orcl",host="localhost",password="welcome1")

# Keeps only the Activity=2 (customer watched the movie)
# and only columns of interest
mov.act2 <- mov[mov$ACTIVITY==2,c("MOVIEID","CUSTID")]

head(mov.act2)
# Join this subset with the Movie Info Table from
# the Oracle Database
mov.aa <- merge(mov.act2, mov.info,
                by.x="MOVIEID",
                by.y="MOVIE_ID")[,c("CUSTID","TITLE")]

# Reviews the Transactional Data
head(mov.aa)

# Execute the in-Database Associations Model

apriori.model <- ore.odmAssocRules(~., mov.aa,
                                   case.id.column = "CUSTID",
                                   item.id.column = "TITLE",
                                   min.support = 0.005,
                                   min.confidence = 0.005,
                                   max.rule.length = 2)

apriori.model

# Loads the library that can manage Association Rules objects
# Created by ORE for Inspection and Graphing
library(arules)
library(arulesViz)

# Generate itemsets and rules of the model
itemsets <- itemsets(apriori.model)
rules <- rules(apriori.model)

# Subsetting Example - Itemsets that contain "Risky Business"
sub.itemsets <- subset(itemsets, min.support=0.01, items=list("Risky Business"))
sub.itemsets

# Subsetting Example - Rule that contains "Gladiator" or "Risky Business"
sub.rules <- subset(rules, min.confidence=0.01,
                    lhs=list("Gladiator", "Risky Business"))
sub.rules

# Convert the rules to the rules object in arules package
# and inspects the top 25 Rules by lift
rules.arules <- ore.pull(rules)
assoc.top25lift <- sort(rules.arules,by="lift")[1:25]

# Inspect Top Associations by Lift on a Text output
inspect(assoc.top25lift)

# Convert itemsets to the itemsets object in arules package
# and inspects the top 25 itemsets by support
# (most frequent movies watched)
itemsets.arules <- ore.pull(itemsets)
item.top25supp <- sort(itemsets.arules,by="support")[1:25]

# Listing of the Top Items by Support (most viewed Movies)
inspect(item.top25supp)

# Plots the top 25 rules into a Graph
plot(assoc.top25lift,
     method = "graph",
     control=list(type="items",
                  arrowSize=0.6,
                  cex=0.8,
                  main="Top 25 Highest Lift Movie Associations (computed from HIVE trans.)",
                  alpha=0.8)
)

# Plots the top 25 rules into a Graph
plot(item.top25supp,
     method = "graph",
     control=list(type="items",
                  arrowSize=0.6,
                  cex=0.8,
                  main="Top 25 Most frequent Movies (computed from HIVE Transactions)",
                  alpha=0.8)
)




# Grouped Rules Plot
plot(sort(rules.arules,by="support")[1:30],
     method="grouped",control=list(k=10))
