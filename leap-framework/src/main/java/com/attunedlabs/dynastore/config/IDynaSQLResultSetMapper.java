package com.attunedlabs.dynastore.config;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface IDynaSQLResultSetMapper {
	public Map<String,Serializable> mapSQLResultSet(ResultSet resultSet)throws SQLException;
}
