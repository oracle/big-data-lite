hadoop jar $OSCH_HOME/jlib/orahdfs.jar \
       oracle.hadoop.exttab.ExternalTable \
       -conf /home/oracle/movie/moviework/osch/moviefact_manual_tab.xml \
       -createTable --noexecute
