-- Create table over log data

CREATE TABLE movielog
  (click VARCHAR2(4000))
  ORGANIZATION EXTERNAL
  (TYPE ORACLE_HDFS
   DEFAULT DIRECTORY DEFAULT_DIR
   LOCATION ('/user/oracle/moviework/applog_json/*')
  ) 
  REJECT LIMIT UNLIMITED;

-- Query the movielog table
select * from movielog where rownum < 20;

-- Query the table using new JSON syntax
SELECT m.click.custid, m.click.movieid, m.click.genreid, m.click.time
FROM movielog m
WHERE rownum < 20;

-- How do top rated movies on MoviePlex compare to top grossing movies?
SELECT m.title, m.year, m.gross, round(avg(f.click.rating), 1) as avg_rating
FROM movielog f, movie m
WHERE f.click.movieid = m.movie_id
GROUP BY m.title, m.year, m.gross
ORDER BY m.gross desc
FETCH FIRST 10 ROWS ONLY; 


-- Create MOVIEAPP_LOG_AVRO table over data in Hive (use SQLDev Hive Connection if available)
CREATE TABLE MOVIEAPP_LOG_AVRO 
(
  CUSTID NUMBER 
, MOVIEID NUMBER 
, ACTIVITY NUMBER 
, GENREID NUMBER 
, RECOMMENDED VARCHAR2(4) 
, TIME VARCHAR2(20) 
, RATING NUMBER 
, PRICE NUMBER 
, POSITION NUMBER 
) 
ORGANIZATION EXTERNAL 
( 
  TYPE ORACLE_HIVE 
  DEFAULT DIRECTORY DEFAULT_DIR 
  ACCESS PARAMETERS 
  ( 
    com.oracle.bigdata.tablename=moviework.movieapp_log_avro 
  ) 
) 
REJECT LIMIT UNLIMITED;


-- Query the table from hive - find clicks where movies were highly rated movies.
SELECT * FROM movieapp_log_avro WHERE rating > 4;

-- We also have recommendation data in Oracle NoSQL Database
CREATE TABLE RECOMMENDATION 
(
  CUSTID NUMBER 
, SNO NUMBER 
, GENREID NUMBER 
, MOVIEID NUMBER 
) 
ORGANIZATION EXTERNAL 
( 
  TYPE ORACLE_HIVE 
  DEFAULT DIRECTORY DEFAULT_DIR 
  ACCESS PARAMETERS 
  ( 
    com.oracle.bigdata.tablename=moviework.recommendation 
  ) 
) 
REJECT LIMIT UNLIMITED;

SELECT * FROM recommendation WHERE rownum <=20;

-- Redact the data
-- See customer redaction - apply the same to data in Hadoop & NoSQL
select cust_id, last_name, first_name from customer;

BEGIN
  -- Avro data from Hive
  DBMS_REDACT.ADD_POLICY(
    object_schema => 'MOVIEDEMO',
    object_name => 'MOVIEAPP_LOG_AVRO',
    column_name => 'CUSTID',
    policy_name => 'mylogdata_redaction',
    function_type => DBMS_REDACT.PARTIAL,
    function_parameters => '9,1,7',
    expression => '1=1'
  );
  
    -- Recommendations data from Oracle NoSQL Database
  DBMS_REDACT.ADD_POLICY(
    object_schema => 'MOVIEDEMO',
    object_name => 'RECOMMENDATION',
    column_name => 'CUSTID',
    policy_name => 'recommendation_redaction',
    function_type => DBMS_REDACT.PARTIAL,
    function_parameters => '9,1,7',
    expression => '1=1'
  );
END;
/

-- Review the redacted data
SELECT * FROM movieapp_log_avro WHERE rownum < 20;
SELECT * FROM recommendation WHERE rownum < 20;

-- Customer Recency, Frequency and Monetary query    
-- Find important customers who haven't visited in a while
  WITH customer_sales AS (
  -- Sales and customer attributes
  SELECT m.cust_id,
         c.last_name,
         c.first_name,
         c.country,
         c.gender,
         c.age,
         c.income_level,
         NTILE (5) over (order by sum(sales)) AS rfm_monetary
  FROM movie_sales m, customer c
  WHERE c.cust_id = m.cust_id
  GROUP BY m.cust_id,         
         c.last_name,
         c.first_name,
         c.country,
         c.gender,
         c.age,
         c.income_level
),
click_data AS (
  -- clicks from application log
  SELECT custid,
       NTILE (5) over (order by max(time)) AS rfm_recency,
       NTILE (5) over (order by count(1))    AS rfm_frequency
  FROM movielog_v
  GROUP BY custid
)
SELECT c.cust_id,
    c.last_name,
    c.first_name,
    cd.rfm_recency,
    cd.rfm_frequency,
    c.rfm_monetary,
    cd.rfm_recency*100 + cd.rfm_frequency*10 + c.rfm_monetary AS rfm_combined,
    c.country,
    c.gender,
    c.age,
    c.income_level
  FROM customer_sales c, click_data cd
  WHERE c.cust_id = cd.custid
  AND c.rfm_monetary >= 4
  AND cd.rfm_recency <= 2
  ORDER BY c.rfm_monetary desc, cd.rfm_recency desc
  ;

-- How is the recommendation engine?  Combine data from: 
--  NoSQL Database (rank how many times movies are recommended)
--  Oracle DB (rank sales revenue)
--  HDFS (rank how many times people have showed interest in the movie - previewed, watched, displayed more info, etc.)
WITH rank_recs AS (
  -- recommendation rank from NoSQL Database
  SELECT movieid,
         RANK () OVER (ORDER BY COUNT(movieid) DESC) AS rec_rank
  FROM recommendation 
  GROUP BY movieid),
rank_sales AS (
  -- sales rank from Oracle Database
  SELECT m.movie_id,
         m.title,
         RANK () OVER (ORDER BY SUM(ms.sales) DESC) as sales_rank
  FROM movie m, movie_sales ms
  WHERE  ms.movie_id = m.movie_id
  GROUP BY m.title, m.movie_id
),
rank_interest AS (
  -- "interest" rank from hdfs logs
  SELECT movieid,
  RANK () OVER (ORDER BY COUNT(movieid) DESC) AS click_rank
  FROM movielog_v
  WHERE activity IN (1,4,5) -- rated, started or browsed the movie
  GROUP BY movieid
)
-- combine the results
SELECT rs.title, 
       sales_rank, 
       rec_rank, 
       click_rank
FROM rank_recs rr, rank_sales rs, rank_interest ri
WHERE rr.movieid = rs.movie_id
  AND ri.movieid = rs.movie_id
ORDER BY rec_rank asc;
