DROP TABLE media_demo_movielog;
CREATE EXTERNAL TABLE media_demo_movielog(
custid int,
movieid int,
genreid int,
time timestamp,
recommended string,
activity int,
rating int,
price decimal,
position string,
movie_avg_rating int,
budget int,
gross int,
movie_year int,
title string,
plot_summary string,
network string,
exposure string
) row format delimited fields terminated by '\t' stored as textfile
 LOCATION '/user/oracle/mediademo/media_movielog'
