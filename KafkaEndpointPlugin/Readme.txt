“MultipleEndpointCustomPlugin” project:
=======================================
We have written a custom maven plugin to generate “soap endpoint” and its “route definition” for the feature. 
•	It has to be added to feature project as plugin in pom.xml. During the build time, the plugin read the “featureservice.xml” file, get the data required to create “endpoint” and its “route definition”.
•	Once “endpoint” and “route definition” is created, write it into “*Execution.xml” available in “resources/META-INF/spring” folder of the project where plugin is added in pom.xml. 
•	Plugin should be added to all the feature project which support “soap endpoint”.


How to build the aplication:
===========================
1) From eclipse or fuse IDE:
-------------------------
 Right click On project "pom.xml" .Run "mvn clean" followed by "mvn install".
 
 2) From Command line :
 ----------------------
 From command line go till project base directory i.e “MultipleEndpointCustomPlugin” location. Enter command squencly
 
	2.1) to build project:
	          "mvn clean install"
    2.2) to clean old jars available in classpath or to clean eclipse environment
	          "mvn eclipse:clean"
    2.3) to recreate eclipse environment
	         "mvn eclipse:eclipse"