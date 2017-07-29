package com.getusroi.config.core;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.core.BeanDependencyResolveException;
import com.getusroi.core.BeanDependencyResolverFactory;
import com.getusroi.core.CoreDataBaseUtil;
import com.getusroi.core.IBeanDependencyResolver;
import com.getusroi.permastore.config.PermaStoreConfigurationBuilderException;
import com.getusroi.permastore.config.jaxb.SQLBuilder;

/** Generic class that takes the SQL query and get the result and builds the PermaStoreCache */
public class SQLCacheObjectBuilder {
	final Logger logger = LoggerFactory.getLogger(SQLCacheObjectBuilder.class);
	public static final String MAPPER_LISTOFMAP="List-Of-Map";
	public static final String MAPPER_JSON="To-JSON";
	private Connection getDataSourceConnection() throws ClassNotFoundException, SQLException, IOException{
		return CoreDataBaseUtil.getConnection();
	}
	
	public Serializable loadDataForCache(String sqlQuery,String mappedClass) throws SQLCacheBuilderException{
		Connection con=null;
		PreparedStatement ptst=null;
		ResultSet rs=null;
		Serializable obj=null;
		logger.debug(".loadDataForCache() SQL IS "+sqlQuery);
		try{
			con=getDataSourceConnection();
			ptst=con.prepareStatement(sqlQuery);
			rs=ptst.executeQuery();
			obj=mapData(rs,mappedClass);
		}catch(SQLException | ClassNotFoundException | IOException sqlExp){
			logger.error("SQLBuilder.. SQLException occured during reading ResultSet for sql="+sqlQuery,sqlExp);
			throw new SQLCacheBuilderException("SQLBuilder.. SQLException occured during reading ResultSet");
		}finally{
			CoreDataBaseUtil.dbCleanup(con, ptst, rs);
		}
		return obj;
	}
	
	private Serializable mapData(ResultSet rs,String mapper) throws SQLCacheBuilderException{
		logger.debug(".mapData() Mapper IS "+mapper);
		if(mapper.equalsIgnoreCase(MAPPER_LISTOFMAP)){
			return mapDataListOfMap(rs);
		}else{
			return customMapper(rs,mapper);
		}
	}
	
	private Serializable mapDataListOfMap(ResultSet rs )throws SQLCacheBuilderException{
		List<Map> dataList=new ArrayList();
		try{
			if (rs != null) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int numberOfColumns = rsmd.getColumnCount();
				while(rs.next()){
					Map<String,Object> map=new HashMap<String, Object>();
					for(int i=0;i<numberOfColumns;i++){
						logger.debug(".mapDataListOfMap() resultSet Index is "+(i+1));
						String columnName=rsmd.getColumnClassName(i+1);
						Object value=rs.getObject(i+1);
						map.put(columnName, value);
					}//end of for
					dataList.add(map);
				}//end of while
				logger.debug(".mapDataListOfMap() generatedListOfMap is "+dataList);
				return (Serializable)dataList;
			}//end of if
		}catch(SQLException sqlExp){
			logger.error("SQLBuilder.. SQLException occured during reading ResultSet from List-Of-Map Mapper {List-Of-Map}", sqlExp);
			throw new SQLCacheBuilderException("SQLBuilder.. SQLException occured during reading ResultSet from List-Of-Map Mapper {List-Of-Map}",sqlExp);
		}
		return null;
	}//end of method
	
	private Serializable customMapper(ResultSet resultSet,String mapperfqcn) throws SQLCacheBuilderException{
		
		try {
			ISQLResultSetMapper mapper=getCustomMapper(mapperfqcn);
			if(mapper==null)
				throw new SQLCacheBuilderException("SQLBuilder.. unable to load the CustomMapper class {"+mapperfqcn+"}");
			
			return mapper.mapSQLResultSet(resultSet);
		} catch (SQLException e) {
			logger.error("SQLBuilder.. SQLException occured during reading ResultSet from Mapper {"+mapperfqcn+"}",e);
			throw new SQLCacheBuilderException("SQLBuilder.. SQLException occured during reading ResultSet from Mapper {"+mapperfqcn+"}",e);
		}
	}
	/**
	 * Looks up and loads the  Mapper Class
	 * @param mapperName
	 * @return
	 */
	private ISQLResultSetMapper getCustomMapper(String mapperName){
		IBeanDependencyResolver beanResolver=BeanDependencyResolverFactory.getBeanDependencyResolver();
		ISQLResultSetMapper mapper=null;
		try {
			mapper = (ISQLResultSetMapper)beanResolver.getBeanInstance(ISQLResultSetMapper.class, mapperName);
		} catch (BeanDependencyResolveException e) {
			//Its ok to eat this exception Here
			e.printStackTrace();
		}
		return mapper;
	}
}
