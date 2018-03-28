package org.apache.ddlutils.platform;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.io.XMLUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.File;
import org.apache.ddlutils.model.FileGroup;
import org.apache.ddlutils.model.Partition;
import org.apache.ddlutils.model.PartitionFunction;
import org.apache.ddlutils.model.PartitionSchema;
import org.apache.ddlutils.model.PartitionType;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TableSpace;

public class AttunedLabsSqlBuilder extends SqlBuilder
{

	public AttunedLabsSqlBuilder(Platform platform) {
		super(platform);
	}

	/**
	 * Adds the support of partition in the given table for msSql database. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	protected static void writeMsSqlCreatePartition(Table table, Database database) throws IOException {
		PartitionFunction partitionFunction = table.getPartitionSchema().getPartitionFunction();
		int valueSize = (partitionFunction.getValues().trim().split(",")).length;
		int fileGroupsize = partitionFunction.getFileGroup().size();
		if (valueSize + 1 == fileGroupsize) {
			writeEmbeddedFileGroupStmt(table, database);
			writeEmbeddedFileStmt(table, database);
			writeEmbeddedPartitionFunctionStmt(table, database);
			writeEmbeddedPartitionSchemaStmt(table, database);
		} else {
			throw new IOException("Number of values and fileGroup specified does not match the requirement.");
		}
	}
	
	/**
	 * Adds the support of partition in the given table to add fileGroup in
	 * database. Added by AttunedLabs to support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	private static void writeEmbeddedFileGroupStmt(Table table, Database database) throws IOException {
		List<FileGroup> fileGroups = table.getPartitionSchema().getPartitionFunction().getFileGroup();
		for (FileGroup fileGroup : fileGroups) {
			println("IF NOT ( EXISTS( SELECT * FROM sys.filegroups WHERE sys.filegroups.name = '" + fileGroup.getFileGroupName().trim() + "'))");
			println("BEGIN");
			println("ALTER DATABASE " + database.getName().trim());
			println("ADD FILEGROUP " + fileGroup.getFileGroupName().trim());
			println("END;");
		}
	}

	/**
	 * Adds the support of partition in the given table to add file in database.
	 * Added by AttunedLabs to support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	private static void writeEmbeddedFileStmt(Table table, Database database) throws IOException {
		List<FileGroup> fileGroups = table.getPartitionSchema().getPartitionFunction().getFileGroup();
		for (FileGroup fileGroup : fileGroups) {
			println("IF NOT ( EXISTS( SELECT * FROM sys.database_files where sys.database_files.data_space_id "
					+ "= (SELECT sys.filegroups.data_space_id FROM sys.filegroups where sys.filegroups.name = '"
					+ fileGroup.getFileGroupName().trim() +"') and sys.database_files.name = '"+ fileGroup.getFileName() +"'))");
			println("BEGIN");
			println("ALTER DATABASE " + database.getName().trim());
			println("ADD FILE ");
			println("(");
			println("    NAME = " + fileGroup.getFileName().trim() + ",");
			println("    FILENAME = '" + fileGroup.getLocation().trim() + "',");
			println("    SIZE = " + fileGroup.getSize().trim() + ",");
			println("    MAXSIZE = " + fileGroup.getMaxSize().trim() + ",");
			println("    FILEGROWTH = " + fileGroup.getFileGrowth().trim());
			println(")");
			println("TO FILEGROUP " + fileGroup.getFileGroupName().trim());
			println("END;");
		}
	}

	/**
	 * Adds the support of partition in the given table to add partition function in
	 * database. Added by AttunedLabs to support partitioning
	 * 
	 * @param table
	 * @throws IOException
	 */
	private static void writeEmbeddedPartitionFunctionStmt(Table table, Database database) throws IOException {
		PartitionFunction partitionFunction = table.getPartitionSchema().getPartitionFunction();
		println("IF NOT ( EXISTS( SELECT * FROM sys.partition_functions WHERE name = '" + partitionFunction.getName().trim() + "'))");
		println("BEGIN");
		println("CREATE PARTITION FUNCTION " + partitionFunction.getName().trim() + " ("
				+ partitionFunction.getColumnType().trim() + ")");
		print("    AS RANGE ");
		if (partitionFunction.getRangeType().trim().equalsIgnoreCase("LEFT"))
			print("LEFT ");
		if (partitionFunction.getRangeType().trim().equalsIgnoreCase("RIGHT"))
			print("RIGHT ");
		println("FOR VALUES (" + partitionFunction.getValues().trim() + ")");
		println("END;");
	}

	/**
	 * Adds the support of partition in the given table to add partition schema in
	 * database. Added by AttunedLabs to support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	private static void writeEmbeddedPartitionSchemaStmt(Table table, Database database) throws IOException {
		PartitionSchema partitionSchema = table.getPartitionSchema();
		List<FileGroup> fileGroups = partitionSchema.getPartitionFunction().getFileGroup();
		println("IF NOT ( EXISTS( SELECT * FROM sys.partition_schemes WHERE name = '" + partitionSchema.getName().trim() + "'))");
		println("BEGIN");
		println("CREATE PARTITION SCHEME " + partitionSchema.getName().trim());
		println("    AS PARTITION " + partitionSchema.getPartitionFunction().getName().trim());
		print("    TO (");
		for (int idx = 0; idx < fileGroups.size(); idx++) {
			print(fileGroups.get(idx).getFileGroupName().trim());
			if (idx != fileGroups.size() - 1) {
				print(", ");
			}
		}
		println(")");
		println("END;");
	}

	/**
	 * Adds the support of partition in the given table. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param table
	 * @throws IOException
	 */
	protected static void writeEmbeddedPartitionKeysStmt(Table table, String database) throws IOException {
		_log.debug("database name : " + database);

		PartitionType partitionType = table.getPartitionType();
		String columnName = (partitionType.getColumnName() == null || partitionType.getColumnName().length() == 0) ? ""
				: partitionType.getColumnName();
		String partType = partitionType.getType();
		if (partType.trim().equalsIgnoreCase("range") && database.equalsIgnoreCase("MsSql")) 
		{
			println();
			print("    ON " + partitionType.getSchema() + " (" + partitionType.getColumnName() + ")");
		} 
		else if (partType.trim().equalsIgnoreCase("range") || partType.trim().equalsIgnoreCase("range columns")) 
		{
			writeRangePartitionQuery(table, database);
		}
		else if (partType.trim().equalsIgnoreCase("list") || partType.trim().equalsIgnoreCase("list columns")) 
		{
			writeListPartitionQuery(table, database);
		}
		else if ((partType.trim().equalsIgnoreCase("hash") || partType.trim().equalsIgnoreCase("linear hash"))) 
		{
			writeHashPartitionQuery(table, database);
		}
		else if ((partType.trim().equalsIgnoreCase("key") || partType.trim().equalsIgnoreCase("linear key"))
				&& database.equalsIgnoreCase("MYSQL")) 
		{
			print("PARTITION BY ");

			if (partType.trim().equalsIgnoreCase("linear key"))
				print("LINEAR ");
			println("KEY (" + columnName + ") ");
			print("    PARTITIONS " + partitionType.getPartitionCount());
		}

		_log.debug("sql builder is finished");
	}

	/**
	 * Reads the Partition Type and creates the Hash partition query. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	private static void writeHashPartitionQuery(Table table, String database) throws IOException {
		PartitionType partitionType = table.getPartitionType();
		List<Partition> partitions = partitionType.getPartitions();
		String columnName = (partitionType.getColumnName() == null || partitionType.getColumnName().length() == 0) ? ""
				: partitionType.getColumnName();
		String partType = partitionType.getType();
		print("PARTITION BY ");

		if (partType.trim().equalsIgnoreCase("linear hash"))
			print("LINEAR ");
		println("HASH (" + columnName.trim() + ") ");
		if (partitions.size() == 0)
			print("    PARTITIONS " + partitionType.getPartitionCount());
		else {
			Partition partition = partitions.get(0);
			if (XMLUtils.isEmpty(partition.getName()) && database.equalsIgnoreCase("ORACLE")) {
				println("    PARTITIONS " + partitionType.getPartitionCount());
				print("    STORE IN (");
				for (int idx = 0; idx < partitions.size(); idx++) {
					print(partitions.get(idx).getTablespace().toUpperCase());
					if (idx + 1 < partitions.size())
						print(",");
				}
				print(")");
			} else if (!XMLUtils.isEmpty(partition.getName()) && !XMLUtils.isEmpty(partition.getTablespace())
					&& database.equalsIgnoreCase("ORACLE")) {
				println("(");
				for (int idx = 0; idx < partitions.size(); idx++) {
					print("    PARTITION " + partitions.get(idx).getName() + " TABLESPACE "
							+ partitions.get(idx).getTablespace().toUpperCase());
					if (idx + 1 < partitions.size())
						println(",");
				}
				print(")");
			}
		}
		
	}

	/**
	 * Reads the Partition Type and creates the List partition query. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	private static void writeListPartitionQuery(Table table, String database) throws IOException {
		PartitionType partitionType = table.getPartitionType();
		List<Partition> partitions = partitionType.getPartitions();
		String columnName = (partitionType.getColumnName() == null || partitionType.getColumnName().length() == 0) ? ""
				: partitionType.getColumnName();
		String[] splitedColumns = columnName.split(",");
		String[] columnTypes = new String[splitedColumns.length];
		String partType = partitionType.getType();
		print("PARTITION BY ");

		if (partType.trim().equalsIgnoreCase("list columns") && database.equalsIgnoreCase("MYSQL"))
			println("LIST COLUMNS (" + columnName.trim() + ") (");
		else
			println("LIST (" + columnName.trim() + ") (");

		for (int index = 0; index < partitions.size(); index++) {
			Partition partition = partitions.get(index);
			String value = partition.getValues();
			Column[] columns = table.getColumns();
			String columnType = "";
			for (int idx = 0; idx < columns.length; idx++) {
				Column column = columns[idx];
				for (int i = 0; i < splitedColumns.length; i++) {
					if (splitedColumns[i].equalsIgnoreCase(column.getName()))
						columnTypes[i] = column.getType();
				}
			}
			_log.debug("columns : " + Arrays.toString(columns));
			print("    PARTITION " + partition.getName() + " VALUES ");
			if (database.equalsIgnoreCase("MYSQL"))

				print("IN ");

			if (value.equalsIgnoreCase("DEFAULT") && database.equalsIgnoreCase("Oracle") && index > 0) {
				print(" (DEFAULT)");
			} else {
				print("(");
				String[] values = value.split(",");
				for (int idx = 0; idx < values.length; idx++) {
					String eachValue = values[idx];
					if (columnType.equalsIgnoreCase("CHAR") || columnType.equalsIgnoreCase("VARCHAR")
							|| columnType.equalsIgnoreCase("BINARY") || columnType.equalsIgnoreCase("VARBINARY")
							|| columnType.equalsIgnoreCase("BLOB") || columnType.equalsIgnoreCase("TEXT")
							|| columnType.equalsIgnoreCase("ENUM") || columnType.equalsIgnoreCase("SET")
							|| columnType.equalsIgnoreCase("DATE") || columnType.equalsIgnoreCase("TIME")
							|| columnType.equalsIgnoreCase("DATETIME") || columnType.equalsIgnoreCase("TIMESTAMP")
							|| columnType.equalsIgnoreCase("YEAR"))
						if (!eachValue.startsWith("TO_DATE")) {
							print("'" + eachValue + "'");
						} else
							print(eachValue);
					else
						print(eachValue);
					if (idx + 1 < values.length)
						print(",");
				}

				print(")");
				if (index + 1 < partitions.size())
					println(",");
				else
					println();
			}
		}

		println(")");		
	}

	/**
	 * Reads the Partition Type and creates the Range partition query. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	private static void writeRangePartitionQuery(Table table, String database) throws IOException {
		PartitionType partitionType = table.getPartitionType();
		List<Partition> partitions = partitionType.getPartitions();
		String columnName = (partitionType.getColumnName() == null || partitionType.getColumnName().length() == 0) ? ""
				: partitionType.getColumnName();
		String[] splitedColumns = columnName.split(",");
		String[] columnTypes = new String[splitedColumns.length];
		String partType = partitionType.getType();
		print("PARTITION BY ");

	if (partType.trim().equalsIgnoreCase("range columns") && database.equalsIgnoreCase("MYSQL"))
		println("RANGE COLUMNS (" + columnName.trim() + ") (");
	else
		println("RANGE (" + columnName.trim() + ") (");

	_log.debug("range");
	for (int index = 0; index < partitions.size(); index++) {
		Partition partition = partitions.get(index);
		String value = partition.getValues();
		Column[] columns = table.getColumns();
		_log.debug(" partitions.size() " +  partitions.size());
		for (int idx = 0; idx < columns.length; idx++) {
			Column column = columns[idx];
			_log.debug("columns.length" +  columns.length);
			for (int i = 0; i < splitedColumns.length; i++) {
				if (splitedColumns[i].equalsIgnoreCase(column.getName()))
					columnTypes[i] = column.getType();
			}
		}
		_log.debug("columnTypes : " + Arrays.toString(columnTypes));
		print("    PARTITION " + partition.getName() + " VALUES LESS THAN");
		if (value.equalsIgnoreCase("MAXVALUE") && index > 0) {
			if (database.equalsIgnoreCase("MYSQL"))
				println(" MAXVALUE");
			else if (database.equalsIgnoreCase("ORACLE"))
				println(" (MAXVALUE)");
		} else {
			print(" (");
			String[] values = value.split(",");
			for (int idx = 0; idx < values.length; idx++) {
				String eachValue = values[idx];
				String columnType = columnTypes[idx];
				if (columnType.equalsIgnoreCase("CHAR") || columnType.equalsIgnoreCase("VARCHAR")
						|| columnType.equalsIgnoreCase("BINARY") || columnType.equalsIgnoreCase("VARBINARY")
						|| columnType.equalsIgnoreCase("BLOB") || columnType.equalsIgnoreCase("TEXT")
						|| columnType.equalsIgnoreCase("ENUM") || columnType.equalsIgnoreCase("SET")
						|| columnType.equalsIgnoreCase("DATE") || columnType.equalsIgnoreCase("TIME")
						|| columnType.equalsIgnoreCase("DATETIME") || columnType.equalsIgnoreCase("TIMESTAMP")
						|| columnType.equalsIgnoreCase("YEAR"))
					if (!eachValue.startsWith("TO_DATE"))
						print("'" + eachValue + "'");
					else
						print(eachValue);
				else
					print(eachValue);
				if (idx + 1 < values.length)
					print(",");
			}
			print(")");
			if(!XMLUtils.isEmpty(partition.getTablespace()))
				print(" TABLESPACE " + partition.getTablespace().toUpperCase());
			_log.debug("range , addition");
			if (index + 1 < partitions.size())
				println(",");
			else
				println();
		}
	}
	_log.debug("range completed");
	println(")");
}

	/**
	 * Reads the TableSpaces and creates the tableSpace query. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param table
	 * @param database
	 * @throws IOException
	 */
	protected static void tableSpacesQueryBuilder(Table table, Database database) throws IOException {
		List<TableSpace> tableSpaces = table.getTableSpaces();
		for (TableSpace tableSpace : tableSpaces) {

			String tableSpacName = tableSpace.getName().trim().toUpperCase();
			List<File> files = tableSpace.getFile();
			
			printTSln("DECLARE");
			printTSln("nCount NUMBER;");
			printTSln("createSQL LONG;");
			printTSln("alterSQL LONG;");
			printTSln("BEGIN");
			printTSln("SELECT count(*) into nCount FROM dba_tablespaces WHERE tablespace_name = '" + tableSpacName + "';");
			printTSln("IF(nCount = 0)");
			printTSln("THEN");
			printTSln("createSQL:='");
			int length = files.size();
			_log.debug("start");
			if (XMLUtils.isEmpty(tableSpace.getType()) || tableSpace.getType().equalsIgnoreCase("SMALLFILE")) {
				_log.debug("start null or small file");

				if(XMLUtils.isEmpty(tableSpace.getType()))
					printTSln("CREATE TABLESPACE " + tableSpacName);
				else if(tableSpace.getType().equalsIgnoreCase("SMALLFILE"))
					printTSln("CREATE SMALLFILE TABLESPACE " + tableSpacName);
				printTS(" DATAFILE ");
				for (int i = 0; i < length; i++) {
					File fileAttr = files.get(i);
					addFileAttributes(fileAttr);
					if (i != length - 1) {
						printTSln(",");
					}
				}
			} else if(tableSpace.getType().equalsIgnoreCase("BIGFILE")){
				_log.debug("start big file");
				printTSln("CREATE BIGFILE TABLESPACE " + tableSpacName);
				printTS(" DATAFILE ");
				File fileAttr = files.get(0);
				addFileAttributes(fileAttr);
//				printTSln(",");
			}
			_log.debug("block");

			printTSln("';");
			printTSln("EXECUTE IMMEDIATE createSQL;");
			printTSln("ELSIF (nCount = 1)");
			printTSln("THEN");
			printTSln("alterSQL:='ALTER TABLESPACE " + tableSpacName );
			printTSln(" ADD DATAFILE ");
			File fileAttr = files.get(0);
			addFileAttributes(fileAttr);
			
			
			printTSln("';");
			printTSln("EXECUTE IMMEDIATE alterSQL;");
			printTSln("END IF;");
			printTSln("END;");
			printTSln(";;");
		}
		
	}

	/**
	 * Reads the file object and creates the query. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param file
	 * @throws IOException
	 */
	private static void addFileAttributes(File fileAttr) throws IOException {
		if(!XMLUtils.isEmpty(fileAttr.getDirectory()))
		{
			printTSln("''" + fileAttr.getDirectory() + "/" + fileAttr.getName() + "'' SIZE " + fileAttr.getSize());
		}
		else
		{
			printTSln("''" + fileAttr.getName() + "'' SIZE " + fileAttr.getSize());
		}
		if (!XMLUtils.isEmpty(fileAttr.getReuse())) {
			printTSln(" REUSE");
		}
		if (!XMLUtils.isEmpty(fileAttr.getAutoExtendNextSize())) {
			printTS(" AUTOEXTEND ON NEXT " + fileAttr.getAutoExtendNextSize());
		}
		if (!XMLUtils.isEmpty(fileAttr.getMaxSize())) {
			printTSln(" MAXSIZE " + fileAttr.getMaxSize());
		}
	}
}
