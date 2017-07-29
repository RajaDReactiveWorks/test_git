package com.getusroi.permastore.config;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.getusroi.config.core.ISQLResultSetMapper;

/** Test Class for testing PermaStoreSQLResultSetMapper */
public class StageAreaPSSQLResultsetMapper implements ISQLResultSetMapper {

	@Override
	public Serializable mapSQLResultSet(ResultSet resultSet) throws SQLException {
		List<StageArea> al=new ArrayList();
		while(resultSet.next()){
			StageArea stageArea=new StageArea();
			stageArea.setStageAreaId(resultSet.getInt(1));
			stageArea.setAreaType(resultSet.getString(2));
			stageArea.setAreaName(resultSet.getString(3));
			al.add(stageArea);
		}
		return (Serializable)al;
	}

}
