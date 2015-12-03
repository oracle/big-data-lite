CREATE EXTERNAL TABLE `MOVIE_UPDATES`(
  `op` string,
  `movie_id` int, 
  `title` string, 
  `year` int, 
  `budget` int, 
  `gross` int, 
  `plot_summary` string,
  `ts` timestamp)
  LOCATION '/user/odi/hive/moviedemo/movie';
