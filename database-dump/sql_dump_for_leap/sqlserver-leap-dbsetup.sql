/*
File name: C:/Users/Reactiveworks/Desktop/leapmssql.sql
Creation date: 12/23/2017
Created by MySQL to MS SQL 7.1 [Demo]
--------------------------------------------------
More conversion tools at http://www.convert-in.com
*/

/*
Table structure for table '[dbo].[confignode]'
*/

IF OBJECT_ID ('[dbo].[confignode]', 'U') IS NOT NULL
DROP TABLE [dbo].[confignode];

CREATE TABLE [dbo].[confignode] (
	[nodeId] INT IDENTITY NOT NULL,
	[nodeName] NVARCHAR(255) ,
	[isRoot] SMALLINT,
	[hasChildren] SMALLINT,
	[parentNodeId] INT,
	[description] NVARCHAR(255) ,
	[type] NVARCHAR(255) ,
	[level] INT NOT NULL,
	[version] NVARCHAR(20) ,
	[primaryFeatureId] INT
);

CREATE CLUSTERED INDEX [nodeId_clust_idx] ON [dbo].[confignode]([nodeId]);

/*
Dumping data for table '[dbo].[confignode]'
*/


/*
Table structure for table '[dbo].[confignodedata]'
*/

IF OBJECT_ID ('[dbo].[confignodedata]', 'U') IS NOT NULL
DROP TABLE [dbo].[confignodedata];

CREATE TABLE [dbo].[confignodedata] (
	[nodeDataId] INT IDENTITY NOT NULL,
	[nodeId] INT NOT NULL,
	[configName] NVARCHAR(100)  NOT NULL,
	[configData] NVARCHAR(MAX),
	[configType] NVARCHAR(20)  NOT NULL,
	[configStatus] NVARCHAR(50)  NOT NULL,
	[isEnabled] INT NOT NULL DEFAULT 0,
	[createdDTM] DATETIME,
	[failureMsg] NVARCHAR(220) 
);
CREATE CLUSTERED INDEX [nodeDataId_clust_idx] ON [dbo].[confignodedata]([nodeDataId]);

/*
Dumping data for table '[dbo].[confignodedata]'
*/

CREATE INDEX [nodeId] ON [dbo].[confignodedata]([nodeId]);

/*
Table structure for table '[dbo].[customeraccount]'
*/

IF OBJECT_ID ('[dbo].[customeraccount]', 'U') IS NOT NULL
DROP TABLE [dbo].[customeraccount];

CREATE TABLE [dbo].[customeraccount] (
	[accountId] INT IDENTITY NOT NULL,
	[accountName] NVARCHAR(50)  NOT NULL,
	[saltSecretKey] NVARCHAR(50)  NOT NULL,
	[internalTenantId] NVARCHAR(50)  NOT NULL,
	[description] NVARCHAR(50) ,
	[tenantTokenExpiration] INT NOT NULL
);
CREATE CLUSTERED INDEX [accountId_clust_idx] ON [dbo].[customeraccount]([accountId]);

/*
Dumping data for table '[dbo].[customeraccount]'
*/

CREATE UNIQUE INDEX [accountName] ON [dbo].[customeraccount]([accountName]) WHERE [accountName] IS NOT NULL;
CREATE UNIQUE INDEX [accountuniquename] ON [dbo].[customeraccount]([accountName], [internalTenantId]) WHERE [accountName] IS NOT NULL AND [internalTenantId] IS NOT NULL;

/*
Table structure for table '[dbo].[customersite]'
*/

IF OBJECT_ID ('[dbo].[customersite]', 'U') IS NOT NULL
DROP TABLE [dbo].[customersite];

CREATE TABLE [dbo].[customersite] (
	[id] INT IDENTITY NOT NULL,
	[accountId] INT NOT NULL,
	[siteId] NVARCHAR(50)  NOT NULL,
	[domain] NVARCHAR(50)  NOT NULL,
	[description] NVARCHAR(50)  NOT NULL,
	[timezone] NVARCHAR(45)  NOT NULL
);

CREATE CLUSTERED INDEX [id_clust_idx] ON [dbo].[customersite]([id]);

/*
Dumping data for table '[dbo].[customersite]'
*/

CREATE UNIQUE INDEX [internalSite] ON [dbo].[customersite]([siteId], [domain]) WHERE [siteId] IS NOT NULL AND [domain] IS NOT NULL;
CREATE UNIQUE INDEX [accountId_siteId] ON [dbo].[customersite]([accountId], [siteId]) WHERE [accountId] IS NOT NULL AND [siteId] IS NOT NULL;

/*
Table structure for table '[dbo].[domainadmin]'
*/

IF OBJECT_ID ('[dbo].[domainadmin]', 'U') IS NOT NULL
DROP TABLE [dbo].[domainadmin];

CREATE TABLE [dbo].[domainadmin] (
	[id] INT IDENTITY NOT NULL,
	[domainid] INT,
	[adminusername] NVARCHAR(100)  NOT NULL,
	[adminpassword] NVARCHAR(100)  NOT NULL
);
CREATE CLUSTERED INDEX [id_clust_idx] ON [dbo].[domainadmin]([id]);

/*
Dumping data for table '[dbo].[domainadmin]'
*/

CREATE INDEX [domainid] ON [dbo].[domainadmin]([domainid]);

/*
Table structure for table '[dbo].[dynastorelog]'
*/

IF OBJECT_ID ('[dbo].[dynastorelog]', 'U') IS NOT NULL
DROP TABLE [dbo].[dynastorelog];

CREATE TABLE [dbo].[dynastorelog] (
	[siteNodeId] INT NOT NULL,
	[sessionId] NVARCHAR(200)  NOT NULL,
	[status] NVARCHAR(50)  NOT NULL,
	[openedDTM] DATETIME,
	[closedDTM] DATETIME,
	[info] NVARCHAR(2000) 
);
CREATE CLUSTERED INDEX [siteNodeId_clust_idx] ON [dbo].[dynastorelog]([siteNodeId]);

/*
Dumping data for table '[dbo].[dynastorelog]'
*/


/*
Table structure for table '[dbo].[eventdispatchtracker]'
*/

IF OBJECT_ID ('[dbo].[eventdispatchtracker]', 'U') IS NOT NULL
DROP TABLE [dbo].[eventdispatchtracker];

CREATE TABLE [dbo].[eventdispatchtracker] (
	[tenantId] NVARCHAR(50)  NOT NULL,
	[siteId] NVARCHAR(50)  NOT NULL,
	[requestId] NVARCHAR(100)  NOT NULL,
	[eventStoreId] NVARCHAR(50)  NOT NULL,
	[eventCreatedDTM] DATETIME,
	[lastFailureDTM] DATETIME,
	[status] NVARCHAR(25)  NOT NULL,
	[failureReason] NVARCHAR(MAX),
	[retryCount] INT
);
CREATE CLUSTERED INDEX [tenantId_clust_idx] ON [dbo].[eventdispatchtracker]([tenantId]);

/*
Dumping data for table '[dbo].[eventdispatchtracker]'
*/


/*
Table structure for table '[dbo].[featuredeployment]'
*/

IF OBJECT_ID ('[dbo].[featuredeployment]', 'U') IS NOT NULL
DROP TABLE [dbo].[featuredeployment];
CREATE TABLE [dbo].[featuredeployment] (
	[featureDeploymentId] INT IDENTITY NOT NULL,
	[featureMasterId] INT NOT NULL,
	[featureName] NVARCHAR(50)  NOT NULL,
	[implementationName] NVARCHAR(50)  NOT NULL,
	[vendorName] NVARCHAR(50)  NOT NULL,
	[featureVersion] NVARCHAR(20) ,
	[isActive] SMALLINT,
	[isPrimary] SMALLINT,
	[isCustomized] SMALLINT,
	[provider] NVARCHAR(45) 
);
CREATE CLUSTERED INDEX [featureDeploymentId_clust_idx] ON [dbo].[featuredeployment]([featureDeploymentId]);

/*
Dumping data for table '[dbo].[featuredeployment]'
*/

CREATE UNIQUE INDEX [index_name] ON [dbo].[featuredeployment]([featureName], [implementationName], [vendorName], [featureVersion], [featureMasterId]) WHERE [featureName] IS NOT NULL AND [implementationName] IS NOT NULL AND [vendorName] IS NOT NULL AND [featureVersion] IS NOT NULL AND [featureMasterId] IS NOT NULL;
CREATE INDEX [featureMasterId_fk] ON [dbo].[featuredeployment]([featureMasterId]);

/*
Table structure for table '[dbo].[featuremaster]'
*/

IF OBJECT_ID ('[dbo].[featuremaster]', 'U') IS NOT NULL
DROP TABLE [dbo].[featuremaster];
CREATE TABLE [dbo].[featuremaster] (
	[featureMasterId] INT IDENTITY NOT NULL,
	[featureName] NVARCHAR(50)  NOT NULL,
	[featureGroup] NVARCHAR(50)  NOT NULL,
	[siteId] INT NOT NULL,
	[version] NVARCHAR(20) ,
	[description] NVARCHAR(200) ,
	[multiVendorSupport] SMALLINT,
	[allowMultipleImpl] SMALLINT,
	[product] NVARCHAR(100)  NOT NULL
);
CREATE CLUSTERED INDEX [featureMasterId_clust_idx] ON [dbo].[featuremaster]([featureMasterId]);

/*
Dumping data for table '[dbo].[featuremaster]'
*/

CREATE UNIQUE INDEX [index_name] ON [dbo].[featuremaster]([siteId], [featureName], [featureGroup], [version]) WHERE [siteId] IS NOT NULL AND [featureName] IS NOT NULL AND [featureGroup] IS NOT NULL AND [version] IS NOT NULL;
CREATE INDEX [siteID_fk] ON [dbo].[featuremaster]([siteId]);

/*
Table structure for table '[dbo].[userdomain]'
*/

IF OBJECT_ID ('[dbo].[userdomain]', 'U') IS NOT NULL
DROP TABLE [dbo].[userdomain];
CREATE TABLE [dbo].[userdomain] (
	[id] INT IDENTITY NOT NULL,
	[username] NVARCHAR(50)  NOT NULL,
	[domainid] INT NOT NULL
);
CREATE CLUSTERED INDEX [id_clust_idx] ON [dbo].[userdomain]([id]);

/*
Dumping data for table '[dbo].[userdomain]'
*/

CREATE INDEX [fk_domain_user] ON [dbo].[userdomain]([domainid]);
