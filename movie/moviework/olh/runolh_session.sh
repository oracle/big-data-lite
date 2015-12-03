hadoop jar ${OLH_HOME}/jlib/oraloader.jar \
       oracle.hadoop.loader.OraLoader \
       -conf /home/oracle/movie/moviework/olh/moviesession.xml \
       -D mapred.reduce.tasks=2
