USE roi;
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (22,'gap',0,1,0,'This is gap tenant','tenant',1);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (23,'site1',0,1,22,'This is  sit1 Site','site',2);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (25,'featuregroup1',0,1,23,'This is  featureGroup1 Feature Group','feature_group',3);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (26,'feature1',0,0,25,'This is  feature1 Feature','feature',4);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (28,'label',0,1,23,'This is  featureGroup2 Feature Group','feature_group',3);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (241,'Parcel',0,0,25,'This is parcel feature','feature',4);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (324,'labelservice',0,0,28,'This is  feature1 Feature','feature',4);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (571,'print',0,1,23,'This is print featuregroup','feature_group',3);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (572,'printservice',0,0,571,'This is printservice feature ','feature',4);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (689,'nicelabel',0,1,23,'This is nicelabel featureGroup','feature_group',3);
insert into `confignode`(`nodeId`,`nodeName`,`isRoot`,`hasChildren`,`parentNodeId`,`description`,`type`,`level`) values (690,'nicelabelservice',0,0,689,'This is nicelabelservice featurename','feature',4);



insert into `featureMaster`(`masterNodeId`,`featureName`,`featureGroup`,`siteId`,`version`,`description`,`multiVendorSupport`,`product`) values (1,'Parcel','featuregroup1',23,'V_1','Test FeatureMaster ',1,'wms2.0');
insert into `featureMaster`(`masterNodeId`,`featureName`,`featureGroup`,`siteId`,`version`,`description`,`multiVendorSupport`,`product`) values (2,'labelservice','label',23,'V_1','Test FeatureMaster ',1,'wms2.0');
insert into `featureMaster`(`masterNodeId`,`featureName`,`featureGroup`,`siteId`,`version`,`description`,`multiVendorSupport`,`product`) values (3,'printservice','print',23,'V_1','print service data',1,'wms2.0');
insert into `featureMaster`(`masterNodeId`,`featureName`,`featureGroup`,`siteId`,`version`,`description`,`multiVendorSupport`,`product`) values (4,'nicelabelservice','nicelabel',23,'V_1','nicelabelservice data',1,'wms2.0');

insert into `AreaList`(`AreaId`,`AreaType`,`AreaName`) values (1,'Stage','STA1');
insert into `AreaList`(`AreaId`,`AreaType`,`AreaName`) values (2,'Stage','STA2');
insert into `AreaList`(`AreaId`,`AreaType`,`AreaName`) values (3,'PIC','PIC1');
insert into `AreaList`(`AreaId`,`AreaType`,`AreaName`) values (4,'PIC','PIC2');
