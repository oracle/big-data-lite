# script to get dependencies for tess4j

# for now, we'll just use wget. see comments at end for how to use maven
# setup ~/.m2/settings.xml for proxy or not

#/u01/Middleware/oracle_common/modules/org.apache.maven_3.0.5/bin/mvn dependency:copy-dependencies -DoutputDirectory=lib

mkdir lib
cd lib
rm *.jar

wget http://central.maven.org/maven2/net/sourceforge/tess4j/tess4j/3.0.0/tess4j-3.0.0.jar
wget http://central.maven.org/maven2/org/slf4j/jul-to-slf4j/1.7.13/jul-to-slf4j-1.7.13.jar
wget http://central.maven.org/maven2/org/slf4j/jcl-over-slf4j/1.7.13/jcl-over-slf4j-1.7.13.jar
wget http://central.maven.org/maven2/org/slf4j/log4j-over-slf4j/1.7.13/log4j-over-slf4j-1.7.13.jar
wget http://central.maven.org/maven2/org/slf4j/slf4j-api/1.7.7/slf4j-api-1.7.7.jar 
wget http://central.maven.org/maven2/net/sourceforge/lept4j/lept4j/1.0.1/lept4j-1.0.1.jar
wget http://central.maven.org/maven2/com/github/jai-imageio/jai-imageio-core/1.3.1/jai-imageio-core-1.3.1.jar
wget http://central.maven.org/maven2/net/java/dev/jna/jna/4.2.1/jna-4.2.1.jar

echo 
echo
echo Tess4J library and dependencies downloaded.
echo

# to use maven to get tess4j and dependencies, there is a pom.xml provided.
# You could simply run this command:
#    /u01/Middleware/oracle_common/modules/org.apache.maven_3.0.5/bin/mvn dependency:copy-dependencies -DoutputDirectory=lib
# However, maven didn't seem to respect http_proxy environment variables, which means that to script this
#  we would need to put proxies into the ~/.m2/settings.xml file which was too much effort for this blog.

