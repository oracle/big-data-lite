echo MOVIEWORK movie_fact  location files

hadoop jar $OSCH_HOME/jlib/orahdfs.jar \
           oracle.hadoop.exttab.ExternalTable \
           -conf /home/oracle/movie/moviework/osch/preprocess/moviefact_dp.xml \
           -createTable
