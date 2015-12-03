CREATE  TABLE `movie_rating`(
  `movie_id` int, 
  `title` string, 
  `year` int, 
  `avg_rating` bigint)
ROW FORMAT DELIMITED 
  FIELDS TERMINATED BY '\;' 

