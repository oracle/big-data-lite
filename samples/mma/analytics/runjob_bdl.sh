#!/bin/sh
#
# $Header: hadoop/jsrc/analytics/example/analytics/runjob.sh /main/1 2015/10/01 15:48:34 jiezhan Exp $
#
# runjob.sh
#
# Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      runjob.sh - run the face recognition job in hadoop cluster
#


hadoop fs -mkdir vinput_data
hadoop fs -put ${MMA_HOME}/example/video/bigdata.mp4 vinput_data
hadoop jar ${MMA_HOME}/lib/ordhadoop-multimedia-analytics.jar -conf /home/oracle/src/samples/mma/analytics/conf/oracle_multimedia_analysis_framework_bdl.xml vinput_data voutput_image



