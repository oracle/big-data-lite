drop table movieapp_log_json;
drop table movieapp_log_avro;
drop table movielog;
drop table recommendation;
-- drop view movielog_v;


/*
CREATE TABLE movielog_t
  (click VARCHAR2(4000))
  ORGANIZATION EXTERNAL
  (TYPE ORACLE_HDFS
   DEFAULT DIRECTORY DEFAULT_DIR
   LOCATION ('/user/oracle/moviework/applog_json/*')
  ) 
  REJECT LIMIT UNLIMITED;

-- select * from movielog_t;
-- Create view to simplify query acess
CREATE OR REPLACE  VIEW movielog_v AS 
  SELECT
  CAST(m.click.custid	AS NUMBER) custid,
  CAST(m.click.movieid AS NUMBER) movieid,
  CAST(m.click.activity	AS NUMBER) activity,
  CAST(m.click.genreid AS NUMBER) genreid,
  CAST(m.click.recommended AS VARCHAR2(1)) recommended,
  CAST(m.click.time AS VARCHAR2(20)) time,
  CAST(m.click.rating AS NUMBER) rating,
  CAST(m.click.price AS NUMBER) price
FROM movielog_t m;

BEGIN
  -- JSON file in HDFS 
  DBMS_REDACT.ADD_POLICY(
    object_schema => 'MOVIEDEMO',
    object_name => 'MOVIE_SALES',
    column_name => 'CUST_ID',
    policy_name => 'moviesales_redaction',
    function_type => DBMS_REDACT.PARTIAL,
    function_parameters => '9,1,7',
    expression => '1=1'
  );
END;
/

BEGIN
  -- JSON file in HDFS 
  DBMS_REDACT.ADD_POLICY(
    object_schema => 'MOVIEDEMO',
    object_name => 'MOVIELOG_V',
    column_name => 'CUSTID',
    policy_name => 'movielog_v_redaction',
    function_type => DBMS_REDACT.PARTIAL,
    function_parameters => '9,1,7',
    expression => '1=1'
  );
END;
/