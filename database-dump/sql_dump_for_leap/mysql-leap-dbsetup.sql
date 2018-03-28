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

DROP TABLE IF EXISTS `confignode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `confignode` (
  `nodeId` int(10) NOT NULL AUTO_INCREMENT,
  `nodeName` varchar(255) DEFAULT NULL,
  `isRoot` tinyint(1) DEFAULT NULL,
  `hasChildren` tinyint(1) DEFAULT NULL,
  `parentNodeId` int(10) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `level` int(10) NOT NULL,
  `version` varchar(20) DEFAULT NULL,
  `primaryFeatureId` int(11) DEFAULT NULL,
  PRIMARY KEY (`nodeId`)
) ENGINE=InnoDB AUTO_INCREMENT=1561 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `confignodedata`
--

DROP TABLE IF EXISTS `confignodedata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `confignodedata` (
  `nodeDataId` int(10) NOT NULL AUTO_INCREMENT,
  `nodeId` int(10) NOT NULL,
  `configName` varchar(100) NOT NULL,
  `configData` text,
  `configType` varchar(20) NOT NULL,
  `configStatus` varchar(50) NOT NULL,
  `isEnabled` int(11) NOT NULL DEFAULT '0',
  `createdDTM` datetime NOT NULL,
  `failureMsg` varchar(220) DEFAULT NULL,
  PRIMARY KEY (`nodeDataId`),
  KEY `nodeId` (`nodeId`),
  CONSTRAINT `leap_confignodedata_ibfk_1` FOREIGN KEY (`nodeId`) REFERENCES `confignode` (`nodeId`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customeraccount`
--

DROP TABLE IF EXISTS `customeraccount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customeraccount` (
  `accountId` int(11) NOT NULL AUTO_INCREMENT,
  `accountName` varchar(50) NOT NULL,
  `saltSecretKey` varchar(50) NOT NULL,
  `internalTenantId` varchar(50) NOT NULL,
  `description` varchar(50) DEFAULT NULL,
  `tenantTokenExpiration` int(11) NOT NULL,
  PRIMARY KEY (`accountId`),
  UNIQUE KEY `accountName` (`accountName`),
  UNIQUE KEY `accountuniquename` (`accountName`,`internalTenantId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customersite`
--

DROP TABLE IF EXISTS `customersite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customersite` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountId` int(11) NOT NULL,
  `siteId` varchar(50) NOT NULL,
  `domain` varchar(50) NOT NULL,
  `description` varchar(50) NOT NULL,
  `timezone` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `internalSite` (`siteId`,`domain`),
  UNIQUE KEY `accountId_siteId` (`accountId`,`siteId`),
  CONSTRAINT `leap_customersite_ibfk_1` FOREIGN KEY (`accountId`) REFERENCES `customeraccount` (`accountId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `domainadmin`
--

DROP TABLE IF EXISTS `domainadmin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domainadmin` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domainid` int(11) DEFAULT NULL,
  `adminusername` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `adminpassword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `domainid` (`domainid`),
  CONSTRAINT `leap_domainadmin_ibfk_1` FOREIGN KEY (`domainid`) REFERENCES `customersite` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dynastorelog`
--

DROP TABLE IF EXISTS `dynastorelog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynastorelog` (
  `siteNodeId` int(10) NOT NULL,
  `sessionId` varchar(200) NOT NULL,
  `status` varchar(50) NOT NULL,
  `openedDTM` datetime NOT NULL,
  `closedDTM` datetime DEFAULT NULL,
  `info` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`siteNodeId`,`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eventdispatchtracker`
--

DROP TABLE IF EXISTS `eventdispatchtracker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eventdispatchtracker` (
  `tenantId` varchar(50) NOT NULL DEFAULT '',
  `siteId` varchar(50) NOT NULL DEFAULT '',
  `requestId` varchar(100) NOT NULL DEFAULT '',
  `eventStoreId` varchar(50) NOT NULL,
  `eventCreatedDTM` timestamp NULL DEFAULT NULL,
  `lastFailureDTM` timestamp NULL DEFAULT NULL,
  `status` varchar(25) NOT NULL,
  `failureReason` varchar(20000) DEFAULT NULL,
  `retryCount` int(11) DEFAULT NULL,
  PRIMARY KEY (`tenantId`,`siteId`,`requestId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `featuredeployment`
--

DROP TABLE IF EXISTS `featuredeployment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `featuredeployment` (
  `featureDeploymentId` int(11) NOT NULL AUTO_INCREMENT,
  `featureMasterId` int(11) NOT NULL,
  `featureName` varchar(50) NOT NULL,
  `implementationName` varchar(50) NOT NULL,
  `vendorName` varchar(50) NOT NULL,
  `featureVersion` varchar(20) DEFAULT NULL,
  `isActive` tinyint(1) DEFAULT NULL,
  `isPrimary` tinyint(1) DEFAULT NULL,
  `isCustomized` tinyint(1) DEFAULT NULL,
  `provider` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`featureDeploymentId`),
  UNIQUE KEY `index_name` (`featureName`,`implementationName`,`vendorName`,`featureVersion`,`featureMasterId`),
  KEY `featureMasterId_fk` (`featureMasterId`),
  CONSTRAINT `leap_featureMasterId_fk` FOREIGN KEY (`featureMasterId`) REFERENCES `featuremaster` (`featureMasterId`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `featuremaster`
--

DROP TABLE IF EXISTS `featuremaster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `featuremaster` (
  `featureMasterId` int(11) NOT NULL AUTO_INCREMENT,
  `featureName` varchar(50) NOT NULL,
  `featureGroup` varchar(50) NOT NULL,
  `siteId` int(11) NOT NULL,
  `version` varchar(20) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `multiVendorSupport` tinyint(1) DEFAULT NULL,
  `allowMultipleImpl` tinyint(1) DEFAULT NULL,
  `product` varchar(100) NOT NULL,
  PRIMARY KEY (`featureMasterId`),
  UNIQUE KEY `index_name` (`siteId`,`featureName`,`featureGroup`,`version`),
  KEY `siteID_fk` (`siteId`),
  CONSTRAINT `leap_siteID_fk` FOREIGN KEY (`siteId`) REFERENCES `confignode` (`nodeId`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userdomain`
--

DROP TABLE IF EXISTS `userdomain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userdomain` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `domainid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_domain_user` (`domainid`),
  CONSTRAINT `leap_fk_domain_user` FOREIGN KEY (`domainid`) REFERENCES `customersite` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
