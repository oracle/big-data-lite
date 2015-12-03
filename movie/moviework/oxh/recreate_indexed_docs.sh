#!/bin/bash

solrctl collection --delete  moviedemo
solrctl instancedir --delete moviedemo
solrctl instancedir --create moviedemo $OXH_DEMO_HOME/solr_configs
solrctl collection --create moviedemo
./oxh_moviedemo.sh 
