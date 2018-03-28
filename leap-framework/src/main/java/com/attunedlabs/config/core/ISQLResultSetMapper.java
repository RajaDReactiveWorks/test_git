package com.attunedlabs.config.core;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ISQLResultSetMapper {
	public Serializable mapSQLResultSet(ResultSet resultSet)throws SQLException;
}
