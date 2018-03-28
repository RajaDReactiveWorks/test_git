?�leap-core� project: 
=========================
�	we have written a camel base core framework. All camel related operations are done here.
�	It contains a �baseRoute.xml� which is an entry point for our application. Execution route, Implementation Route of feature are imported in �baseRoute.xml�.
�	This project is responsible for initializing Leap Header with request context object, feature group, feature, service, hazelcast transactional context object etc. Also responsible for generating unique id for each request.
�	It is also used to route from base route to execution route based on featuregroup and feature name. Route from execution route to Implementation route by loading specific feature, checking if service is enable for the feature service then get the implementation route associated with it.
�	It is used to call camel notifier at the end of the camel context which is responsible for dispatching component, service, system events configured for that feature and service.

2.3) Database setup for Application:
=====================================
	Application is configured with 2 types of database schema. One used for configuration level and other used for application level.
	
Note: write now we are using same database schema for both configuration and application level.
 
  2.3.1) Application Level database setup:
  ----------------------------------------
 	Application database setup is done in �baseRoute.xml� file available in �resources/META-INF/spring� folder of �leap-core� project for bean declaration whose id is �dataSourceA�. Change it according to the database properties of your system.


How to test framework: 
=====================
	
Note: Test cases is written only for �leap-framework� project to check the behaviour of configuration service class for event, permatsore, dynastore etc. We have not provided any test case for camel based �leap-core� framework�.

How to build the aplication:
===========================
1) From eclipse or fuse IDE:
-------------------------
 Right click On project "pom.xml" .Run "mvn clean" followed by "mvn install".
 
 2) From Command line :
 ----------------------
 From command line go till project base directory i.e "leap-core" location. Enter command squencly
 
	2.1) to build project:
	          "mvn clean install"
    2.2) to clean old jars available in classpath or to clean eclipse environment
	          "mvn eclipse:clean"
    2.3) to recreate eclipse environment
	         "mvn eclipse:eclipse"
	         
Steps to get Oracle Driver dependency:
--------------------------------------

As oracle driver is not available in maven repository due to licensing problem. So we need to make is available in local m2. To do that follow the steps given below :
1) To download we should have our account with oracle. You can register from "http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html"
2) download the ojdbc jar from link given "http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html". Based on the version of oracle installed on the machine ( code is tested with 11.2.0.2.0)
3) Go to the specified location where ojdbc.jar is downloaded. From command line issues following command :
  mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.2.0 -Dpackaging=jar
	         
Steps to get MS SQL maven dependency 
---------------------------------------
1) Download the driver from http://www.microsoft.com/download/en/details.aspx?displaylang=en&id=11774 (zip/tar)
2) Extract it and go the location where you can see sqljdbc/sqljdbc4 jar.
3) add jar to local maven using the command given below:
mvn install:install-file -Dfile=sqljdbc4.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=4.0 -Dpackaging=jar
mvn install:install-file -Dfile=sqljdbc41.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc41 -Dversion=4.0 -Dpackaging=jar
mvn install:install-file -Dfile=sqljdbc42.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc42 -Dversion=4.0 -Dpackaging=jar

4) added maven dependency in pom :
<dependency>
  <groupId>com.microsoft.sqlserver</groupId>
  <artifactId>sqljdbc41</artifactId>
  <version>4.0</version>
</dependency>



