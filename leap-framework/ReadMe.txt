�leap-framework� project: 
=========================

�We have written a pure java based core framework that has a service class for adding, getting, updating, enabling or disabling feature/service, deleting the configuration in database and cache. 
�It supports eventing, dynastore, permastore, policy and feature Implementation configuration and can perform above specified operation using its respective service class.

Database setup for Application:
===============================
	Application is configured with 2 types of database schema. One used for configuration level and other used for application level.
Note: write now we are using same database schema for both configuration and application level.
Configuration Level database setup:
-----------------------------------
 	A property file named �globalAppDeploymentConfig.properties� available in �leap-framework� project inside �resources� folder. Change it to the database properties of your system
 



How to test framework: 
======================
Framework is provided with test classes and test suites which runs the test classes. It is used to test the framework is running in expected manner or not. There are different ways we can run this test cases: 

Run all test cases at once:
---------------------------
	In this, all the test cases written for framework (�leap-framework�) is executed at once.
To do this, follow the below steps:
�	Go the �leap-framework� project location from command line. You can see the pom.xml file written for the project in that folder.
�	Type �mvn clean install� and press enter button from command line. This will run all the test case written for framework one by one and return you the result containing success or failure of test cases. Also build the project.

3.2) Run Test suites from eclipse/Fuse IDE:
-------------------------------------------
	Test suites are written to run the group of test class from eclipse.
To do this, follow the below steps:
�	From eclipse/fuse IDE, open �src/test/java� then open a package name �com.attunedlabs.test.suite� of project �leap-framework�.
�	You can see the class available in the package. You can run this just by right click on the selected class and select �run as junit� on open popup. 
�	In order to run configuration DAO, permastore or policy related test classes run class named �RoiTestSuite�, to run dynastore related test class run class named �DynastoreTestSuite�, to run feature related test classes, run class named as �FeatureTestSuite�, to sun eventing based test classes, run class named �EventingTestSuite�.
