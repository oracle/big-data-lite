# New ORE 1.4 interface to In-Database Associations can process
# Large problems using the in-Database Apriori Associations rules
# Engine instead of using the open-source one
# Generated rules are then brought to R for inspection and plotting
#
# Loads the Oracle R Enterprise Libraries and connects
# to the Oracle Database, and loads the resulting Model into memory

library(ORE)
ore.connect("moviedemo","orcl","bigdatalite.localdomain","welcome1")

# Step 1 - using Transparency Layer functions to join large tables
ore.sync("MOVIEDEMO","MOVIE_FACT")
ore.sync("MOVIEDEMO","MOVIE")
ore.attach()
MF <- MOVIE_FACT
MF <- MF[c("CUST_ID","MOVIE_ID","ACTIVITY_ID")]
MV <- MOVIE[,c("MOVIE_ID","TITLE")]

# Checking the contents of both tables
head(MF)
head(MV)

# Step 2 - Joining the Tables into an Activity Table
MOV_AA <- merge(MF[MF$ACTIVITY_ID==2,], MV,
                   by="MOVIE_ID")[,c("CUST_ID","TITLE")]
head(MOV_AA)

# Step 3 - Execute the in-Database Associations Model
# We will limit it to 10,000 transactions for this Demo, but it can run on the full
#
ASSOC <- head(MOV_AA,10000)
ar.mod1 <- ore.odmAssocRules(~., ASSOC,
                             case.id.column = "CUST_ID",
                             item.id.column = "TITLE",
                             min.support = 0.005,
                             min.confidence = 0.005,
                             max.rule.length = 2)
ar.mod1
# Loads the library that can manage Association Rules objects
# Created by ORE for Inspection and Graphing
library(arules)
library(arulesViz)

# Generate itemsets and rules of the model
itemsets <- itemsets(ar.mod1)
rules <- rules(ar.mod1)

# subsetting Examples
sub.itemsets <- subset(itemsets, min.support=0.05, items=list("Gladiator"))
sub.itemsets

sub.rules <- subset(rules, min.confidence=0.05,
                    lhs=list("Gladiator", "Inception"))
sub.rules

# Convert the rules to the rules object in arules package
# and inspects the top 25 Rules by lift
rules.arules <- ore.pull(rules)
assoc.top25lift <- sort(rules.arules,by="lift")[1:25]

# Plots the top 25 rules into a Graph
plot(assoc.top25lift,
     method = "graph",
     control=list(type="items",
                  arrowSize=0.6,
                  cex=0.8,
                  main="Top 25 Movie Associations (computed from 10k trans.)",
                  alpha=0.8)
)

# Inspect Top Associations by Lift on a Text output
inspect(assoc.top25lift)

# Convert itemsets to the itemsets object in arules package
# and inspects the top 25 itemsets by support
# (most frequent movies watched)
itemsets.arules <- ore.pull(itemsets)
item.top25supp <- sort(itemsets.arules,by="support")[1:25]

# Listing of the Top Items by Support (most viewed Movies)
inspect(item.top25supp)

# Grouped Rules Plot
plot(sort(rules.arules,by="support")[1:20],
     method="grouped",control=list(k=10))
