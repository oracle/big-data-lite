-- Create SESSIONS table
CREATE TABLE "MOVIEDEMO"."SESSIONS_HDFS_EXT_TAB"
    (
      "SESSION_ID" NUMBER,
      "TIME_ID" DATE,
      "CUST_ID"             NUMBER,
      "DURATION_SESSION"    NUMBER,
      "NUM_RATED"           NUMBER,
      "DURATION_RATED"      NUMBER,
      "NUM_COMPLETED"       NUMBER,
      "DURATION_COMPLETED"  NUMBER,
      "TIME_TO_FIRST_START" NUMBER,
      "NUM_STARTED"         NUMBER,
      "NUM_BROWSED"         NUMBER,
      "DURATION_BROWSED"    NUMBER,
      "NUM_LISTED"          NUMBER,
      "DURATION_LISTED"     NUMBER,
      "NUM_INCOMPLETE"      NUMBER,
      "NUM_SEARCHED"        NUMBER
    )
    ORGANIZATION EXTERNAL
    (
      TYPE ORACLE_LOADER 
      DEFAULT DIRECTORY "MOVIEDEMO_DIR" 
      ACCESS PARAMETERS 
      (  
        RECORDS DELIMITED BY NEWLINE 
        BADFILE MOVIEDEMO_DIR:'SESSIONS_HDFS_EXT_TAB.bad' 
        LOGFILE MOVIEDEMO_DIR:'SESSIONS_HDFS_EXT_TAB.log' 
        PREPROCESSOR hdfs_bin_path:'hdfs_stream' 
        FIELDS TERMINATED BY 0x'09' 
        ( SESSION_ID, 
          TIME_ID DATE 'YYYY-MM-DD:HH24:MI:SS', 
          CUST_ID, 
          DURATION_SESSION, 
          NUM_RATED, 
          DURATION_RATED, 
          NUM_COMPLETED, 
          DURATION_COMPLETED, 
          TIME_TO_FIRST_START, 
          NUM_STARTED, 
          NUM_BROWSED, 
          DURATION_BROWSED, 
          NUM_LISTED, 
          DURATION_LISTED, 
          NUM_INCOMPLETE, 
          NUM_SEARCHED 
         ) 
        ) 
       LOCATION ( 'sessions.loc' )
    )
    REJECT LIMIT UNLIMITED ;

