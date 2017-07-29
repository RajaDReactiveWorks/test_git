package com.getusroi.config.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.featuremaster.FeatureMaster;

public class ConfigFeatureMasterDAO {
	
	final Logger logger = LoggerFactory.getLogger(ConfigFeatureMasterDAO.class);
	
	
	public static final String SELECTBYFEATURENAMEBYGROUPBYSITEID = "SELECT * FROM featureMaster WHERE featurename =? and featureGroup=? and version=? and siteId=?";
	public static final String INSERT_FEATURE_MASTER_SQL_QUERY = "insert into featureMaster  (featurename,featureGroup,siteId,version,description,multiVendorSupport,product) values(?,?,?,?,?,?,?)";
	public static final String DELETE_FEATURE_MASTER_SQL_QUERY = "delete from featureMaster  where featurename=? and siteId=?";

	/**
	 * check feature Exist featuremaster Table
	 * @param featureName
	 * @param featureGroup
	 * @param siteId
	 * @return masterNodeId;
	 * @throws IOException 
	 * @throws SQLException 
	 */

	public int getFeatureMasterIdByFeatureAndFeaturegroup(String featureName,String featureGroup,String version,int siteId) throws SQLException, IOException{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int masterNodeId=0;		
		try
		{
			conn = DataBaseUtil.getConnection();
			ps =  (PreparedStatement) conn.prepareStatement(SELECTBYFEATURENAMEBYGROUPBYSITEID);
			ps.setString(1, featureName);
			ps.setString(2, featureGroup);
			ps.setString(3, version);
			ps.setInt(4, siteId);
			rs = ps.executeQuery();
			if(rs.next()){
				if(rs.wasNull()){
				masterNodeId=rs.getInt("featureMasterId");
				}else{
					masterNodeId=rs.getInt("featureMasterId");
				}
			}
		}
		catch (ClassNotFoundException cnfe)
		{
			logger.error("Failed to Load the DB Driver",cnfe);
			//#TODO Exception Handling
		}
		finally {
			DataBaseUtil.dbCleanup(conn,ps,rs);
		}
		return masterNodeId;
		
	}
	
	/**
	 * To insert featureMaster Details 
	 * @param featureMaster
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
public boolean insertFeatureMasterDetails(FeatureMaster featureMaster) throws SQLException, IOException{
		
		Connection conn = null;
		PreparedStatement ps = null;
		Boolean isInserted = false;
		try
		{
			conn = DataBaseUtil.getConnection();
			ps =  (PreparedStatement) conn.prepareStatement(INSERT_FEATURE_MASTER_SQL_QUERY);
			ps.setString(1, featureMaster.getFeature());
			ps.setString(2, featureMaster.getFeatureGroup());
			ps.setInt(3, featureMaster.getSiteId());
			ps.setString(4, featureMaster.getVersion());
			ps.setString(5, featureMaster.getDescription());
			ps.setBoolean(6, featureMaster.isMultipleVendorSupport());
			ps.setString(7, featureMaster.getProduct());

			 if(ps.executeUpdate()!=0)
				 isInserted=true;
		
		}
		catch (ClassNotFoundException cnfe)
		{
			logger.error("Failed to Load the DB Driver",cnfe);
			//#TODO Exception Handling
		}
		finally {
			DataBaseUtil.dbCleanUp(conn,ps);
		}
		return isInserted;
		
	}

/**
 * To delete feature master from featurMaster Table
 * @param feature
 * @param siteId
 * @return
 * @throws SQLException
 * @throws IOException
 */
public boolean deleteFeatureMasterDetails(String feature,int siteId) throws SQLException, IOException{
	Connection conn = null;
	PreparedStatement ps = null;
	boolean isUpdated=false;
	try
	{
		conn = DataBaseUtil.getConnection();
		ps =  (PreparedStatement) conn.prepareStatement(DELETE_FEATURE_MASTER_SQL_QUERY);
		ps.setString(1, feature);
		ps.setInt(2, siteId);
		if( ps.executeUpdate()>0);
		isUpdated=true;
		
	}
	catch (ClassNotFoundException cnfe)
	{
		logger.error("Failed to Load the DB Driver",cnfe);
		//#TODO Exception Handling
	}
	finally {
		DataBaseUtil.dbCleanUp(conn,ps);
	}
	return isUpdated;
	
	

}


}
