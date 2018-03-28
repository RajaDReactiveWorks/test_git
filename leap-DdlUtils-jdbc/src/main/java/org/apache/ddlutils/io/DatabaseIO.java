package org.apache.ddlutils.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.model.CascadeActionEnum;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.FileGroup;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Partition;
import org.apache.ddlutils.model.PartitionFunction;
import org.apache.ddlutils.model.PartitionSchema;
import org.apache.ddlutils.model.PartitionType;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TableSpace;
import org.apache.ddlutils.model.UniqueIndex;
import org.xml.sax.InputSource;

/**
 * This class provides functions to read and write database models from/to XML.
 * 
 * @version $Revision$
 */
public class DatabaseIO {
	/**
	 * The name of the XML attribute use to denote that teh content of a data XML
	 * element uses Base64 encoding.
	 */
	public static final String BASE64_ATTR_NAME = "base64";

	/** The namespace used by DdlUtils. */
	public static final String DDLUTILS_NAMESPACE = "http://db.apache.org/ddlutils/schema/1.1";

	/** Qualified name of the column element. */
	public static final QName QNAME_ELEMENT_COLUMN = new QName(DDLUTILS_NAMESPACE, "column");
	/** Qualified name of the database element. */
	public static final QName QNAME_ELEMENT_DATABASE = new QName(DDLUTILS_NAMESPACE, "database");
	/** Qualified name of the foreign-key element. */
	public static final QName QNAME_ELEMENT_FOREIGN_KEY = new QName(DDLUTILS_NAMESPACE, "foreign-key");
	/** Qualified name of the index element. */
	public static final QName QNAME_ELEMENT_INDEX = new QName(DDLUTILS_NAMESPACE, "index");
	/** Qualified name of the index-column element. */
	public static final QName QNAME_ELEMENT_INDEX_COLUMN = new QName(DDLUTILS_NAMESPACE, "index-column");
	/** Qualified name of the reference element. */
	public static final QName QNAME_ELEMENT_REFERENCE = new QName(DDLUTILS_NAMESPACE, "reference");
	/** Qualified name of the table element. */
	public static final QName QNAME_ELEMENT_TABLE = new QName(DDLUTILS_NAMESPACE, "table");
	/** Qualified name of the unique element. */
	public static final QName QNAME_ELEMENT_UNIQUE = new QName(DDLUTILS_NAMESPACE, "unique");
	/** Qualified name of the unique-column element. */
	public static final QName QNAME_ELEMENT_UNIQUE_COLUMN = new QName(DDLUTILS_NAMESPACE, "unique-column");
	/** Qualified name of the partition element. */
	public static final QName QNAME_ELEMENT_PARTITION_TYPE = new QName(DDLUTILS_NAMESPACE, "partitionType");
	/** Qualified name of the partition element. */
	public static final QName QNAME_ELEMENT_PARTITION = new QName(DDLUTILS_NAMESPACE, "partition");
	/** Qualified name of the partition element. */
	public static final QName QNAME_ELEMENT_PARTITION_SCHEMA = new QName(DDLUTILS_NAMESPACE, "partitionSchema");
	/** Qualified name of the partition element. */
	public static final QName QNAME_ELEMENT_PARTITION_FUNCTION = new QName(DDLUTILS_NAMESPACE, "partitionFunction");
	/** Qualified name of the Table spaces element. */
	public static final QName QNAME_ELEMENT_TABLESPACES = new QName(DDLUTILS_NAMESPACE, "tableSpaces");
	/** Qualified name of the Table space element. */
	public static final QName QNAME_ELEMENT_TABLESPACE = new QName(DDLUTILS_NAMESPACE, "tableSpace");
	/** Qualified name of the file element. */
	public static final QName QNAME_ELEMENT_FILE = new QName(DDLUTILS_NAMESPACE, "file");
	/** Qualified name of the defaultParam element. */
	public static final QName QNAME_ELEMENT_DEFAULT_PARAMS = new QName(DDLUTILS_NAMESPACE, "defaultParams");
	/** Qualified name of the fileGroup element. */
	public static final QName QNAME_ELEMENT_FILE_GROUP = new QName(DDLUTILS_NAMESPACE, "fileGroup");
	
	
	/** Qualified name of the autoIncrement attribute. */
	public static final QName QNAME_ATTRIBUTE_AUTO_INCREMENT = new QName(DDLUTILS_NAMESPACE, "autoIncrement");
	/** Qualified name of the default attribute. */
	public static final QName QNAME_ATTRIBUTE_DEFAULT = new QName(DDLUTILS_NAMESPACE, "default");
	/** Qualified name of the defaultIdMethod attribute. */
	public static final QName QNAME_ATTRIBUTE_DEFAULT_ID_METHOD = new QName(DDLUTILS_NAMESPACE, "defaultIdMethod");
	/** Qualified name of the description attribute. */
	public static final QName QNAME_ATTRIBUTE_DESCRIPTION = new QName(DDLUTILS_NAMESPACE, "description");
	/** Qualified name of the foreign attribute. */
	public static final QName QNAME_ATTRIBUTE_FOREIGN = new QName(DDLUTILS_NAMESPACE, "foreign");
	/** Qualified name of the foreignTable attribute. */
	public static final QName QNAME_ATTRIBUTE_FOREIGN_TABLE = new QName(DDLUTILS_NAMESPACE, "foreignTable");
	/** Qualified name of the javaName attribute. */
	public static final QName QNAME_ATTRIBUTE_JAVA_NAME = new QName(DDLUTILS_NAMESPACE, "javaName");
	/** Qualified name of the local attribute. */
	public static final QName QNAME_ATTRIBUTE_LOCAL = new QName(DDLUTILS_NAMESPACE, "local");
	/** Qualified name of the name attribute. */
	public static final QName QNAME_ATTRIBUTE_NAME = new QName(DDLUTILS_NAMESPACE, "name");
	/** Qualified name of the onDelete attribute. */
	public static final QName QNAME_ATTRIBUTE_ON_DELETE = new QName(DDLUTILS_NAMESPACE, "onDelete");
	/** Qualified name of the onUpdate attribute. */
	public static final QName QNAME_ATTRIBUTE_ON_UPDATE = new QName(DDLUTILS_NAMESPACE, "onUpdate");
	/** Qualified name of the primaryKey attribute. */
	public static final QName QNAME_ATTRIBUTE_PRIMARY_KEY = new QName(DDLUTILS_NAMESPACE, "primaryKey");
	/** Qualified name of the required attribute. */
	public static final QName QNAME_ATTRIBUTE_REQUIRED = new QName(DDLUTILS_NAMESPACE, "required");
	/** Qualified name of the size attribute. */
	public static final QName QNAME_ATTRIBUTE_SIZE = new QName(DDLUTILS_NAMESPACE, "size");
	/** Qualified name of the type attribute. */
	public static final QName QNAME_ATTRIBUTE_TYPE = new QName(DDLUTILS_NAMESPACE, "type");
	/** Qualified name of the version attribute. */
	public static final QName QNAME_ATTRIBUTE_VERSION = new QName(DDLUTILS_NAMESPACE, "version");
	/** Qualified name of the columnName AttunedLabs attribute. */
	public static final QName QNAME_ATTRIBUTE_COLUMN_NAME = new QName(DDLUTILS_NAMESPACE, "columnName");
	/** Qualified name of the partitionCount AttunedLabs attribute. */
	public static final QName QNAME_ATTRIBUTE_PARTITION_COUNT = new QName(DDLUTILS_NAMESPACE, "partitionCount");
	/** Qualified name of the values AttunedLabs attribute. */
	public static final QName QNAME_ATTRIBUTE_VALUES = new QName(DDLUTILS_NAMESPACE, "values");
	/** Qualified name of the partition AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_PARTITION_SUPPORTED = new QName(DDLUTILS_NAMESPACE, "isPartitionable");
	/** Qualified name of the tablespace AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_TABLE_SPACE = new QName(DDLUTILS_NAMESPACE, "tablespace");
	/** Qualified name of the schema AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_SCHEMA = new QName(DDLUTILS_NAMESPACE, "schema");
	/** Qualified name of the column type AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_COLUMN_TYPE = new QName(DDLUTILS_NAMESPACE, "columnType");
	/** Qualified name of the range type AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_RANGE_TYPE = new QName(DDLUTILS_NAMESPACE, "rangeType");
	/** Qualified name of the file group name AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_FILE_GROUP_NAME = new QName(DDLUTILS_NAMESPACE, "fileGroupName");
	/** Qualified name of the file name AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_FILE_NAME = new QName(DDLUTILS_NAMESPACE, "fileName");
	/** Qualified name of the location AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_LOCATION = new QName(DDLUTILS_NAMESPACE, "location");
	/** Qualified name of the maxSize AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_MAX_SIZE = new QName(DDLUTILS_NAMESPACE, "maxSize");
	/** Qualified name of the fileGrowth AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_FILE_GROWTH = new QName(DDLUTILS_NAMESPACE, "fileGrowth");	
	/** Qualified name of the blockSize AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_BLOCK_SIZE = new QName(DDLUTILS_NAMESPACE, "blockSize");	
	/** Qualified name of the logging AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_LOGGING = new QName(DDLUTILS_NAMESPACE, "logging");	
	/** Qualified name of the logging AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_FORCE_LOGGING = new QName(DDLUTILS_NAMESPACE, "forceLogging");	
	/** Qualified name of the status AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_STATUS = new QName(DDLUTILS_NAMESPACE, "status");	
	/** Qualified name of the segmentSpaceManagement AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_SEGMENTSPACE_MANAGEMENT = new QName(DDLUTILS_NAMESPACE, "segmentSpaceManagement");	
	/** Qualified name of the extentManagement AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_EXTENT_MANAGEMENT = new QName(DDLUTILS_NAMESPACE, "extentManagement");	
	/** Qualified name of the extentManagementUniformSize AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_EXTENT_MANAGEMENT_UNIFORM_SIZE = new QName(DDLUTILS_NAMESPACE, "extentManagementUniformSize");	
	/** Qualified name of the directory AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_DIRECTORY = new QName(DDLUTILS_NAMESPACE, "directory");	
	/** Qualified name of the reuse AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_REUSE = new QName(DDLUTILS_NAMESPACE, "reuse");	
	/** Qualified name of the autoExtendNextSize AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_AUTO_EXTEND_NEXT_SIZE = new QName(DDLUTILS_NAMESPACE, "autoExtendNextSize");		
	/** Qualified name of the tableCompression AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_TABLE_COMPRESSION = new QName(DDLUTILS_NAMESPACE, "tableCompression");	
	/** Qualified name of the compression AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_COMPRESSION = new QName(DDLUTILS_NAMESPACE, "compression");	
	/** Qualified name of the priority AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_PRIORITY = new QName(DDLUTILS_NAMESPACE, "priority");	
	/** Qualified name of the distribute AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_DISTRIBUTE = new QName(DDLUTILS_NAMESPACE, "distribute");	
	/** Qualified name of the duplicate AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_DUPLICATE = new QName(DDLUTILS_NAMESPACE, "duplicate");	
	/** Qualified name of the inMemory AttunedLabs support. */
	public static final QName QNAME_ATTRIBUTE_IN_MEMORY = new QName(DDLUTILS_NAMESPACE, "inMemory");	

	/** The log. */
	private final Log _log = LogFactory.getLog(DatabaseIO.class);

	/** Whether to validate the XML. */
	private boolean _validateXml = true;
	/** Whether to use the internal dtd that comes with DdlUtils. */
	private boolean _useInternalDtd = true;

	/**
	 * Returns whether XML is validated upon reading it.
	 * 
	 * @return <code>true</code> if read XML is validated
	 */
	public boolean isValidateXml() {
		return _validateXml;
	}

	/**
	 * Specifies whether XML shall be validated upon reading it.
	 * 
	 * @param validateXml
	 *            <code>true</code> if read XML shall be validated
	 */
	public void setValidateXml(boolean validateXml) {
		_validateXml = validateXml;
	}

	/**
	 * Returns whether the internal dtd that comes with DdlUtils is used.
	 * 
	 * @return <code>true</code> if parsing uses the internal dtd
	 * @deprecated Switched to XML schema, and the internal XML schema should always
	 *             be used
	 */
	public boolean isUseInternalDtd() {
		return _useInternalDtd;
	}

	/**
	 * Specifies whether the internal dtd is to be used.
	 *
	 * @param useInternalDtd
	 *            Whether to use the internal dtd
	 * @deprecated Switched to XML schema, and the internal XML schema should always
	 *             be used
	 */
	public void setUseInternalDtd(boolean useInternalDtd) {
		_useInternalDtd = useInternalDtd;
	}

	/**
	 * Reads the database model contained in the specified file.
	 * 
	 * @param filename
	 *            The model file name
	 *            
	 * @return The database model
	 */
	public Database read(String filename) throws DdlUtilsXMLException {
		return read(new File(filename));
	}

	/**
	 * Reads the database model contained in the specified file.
	 * 
	 * @param file
	 *            The model file
	 * @return The database model
	 */
	public Database read(File file) throws DdlUtilsXMLException {
		FileReader reader = null;

		if (_validateXml) {
			try {
				reader = new FileReader(file);
				new ModelValidator().validate(new StreamSource(reader));
			} catch (IOException ex) {
				throw new DdlUtilsXMLException(ex);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
						_log.warn("Could not close reader for file " + file.getAbsolutePath());
					}
					reader = null;
				}
			}
		}

		try {
			reader = new FileReader(file);
			return read(getXMLInputFactory().createXMLStreamReader(reader));
		} catch (XMLStreamException ex) {
			throw new DdlUtilsXMLException(ex);
		} catch (IOException ex) {
			throw new DdlUtilsXMLException(ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					_log.warn("Could not close reader for file " + file.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Reads the database model given by the reader. Note that this method does not
	 * close the given reader.
	 * 
	 * @param reader
	 *            The reader that returns the model XML
	 * @return The database model
	 */
	public Database read(Reader reader) throws DdlUtilsXMLException {
		try {
			if (_validateXml) {
				StringBuffer tmpXml = new StringBuffer();
				char[] buf = new char[4096];
				int len;

				while ((len = reader.read(buf)) >= 0) {
					tmpXml.append(buf, 0, len);
				}
				_log.debug("tmpXml : " + tmpXml.toString());
				new ModelValidator().validate(new StreamSource(new StringReader(tmpXml.toString())));
				return read(getXMLInputFactory().createXMLStreamReader(new StringReader(tmpXml.toString())));
			} else {
				return read(getXMLInputFactory().createXMLStreamReader(reader));
			}
		} catch (XMLStreamException ex) {
			throw new DdlUtilsXMLException(ex);
		} catch (IOException ex) {
			throw new DdlUtilsXMLException(ex);
		}
	}

	/**
	 * Reads the database model from the given input source.
	 *
	 * @param source
	 *            The input source
	 * @return The database model
	 */
	public Database read(InputSource source) throws DdlUtilsXMLException {
		return read(source.getCharacterStream());
	}

	/**
	 * Creates a new, initialized XML input factory object.
	 * 
	 * @return The factory object
	 */
	private XMLInputFactory getXMLInputFactory() {
		XMLInputFactory factory = XMLInputFactory.newInstance();

		factory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
		factory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
		return factory;
	}

	/**
	 * Reads the database model from the given XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The database model
	 */
	private Database read(XMLStreamReader xmlReader) throws DdlUtilsXMLException {
		Database model = null;
		try {
			while (xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
				if (xmlReader.next() == XMLStreamReader.END_DOCUMENT) {
					return null;
				}
			}
			if (isSameAs(xmlReader.getName(), QNAME_ELEMENT_DATABASE)) {
				model = readDatabaseElement(xmlReader);
			}
		} catch (IOException ex) {
			throw new DdlUtilsXMLException(ex);
		} catch (XMLStreamException ex) {
			throw new DdlUtilsXMLException(ex);
		}
		if (model != null) {
			model.initialize();
		}
		return model;
	}

	/**
	 * Reads a database element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The database object
	 */
	private Database readDatabaseElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		Database model = new Database();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				model.setName(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_DEFAULT_ID_METHOD)) {
				model.setIdMethod(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_VERSION)) {
				model.setVersion(xmlReader.getAttributeValue(idx));
			}
		}
		readTableElements(xmlReader, model);
		consumeRestOfElement(xmlReader);
		return model;
	}

	/**
	 * Reads table elements from the XML stream reader and adds them to the given
	 * database model.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @param model
	 *            The database model to add the table objects to
	 */
	private void readTableElements(XMLStreamReader xmlReader, Database model) throws XMLStreamException, IOException {
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				if (isSameAs(xmlReader.getName(), QNAME_ELEMENT_TABLE)) {
					model.addTable(readTableElement(xmlReader));
				} else {
					readOverElement(xmlReader);
				}
			}
		}
	}

	/**
	 * Reads a table element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The table object
	 */
	private Table readTableElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		Table table = new Table();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				table.setName(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_DESCRIPTION)) {
				table.setDescription(xmlReader.getAttributeValue(idx));
			}
			/** Added by AttunedLabs to support partitioning */
			else if (isSameAs(attrQName, QNAME_ATTRIBUTE_PARTITION_SUPPORTED)) {
				table.setPartitionSupported(Boolean.getBoolean(xmlReader.getAttributeValue(idx)));
			}
		}
		readTableSubElements(xmlReader, table);
		consumeRestOfElement(xmlReader);
		return table;
	}

	/**
	 * Reads table sub elements (column, foreign key, index) from the XML stream
	 * reader and adds them to the given table.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @param table
	 *            The table
	 */
	private void readTableSubElements(XMLStreamReader xmlReader, Table table) throws XMLStreamException, IOException {
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName elemQName = xmlReader.getName();

				if (isSameAs(elemQName, QNAME_ELEMENT_COLUMN)) {
					table.addColumn(readColumnElement(xmlReader));
				} else if (isSameAs(elemQName, QNAME_ELEMENT_FOREIGN_KEY)) {
					table.addForeignKey(readForeignKeyElement(xmlReader));
				} else if (isSameAs(elemQName, QNAME_ELEMENT_INDEX)) {
					table.addIndex(readIndexElement(xmlReader));
				} else if (isSameAs(elemQName, QNAME_ELEMENT_UNIQUE)) {
					table.addIndex(readUniqueElement(xmlReader));
				}
				/** Added by AttunedLabs to support partitioning */
				else if (isSameAs(elemQName, QNAME_ELEMENT_PARTITION_TYPE)) {
					table.setPartitionSupported(true);
					table.setPartitionType(readPartitionTypeElement(xmlReader));
				} else if (isSameAs(elemQName, QNAME_ELEMENT_PARTITION_SCHEMA)) {
				table.setPartitionSchema(readPartitionSchemaElement(xmlReader));
				} else if (isSameAs(elemQName, QNAME_ELEMENT_TABLESPACES)) {
					table.setTableSpaces(readTableSpacesElement(xmlReader));
				} else {
					readOverElement(xmlReader);
				}
			}
		}
	}

	/**
	 * Reads a column element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The column object
	 */
	private Column readColumnElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		Column column = new Column();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				column.setName(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_PRIMARY_KEY)) {
				column.setPrimaryKey(getAttributeValueAsBoolean(xmlReader, idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_REQUIRED)) {
				column.setRequired(getAttributeValueAsBoolean(xmlReader, idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_TYPE)) {
				column.setType(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_SIZE)) {
				column.setSize(getAttributeValueBeingNullAware(xmlReader, idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_DEFAULT)) {
				column.setDefaultValue(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_AUTO_INCREMENT)) {
				column.setAutoIncrement(getAttributeValueAsBoolean(xmlReader, idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_DESCRIPTION)) {
				column.setDescription(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_JAVA_NAME)) {
				column.setJavaName(xmlReader.getAttributeValue(idx));
			}
		}
		consumeRestOfElement(xmlReader);
		return column;
	}

	/**
	 * Reads a foreign key element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The foreign key object
	 */
	private ForeignKey readForeignKeyElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		ForeignKey foreignKey = new ForeignKey();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_FOREIGN_TABLE)) {
				foreignKey.setForeignTableName(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				foreignKey.setName(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_ON_UPDATE)) {
				foreignKey.setOnUpdate(getAttributeValueAsCascadeEnum(xmlReader, idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_ON_DELETE)) {
				foreignKey.setOnDelete(getAttributeValueAsCascadeEnum(xmlReader, idx));
			}
		}
		readReferenceElements(xmlReader, foreignKey);
		consumeRestOfElement(xmlReader);
		return foreignKey;
	}

	/**
	 * Reads reference elements from the XML stream reader and adds them to the
	 * given foreign key.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @param foreignKey
	 *            The foreign key
	 */
	private void readReferenceElements(XMLStreamReader xmlReader, ForeignKey foreignKey)
			throws XMLStreamException, IOException {
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName elemQName = xmlReader.getName();

				if (isSameAs(elemQName, QNAME_ELEMENT_REFERENCE)) {
					foreignKey.addReference(readReferenceElement(xmlReader));
				} else {
					readOverElement(xmlReader);
				}
			}
		}
	}

	/**
	 * Reads a reference element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The reference object
	 */
	private Reference readReferenceElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		Reference reference = new Reference();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_LOCAL)) {
				reference.setLocalColumnName(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_FOREIGN)) {
				reference.setForeignColumnName(xmlReader.getAttributeValue(idx));
			}
		}
		consumeRestOfElement(xmlReader);
		return reference;
	}

	/**
	 * Reads an index element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The index object
	 */
	private Index readIndexElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		Index index = new NonUniqueIndex();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				index.setName(xmlReader.getAttributeValue(idx));
			}
		}
		readIndexColumnElements(xmlReader, index);
		consumeRestOfElement(xmlReader);
		return index;
	}

	/**
	 * Reads an unique index element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The unique index object
	 */
	private Index readUniqueElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		Index index = new UniqueIndex();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				index.setName(xmlReader.getAttributeValue(idx));
			}
		}
		readUniqueColumnElements(xmlReader, index);
		consumeRestOfElement(xmlReader);
		return index;
	}

	/**
	 * Reads an partition element from the XML stream reader. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The PartitionType object
	 * @throws XMLStreamException
	 */
	private PartitionType readPartitionTypeElement(XMLStreamReader xmlReader) throws XMLStreamException {
		PartitionType partitionType = new PartitionType();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_TYPE)) {
				partitionType.setType(xmlReader.getAttributeValue(idx));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_COLUMN_NAME)) {
				partitionType.setColumnName(xmlReader.getAttributeValue(idx));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_PARTITION_COUNT)) {
				partitionType.setPartitionCount(xmlReader.getAttributeValue(idx));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_SCHEMA)) {
				partitionType.setSchema(xmlReader.getAttributeValue(idx));
			}
		}
		partitionType = readPartitionElements(xmlReader, partitionType);

		return partitionType;
	}

	/**
	 * Reads all partition elements from the XML stream reader. Added by AttunedLabs to
	 * support partitioning
	 * 
	 * @param xmlReader
	 * @param partitionType
	 * @return
	 * @throws XMLStreamException
	 */
	private PartitionType readPartitionElements(XMLStreamReader xmlReader, PartitionType partitionType)
			throws XMLStreamException {

		List<Partition> partitions = new ArrayList<>();

		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName attrQName = xmlReader.getName();

				if (isSameAs(attrQName, QNAME_ELEMENT_PARTITION)) {
					partitions = readPartitionElement(xmlReader, partitions);
				}
			}
		}
		partitionType.setPartitions(partitions);

		return partitionType;
	}

	/**
	 * Reads a single partition element from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 * @param partitions
	 * @return
	 * @throws XMLStreamException
	 */
	private List<Partition> readPartitionElement(XMLStreamReader xmlReader, List<Partition> partitions)
			throws XMLStreamException {
		Partition partition = new Partition();

		for (int index = 0; index < xmlReader.getAttributeCount(); index++) {
			QName attrQName = xmlReader.getAttributeName(index);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				partition.setName(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_VALUES)) {
				partition.setValues(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_TABLE_SPACE)) {
				partition.setTablespace(xmlReader.getAttributeValue(index));
			}
		}
		partitions.add(partition);

		consumeRestOfElement(xmlReader);
		return partitions;
	}

	/**
	 * Reads an partition schema element from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The PartitionType object
	 * @throws XMLStreamException
	 */
	private PartitionSchema readPartitionSchemaElement(XMLStreamReader xmlReader) throws XMLStreamException {
		PartitionSchema partitionSchema = new PartitionSchema();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				partitionSchema.setName(xmlReader.getAttributeValue(idx));
			}
		}
		partitionSchema = readPartitionSchemaElements(xmlReader, partitionSchema);
		return partitionSchema;
	}

	/**
	 * Reads all partition schema elements from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 * @param partitionType
	 * @return
	 * @throws XMLStreamException
	 */
	private PartitionSchema readPartitionSchemaElements(XMLStreamReader xmlReader, PartitionSchema partitionSchema)
			throws XMLStreamException { 

		PartitionFunction partitionFunction = new PartitionFunction();

		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName attrQName = xmlReader.getName();

				if (isSameAs(attrQName, QNAME_ELEMENT_PARTITION_FUNCTION)) {
					partitionFunction = readPartitionFunctionElement(xmlReader, partitionFunction);
				}
			}
		}
		partitionSchema.setPartitionFunction(partitionFunction);
		return partitionSchema;
	}

	/**
	 * Reads a partition function element from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 * @param partitions
	 * @return
	 * @throws XMLStreamException
	 */
	private PartitionFunction readPartitionFunctionElement(XMLStreamReader xmlReader,
			PartitionFunction partitionFunction) throws XMLStreamException {

		for (int index = 0; index < xmlReader.getAttributeCount(); index++) {
			QName attrQName = xmlReader.getAttributeName(index);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				partitionFunction.setName(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_VALUES)) {
				partitionFunction.setValues(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_COLUMN_TYPE)) {
				partitionFunction.setColumnType(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_RANGE_TYPE)) {
				partitionFunction.setRangeType(xmlReader.getAttributeValue(index));
			}
		}
		partitionFunction.setFileGroup(readFilegroupElement(xmlReader));
		return partitionFunction;
	}

	/**
	 * Reads an partition schema element from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The PartitionType object
	 * @throws XMLStreamException
	 */
	private List<FileGroup> readFilegroupElement(XMLStreamReader xmlReader) throws XMLStreamException {
		
		List<FileGroup> fileGroups = new ArrayList<>();
		
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName attrQName = xmlReader.getName();

				if (isSameAs(attrQName, QNAME_ELEMENT_FILE_GROUP)) {
					fileGroups = readFileGroupAndFileElement(xmlReader, fileGroups);
				}
			}
		}
		_log.debug(fileGroups);
		return fileGroups;
	}

	/**
	 * Reads a single partition element from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 * @param partitions
	 * @return
	 * @throws XMLStreamException
	 */
	private List<FileGroup> readFileGroupAndFileElement(XMLStreamReader xmlReader, List<FileGroup> fileGroups)
			throws XMLStreamException {
		
		FileGroup fileGroup = new FileGroup();
		for (int index = 0; index < xmlReader.getAttributeCount(); index++) {
			QName attrQName = xmlReader.getAttributeName(index);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_FILE_GROUP_NAME)) {
				fileGroup.setFileGroupName(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_FILE_NAME)) {
				fileGroup.setFileName(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_LOCATION)) {
				fileGroup.setLocation(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_MAX_SIZE)) {
				fileGroup.setMaxSize(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_SIZE)) {
				fileGroup.setSize(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_FILE_GROWTH)) {
				fileGroup.setFileGrowth(xmlReader.getAttributeValue(index));
			}
		}
		fileGroups.add(fileGroup);
		_log.debug(fileGroups);
		consumeRestOfElement(xmlReader);
		return fileGroups;
	}

	/**
	 * Reads an partition schema element from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The PartitionType object
	 * @throws XMLStreamException
	 */
	private List<TableSpace> readTableSpacesElement(XMLStreamReader xmlReader) throws XMLStreamException {
		
		List<TableSpace> tableSpaces = new ArrayList<>();
		
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName attrQName = xmlReader.getName();

				if (isSameAs(attrQName, QNAME_ELEMENT_TABLESPACE)) {
					tableSpaces = readTableSpaceElement(xmlReader, tableSpaces);
				}
			}
		}
		return tableSpaces;
		
	}
	
	/**
	 * Reads all partition schema elements from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 * @param partitionType
	 * @return
	 * @throws XMLStreamException
	 */
	private List<TableSpace> readTableSpaceElement(XMLStreamReader xmlReader, List<TableSpace> tableSpaces)
			throws XMLStreamException {
		TableSpace tableSpace = new TableSpace();
		
		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				tableSpace.setName(xmlReader.getAttributeValue(idx));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_TYPE)) {
				tableSpace.setType(xmlReader.getAttributeValue(idx));
			}
		
		}
		tableSpace = readFileAndDefaultParamsElements(xmlReader, tableSpace);
		tableSpaces.add(tableSpace);

		return tableSpaces;
	}

	/**
	 * Reads all partition schema elements from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 * @param partitionType
	 * @return
	 * @throws XMLStreamException
	 */
	private TableSpace readFileAndDefaultParamsElements(XMLStreamReader xmlReader, TableSpace tableSpace)
			throws XMLStreamException {

		List<org.apache.ddlutils.model.File> files = new ArrayList<>();
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName attrQName = xmlReader.getName();

				if (isSameAs(attrQName, QNAME_ELEMENT_FILE)) {
					files = readOracleFileElement(xmlReader, files);
				}

			}
		}
		tableSpace.setFile(files);
		return tableSpace;
	}

	/**
	 * Reads a partition function element from the XML stream reader. Added by
	 * AttunedLabs to support partitioning
	 * 
	 * @param xmlReader
	 * @param partitions
	 * @return
	 * @throws XMLStreamException
	 */
	private List<org.apache.ddlutils.model.File> readOracleFileElement(XMLStreamReader xmlReader,
			List<org.apache.ddlutils.model.File> files) throws XMLStreamException {
		org.apache.ddlutils.model.File file = new org.apache.ddlutils.model.File();
		for (int index = 0; index < xmlReader.getAttributeCount(); index++) {
			QName attrQName = xmlReader.getAttributeName(index);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				file.setName(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_DIRECTORY)) {
				file.setDirectory(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_SIZE)) {
				file.setSize(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_REUSE)) {
				file.setReuse(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_AUTO_EXTEND_NEXT_SIZE)) {
				file.setAutoExtendNextSize(xmlReader.getAttributeValue(index));
			}
			if (isSameAs(attrQName, QNAME_ATTRIBUTE_MAX_SIZE)) {
				file.setMaxSize(xmlReader.getAttributeValue(index));
			}
		}
		files.add(file);
		consumeRestOfElement(xmlReader);
		return files;
	}

	/**
	 * Reads index column elements from the XML stream reader and adds them to the
	 * given index object.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @param index
	 *            The index object
	 */
	private void readIndexColumnElements(XMLStreamReader xmlReader, Index index)
			throws XMLStreamException, IOException {
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName elemQName = xmlReader.getName();

				if (isSameAs(elemQName, QNAME_ELEMENT_INDEX_COLUMN)) {
					index.addColumn(readIndexColumnElement(xmlReader));
				} else {
					readOverElement(xmlReader);
				}
			}
		}
	}

	/**
	 * Reads unique index column elements from the XML stream reader and adds them
	 * to the given index object.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @param index
	 *            The index object
	 */
	private void readUniqueColumnElements(XMLStreamReader xmlReader, Index index)
			throws XMLStreamException, IOException {
		int eventType = XMLStreamReader.START_ELEMENT;

		while (eventType != XMLStreamReader.END_ELEMENT) {
			eventType = xmlReader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				QName elemQName = xmlReader.getName();

				if (isSameAs(elemQName, QNAME_ELEMENT_UNIQUE_COLUMN)) {
					index.addColumn(readIndexColumnElement(xmlReader));
				} else {
					readOverElement(xmlReader);
				}
			}
		}
	}

	/**
	 * Reads an index column element from the XML stream reader.
	 * 
	 * @param xmlReader
	 *            The reader
	 * @return The index column object
	 */
	private IndexColumn readIndexColumnElement(XMLStreamReader xmlReader) throws XMLStreamException, IOException {
		IndexColumn indexColumn = new IndexColumn();

		for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++) {
			QName attrQName = xmlReader.getAttributeName(idx);

			if (isSameAs(attrQName, QNAME_ATTRIBUTE_NAME)) {
				indexColumn.setName(xmlReader.getAttributeValue(idx));
			} else if (isSameAs(attrQName, QNAME_ATTRIBUTE_SIZE)) {
				indexColumn.setSize(getAttributeValueBeingNullAware(xmlReader, idx));
			}
		}
		consumeRestOfElement(xmlReader);
		return indexColumn;
	}

	/**
	 * Compares the given qnames. This specifically ignores the namespace uri of the
	 * other qname if the namespace of the current element is empty.
	 * 
	 * @param curElemQName
	 *            The qname of the current element
	 * @param qName
	 *            The qname to compare to
	 * @return <code>true</code> if they are the same
	 */
	private boolean isSameAs(QName curElemQName, QName qName) {
		if (StringUtils.isEmpty(curElemQName.getNamespaceURI())) {
			return qName.getLocalPart().equals(curElemQName.getLocalPart());
		} else {
			return qName.equals(curElemQName);
		}
	}

	/**
	 * Returns the value of the indicated attribute of the current element as a
	 * string. This method can handle "null" in which case it returns a null object.
	 * 
	 * @param xmlReader
	 *            The xml reader
	 * @param attributeIdx
	 *            The index of the attribute
	 * @return The attribute's value
	 */
	private String getAttributeValueBeingNullAware(XMLStreamReader xmlReader, int attributeIdx)
			throws DdlUtilsXMLException {
		String value = xmlReader.getAttributeValue(attributeIdx);
		
		return "null".equalsIgnoreCase(value) ? null : value;
	}

	/**
	 * Returns the value of the indicated attribute of the current element as a
	 * boolean. If the value is not a valid boolean, then an exception is thrown.
	 * 
	 * @param xmlReader
	 *            The xml reader
	 * @param attributeIdx
	 *            The index of the attribute
	 * @return The attribute's value as a boolean
	 */
	private boolean getAttributeValueAsBoolean(XMLStreamReader xmlReader, int attributeIdx)
			throws DdlUtilsXMLException {
		String value = xmlReader.getAttributeValue(attributeIdx);

		if ("true".equalsIgnoreCase(value)) {
			return true;
		} else if ("false".equalsIgnoreCase(value)) {
			return false;
		} else {
			throw new DdlUtilsXMLException("Illegal boolean value '" + value + "' for attribute "
					+ xmlReader.getAttributeLocalName(attributeIdx));
		}
	}

	/**
	 * Returns the value of the indicated attribute of the current element as a
	 * boolean. If the value is not a valid boolean, then an exception is thrown.
	 * 
	 * @param xmlReader
	 *            The xml reader
	 * @param attributeIdx
	 *            The index of the attribute
	 * @return The attribute's value as a boolean
	 */
	private CascadeActionEnum getAttributeValueAsCascadeEnum(XMLStreamReader xmlReader, int attributeIdx)
			throws DdlUtilsXMLException {
		String value = xmlReader.getAttributeValue(attributeIdx);
		CascadeActionEnum enumValue = value == null ? null : CascadeActionEnum.getEnum(value.toLowerCase());

		if (enumValue == null) {
			throw new DdlUtilsXMLException("Illegal boolean value '" + value + "' for attribute "
					+ xmlReader.getAttributeLocalName(attributeIdx));
		} else {
			return enumValue;
		}
	}

	/**
	 * Consumes the rest of the current element. This assumes that the current XML
	 * stream event type is not START_ELEMENT.
	 * 
	 * @param reader
	 *            The xml reader
	 */
	private void consumeRestOfElement(XMLStreamReader reader) throws XMLStreamException {
		int eventType = reader.getEventType();

		while ((eventType != XMLStreamReader.END_ELEMENT) && (eventType != XMLStreamReader.END_DOCUMENT)) {
			eventType = reader.next();
		}
	}

	/**
	 * Reads over the current element. This assumes that the current XML stream
	 * event type is START_ELEMENT.
	 * 
	 * @param reader
	 *            The xml reader
	 */
	private void readOverElement(XMLStreamReader reader) throws XMLStreamException {
		int depth = 1;

		while (depth > 0) {
			int eventType = reader.next();

			if (eventType == XMLStreamReader.START_ELEMENT) {
				depth++;
			} else if (eventType == XMLStreamReader.END_ELEMENT) {
				depth--;
			}
		}
	}

	/**
	 * Writes the database model to the specified file.
	 * 
	 * @param model
	 *            The database model
	 * @param filename
	 *            The model file name
	 */
	public void write(Database model, String filename) throws DdlUtilsXMLException {
		try {
			BufferedWriter writer = null;

			try {
				writer = new BufferedWriter(new FileWriter(filename));

				write(model, writer);
				writer.flush();
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		} catch (Exception ex) {
			throw new DdlUtilsXMLException(ex);
		}
	}

	/**
	 * Writes the database model to the given output stream. Note that this method
	 * does not flush or close the stream.
	 * 
	 * @param model
	 *            The database model
	 * @param output
	 *            The output stream
	 */
	public void write(Database model, OutputStream output) throws DdlUtilsXMLException {
		PrettyPrintingXmlWriter xmlWriter = new PrettyPrintingXmlWriter(output, "UTF-8");

		xmlWriter.setDefaultNamespace(DDLUTILS_NAMESPACE);
		xmlWriter.writeDocumentStart();
		writeDatabaseElement(model, xmlWriter);
		xmlWriter.writeDocumentEnd();
	}

	/**
	 * Writes the database model to the given output writer. Note that this method
	 * does not flush or close the writer.
	 * 
	 * @param model
	 *            The database model
	 * @param output
	 *            The output writer
	 */
	public void write(Database model, Writer output) throws DdlUtilsXMLException {
		PrettyPrintingXmlWriter xmlWriter = new PrettyPrintingXmlWriter(output, "UTF-8");

		xmlWriter.setDefaultNamespace(DDLUTILS_NAMESPACE);
		xmlWriter.writeDocumentStart();
		writeDatabaseElement(model, xmlWriter);
		xmlWriter.writeDocumentEnd();
	}

	/**
	 * Writes the database model to the given XML writer.
	 * 
	 * @param model
	 *            The database model
	 * @param xmlWriter
	 *            The XML writer
	 */
	private void writeDatabaseElement(Database model, PrettyPrintingXmlWriter xmlWriter) throws DdlUtilsXMLException {
		writeElementStart(xmlWriter, QNAME_ELEMENT_DATABASE);
		xmlWriter.writeNamespace(null, DDLUTILS_NAMESPACE);
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_NAME, model.getName());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_DEFAULT_ID_METHOD, model.getIdMethod());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_VERSION, model.getVersion());
		if (model.getTableCount() > 0) {
			xmlWriter.printlnIfPrettyPrinting();
			for (int idx = 0; idx < model.getTableCount(); idx++) {
				writeTableElement(model.getTable(idx), xmlWriter);
			}
		}
		writeElementEnd(xmlWriter);
	}

	/**
	 * Writes the table object to the given XML writer.
	 * 
	 * @param table
	 *            The table object
	 * @param xmlWriter
	 *            The XML writer
	 */
	private void writeTableElement(Table table, PrettyPrintingXmlWriter xmlWriter) throws DdlUtilsXMLException {
		xmlWriter.indentIfPrettyPrinting(1);
		writeElementStart(xmlWriter, QNAME_ELEMENT_TABLE);
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_NAME, table.getName());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_DESCRIPTION, table.getDescription());
		if ((table.getColumnCount() > 0) || (table.getForeignKeyCount() > 0) || (table.getIndexCount() > 0)) {
			xmlWriter.printlnIfPrettyPrinting();
			for (int idx = 0; idx < table.getColumnCount(); idx++) {
				writeColumnElement(table.getColumn(idx), xmlWriter);
			}
			for (int idx = 0; idx < table.getForeignKeyCount(); idx++) {
				writeForeignKeyElement(table.getForeignKey(idx), xmlWriter);
			}
			for (int idx = 0; idx < table.getIndexCount(); idx++) {
				writeIndexElement(table.getIndex(idx), xmlWriter);
			}
			xmlWriter.indentIfPrettyPrinting(1);
		}
		writeElementEnd(xmlWriter);
	}

	/**
	 * Writes the column object to the given XML writer.
	 * 
	 * @param column
	 *            The column object
	 * @param xmlWriter
	 *            The XML writer
	 */
	private void writeColumnElement(Column column, PrettyPrintingXmlWriter xmlWriter) throws DdlUtilsXMLException {
		xmlWriter.indentIfPrettyPrinting(2);
		writeElementStart(xmlWriter, QNAME_ELEMENT_COLUMN);
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_NAME, column.getName());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_PRIMARY_KEY, String.valueOf(column.isPrimaryKey()));
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_REQUIRED, String.valueOf(column.isRequired()));
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_TYPE, column.getType());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_SIZE, column.getSize());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_DEFAULT, column.getDefaultValue());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_AUTO_INCREMENT, String.valueOf(column.isAutoIncrement()));
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_DESCRIPTION, column.getDescription());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_JAVA_NAME, column.getJavaName());
		writeElementEnd(xmlWriter);
	}

	/**
	 * Writes the foreign key object to the given XML writer.
	 * 
	 * @param foreignKey
	 *            The foreign key object
	 * @param xmlWriter
	 *            The XML writer
	 */
	private void writeForeignKeyElement(ForeignKey foreignKey, PrettyPrintingXmlWriter xmlWriter)
			throws DdlUtilsXMLException {
		xmlWriter.indentIfPrettyPrinting(2);
		writeElementStart(xmlWriter, QNAME_ELEMENT_FOREIGN_KEY);
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_FOREIGN_TABLE, foreignKey.getForeignTableName());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_NAME, foreignKey.getName());
		if (foreignKey.getOnUpdate() != CascadeActionEnum.NONE) {
			writeAttribute(xmlWriter, QNAME_ATTRIBUTE_ON_UPDATE, foreignKey.getOnUpdate().getName());
		}
		if (foreignKey.getOnDelete() != CascadeActionEnum.NONE) {
			writeAttribute(xmlWriter, QNAME_ATTRIBUTE_ON_DELETE, foreignKey.getOnDelete().getName());
		}
		if (foreignKey.getReferenceCount() > 0) {
			xmlWriter.printlnIfPrettyPrinting();
			for (int idx = 0; idx < foreignKey.getReferenceCount(); idx++) {
				writeReferenceElement(foreignKey.getReference(idx), xmlWriter);
			}
			xmlWriter.indentIfPrettyPrinting(2);
		}
		writeElementEnd(xmlWriter);
	}

	/**
	 * Writes the reference object to the given XML writer.
	 * 
	 * @param reference
	 *            The reference object
	 * @param xmlWriter
	 *            The XML writer
	 */
	private void writeReferenceElement(Reference reference, PrettyPrintingXmlWriter xmlWriter)
			throws DdlUtilsXMLException {
		xmlWriter.indentIfPrettyPrinting(3);
		writeElementStart(xmlWriter, QNAME_ELEMENT_REFERENCE);
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_LOCAL, reference.getLocalColumnName());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_FOREIGN, reference.getForeignColumnName());
		writeElementEnd(xmlWriter);
	}

	/**
	 * Writes the index object to the given XML writer.
	 * 
	 * @param index
	 *            The index object
	 * @param xmlWriter
	 *            The XML writer
	 */
	private void writeIndexElement(Index index, PrettyPrintingXmlWriter xmlWriter) throws DdlUtilsXMLException {
		xmlWriter.indentIfPrettyPrinting(2);
		writeElementStart(xmlWriter, index.isUnique() ? QNAME_ELEMENT_UNIQUE : QNAME_ELEMENT_INDEX);
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_NAME, index.getName());
		if (index.getColumnCount() > 0) {
			xmlWriter.printlnIfPrettyPrinting();
			for (int idx = 0; idx < index.getColumnCount(); idx++) {
				writeIndexColumnElement(index.getColumn(idx), index.isUnique(), xmlWriter);
			}
			xmlWriter.indentIfPrettyPrinting(2);
		}
		writeElementEnd(xmlWriter);
	}

	/**
	 * Writes the index column object to the given XML writer.
	 * 
	 * @param indexColumn
	 *            The index column object
	 * @param isUnique
	 *            Whether the index that the index column belongs to, is unique
	 * @param xmlWriter
	 *            The XML writer
	 */
	private void writeIndexColumnElement(IndexColumn indexColumn, boolean isUnique, PrettyPrintingXmlWriter xmlWriter)
			throws DdlUtilsXMLException {
		xmlWriter.indentIfPrettyPrinting(3);
		writeElementStart(xmlWriter, isUnique ? QNAME_ELEMENT_UNIQUE_COLUMN : QNAME_ELEMENT_INDEX_COLUMN);
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_NAME, indexColumn.getName());
		writeAttribute(xmlWriter, QNAME_ATTRIBUTE_SIZE, indexColumn.getSize());
		writeElementEnd(xmlWriter);
	}

	/**
	 * Writes the start of the specified XML element to the given XML writer.
	 * 
	 * @param xmlWriter
	 *            The xml writer
	 * @param qName
	 *            The qname of the XML element
	 */
	private void writeElementStart(PrettyPrintingXmlWriter xmlWriter, QName qName) throws DdlUtilsXMLException {
		xmlWriter.writeElementStart(qName.getNamespaceURI(), qName.getLocalPart());
	}

	/**
	 * Writes an attribute to the given XML writer.
	 * 
	 * @param xmlWriter
	 *            The xml writer
	 * @param qName
	 *            The qname of the attribute
	 * @param value
	 *            The value; if empty, then nothing is written
	 */
	private void writeAttribute(PrettyPrintingXmlWriter xmlWriter, QName qName, String value)
			throws DdlUtilsXMLException {
		if (value != null) {
			xmlWriter.writeAttribute(null, qName.getLocalPart(), value);
		}
	}

	/**
	 * Writes the end of the current XML element to the given XML writer.
	 * 
	 * @param xmlWriter
	 *            The xml writer
	 */
	private void writeElementEnd(PrettyPrintingXmlWriter xmlWriter) throws DdlUtilsXMLException {
		xmlWriter.writeElementEnd();
		xmlWriter.printlnIfPrettyPrinting();
	}
}
