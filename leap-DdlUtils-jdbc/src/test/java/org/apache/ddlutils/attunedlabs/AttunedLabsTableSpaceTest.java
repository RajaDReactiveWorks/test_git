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
import org.apache.ddlutils.platform.oracle.Oracle10Builder;

import junit.framework.TestCase;

public class AttunedLabsTableSpaceTest extends TestCase {

	protected final static Log _log = LogFactory.getLog(AttunedLabsTableSpaceTest.class);
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
            "	 <tableSpaces>\n" + 
            "       <tableSpace name='tablespace1'>\n" + 
            "          	<file name='tbs_perm_01.dat' directory='' size='10M'\n" + 
            "          		autoExtendNextSize='10M' maxSize='50M' />\n" + 
            "          </tableSpace>\n" + 
            "          <tableSpace name='tablespace2' type='SMALLFILE'>\n" + 
            "          	<file name='tbs_perm_02.dat' directory='' size='10M'\n" + 
            "          		autoExtendNextSize='10M' maxSize='50M' />\n" + 
            "       </tableSpace>\n" + 
            "	 </tableSpaces> \n" +
            "  </table>\n"+
            "</database>";

        Database         database = parseDatabaseFromString(schema);
        for(Table table : database.getTables())
        {
        	Platform platform = new TestPlatform();
        	SqlBuilder builder = new Oracle10Builder(platform);
        	StringWriter tableSpaceWriter = new StringWriter();
        	builder.setTableSpaceWriter(tableSpaceWriter);
        	String writeEmbeddedTableSpaceStmt = builder.writeEmbeddedTableSpaceStmt(table, database);
        	_log.info(writeEmbeddedTableSpaceStmt);
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
