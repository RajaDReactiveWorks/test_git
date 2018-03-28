-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: localhost    Database: leap
-- ------------------------------------------------------
-- Server version	5.7.18-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `confignode`
--

DROP TABLE IF EXISTS confignode;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE confignode_seq;

CREATE TABLE confignode (
  nodeId int NOT NULL DEFAULT NEXTVAL ('confignode_seq'),
  nodeName varchar(255) DEFAULT NULL,
  isRoot boolean DEFAULT NULL,
  hasChildren boolean DEFAULT NULL,
  parentNodeId int DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  type varchar(255) DEFAULT NULL,
  level int NOT NULL,
  version varchar(20) DEFAULT NULL,
  primaryFeatureId int DEFAULT NULL,
  PRIMARY KEY (nodeId)
)  ;

ALTER SEQUENCE confignode_seq RESTART WITH 10;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `confignodedata`
--

DROP TABLE IF EXISTS confignodedata;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE confignodedata_seq;

CREATE TABLE confignodedata (
  nodeDataId int NOT NULL DEFAULT NEXTVAL ('confignodedata_seq'),
  nodeId int NOT NULL,
  configName varchar(100) NOT NULL,
  configData varchar(10000),
  configType varchar(20) NOT NULL,
  configStatus varchar(50) NOT NULL,
  isEnabled int NOT NULL DEFAULT '0',
  createdDTM timestamp(0) NOT NULL,
  failureMsg varchar(220) DEFAULT NULL,
  PRIMARY KEY (nodeDataId)
 ,
  CONSTRAINT leap_confignodedata_ibfk_1 FOREIGN KEY (nodeId) REFERENCES confignode (nodeId)
)  ;

ALTER SEQUENCE confignodedata_seq RESTART WITH 10;
/*!40101 SET character_set_client = @saved_cs_client */;

CREATE INDEX nodeId ON confignodedata (nodeId);

--
-- Table structure for table `customeraccount`
--

DROP TABLE IF EXISTS customeraccount;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE customeraccount_seq;

CREATE TABLE customeraccount (
  accountId int NOT NULL DEFAULT NEXTVAL ('customeraccount_seq'),
  accountName varchar(50) NOT NULL,
  saltSecretKey varchar(50) NOT NULL,
  internalTenantId varchar(50) NOT NULL,
  description varchar(50) DEFAULT NULL,
  tenantTokenExpiration int NOT NULL,
  PRIMARY KEY (accountId),
  CONSTRAINT accountName UNIQUE  (accountName),
  CONSTRAINT accountuniquename UNIQUE  (accountName,internalTenantId)
) ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customersite`
--

DROP TABLE IF EXISTS customersite;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE customersite_seq;

CREATE TABLE customersite (
  id int NOT NULL DEFAULT NEXTVAL ('customersite_seq'),
  accountId int NOT NULL,
  siteId varchar(50) NOT NULL,
  domain varchar(50) NOT NULL,
  description varchar(50) NOT NULL,
  timezone varchar(45) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT internalSite UNIQUE  (siteId,domain),
  CONSTRAINT accountId_siteId UNIQUE  (accountId,siteId),
  CONSTRAINT leap_customersite_ibfk_1 FOREIGN KEY (accountId) REFERENCES customeraccount (accountId)
) ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `domainadmin`
--

DROP TABLE IF EXISTS domainadmin;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE domainadmin_seq;

CREATE TABLE domainadmin (
  id int NOT NULL DEFAULT NEXTVAL ('domainadmin_seq'),
  domainid int DEFAULT NULL,
  adminusername varchar(100) NOT NULL,
  adminpassword varchar(100) NOT NULL,
  PRIMARY KEY (id)
 ,
  CONSTRAINT leap_domainadmin_ibfk_1 FOREIGN KEY (domainid) REFERENCES customersite (id)
) ;

CREATE INDEX domainid ON domainadmin (domainid);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dynastorelog`
--

DROP TABLE IF EXISTS dynastorelog;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE dynastorelog (
  siteNodeId int NOT NULL,
  sessionId varchar(200) NOT NULL,
  status varchar(50) NOT NULL,
  openedDTM timestamp(0) NOT NULL,
  closedDTM timestamp(0) DEFAULT NULL,
  info varchar(2000) DEFAULT NULL,
  PRIMARY KEY (siteNodeId,sessionId)
) ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eventdispatchtracker`
--

DROP TABLE IF EXISTS eventdispatchtracker;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE eventdispatchtracker (
  tenantId varchar(50) NOT NULL DEFAULT '',
  siteId varchar(50) NOT NULL DEFAULT '',
  requestId varchar(100) NOT NULL DEFAULT '',
  eventStoreId varchar(50) NOT NULL,
  eventCreatedDTM timestamp(0) NULL DEFAULT NULL,
  lastFailureDTM timestamp(0) NULL DEFAULT NULL,
  status varchar(25) NOT NULL,
  failureReason varchar(20000) DEFAULT NULL,
  retryCount int DEFAULT NULL,
  PRIMARY KEY (tenantId,siteId,requestId)
) ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `featuremaster`
--

DROP TABLE IF EXISTS featuremaster;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE featuremaster_seq;

CREATE TABLE featuremaster (
  featureMasterId int NOT NULL DEFAULT NEXTVAL ('featuremaster_seq'),
  featureName varchar(50) NOT NULL,
  featureGroup varchar(50) NOT NULL,
  siteId int NOT NULL,
  version varchar(20) DEFAULT NULL,
  description varchar(200) DEFAULT NULL,
  multiVendorSupport boolean DEFAULT NULL,
  allowMultipleImpl boolean DEFAULT NULL,
  product varchar(100) NOT NULL,
  PRIMARY KEY (featureMasterId),
  CONSTRAINT featuremaster_siteId_feat_fg UNIQUE  (siteId,featureName,featureGroup,version)
 ,
  CONSTRAINT leap_siteID_fk FOREIGN KEY (siteId) REFERENCES confignode (nodeId)
)  ;

ALTER SEQUENCE featuremaster_seq RESTART WITH 10;
/*!40101 SET character_set_client = @saved_cs_client */;

CREATE INDEX siteID_fk ON featuremaster (siteId);

--
-- Table structure for table `featuredeployment`
--

DROP TABLE IF EXISTS featuredeployment;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE featuredeployment_seq;

CREATE TABLE featuredeployment (
  featureDeploymentId int NOT NULL DEFAULT NEXTVAL ('featuredeployment_seq'),
  featureMasterId int NOT NULL,
  featureName varchar(50) NOT NULL,
  implementationName varchar(50) NOT NULL,
  vendorName varchar(50) NOT NULL,
  featureVersion varchar(20) DEFAULT NULL,
  isActive boolean DEFAULT NULL,
  isPrimary boolean DEFAULT NULL,
  isCustomized boolean DEFAULT NULL,
  provider varchar(45) DEFAULT NULL,
  PRIMARY KEY (featureDeploymentId),
  CONSTRAINT featuredeployment_featurename UNIQUE  (featureName,implementationName,vendorName,featureVersion,featureMasterId)
 ,
  CONSTRAINT leap_featureMasterId_fk FOREIGN KEY (featureMasterId) REFERENCES featuremaster (featureMasterId)
)  ;

ALTER SEQUENCE featuredeployment_seq RESTART WITH 10;
/*!40101 SET character_set_client = @saved_cs_client */;

CREATE INDEX featureMasterId_fk ON featuredeployment (featureMasterId);

--
-- Table structure for table `userdomain`
--

DROP TABLE IF EXISTS userdomain;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE SEQUENCE userdomain_seq;

CREATE TABLE userdomain (
  id int NOT NULL DEFAULT NEXTVAL ('userdomain_seq'),
  username varchar(50) NOT NULL,
  domainid int NOT NULL,
  PRIMARY KEY (id)
 ,
  CONSTRAINT leap_fk_domain_user FOREIGN KEY (domainid) REFERENCES customersite (id)
) ;

CREATE INDEX fk_domain_user ON userdomain (domainid);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'leap'
--

--
-- Dumping routines for database 'leap'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-12-23 11:02:00
