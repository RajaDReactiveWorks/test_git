package com.getusroi.integrationfwk.jdbcIntactivity.config.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.w3c.dom.Document;

import com.getusroi.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.getusroi.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityExecutionException;

/**
 * 
 * @author bizruntime
 *
 */
public interface IJdbcIntActivityService {

	public int insertActivityConfigParams(JdbcDataContext datacontext, Table table, Set<String> insertColumnKeySet,
			List<Object> insertListOfValues) throws JdbcIntActivityPersistenceException, JdbcIntActivityExecutionException;
	public int insertActivityConfigParams(UpdateableDataContext updateableDatacontext, Table table, Set<String> insertColumnKeySet,
			List<Object> insertListOfValues,Map<String,String> setOfValuesProcessed, Document xmlDocument) throws JdbcIntActivityExecutionException, JdbcIntActivityConfigurationException;
	public int updateActivityConfigParams(JdbcDataContext datacontext, Table table, Set<String> updateColumnKeySet,
			List<Object> updateListOfValues, Map<String, Map<String, Object>> mapOfConstraints) throws JdbcIntActivityPersistenceException, JdbcIntActivityExecutionException;
	public int updateActivityConfigParamsForCassandra(UpdateableDataContext datacontext, Table table, Set<String> updateColumnKeySet,
			List<Object> updateListOfValues,String whereConstraints,Map<String,String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityPersistenceException, JdbcIntActivityConfigurationException;
	public Row selectActivityConfigParams(DataContext datacontext, Table table1, Table table2,
			List<String> columnSelectKeySet, Map<String, Map<String, Object>> mapOfConstraints, boolean isJoin, String joinType)
			throws JdbcIntActivityPersistenceException;

	public int deleteActivityConfigParams(JdbcDataContext datacontext, Table table, Map<String, Map<String, Object>> mapOfConstraints)
			throws JdbcIntActivityPersistenceException;
	public int deleteActivityConfigParamsForCassandra(UpdateableDataContext datacontext, Table table,
			String whereConstraints,Map<String,String> setOfValuesProcessed, Document xmlDocument)throws JdbcIntActivityPersistenceException, JdbcIntActivityConfigurationException ;

}
