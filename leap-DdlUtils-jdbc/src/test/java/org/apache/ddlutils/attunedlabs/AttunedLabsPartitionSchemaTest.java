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

public class AttunedLabsPartitionSchemaTest extends TestCase {

	protected final static Log _log = LogFactory.getLog(AttunedLabsPartitionSchemaTest.class);
	public void testToColumnValues() throws IOException
    {
        final String schema =
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n"+
            "  <table name='TestTable'>\n"+
            "    <column name='id' autoIncrement='true' type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='name' type='VARCHAR' size='15'/>\n"+
            "	 <partitionType type='hash' columnName='id' partitionCount='2'> \n" + 
            "          <partition tablespace='tablespace1' /> \n" + 
            "          <partition tablespace='tablespace2' /> \n" + 
            "	 </partitionType>\n" + 
            "	 <partitionSchema name='customerpartschema'>\n" + 
            "		<partitionFunction name='customerpartfunction' columnType='int' rangeType='left' values='1,100,1000'>\n" + 
            "			<fileGroup fileGroupName='fg1' fileName='Nfile1' location='C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS\\MSSQL\\DATA\\file1.ndf' size='10MB' maxSize='100MB' fileGrowth='1MB' />\n" + 
            "			<fileGroup fileGroupName='fg2' fileName='Nfile2' location='C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS\\MSSQL\\DATA\\file2.ndf' size='20MB' maxSize='200MB' fileGrowth='2MB' />\n" + 
            "			<fileGroup fileGroupName='fg3' fileName='Nfile3' location='C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS\\MSSQL\\DATA\\file3.ndf' size='30MB' maxSize='30MB' fileGrowth='3MB' />\n" + 
            "			<fileGroup fileGroupName='fg4' fileName='Nfile4' location='C:\\Program Files\\Microsoft SQL Server\\MSSQL14.SQLEXPRESS\\MSSQL\\DATA\\file4.ndf' size='40MB' maxSize='400MB' fileGrowth='4MB' />\n" + 
            "		</partitionFunction>\n" + 
            "	</partitionSchema>" +
            "  </table>\n"+
            "</database>";

        Database         database = parseDatabaseFromString(schema);
        for(Table table : database.getTables())
        {
        	Platform platform = new TestPlatform();
        	SqlBuilder builder = new MySql50Builder(platform);
        	builder.setDatabase("MSSQL");
        	StringWriter writer = new StringWriter();
        	builder.setWriter(writer);
        	builder.createTable(database, table);
        	String createQuery = builder.getWriter().toString();
        	_log.info(createQuery);
        }

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
