USE roi;
CREATE TABLE `featureMaster` (
  `masterNodeId` int(11) NOT NULL AUTO_INCREMENT,
  `featureName` varchar(50) NOT NULL,
  `featureGroup` varchar(50) NOT NULL,
  `siteId` int(11) NOT NULL,
  `version` varchar(20) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `multiVendorSupport` tinyint(1) DEFAULT NULL,
  `product` varchar(100) NOT NULL,
  PRIMARY KEY (`masterNodeId`),
  UNIQUE KEY `index_name` (`featureName`,`featureGroup`,`siteId`),
  KEY `siteID_fk` (`siteId`),
  CONSTRAINT `siteID_fk` FOREIGN KEY (`siteId`) REFERENCES `confignode` (`nodeId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

CREATE TABLE `dynastorelog` (
  `siteNodeId` int(10) NOT NULL,
  `sessionId` varchar(200) NOT NULL,
  `status` varchar(50) NOT NULL,
  `openedDTM` datetime NOT NULL,
  `closedDTM` datetime DEFAULT NULL,
  `info` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`siteNodeId`,`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `confignodedata` (
  `nodeDataId` int(10) NOT NULL AUTO_INCREMENT,
  `nodeId` int(10) NOT NULL,
  `configName` varchar(100) NOT NULL,
  `configData` varchar(2000) NOT NULL,
  `configType` varchar(20) NOT NULL,
  `configStatus` varchar(50) NOT NULL,
  `isEnabled` int(11) NOT NULL DEFAULT '0',
  `createdDTM` datetime NOT NULL,
  `failureMsg` varchar(220) DEFAULT NULL,
  PRIMARY KEY (`nodeDataId`),
  KEY `nodeId` (`nodeId`),
  CONSTRAINT `confignodedata_ibfk_1` FOREIGN KEY (`nodeId`) REFERENCES `confignode` (`nodeId`)
) ENGINE=InnoDB AUTO_INCREMENT=19200 DEFAULT CHARSET=latin1;

CREATE TABLE `confignode` (
  `nodeId` int(10) NOT NULL AUTO_INCREMENT,
  `nodeName` varchar(255) DEFAULT NULL,
  `isRoot` tinyint(1) DEFAULT NULL,
  `hasChildren` tinyint(1) DEFAULT NULL,
  `parentNodeId` int(10) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `level` int(10) NOT NULL,
  PRIMARY KEY (`nodeId`)
) ENGINE=InnoDB AUTO_INCREMENT=726 DEFAULT CHARSET=latin1;

CREATE TABLE `AreaList` (
  `AreaId` int(11) NOT NULL,
  `AreaType` varchar(50) NOT NULL,
  `AreaName` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

