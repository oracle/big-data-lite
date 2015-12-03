# Connects to the Oracle Database 12c as moviedemo user
ore.connect("moviedemo","orcl","bigdatalite", "oracle",all=TRUE)

# See objects in the MOVIEDEMO schema
ore.ls("MOVIEDEMO")

#Select the MOVIE_ID and TITLE from the MOVIE table
themovies <-ore.get("MOVIE", schema = "MOVIEDEMO")[,c("MOVIE_ID","TITLE")]
head(themovies)

# Uses the traditional algorithm "A Priori" to generate association rules
# Parameters defined for rules: past movie watched => movie to be offered
# At least two movies are present in the Rule (minlen)
# At least 3% of total customers watched the movie to be offered (support)
# At least 25% of customers that watched the past movies, also watched 
# the movie offered (confidence)
# We expect to generate 4,341 rules with these settings
assocRules <- apriori(trans.movie, 
                      parameter=list(minlen=2,
                                     maxlen=2,
                                     support=0.05, 
                                     confidence=0.1))

# List the top 25 rules based on support 
inspect(sort(assocRules,by="support")[1:25])

# Plots the same Chart in an interactive Interface depending on the
# type of display and OS
plot(sort(assocRules,by="support")[1:50], method="graph", interactive=TRUE, control=list(type="items"))

