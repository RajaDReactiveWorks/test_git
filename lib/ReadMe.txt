						Setting Jars to Local Maven Repository

Steps to get Oracle Driver dependency:
--------------------------------------
As oracle driver is not available in maven repository due to licensing problem. So we need to make is available in local m2. To do that follow the steps given below :
I) If jar available in lib folder:
1.1)Got to the  lib/meshRequireJar directory from terminal.
1.2) Test maven 3.3.9 is setup in your machine using the command given below :
           mvn -version
You will get the information related to maven.If not installed,Please install maven to your system first.
1.3) To make oracle jar to be available in local maven, use the command given below in terminal.
		mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.2.0 -Dpackaging=jar

II) If jar is unavailable in lib folder:
2.1) To download we should have our account with oracle. You can register from "http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html"
2.2) download the ojdbc jar from link given "http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html". Based on the version of oracle installed on the machine ( code is tested with 11.2.0.2.0)
2.3) Go to the specified location where ojdbc.jar is downloaded. From command line issues following command :
  mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.2.0 -Dpackaging=jar
	         
Steps to get MS SQL maven dependency 
---------------------------------------
I) If jar available in lib folder:
1.1)Got to the  lib/meshRequireJar directory from terminal.
1.2) Test maven 3.3.9 is setup in your machine using the command given below :
           mvn -version
You will get the information related to maven.If not installed,Please install maven to your system first.
1.3) To make oracle jar to be available in local maven, use the command given below in terminal.
		mvn install:install-file -Dfile=sqljdbc41.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc41 -Dversion=4.0 -Dpackaging=jar

II) If jar is unavailable in lib folder:
2.1) Download the driver from http://www.microsoft.com/download/en/details.aspx?displaylang=en&id=11774 (zip/tar)
2.2) Extract it and go the location where you can see sqljdbc/sqljdbc4 jar.
2.3) add jar to local maven using the command given below:
mvn install:install-file -Dfile=sqljdbc41.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc41 -Dversion=4.0 -Dpackaging=jar

note:  added maven dependency in pom :
<dependency>
  <groupId>com.microsoft.sqlserver</groupId>
  <artifactId>sqljdbc41</artifactId>
  <version>4.0</version>
</dependency>


Steps to get PostgreSQL maven dependency 
---------------------------------------
I) If jar available in lib folder:
1.1)Got to the  lib/meshRequireJar directory from terminal.
1.2) Test maven 3.3.9 is setup in your machine using the command given below :
           mvn -version
You will get the information related to maven.If not installed,Please install maven to your system first.
1.3) To make oracle jar to be available in local maven, use the command given below in terminal.
		mvn install:install-file -Dfile=postgresql-9.4.1212.jre7.jar -DgroupId=com.postgresql -DartifactId=postgresql -Dversion=9.4.1212 -Dpackaging=jar

II) If jar is unavailable in lib folder:
2.1) Download the driver from https://jdbc.postgresql.org/download/postgresql-9.4.1212.jre7.jar
2.2) add jar to local maven using the command given below:
mvn install:install-file -Dfile=postgresql-9.4.1212.jre7.jar -DgroupId=com.postgresql -DartifactId=postgresql -Dversion=9.4.1212 -Dpackaging=jar

note:  added maven dependency in pom :
<dependency>
	<groupId>com.postgresql</groupId>
	<artifactId>postgresql</artifactId>
	<version>9.4.1212</version>
</dependency>

						
						




