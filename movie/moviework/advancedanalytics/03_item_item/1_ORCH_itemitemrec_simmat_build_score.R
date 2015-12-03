#####################################################
##
## Oracle R Connector for Hadoop
## Big Data Appliance Movie Demo
##
## ORCH_itemitemrec_simmat_build_score.R - 7/25/2012
## ORCH 2.0 related changes - 1/28/2013
## ORCH 2.1 related changes - 5/2/2013
##
#####################################################

#
# Build Similarity Matrix
#
movieMovieSimilarity <- 
      function (interest.dfs.id,   # movies of interest input data
            schema="moviedemo",   # schema where similarity matrix should be placed (optional) 
            password="welcome1", # password for schema (optional) 
            similarity.matrix.table.name="MOVIE_SIMILARITY" # table where to store matrix (optional)
)
{
  if(missing(interest.dfs.id))
    stop("input data not specified")
  
  # step 1: compute the item-item similarity matrix from the input data:
  #
  # Data is in the form (UserID, MovieID, Rating). Note this code assumes 
  # no key. The 3 value columns in order are  "UserID", "MovieID", "Rating"
  
  cat("\nstarting phase-1 MR job of item-item similarity calculation....\n")
  dfs.step1out <- hadoop.exec(
    dfs.id  = interest.dfs.id , 
    mapper  = function(k, vals) orch.keyvals(vals$UserID, vals[,-1]),
    reducer = function(k, vals)
    {
      movie <- vals$MovieID
      rating <- vals$Rating
      output <- lapply(seq_len(length(movie)),
                       function(i)
                       {
                         key <- movie[i]
                         keep <- which(movie > key)
                         if (length(keep) == 0) {
                           NULL
                         }
                         else {
                           val  <- data.frame(MovieKey = key,
                                              MovieID  = movie[keep],
                                              Rating1  = rating[movie == key],
                                              Rating2  = rating[keep])
                         }
                       })

      keep <- !unlist(lapply(output, is.null), use.names = FALSE)
      df <- as.data.frame(do.call(rbind, output[keep]))
      orch.keyvals(df[,1], df[,-1,drop=FALSE])
    },
    config = new("mapred.config",
                 map.output = data.frame(key=0.0, MovieID=0.0, Rating=0.0),
                 reduce.output = data.frame(key=0.0, MovieID=0.0, Rating1=0.0, Rating2=0.0),
                 output.pristine = TRUE,
                 output.key.sep=",",
                 reduce.tasks = 45,
                 job.name = 'Movie-Movie Similarity Phase 1')
  )

  cat("\nphase-1 MR job computation completed\n")
  cat("\nstarting phase-2 MR job of item-item similarity calculation....\n")

  res <- hadoop.exec(
    dfs.id  = dfs.step1out,
    mapper  = function(k, vals) orch.keyvals(k, vals),
    reducer = function(k, vals)
    {
       cors <- unlist(lapply(split(vals[,c("Rating1", "Rating2")],
                                   vals[,"MovieID"]),
                             function(x) cor(x[,1L], x[,2L])))
       val  <- data.frame(mid1 = as.integer(k),
                          mid2 = as.integer(names(cors)),
                          cor = unname(cors))

       orch.keyvals(NULL,na.omit(val))
    },
    config = new("mapred.config",
                 reduce.output = data.frame(key=NA, mid1=0.0, mid2=0.0, cor=0.0),
                 output.key.sep=",",
                 reduce.tasks = 45,
                 job.name = 'Movie-Movie Similarity Phase 2')

  )

  cat("\nphase-2 MR job computation completed\n")

  cat("\nloading the similarity matrix into DB...\n")

  # DB Connection to SQOOP
  orch.connect(host="localhost", user=schema, password=password, 
               sid="orcl", secure=F)
        
   # Load the database 
  hdfs.pull(dfs.id=res, db.name=similarity.matrix.table.name, overwrite=TRUE)
  
  cat("\nsimilarity matrix loaded into DB table ", 
      similarity.matrix.table.name, "\n")

}


#sample user rating data: 
#newly.rated.movies <- data.frame(USERID=c(1,1,1,1,1,1,2,2,2,2,2), 
#                          MOVIEID=c(14,153,167,179,25,100,157,28,113,116,74), 
#                          RATING=c(3,4,2,3,1,3,5,4,4,2,1))
#
# scoring through R using similarity matrix from database
#
movieMovieRecommender <- function (
  newly.rated.movies,  # data.frame of newly rated movies
  schema = "moviedemo",   # schema where similarity matrix should be placed (optional) 
  password = "welcome1", # password for schema (optional) 
  similarity.matrix.table.name = "MOVIE_SIMILARITY",
  # table name where similarity matrix is to be stored (optional)
  userid = 1,         # userid for which recommendation is needed 
  topN = 20)          # number of movies to recommend
{
  
  if(missing(newly.rated.movies))
  {
    stop("newly rated movie data cannot be empty")
  }
  
  # step 1: use ore to push the movie rating table into DB 
  suppressWarnings(ore.connect(schema, "orcl", "localhost", password, all=TRUE))
  user_rating <- ore.push(newly.rated.movies)
  
  #step 2: prepare the topN join query
  # NOTE: the column names are hardcoded as follows:
  # MOVIE_SIMILARITY: mid1(movieid1), mid2(movieid2), cor(corr coefficient)
  # CUST_RATING: USERID (customer user id),
  #              MOVIEID (movie id rated by the user), 
  #              RATING (user rating)
  #
  # Required query is 
  #
  #    select * 
  #    from (select s.mid2 resmovie,  (c.rating * s.cor) score
  #          from user_rating c, similarity.matrix.table.name s
  #          where c.movieid = s.mid1 and c.userid = :userid
  #          order by score desc
  #         )
  #    where rownum <= :topN
  #
  # Query is implemented using ORE public APIs
  #

  simtab <- eval(parse(text=similarity.matrix.table.name))

  x1 <- user_rating[user_rating$USERID==userid,]
  x2 <- merge(x1, simtab, by.x="MOVIEID", by.y="MID1")
  x3 <- x2$RATING * x2$COR
  x4 <- cbind(x2$MID2, x3)
  names(x4) = c("RESMOVIE", "SCORE")
  x5 <- ore.sort(x4, by="SCORE", reverse=TRUE)
  rset <- suppressWarnings(head(x5, topN))

  rset
}
