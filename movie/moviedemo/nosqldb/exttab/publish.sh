echo Generate locator for NOSQL_GENRE table
java -cp $KVHOME/lib/kvstore-ee.jar:$KVHOME/lib/kvstore.jar:$ORACLE_HOME/jdbc/lib/ojdbc6.jar oracle.kv.exttab.Publish -config genre_config.xml -publish
echo Generate locator for NOSQL_GENRE_MOVIE table
java -cp $KVHOME/lib/kvstore-ee.jar:$KVHOME/lib/kvstore.jar:$ORACLE_HOME/jdbc/lib/ojdbc6.jar oracle.kv.exttab.Publish -config genre_movie_config.xml -publish
echo Generate locator for NOSQL_MOVIE table
java -cp $KVHOME/lib/kvstore-ee.jar:$KVHOME/lib/kvstore.jar:$ORACLE_HOME/jdbc/lib/ojdbc6.jar oracle.kv.exttab.Publish -config movie_config.xml -publish 
