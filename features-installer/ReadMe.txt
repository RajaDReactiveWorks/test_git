“features-installer" project:
================================
We have written a feature installer for non-osgi environment whose responsibility is to load all the features configuration (configuration like event configuration, feature Implementation configuration etc.) in database and cache before starting the application. This is done as follows: 
•	It searches for “featureMetaInfo.xml file” in all the jar available in its class path. If found, see the resource defined at feature level (resource like configurable xml for event, permastore, dynastore, featureImpl etc.). Load, parse and add its configuration into database as well as in data grid(hazelcast).
•	Once all the configuration is loaded, it will load, parse the “baseRoute.xml” file available in “leap-core” project and start the camel context defined in it.


How to build the aplication:
===========================
1) From eclipse or fuse IDE:
-------------------------
 Right click On project "pom.xml" .Run "mvn clean" followed by "mvn install".
 
 2) From Command line :
 ----------------------
 From command line go till project base directory i.e "features-installer" location. Enter command squencly
 
	2.1) to build project:
	          "mvn clean install"
    2.2) to clean old jars available in classpath or to clean eclipse environment
	          "mvn eclipse:clean"
    2.3) to recreate eclipse environment
	         "mvn eclipse:eclipse"

