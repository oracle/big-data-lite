hadoop jar $OXH_HOME/lib/oxh.jar -conf $OXH_DEMO_HOME/oxh-site.xml $OXH_DEMO_HOME/moviedemo.xq 

## then open http://localhost:8983/solr/moviedemo/select?q=description_en:adventure%20OR%20title_en:adventure
