UNRECOVERABLE
LOAD DATA 
APPEND
into table r2rml_table
when (1) <> '#'
(
 RDF$STC_sub   CHAR(4000) terminated by whitespace,
 RDF$STC_pred  CHAR(4000) terminated by whitespace,
 RDF$STC_obj   CHAR(5000) "rtrim(:RDF$STC_obj,'. '||CHR(9)||CHR(10)||CHR(13))"
)
