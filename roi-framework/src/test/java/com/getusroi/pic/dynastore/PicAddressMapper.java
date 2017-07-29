package com.getusroi.pic.dynastore;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.dynastore.config.IDynaSQLResultSetMapper;
import com.getusroi.dynastore.config.impl.DynaSQLCacheObjectBuilder;

public class PicAddressMapper implements IDynaSQLResultSetMapper{
	final Logger logger = LoggerFactory.getLogger(DynaSQLCacheObjectBuilder.class);
	public PicAddressMapper() {
		
	}
	
	public Map<String, Serializable> mapSQLResultSet(ResultSet rs) throws SQLException {
		Map<String,Serializable> dataMap=new HashMap();
			if (rs != null) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int numberOfColumns = rsmd.getColumnCount();
				while(rs.next()){
					Map<String,Serializable> map=new HashMap<String, Serializable>();
					String key=null;
					for(int i=0;i<numberOfColumns;i++){
						logger.debug(".mapDataListOfMap() resultSet Index is "+(i+1));
						String columnName=rsmd.getColumnClassName(i+1);
						Object value=rs.getObject(i+1);
						if(i==0)
							key=value.toString();
						map.put(columnName, (Serializable)value);
						
					}//end of for
					String keyValue=key;
					dataMap.put(keyValue,(Serializable)map);
				}//end of while
				logger.debug(".mapResultSetToMap() generatedListOfMap is "+dataMap);
				return dataMap;
			}//end of if
		return dataMap;
	}

}
