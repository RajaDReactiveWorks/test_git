package org.apache.ddlutils.attunedlabs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.platform.TestPlatform;
import org.apache.ddlutils.platform.mysql.MySql50Builder;

import junit.framework.TestCase;

public class AttunedLabsPartitionTypeTest extends TestCase {

	protected final static Log _log = LogFactory.getLog(AttunedLabsPartitionTypeTest.class);
	public void testToColumnValues() throws IOException
    {
        final String schema =
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n"+
            "  <table name='TestTable'>\n"+
            "    <column name='id' autoIncrement='true' type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='name' type='VARCHAR' size='15'/>\n"+
            "	 <partitionType type='list' columnName='id'> \n" +
            "		<partition name='below_7' values='1,2,3,4,5,6,7' /> \n" +
            "		<partition name='below_11' values='8,9,10,11,12' /> \n" + 
            "		<partition name='above_11' values='default' /> \n" +
            "	 </partitionType>\n" + 
            "  </table>\n"+
            "</database>";

        Database         database = parseDatabaseFromString(schema);
        for(Table table : database.getTables())
        {
        	Platform platform = new TestPlatform();
        	SqlBuilder builder = new MySql50Builder(platform);
        	builder.setDatabase("MYSQL");
        	StringWriter writer = new StringWriter();
        	builder.setWriter(writer);
        	builder.createTable(database, table);
        	String createQuery = builder.getWriter().toString();
        	_log.info(createQuery);
        }
        _log.info(database);

    }
    @SuppressWarnings("deprecation")
	protected Database parseDatabaseFromString(String dbDef)
    {
        DatabaseIO dbIO = new DatabaseIO();
        
        dbIO.setUseInternalDtd(true);
        dbIO.setValidateXml(true);
        return dbIO.read(new StringReader(dbDef));
    }
}
