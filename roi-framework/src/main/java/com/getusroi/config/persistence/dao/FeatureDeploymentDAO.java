package com.getusroi.config.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.featuredeployment.FeatureDeployment;

public class FeatureDeploymentDAO {

	final Logger logger = LoggerFactory.getLogger(FeatureDeploymentDAO.class);
	
	public static final String SELECTBYFEATURENAMEBYGROUPBYSITEID = "SELECT * FROM featureDeployment WHERE featureName =? and implementationName=? and VendorName=? and featureVersion=? and featureMasterId=?";
	public static final String INSERT_FEATURE_DEPLOYMENT_SQL_QUERY = "insert into featureDeployment  (featureMasterId,featureName,implementationName,VendorName,featureVersion,isActive,isPrimary,isCustomized) values(?,?,?,?,?,?,?,?)";
	public static final String DELETE_FEATURE_DEPLOYMENT_SQL_QUERY = "delete from featureDeployment  where featureName =? and implementationName=? and VendorName=? and featureVersion=? and featureMasterId=?";
	public static final String UPDATE_FEATURE_DEPLOYMENT_SQL_QUERY= "update featureDeployment set isPrimary=?, isActive=? where featureName=? and implementationName=? and vendorName=? and featureVersion=? and featureMasterId=?";
	
	/**
	 * To insert featureDeployment Details 
	 * @param featureDeployment
	 * @return isInserted : Boolean (true if insert successful else false in failure)
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean insertFeatureDeploymentDetails(FeatureDeployment featureDeployment) throws SQLException, IOException{
		
		Connection conn = null;
		PreparedStatement ps = null;
		Boolean isInserted = false;
		try
		{
			conn = DataBaseUtil.getConnection();
			ps =  (PreparedStatement) conn.prepareStatement(INSERT_FEATURE_DEPLOYMENT_SQL_QUERY);
			ps.setInt(1,featureDeployment.getFeatureMasterId());
			ps.setString(2, featureDeployment.getFeatureName());
			ps.setString(3, featureDeployment.getImplementationName());
			ps.setString(4, featureDeployment.getVendorName());
			ps.setString(5, featureDeployment.getFeatureVersion());
			ps.setBoolean(6, featureDeployment.isActive());
			ps.setBoolean(7, featureDeployment.isPrimary());
			ps.setBoolean(8, featureDeployment.isCustomized());
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
	 * check feature Exist featureDeployment Table
	 * @param featureName
	 * @param implName
	 * @param vendorName
	 * @param version
	 * @return masterNodeId;
	 * @throws IOException 
	 * @throws SQLException 
	 */

	public FeatureDeployment getFeatureDeploymentByFeatureAndImplName(int featureMasterId,String featureName,String implName,String vendorName,String version) throws SQLException, IOException{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FeatureDeployment featureDeployment=null;
		try
		{
			conn = DataBaseUtil.getConnection();
			ps =  (PreparedStatement) conn.prepareStatement(SELECTBYFEATURENAMEBYGROUPBYSITEID);
			ps.setString(1, featureName);
			ps.setString(2, implName);
			ps.setString(3, vendorName);
			ps.setString(4, version);
			ps.setInt(5,featureMasterId);
			rs = ps.executeQuery();
			while(rs.next()){
				int featureDeploymentId=rs.getInt("featureDeploymentId");
				boolean isActive=rs.getBoolean("isActive");
				boolean isPrimary=rs.getBoolean("isPrimary");
				boolean isCustomized=rs.getBoolean("isCustomized");
				
				featureDeployment=new FeatureDeployment(featureDeploymentId,featureMasterId, featureName, implName, vendorName, version, isActive, isPrimary, isCustomized);
				
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
		return featureDeployment;
		
	}
	
	
	public boolean updateFeatureDeployment(int featureMasterId,String featureName,String implName,String vendorName,String version,boolean isPrimary,boolean isActive) throws SQLException, IOException{
		logger.debug(".updateFeatureDeployment method of FeatureDeploymentDAO");
		Connection conn = null;
		PreparedStatement ps = null;
		boolean isUpdated=false;
		try
		{
			conn = DataBaseUtil.getConnection();
			ps =  (PreparedStatement) conn.prepareStatement(UPDATE_FEATURE_DEPLOYMENT_SQL_QUERY);
			ps.setBoolean(1, isPrimary);
			ps.setBoolean(2, isActive);
			ps.setString(3, featureName);
			ps.setString(4, implName);
			ps.setString(5, vendorName);
			ps.setString(6, version);
			ps.setInt(7,featureMasterId);
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
	
	/**
	 * To delete feature master from featureDeployment Table
	 * @param featureName
	 * @param implName
	 * @param vendorName
	 * @param version
	 * @return true if deleted else false
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean deleteFeatureDeployment(int featureMasterId,String featureName,String implName,String vendorName,String version) throws SQLException, IOException{
		Connection conn = null;
		PreparedStatement ps = null;
		boolean isDeleted=false;
		try
		{
			conn = DataBaseUtil.getConnection();
			ps =  (PreparedStatement) conn.prepareStatement(DELETE_FEATURE_DEPLOYMENT_SQL_QUERY);
			ps.setString(1, featureName);
			ps.setString(2, implName);
			ps.setString(3, vendorName);
			ps.setString(4, version);
			ps.setInt(5,featureMasterId);
			if( ps.executeUpdate()>0);
			isDeleted=true;
			
		}
		catch (ClassNotFoundException cnfe)
		{
			logger.error("Failed to Load the DB Driver",cnfe);
			//#TODO Exception Handling
		}
		finally {
			DataBaseUtil.dbCleanUp(conn,ps);
		}
		return isDeleted;
		
		

	}
}
