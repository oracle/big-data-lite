#####################################################
##
## Oracle R Connector for Hadoop
## Big Data Appliance Movie Demo
## SQL_itemitemrec_realtimequery.sql
## 7/25/2012
##
#####################################################

sqlplus moviedemo


select * from (select s.mid2 resmovie,  (c.rating * s.cor) score
               from cust_rating c, movie_similarity s
               where c.movieid = s.mid1 and s.mid2 != c.movieid and c.userid = 2 order by score desc)
where rownum <= 20;


