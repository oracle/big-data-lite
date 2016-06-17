# Copy to Hadoop with the command-line tool Oracle Shell for Hadoop Loaders

# Copy this example to /home/oracle/src/samples/c2hadoop and 
# cd to this directory.

# Type ohsh to start up Oracle Shell for Hadoop loaders.

prompt> ohsh

# setresources.ohsh sets up the basic resources needed to use Copy to Hadoop.
# Each line is explained in the comments.

# Run setresources.ohsh.  You will need the password for the moviedemo schema
# in Oracle Database (on Big Data Lite this is 'welcome1' enter it for both
# the sql resource and the JDBC resource).

ohsh> @setresources;

# You can now run the commands in c2h.ohsh.   For example

ohsh> create hive table hive_moviedemo:movie_sessions_tab from oracle table moviedemo:movie_sessions_tab using stage;
