Rem
Rem $Header: hadoop/demo/osh/sql/CreateMovieData.sql ratiwary_osh_newsplitters_outputformat/1 2016/01/19 05:23:49 ratiwary Exp $
Rem
Rem CreateMovieData.sql
Rem
Rem Copyright (c) 2015, 2016, Oracle and/or its affiliates. 
Rem All rights reserved.
Rem
Rem    NAME
Rem      CreateMovieData.sql
Rem
Rem    DESCRIPTION
Rem      Creates partitioned external table for OTA4H storage handler
Rem
Rem    NOTES
Rem
Rem    BEGIN SQL_FILE_METADATA 
Rem    SQL_SOURCE_FILE: hadoop/demo/osh/sql/CreateMovieData.sql 
Rem    SQL_SHIPPED_FILE: 
Rem    SQL_PHASE: 
Rem    SQL_STARTUP_MODE: NORMAL 
Rem    SQL_IGNORABLE_ERRORS: NONE 
Rem    SQL_CALLING_FILE: 
Rem    END SQL_FILE_METADATA
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    ratiwary    03/20/15 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

drop table MovieData;

CREATE TABLE MovieData (
    custid NUMBER,
    movieid NUMBER,
    genreid NUMBER,
    time VARCHAR2(40),
    recommended NUMBER,
    activity NUMBER,
    rating NUMBER);
quit;
