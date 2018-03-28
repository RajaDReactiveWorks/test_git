package org.apache.metamodel.cassandra;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.SimpleTableDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Selection;
import com.google.common.reflect.TypeToken;

/**
 * DataContext implementation for Apache Cassandra database.
 *
 * When instantiating this DataContext, a keyspace name is provided. In
 * Cassandra, the keyspace is the container for your application data, similar
 * to a schema in a relational database. Keyspaces are used to group column
 * families together.
 * 
 * This implementation supports either automatic discovery of a schema or manual
 * specification of a schema, through the {@link SimpleTableDef} class.
 *
 */
public class CassandraCustomDataContext extends CassandraCustomQueryProcessor implements DataContext {

	private static final Logger logger = LoggerFactory.getLogger(CassandraDataContext.class);

	private final Cluster cassandraCluster;
	private final SimpleTableDef[] tableDefs;
	private final String keySpaceName;
	private String tenantId;
	private Session session;
	private Map<String, TypeToken<?>> _typeTokenConverters;

	/**
	 * Constructs a {@link CassandraDataContext}. This constructor accepts a
	 * custom array of {@link SimpleTableDef}s which allows the user to define
	 * his own view on the indexes in the engine.
	 *
	 * @param cluster
	 *            the Cassandra cluster
	 * @param keySpace
	 *            the name of the Cassandra keyspace
	 * @param tableDefs
	 *            an array of {@link SimpleTableDef}s, which define the table
	 *            and column model of the ElasticSearch index.
	 */
	public CassandraCustomDataContext(Cluster cluster, String keySpace, SimpleTableDef... tableDefs) {
		this.cassandraCluster = cluster;
		this.keySpaceName = keySpace;
		this.tableDefs = tableDefs;
		this.session = cassandraCluster.connect();
	}

	/**
	 * Constructs a {@link CassandraDataContext} and automatically detects the
	 * schema structure/view on the keyspace (see
	 * {@link #detectSchema(Cluster, String)}).
	 *
	 * @param cluster
	 *            the Cassandra cluster
	 * @param keySpace
	 *            the name of the Cassandra keyspace to represent
	 */
	public CassandraCustomDataContext(Cluster cluster, String keySpace) {
		this(cluster, keySpace, detectSchema(cluster, keySpace));
	}

	public CassandraCustomDataContext(Cluster cluster, String keySpace, String tenantId) {
		this(cluster, keySpace, detectSchema(cluster, keySpace));
		this.tenantId = tenantId;
	}

	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Performs an analysis of the given keyspace in a Cassandra cluster
	 * {@link Cluster} instance and detects the cassandra types structure based
	 * on the metadata provided by the datastax cassandra java client.
	 *
	 * @see #detectTable(TableMetadata)
	 *
	 * @param cluster
	 *            the cluster to inspect
	 * @param keyspaceName
	 * @return a mutable schema instance, useful for further fine tuning by the
	 *         user.
	 */
	public static SimpleTableDef[] detectSchema(Cluster cluster, String keyspaceName) {
		final Metadata metadata = cluster.getMetadata();
		final KeyspaceMetadata keyspace = metadata.getKeyspace(keyspaceName);
		if (keyspace == null) {
			throw new IllegalArgumentException("Keyspace '" + keyspaceName + "' does not exist in the database");
		}
		final Collection<TableMetadata> tables = keyspace.getTables();
		final SimpleTableDef[] result = new SimpleTableDef[tables.size()];
		int i = 0;
		for (final TableMetadata tableMetaData : tables) {
			final SimpleTableDef table = detectTable(tableMetaData);
			result[i] = table;
			i++;
		}

		return result;
	}

	/**
	 * Performs an analysis of an available table in Cassandra.
	 *
	 * @param tableMetaData
	 *            the table meta data
	 * @return a table definition for cassandra.
	 */
	public static SimpleTableDef detectTable(TableMetadata tableMetaData) {
		final List<ColumnMetadata> columns = tableMetaData.getColumns();
		final String[] columnNames = new String[columns.size()];
		final ColumnType[] columnTypes = new ColumnType[columns.size()];
		int i = 0;
		for (final ColumnMetadata column : columns) {
			columnNames[i] = column.getName();
			columnTypes[i] = getColumnTypeFromMetaDataField(column.getType().getName());
			i++;
		}

		return new SimpleTableDef(tableMetaData.getName(), columnNames, columnTypes);
	}

	@Override
	protected Schema getMainSchema() throws MetaModelException {
		final MutableSchema theSchema = new MutableSchema(getMainSchemaName());
		if (tableDefs != null)
			for (final SimpleTableDef tableDef : tableDefs) {
				final MutableTable table = tableDef.toTable().setSchema(theSchema);

				final TableMetadata cassandraTable = cassandraCluster.getMetadata().getKeyspace(keySpaceName)
						.getTable(table.getName());
				if (cassandraTable != null) {
					final List<ColumnMetadata> primaryKeys = cassandraTable.getPrimaryKey();
					for (ColumnMetadata primaryKey : primaryKeys) {
						final MutableColumn column = (MutableColumn) table.getColumnByName(primaryKey.getName());
						if (column != null) {
							column.setPrimaryKey(true);
						}
						column.setNativeType(primaryKey.getType().getName().name());
					}
				}

				theSchema.addTable(table);
			}
		return theSchema;
	}

	@Override
	protected String getMainSchemaName() throws MetaModelException {
		return keySpaceName;
	}

	@Override
	protected DataSet materializeMainSchemaTable(Table table, List<Column> columns, int maxRows) {
		final Select query = QueryBuilder.select().all().from(keySpaceName, table.getName());
		if (limitMaxRowsIsSet(maxRows)) {
			query.limit(maxRows);
		}
		final ResultSet resultSet = session.execute(query);

		final Iterator<Row> response = resultSet.iterator();
		return new CassandraDataSet(response, columns, _typeTokenConverters);
	}

	@Override
	protected DataSet materializeMainSchemaTableUsingSql(Table table, Column[] columns, String query, int maxRows) {
		final ResultSet resultSet = session.execute(query);
		final Iterator<Row> response = resultSet.iterator();
		CassandraDataSet cs =  new CassandraDataSet(response, Arrays.asList(columns), _typeTokenConverters);

		return cs;
	}

	private boolean limitMaxRowsIsSet(int maxRows) {
		return (maxRows != -1);
	}

	@Override
	protected org.apache.metamodel.data.Row executePrimaryKeyLookupQuery(Table table, List<SelectItem> selectItems,
			Column primaryKeyColumn, Object keyValue) {

		if (primaryKeyColumn.getType() == ColumnType.UUID && keyValue instanceof String) {
			keyValue = UUID.fromString(keyValue.toString());
		}

		Selection select = QueryBuilder.select();
		for (SelectItem selectItem : selectItems) {
			final Column column = selectItem.getColumn();
			assert column != null;
			select = select.column(column.getName());
		}

		final Statement statement = select.from(keySpaceName, table.getName())
				.where(QueryBuilder.eq(primaryKeyColumn.getName(), keyValue));

		final Row row = session.execute(statement).one();

		return CassandraUtils.toRow(row, new SimpleDataSetHeader(selectItems), getTypeTokenConverters());
	}

	@Override
	protected Number executeCountQuery(Table table, List<FilterItem> whereItems, boolean functionApproximationAllowed) {
		if (!whereItems.isEmpty()) {
			// not supported - will have to be done by counting client-side
			logger.debug(
					"Not able to execute count query natively - resorting to query post-processing, which may be expensive");
			return null;
		}
		final Statement statement = QueryBuilder.select().countAll().from(keySpaceName, table.getName());
		final Row response = cassandraCluster.connect().execute(statement).one();
		return response.getLong(0);
	}

	public ResultSet insertIntoTable(String tableName, Map<String, Object> columnNameValuePair) {
		String columnNames = "";
		String values = "";

		for (String columnName : columnNameValuePair.keySet()) {
			columnNames += columnName + ",";
			if (columnNameValuePair.get(columnName) instanceof String) {
				values += "'" + columnNameValuePair.get(columnName) + "',";
			} else {
				values += columnNameValuePair.get(columnName) + ",";
			}
		}

		columnNames = columnNames.substring(0, columnNames.lastIndexOf(","));
		values = values.substring(0, values.lastIndexOf(","));
		return execureRawQuery("INSERT INTO " + this.keySpaceName + "." + tableName + " (" + columnNames + ") "
				+ "VALUES (" + values + ");");
	}

	public ResultSet createTable(String tableName, Map<String, String> columns, List<String> primaryKeyColumnNames,
			boolean dropTableIfExist) throws Exception {
		if (columns.isEmpty()) {
			throw new Exception("No columns Found!!");
		}
		String columnsQuery = "";
		for (String columnName : columns.keySet()) {
			String columnType = columns.get(columnName);
			columnsQuery += columnName + " " + columnType + ",";
		}
		columnsQuery = columnsQuery.substring(0, columnsQuery.lastIndexOf(","));

		if (!primaryKeyColumnNames.isEmpty()) {
			String primaryKeys = "";
			for (String columnName : primaryKeyColumnNames) {
				primaryKeys += columnName + ",";
			}
			primaryKeys = primaryKeys.substring(0, primaryKeys.lastIndexOf(","));
			columnsQuery += ", PRIMARY KEY (" + primaryKeys + ")";
		}

		if (dropTableIfExist) {
			dropTable(tableName);
		}

		return execureRawQuery(
				"CREATE TABLE IF NOT EXISTS " + this.keySpaceName + "." + tableName + " (" + columnsQuery + ");");
	}

	public ResultSet dropTable(String tableName) {
		return execureRawQuery("DROP TABLE IF EXISTS " + this.keySpaceName + "." + tableName + ";");
	}

	public ResultSet deleteRow(String tableName, Map<String, Object> keyValuePairs, List<String> columnsToDelete) {
		String query = "DELETE ";
		if (columnsToDelete != null && !columnsToDelete.isEmpty()) {
			for (String column : columnsToDelete) {
				query += column + " ,";
			}
			query = query.substring(0, query.lastIndexOf(","));
		}

		if (keyValuePairs != null && !keyValuePairs.isEmpty()) {
			query += " WHERE ";
			for (String column : keyValuePairs.keySet()) {
				Object value = keyValuePairs.get(column);
				query += column + " = ";
				if (value instanceof String) {
					query += "'" + value + "' AND ";
				} else {
					query += value + " AND ";
				}
			}
			query = query.substring(0, query.lastIndexOf(" AND "));
		}

		return execureRawQuery(query);
	}

	public ResultSet createKeySpace(String clazz, int reploicationFactor) {
		return execureRawQuery("CREATE KEYSPACE IF NOT EXISTS " + this.keySpaceName + " WITH replication "
				+ "= {'class':'" + clazz + "', 'replication_factor':" + reploicationFactor + "};");
	}

	public ResultSet updateRow(String tableName, Map<String, Object> keyValuePairs, Map<String, Object> condition) {
		String query = "UPDATE " + this.keySpaceName + "." + tableName + " ";

		if (keyValuePairs != null && !keyValuePairs.isEmpty()) {
			query += " SET ";
			for (String column : keyValuePairs.keySet()) {
				Object value = keyValuePairs.get(column);
				query += column + " = ";
				if (value instanceof String) {
					query += "'" + value + "' , ";
				} else {
					query += value + " , ";
				}
			}
			query = query.substring(0, query.lastIndexOf(","));
		}

		if (condition != null && !condition.isEmpty()) {
			query += " WHERE ";
			for (String column : condition.keySet()) {
				Object value = condition.get(column);
				query += column + " = ";
				if (value instanceof String) {
					query += "'" + value + "' AND ";
				} else {
					query += value + " AND ";
				}
			}
			query = query.substring(0, query.lastIndexOf(" AND "));
		}
		return execureRawQuery(query);
	}

	private ResultSet execureRawQuery(String query) {
		return session.execute(query);
	}

	private static ColumnType getColumnTypeFromMetaDataField(DataType.Name metaDataName) {
		switch (metaDataName) {
		case BIGINT:
		case COUNTER:
			return ColumnType.BIGINT;
		case BLOB:
			return ColumnType.BLOB;
		case BOOLEAN:
			return ColumnType.BOOLEAN;
		case DECIMAL:
			return ColumnType.DECIMAL;
		case DOUBLE:
			return ColumnType.DOUBLE;
		case FLOAT:
			return ColumnType.FLOAT;
		case INT:
			return ColumnType.INTEGER;
		case TEXT:
			return ColumnType.STRING;
		case TIMESTAMP:
			return ColumnType.TIMESTAMP;
		case UUID:
			return ColumnType.UUID;
		case VARCHAR:
			return ColumnType.VARCHAR;
		case VARINT:
			return ColumnType.BIGINT;
		case LIST:
			return ColumnType.LIST;
		case MAP:
			return ColumnType.MAP;
		case CUSTOM:
			return ColumnType.OTHER;
		case INET:
			return ColumnType.INET;
		case SET:
			return ColumnType.SET;
		default:
			return ColumnType.STRING;
		}
	}

	public Map<String, TypeToken<?>> getTypeTokenConverters() {
		return _typeTokenConverters;
	}

	public void setTypeTokenConverters(Map<String, TypeToken<?>> typeTokenConverters) {
		this._typeTokenConverters = typeTokenConverters;
	}

}
