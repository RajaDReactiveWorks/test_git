package com.getusroi.dynastore.config.impl;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.core.BeanDependencyResolveException;
import com.getusroi.core.BeanDependencyResolverFactory;
import com.getusroi.core.CoreDataBaseUtil;
import com.getusroi.core.IBeanDependencyResolver;
import com.getusroi.dynastore.config.IDynaSQLResultSetMapper;
import com.getusroi.dynastore.config.IDynaStoreCustomInitializer;
import com.getusroi.dynastore.config.jaxb.DynastoreInitializer;

/** Generic class that takes the SQL query and get the result and builds the PermaStoreCache */
public class DynaSQLCacheObjectBuilder implements IDynaStoreCustomInitializer{
	final Logger logger = LoggerFactory.getLogger(DynaSQLCacheObjectBuilder.class);
	public static final String MAPPER_MAP_OF_MAP="Map-Of-Map";
	public static final String MAPPER_JSON="To-JSON";
	
	
	
	public Map<String, Serializable> initializeDynastoreWithData(DynastoreInitializer configBuilderConfig)throws ConfigDynaStoreInitializationException {
		Connection con=null;
		PreparedStatement ptst=null;
		ResultSet rs=null;
		String sqlString=configBuilderConfig.getSQLBuilder().getSQLQuery().getValue();
		String mapper=configBuilderConfig.getSQLBuilder().getSQLQuery().getMappedClass();
		//hardcoding as of now till we make changes in the jaxb classes
		String mapKey=configBuilderConfig.getSQLBuilder().getSQLQuery().getUniqueColumn();
		try{
			con=getDataSourceConnection();
			ptst=con.prepareStatement(sqlString);
			rs=ptst.executeQuery();
			return mapData(rs,mapper,mapKey);
		}catch(SQLException | ClassNotFoundException | IOException sqlExp){
			logger.error("SQLBuilder.. SQLException occured during reading ResultSet for sql="+sqlString,sqlExp);
			throw new ConfigDynaStoreInitializationException("SQLBuilder.. SQLException occured during reading ResultSet for for sql="+sqlString,sqlExp);
		}finally{
			CoreDataBaseUtil.dbCleanup(con, ptst, rs);
		}
		
	}
	
	
	private Map<String, Serializable> mapData(ResultSet rs,String mapper,String mapKey) throws ConfigDynaStoreInitializationException{
		logger.debug(".mapData() Mapper IS "+mapper);
		if(mapper.equalsIgnoreCase(MAPPER_MAP_OF_MAP)){
			return mapResultSetToMap(rs,mapKey);
		}else{
			return customMapper(rs,mapper);
		}
	}
	
	private Map<String,Serializable> mapResultSetToMap(ResultSet rs,String key )throws ConfigDynaStoreInitializationException{
		Map<String,Serializable> dataMap=new HashMap();
		try{
			if (rs != null) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int numberOfColumns = rsmd.getColumnCount();
				while(rs.next()){
					Map<String,Serializable> map=new HashMap<String, Serializable>();
					for(int i=0;i<numberOfColumns;i++){
						logger.debug(".mapDataListOfMap() resultSet Index is "+(i+1));
						String columnName=rsmd.getColumnClassName(i+1);
						Object value=rs.getObject(i+1);
						map.put(columnName, (Serializable)value);
						
					}//end of for
					String keyValue=getMapKeyValue(rs,key);
					dataMap.put(keyValue,(Serializable)map);
				}//end of while
				logger.debug(".mapResultSetToMap() generatedListOfMap is "+dataMap);
				return dataMap;
			}//end of if
		}catch(SQLException sqlExp){
			logger.error("SQLBuilder.. SQLException occured during reading ResultSet from List-Of-Map Mapper {List-Of-Map}", sqlExp);
			throw new ConfigDynaStoreInitializationException("SQLBuilder.. SQLException occured during reading ResultSet from List-Of-Map Mapper {List-Of-Map}",sqlExp);
		}
		return null;
	}//end of method
	
	private String getMapKeyValue(ResultSet rs,String key) throws SQLException,ConfigDynaStoreInitializationException{
		Object keyValue=rs.getObject(key);
		if(keyValue==null)
			throw new ConfigDynaStoreInitializationException("SQLBuilder.. key{"+key+"} for map in configuration is not found in the resultset");
		return keyValue.toString();
	}
	
	private Map<String,Serializable> customMapper(ResultSet resultSet,String mapperfqcn) throws ConfigDynaStoreInitializationException{
		
		try {
			IDynaSQLResultSetMapper mapper=getCustomMapper(mapperfqcn);
			if(mapper==null)
				throw new ConfigDynaStoreInitializationException("SQLBuilder.. unable to load the CustomMapper class {"+mapperfqcn+"}");
			
			return mapper.mapSQLResultSet(resultSet);
		} catch (SQLException e) {
			logger.error("SQLBuilder.. SQLException occured during reading ResultSet from Mapper {"+mapperfqcn+"}",e);
			throw new ConfigDynaStoreInitializationException("SQLBuilder.. SQLException occured during reading ResultSet from Mapper {"+mapperfqcn+"}",e);
		}
		
	}
	/**
	 * Looks up and loads the  Mapper Class
	 * @param mapperName
	 * @return
	 */
	private IDynaSQLResultSetMapper getCustomMapper(String mapperName){
		IBeanDependencyResolver beanResolver=BeanDependencyResolverFactory.getBeanDependencyResolver();
		IDynaSQLResultSetMapper mapper=null;
		try {
			mapper = (IDynaSQLResultSetMapper)beanResolver.getBeanInstance(IDynaSQLResultSetMapper.class, mapperName);
		} catch (BeanDependencyResolveException e) {
			//Its ok to eat this exception Here
			e.printStackTrace();
		}
		return mapper;
	}	

	
	
	private Connection getDataSourceConnection() throws ClassNotFoundException, SQLException, IOException{
		return CoreDataBaseUtil.getConnection();
	}
}
