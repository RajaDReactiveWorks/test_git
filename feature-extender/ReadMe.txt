“feature-extender" project (OSGI):
================================
“feature-extender” project(osgi):
We have written a feature extender for osgi environment whose responsibility is to load all the features configuration (configuration like event configuration, feature Implementation configuration etc.) in database and cache when a feature bundle is install and remove the configuration if feature bundle is removed.
•	The project is provided with Activator class which has start and stop method implementation. On start the activator starts bundle tracker which listen and trigger an event when a bundle is get installed/start/stop/uninstalled. Also it has a custom service tacker class which maintain a registry of new service added or removed. On stop, it cleans the service registry and remove all registered services.
•	Whenever any new bundle is installed and started in karaf. It checks if the new bundle contains “featureMetaInfo.xml” file or not. If found, see the resources defined at feature level (resources means configurable xml for event, permastore, dynastore, policy etc.). Then it loads, parse and add its configuration into database as well as in data grid(hazelcast).
•	Whenever any bundle is stopped or uninstalled from karaf. It checks if the bundle getting uninstalled or stopped contains “featureMetaInfo.xml” file or not. If found, see the resources defined at feature level (resources means configurable xml for event, permastore, dynastore, policy etc.). Then removes its configuration from database as well as in data grid(hazelcast).



How to build the aplication:
===========================
1) From eclipse or fuse IDE:
-------------------------
 Right click On project "pom.xml" .Run "mvn clean" followed by "mvn install".
 
 2) From Command line :
 ----------------------
 From command line go till project base directory i.e "feature-extender" location. Enter command squencly
 
	2.1) to build project:
	          "mvn clean install"
    2.2) to clean old jars available in classpath or to clean eclipse environment
	          "mvn eclipse:clean"
    2.3) to recreate eclipse environment
	         "mvn eclipse:eclipse"

