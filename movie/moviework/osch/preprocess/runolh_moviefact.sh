hadoop jar ${OLH_HOME}/jlib/oraloader.jar \
           oracle.hadoop.loader.OraLoader \
           -D oracle.hadoop.loader.defaultDateFormat=yyyy-MM-dd:HH:mm:ss \
           -conf /home/oracle/movie/moviework/osch/preprocess/olh_moviefact.xml 
