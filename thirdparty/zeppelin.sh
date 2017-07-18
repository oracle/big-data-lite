#!/bin/bash
#
# zeppelin  Start/Stop the Zeppelin notebooks
#
# chkconfig: 5 90 60

# If anything goes wrong, fail
exit_err () {
  echo "There was an error, please review the output above and, if appropriate, report a bug on https://github.com/oracle/big-data-lite with the full output of the script."
  echo ""
  echo "If you are having connection difficulties, ensure you set the https_proxy before running this script."
  echo "  Example:"
  echo "  export https_proxy=myproxy.company.com:80"
  exit 1
}
trap 'exit_err' ERR

# If run as root or as service, rerun as owner
owner=$(stat -c '%U' $0)
if [ "$USER" != "$owner" ] && ( [ "$USER" == "root" ] || [ "$USER" == "" ] ); then
  sudo -E -u $owner bash -c "$0 $*"
  exit $?
fi

dir=$(dirname "$(readlink -f $0)")
thirdparty_root=/opt/thirdparty
zeppelin_version=0.7.0
zeppelin_pkg_url=https://archive.apache.org/dist/zeppelin/zeppelin-${zeppelin_version}/zeppelin-${zeppelin_version}-bin-all.tgz
pgx_interpreter_version=2.4.1
pgx_interpreter_pkg_url=/u01/oracle-spatial-graph/property_graph/pgx/client/pgx-${pgx_interpreter_version}-zeppelin-interpreter.zip

cd $dir

if [ "$1" == "install" ]; then
  sudo mkdir -p $thirdparty_root
  sudo chmod 777 $thirdparty_root
  zeppelin_pkg=$thirdparty_root/$(basename $zeppelin_pkg_url)
  echo "Setting up Zeppelin, including the PGX interpreter..."

  echo "Getting Zeppelin version ${zeppelin_version}..."
  [ -f $zeppelin_pkg ] ||
    curl $zeppelin_pkg_url -o $zeppelin_pkg
  echo "Unpacking Zeppelin..."
  tar xf $zeppelin_pkg -C $thirdparty_root
  
  (
  pgx_interpreter_pkg=$thirdparty_root/$(basename $pgx_interpreter_pkg_url)
  echo "Getting the PGX interpreter version ${pgx_interpreter_version}..."
  [ -f $pgx_interpreter_pkg ] ||
    cp $pgx_interpreter_pkg_url $pgx_interpreter_pkg
  if [ ! -f $pgx_interpreter_pkg ]; then
    echo "Error: can't find the PGX interpreter package (pgx-${pgx_interpreter_version}-zeppelin-interpreter.zip)."
    echo "Download it from the Oracle Technology Network (http://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytics/downloads/index.html)"
    echo "and save the zip archive archive to $pgx_interpreter_pkg."
    exit 1
  fi
  echo "Unpacking the PGX interpreter.."
  zeppelin_root=$(find $thirdparty_root -maxdepth 1 -type d | grep zeppelin-${zeppelin_version} )
  unzip $pgx_interpreter_pkg -d $zeppelin_root/interpreter/pgx
  # Do not use https to conect to the PGX server; NB: may need to update the patch
  # when updating the PGX interpreter version
  (
  cd $zeppelin_root/interpreter/pgx
  patch -p0 <<EOP
--- interpreter-setting.json    2017-03-06 11:58:48.000000000 -0500
+++ interpreter-setting.json.http       2017-03-07 08:05:20.683000001 -0500
@@ -7,7 +7,7 @@
       "pgx.baseUrl": {
         "envName": "PGX_SERVER_BASEURL",
         "propertyName": "pgx.baseUrl",
-        "defaultValue": "https://localhost:7007",
+        "defaultValue": "http://localhost:7007",
         "description": "Base URL of a running PGX server this interpreter should connect to"
       },
       "pgx.trustStore": {
EOP
  )

  # Register the %pgx interpreter; NB: may need to update the patch
  # when updating the zeppelin version
  cd $zeppelin_root
  rm -f conf/zeppelin-site.xml
  cp conf/zeppelin-site.xml.template conf/zeppelin-site.xml
  patch -p0 <<EOP
--- conf/zeppelin-site.xml      2017-03-06 08:24:17.617000000 -0500
+++ conf/zeppelin-site.xml.pgx  2017-03-06 08:24:01.189000000 -0500
@@ -200,7 +200,7 @@

 <property>
   <name>zeppelin.interpreters</name>
-  <value>org.apache.zeppelin.spark.SparkInterpreter,org.apache.zeppelin.spark.PySparkInterpreter,org.apache.zeppelin.rinterpreter.RRepl,org.apache.zeppelin.rinterpreter.KnitR,org.apache.zeppelin.spark.SparkRInterpreter,org.apache.zeppelin.spark.SparkSqlInterpreter,org.apache.zeppelin.spark.DepInterpreter,org.apache.zeppelin.markdown.Markdown,org.apache.zeppelin.angular.AngularInterpreter,org.apache.zeppelin.shell.ShellInterpreter,org.apache.zeppelin.file.HDFSFileInterpreter,org.apache.zeppelin.flink.FlinkInterpreter,,org.apache.zeppelin.python.PythonInterpreter,org.apache.zeppelin.python.PythonInterpreterPandasSql,org.apache.zeppelin.python.PythonCondaInterpreter,org.apache.zeppelin.python.PythonDockerInterpreter,org.apache.zeppelin.lens.LensInterpreter,org.apache.zeppelin.ignite.IgniteInterpreter,org.apache.zeppelin.ignite.IgniteSqlInterpreter,org.apache.zeppelin.cassandra.CassandraInterpreter,org.apache.zeppelin.geode.GeodeOqlInterpreter,org.apache.zeppelin.postgresql.PostgreSqlInterpreter,org.apache.zeppelin.jdbc.JDBCInterpreter,org.apache.zeppelin.kylin.KylinInterpreter,org.apache.zeppelin.elasticsearch.ElasticsearchInterpreter,org.apache.zeppelin.scalding.ScaldingInterpreter,org.apache.zeppelin.alluxio.AlluxioInterpreter,org.apache.zeppelin.hbase.HbaseInterpreter,org.apache.zeppelin.livy.LivySparkInterpreter,org.apache.zeppelin.livy.LivyPySparkInterpreter,org.apache.zeppelin.livy.LivyPySpark3Interpreter,org.apache.zeppelin.livy.LivySparkRInterpreter,org.apache.zeppelin.livy.LivySparkSQLInterpreter,org.apache.zeppelin.bigquery.BigQueryInterpreter,org.apache.zeppelin.beam.BeamInterpreter,org.apache.zeppelin.pig.PigInterpreter,org.apache.zeppelin.pig.PigQueryInterpreter,org.apache.zeppelin.scio.ScioInterpreter</value>
+  <value>org.apache.zeppelin.spark.SparkInterpreter,org.apache.zeppelin.spark.PySparkInterpreter,org.apache.zeppelin.rinterpreter.RRepl,org.apache.zeppelin.rinterpreter.KnitR,org.apache.zeppelin.spark.SparkRInterpreter,org.apache.zeppelin.spark.SparkSqlInterpreter,org.apache.zeppelin.spark.DepInterpreter,org.apache.zeppelin.markdown.Markdown,org.apache.zeppelin.angular.AngularInterpreter,org.apache.zeppelin.shell.ShellInterpreter,org.apache.zeppelin.file.HDFSFileInterpreter,org.apache.zeppelin.flink.FlinkInterpreter,,org.apache.zeppelin.python.PythonInterpreter,org.apache.zeppelin.python.PythonInterpreterPandasSql,org.apache.zeppelin.python.PythonCondaInterpreter,org.apache.zeppelin.python.PythonDockerInterpreter,org.apache.zeppelin.lens.LensInterpreter,org.apache.zeppelin.ignite.IgniteInterpreter,org.apache.zeppelin.ignite.IgniteSqlInterpreter,org.apache.zeppelin.cassandra.CassandraInterpreter,org.apache.zeppelin.geode.GeodeOqlInterpreter,org.apache.zeppelin.postgresql.PostgreSqlInterpreter,org.apache.zeppelin.jdbc.JDBCInterpreter,org.apache.zeppelin.kylin.KylinInterpreter,org.apache.zeppelin.elasticsearch.ElasticsearchInterpreter,org.apache.zeppelin.scalding.ScaldingInterpreter,org.apache.zeppelin.alluxio.AlluxioInterpreter,org.apache.zeppelin.hbase.HbaseInterpreter,org.apache.zeppelin.livy.LivySparkInterpreter,org.apache.zeppelin.livy.LivyPySparkInterpreter,org.apache.zeppelin.livy.LivyPySpark3Interpreter,org.apache.zeppelin.livy.LivySparkRInterpreter,org.apache.zeppelin.livy.LivySparkSQLInterpreter,org.apache.zeppelin.bigquery.BigQueryInterpreter,org.apache.zeppelin.beam.BeamInterpreter,org.apache.zeppelin.pig.PigInterpreter,org.apache.zeppelin.pig.PigQueryInterpreter,org.apache.zeppelin.scio.ScioInterpreter,oracle.pgx.zeppelin.PgxInterpreter</value>
   <description>Comma separated interpreter configurations. First interpreter become a default</description>
 </property>

EOP
  )

  echo "Done; you can run $0 {start | stop} to start / stop the Zeppelin service"

  (
  a=""
  while [ "$a" != "y" ] && [ "$a" != "n" ]; do
    echo "Add to system services? (works on Oracle Linux, requires sudo) [y/n]"
    read a
  done
  [ "$a" == "n" ] && exit 0
  sudo rm -f /etc/init.d/zeppelin
  sudo ln -s "$(readlink -f $0)" /etc/init.d/zeppelin
  sudo chkconfig --add zeppelin
  if ! grep -q 'Zeppelin,zeppelin,Zeppelin Notebooks,1' /opt/bin/services.prop; then
    sudo echo 'Zeppelin,zeppelin,Zeppelin Notebooks,1' >> /opt/bin/services.prop
  fi
  )

  exit 0
elif [ "$1" == "uninstall" ]; then
  zeppelin_root=$(find $thirdparty_root -maxdepth 1 -type d | grep zeppelin-${zeppelin_version} ) || echo "Zeppelin not installed!"
  [ -d $zeppelin_root ] && rm -rf $zeppelin_root
  [ -f /etc/init.d/zeppelin ] && (sudo rm -f /etc/init.d/zeppelin)

  exit 0
elif [ "$1" == "start" ]; then
  (
  cd $thirdparty_root
  zeppelin_root=$(find . -maxdepth 1 -type d | grep zeppelin-${zeppelin_version} )
  unset CLASSPATH
  ZEPPELIN_PORT=8090 $zeppelin_root/bin/zeppelin-daemon.sh start
  echo "Remember to start the PGX server in order to be able to use the %pgx interpreter"
  )
  exit 0

elif [ "$1" == "stop" ]; then
  (
  cd $thirdparty_root
  zeppelin_root=$(find . -maxdepth 1 -type d | grep zeppelin-${zeppelin_version} )
  unset CLASSPATH
  $zeppelin_root/bin/zeppelin-daemon.sh stop 
  )

  exit 0

elif [ "$1" == "status" ]; then
  trap '' ERR
  (
  cd $thirdparty_root
  zeppelin_root=$(find . -maxdepth 1 -type d | grep zeppelin-${zeppelin_version} )
  unset CLASSPATH
  $zeppelin_root/bin/zeppelin-daemon.sh status 
  )

  exit $?
fi

echo "Usage: $0 {install | uninstall | start | stop | status}"
exit -1

