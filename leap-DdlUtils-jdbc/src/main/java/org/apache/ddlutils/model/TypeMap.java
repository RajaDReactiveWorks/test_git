package org.apache.ddlutils.model;

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

import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class that maps SQL type names to their JDBC type ID found in
 * {@link java.sql.Types} and vice versa.
 *
 * @version $Revision$
 */
public abstract class TypeMap {
	private final static Log _log = LogFactory.getLog(TypeMap.class);
	/**
	 * The string representation of the {@link java.sql.Types#ARRAY} constant.
	 */
	public static final String ARRAY = "ARRAY";
	/**
	 * The string representation of the {@link java.sql.Types#BIGINT} constant.
	 */
	public static final String BIGINT = "BIGINT";
	/**
	 * The string representation of the {@link java.sql.Types#BINARY} constant.
	 */
	public static final String BINARY = "BINARY";
	/** The string representation of the {@link java.sql.Types#BIT} constant. */
	public static final String BIT = "BIT";
	/**
	 * The string representation of the {@link java.sql.Types#BLOB} constant.
	 */
	public static final String BLOB = "BLOB";
	/**
	 * The string representation of the {@link java.sql.Types#BOOLEAN} constant.
	 */
	public static final String BOOLEAN = "BOOLEAN";
	/**
	 * The string representation of the {@link java.sql.Types#CHAR} constant.
	 */
	public static final String CHAR = "CHAR";
	/**
	 * The string representation of the {@link java.sql.Types#CLOB} constant.
	 */
	public static final String CLOB = "CLOB";
	/**
	 * The string representation of the {@link java.sql.Types#DATALINK}
	 * constant.
	 */
	public static final String DATALINK = "DATALINK";
	/**
	 * The string representation of the {@link java.sql.Types#DATE} constant.
	 */
	public static final String DATE = "DATE";
	/**
	 * The string representation of the {@link java.sql.Types#DECIMAL} constant.
	 */
	public static final String DECIMAL = "DECIMAL";
	/**
	 * The string representation of the {@link java.sql.Types#DISTINCT}
	 * constant.
	 */
	public static final String DISTINCT = "DISTINCT";
	/**
	 * The string representation of the {@link java.sql.Types#DOUBLE} constant.
	 */
	public static final String DOUBLE = "DOUBLE";
	/**
	 * The string representation of the {@link java.sql.Types#FLOAT} constant.
	 */
	public static final String FLOAT = "FLOAT";
	/**
	 * The string representation of the {@link java.sql.Types#INTEGER} constant.
	 */
	public static final String INTEGER = "INTEGER";
	/**
	 * The string representation of the {@link java.sql.Types#JAVA_OBJECT}
	 * constant.
	 */
	public static final String JAVA_OBJECT = "JAVA_OBJECT";
	/**
	 * The string representation of the {@link java.sql.Types#LONGVARBINARY}
	 * constant.
	 */
	public static final String LONGVARBINARY = "LONGVARBINARY";
	/**
	 * The string representation of the {@link java.sql.Types#LONGVARCHAR}
	 * constant.
	 */
	public static final String LONGVARCHAR = "LONGVARCHAR";
	/**
	 * The string representation of the {@link java.sql.Types#NULL} constant.
	 */
	public static final String NULL = "NULL";
	/**
	 * The string representation of the {@link java.sql.Types#NUMERIC} constant.
	 */
	public static final String NUMERIC = "NUMERIC";
	/**
	 * The string representation of the {@link java.sql.Types#OTHER} constant.
	 */
	public static final String OTHER = "OTHER";
	/**
	 * The string representation of the {@link java.sql.Types#REAL} constant.
	 */
	public static final String REAL = "REAL";
	/** The string representation of the {@link java.sql.Types#REF} constant. */
	public static final String REF = "REF";
	/**
	 * The string representation of the {@link java.sql.Types#SMALLINT}
	 * constant.
	 */
	public static final String SMALLINT = "SMALLINT";
	/**
	 * The string representation of the {@link java.sql.Types#STRUCT} constant.
	 */
	public static final String STRUCT = "STRUCT";
	/**
	 * The string representation of the {@link java.sql.Types#TIME} constant.
	 */
	public static final String TIME = "TIME";
	/**
	 * The string representation of the {@link java.sql.Types#TIMESTAMP}
	 * constant.
	 */
	public static final String TIMESTAMP = "TIMESTAMP";
	/**
	 * The string representation of the {@link java.sql.Types#TINYINT} constant.
	 */
	public static final String TINYINT = "TINYINT";
	/**
	 * The string representation of the {@link java.sql.Types#VARBINARY}
	 * constant.
	 */
	public static final String VARBINARY = "VARBINARY";
	/**
	 * The string representation of the {@link java.sql.Types#VARCHAR} constant.
	 */
	public static final String VARCHAR = "VARCHAR";
	// ...AttunedLabs
	public static final String NVARCHAR = "NVARCHAR";
	
	//..AttunedLabs
	public static final String SQLXML = "SQLXML";

	/**
	 * Maps type names to the corresponding {@link java.sql.Types} constants.
	 */
	private static HashMap _typeNameToTypeCode = new HashMap();
	/**
	 * Maps {@link java.sql.Types} type code constants to the corresponding type
	 * names.
	 */
	private static HashMap _typeCodeToTypeName = new HashMap();
	/** Conatins the types per category. */
	private static HashMap _typesPerCategory = new HashMap();

	static {
		registerJdbcType(Types.ARRAY, ARRAY, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.BIGINT, BIGINT, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.BINARY, BINARY, JdbcTypeCategoryEnum.BINARY);
		registerJdbcType(Types.BIT, BIT, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.BLOB, BLOB, JdbcTypeCategoryEnum.BINARY);
		registerJdbcType(Types.BOOLEAN, BOOLEAN, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.CHAR, CHAR, JdbcTypeCategoryEnum.TEXTUAL);
		registerJdbcType(Types.CLOB, CLOB, JdbcTypeCategoryEnum.TEXTUAL);
		registerJdbcType(Types.DATALINK, DATALINK, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.DATE, DATE, JdbcTypeCategoryEnum.DATETIME);
		registerJdbcType(Types.DECIMAL, DECIMAL, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.DISTINCT, DISTINCT, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.DOUBLE, DOUBLE, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.FLOAT, FLOAT, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.INTEGER, INTEGER, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.JAVA_OBJECT, JAVA_OBJECT, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.LONGVARBINARY, LONGVARBINARY, JdbcTypeCategoryEnum.BINARY);
		registerJdbcType(Types.LONGVARCHAR, LONGVARCHAR, JdbcTypeCategoryEnum.TEXTUAL);
		registerJdbcType(Types.NULL, NULL, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.NUMERIC, NUMERIC, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.OTHER, OTHER, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.REAL, REAL, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.REF, REF, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.SMALLINT, SMALLINT, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.STRUCT, STRUCT, JdbcTypeCategoryEnum.SPECIAL);
		registerJdbcType(Types.TIME, TIME, JdbcTypeCategoryEnum.DATETIME);
		registerJdbcType(Types.TIMESTAMP, TIMESTAMP, JdbcTypeCategoryEnum.DATETIME);
		registerJdbcType(Types.TINYINT, TINYINT, JdbcTypeCategoryEnum.NUMERIC);
		registerJdbcType(Types.VARBINARY, VARBINARY, JdbcTypeCategoryEnum.BINARY);
		registerJdbcType(Types.VARCHAR, VARCHAR, JdbcTypeCategoryEnum.TEXTUAL);

		// ..AttunedLabs
		registerJdbcType(Types.NVARCHAR, NVARCHAR, JdbcTypeCategoryEnum.TEXTUAL);
		registerJdbcType(Types.SQLXML, SQLXML, JdbcTypeCategoryEnum.TEXTUAL);

		// Torque/Turbine extensions which we only support when reading from an
		// XML schema
		_typeNameToTypeCode.put("BOOLEANINT", new Integer(Types.TINYINT));
		_typeNameToTypeCode.put("BOOLEANCHAR", new Integer(Types.CHAR));
		_typeNameToTypeCode.put("TINYTEXT", new Integer(LeapTypes.TINYTEXT));
		_typeNameToTypeCode.put("TEXT", new Integer(LeapTypes.TEXT));
		_typeNameToTypeCode.put("MEDIUMTEXT", new Integer(LeapTypes.MEDIUMTEXT));
		_typeNameToTypeCode.put("LONGTEXT", new Integer(LeapTypes.LONGTEXT));
		_typeNameToTypeCode.put("BLOB", new Integer(LeapTypes.BLOB));
		_typeNameToTypeCode.put("LONG RAW", new Integer(LeapTypes.LONG_RAW));
		_typeNameToTypeCode.put("LONG", new Integer(LeapTypes.LONG));
//		_typeNameToTypeCode.put("TIMESTAMP", new Integer(LeapTypes.TIMESTAMP));
		_typeNameToTypeCode.put("CLOB", new Integer(LeapTypes.CLOB));
		_typeNameToTypeCode.put("NCLOB", new Integer(LeapTypes.NCLOB));
	}

	/**
	 * Returns all supported JDBC types.
	 * 
	 * @return The type codes ({@link java.sql.Types} constants)
	 */
	public static int[] getSuportedJdbcTypes() {
		int[] typeCodes = new int[_typeCodeToTypeName.size()];
		int idx = 0;

		for (Iterator it = _typeCodeToTypeName.keySet().iterator(); it.hasNext(); idx++) {
			typeCodes[idx] = ((Integer) it.next()).intValue();
		}
		return typeCodes;
	}

	/**
	 * Returns the JDBC type code (one of the {@link java.sql.Types} constants)
	 * that corresponds to the given JDBC type name.
	 * 
	 * @param typeName
	 *            The JDBC type name (case is ignored)
	 * @return The type code or <code>null</code> if the type is unknown
	 */
	public static Integer getJdbcTypeCode(String typeName) {
		return (Integer) _typeNameToTypeCode.get(typeName.toUpperCase());
	}

	/**
	 * Returns the JDBC type name that corresponds to the given type code (one
	 * of the {@link java.sql.Types} constants).
	 * 
	 * @param typeCode
	 *            The type code
	 * @return The JDBC type name (one of the constants in this class) or
	 *         <code>null</code> if the type is unknown
	 */
	public static String getJdbcTypeName(int typeCode) {
		// ..AttunedLabs.. type conversion from 2007(varchar2) to 12(varchar)
		switch (typeCode) {
		case 2007:
			typeCode = Types.VARCHAR;
			// ..set the varchar2(oracle) to varchar(java.sql)
			break;
		case -101:
			typeCode = Types.TIMESTAMP;
			// ..set the TimeStampWithTimeZone(oracle) to TimeStamp(java.sql)
			break;
		case -102:
			typeCode = Types.TIMESTAMP;
			// ..set the TimeStampWithTimeZone(oracle) to TimeStamp(java.sql)
			break;
		case 5000:
			typeCode = LeapTypes.TINYTEXT;
			// ..set the tinyText
			return "TINYTEXT";
		case 5001:
			typeCode = LeapTypes.TEXT;
			// ..set the text
			return "TEXT";
		case 5002:
			typeCode = LeapTypes.MEDIUMTEXT;
			// ..set the mediumText
			return "MEDIUMTEXT";
		case 5003:
			typeCode = LeapTypes.LONGTEXT;
			// ..set the longText
			return "LONGTEXT";
		case 5004:
			typeCode = LeapTypes.BLOB;
			// ..set the lob
			return "BLOB";
		case 5005:
			typeCode = LeapTypes.LONG;
			// ..set the long
			return "LONG";
		case 5006:
			typeCode = LeapTypes.LONG_RAW;
			// ..set the long raw
			return "LONG RAW";
		/*case 5007:
			typeCode = LeapTypes.TIMESTAMP;
			// ..set the long raw
			return "TIMESTAMP";*/
		case 5008:
			typeCode = LeapTypes.CLOB;
			// ..set the lob
			return "CLOB";
		case 5009:
			typeCode = LeapTypes.NCLOB;
			// ..set the lob
			return "NCLOB";
		default:
			break;
		}
		return (String) _typeCodeToTypeName.get(new Integer(typeCode));
	}

	/**
	 * Registers a jdbc type.
	 * 
	 * @param typeCode
	 *            The type code (one of the {@link java.sql.Types} constants)
	 * @param typeName
	 *            The type name (case is ignored)
	 * @param category
	 *            The type category
	 */
	protected static void registerJdbcType(int typeCode, String typeName, JdbcTypeCategoryEnum category) {
		Integer typeId = new Integer(typeCode);

		_typeNameToTypeCode.put(typeName.toUpperCase(), typeId);
		_typeCodeToTypeName.put(typeId, typeName.toUpperCase());

		Set typesInCategory = (Set) _typesPerCategory.get(category);

		if (typesInCategory == null) {
			typesInCategory = new HashSet();
			_typesPerCategory.put(category, typesInCategory);
		}
		typesInCategory.add(typeId);
	}

	/**
	 * Determines whether the given jdbc type (one of the {@link java.sql.Types}
	 * constants) is a numeric type.
	 * 
	 * @param jdbcTypeCode
	 *            The type code
	 * @return <code>true</code> if the type is a numeric one
	 */
	public static boolean isNumericType(int jdbcTypeCode) {
		Set typesInCategory = (Set) _typesPerCategory.get(JdbcTypeCategoryEnum.NUMERIC);

		return typesInCategory == null ? false : typesInCategory.contains(new Integer(jdbcTypeCode));
	}

	/**
	 * Determines whether the given jdbc type (one of the {@link java.sql.Types}
	 * constants) is a date/time type.
	 * 
	 * @param jdbcTypeCode
	 *            The type code
	 * @return <code>true</code> if the type is a numeric one
	 */
	public static boolean isDateTimeType(int jdbcTypeCode) {
		Set typesInCategory = (Set) _typesPerCategory.get(JdbcTypeCategoryEnum.DATETIME);

		return typesInCategory == null ? false : typesInCategory.contains(new Integer(jdbcTypeCode));
	}

	/**
	 * Determines whether the given jdbc type (one of the {@link java.sql.Types}
	 * constants) is a text type.
	 * 
	 * @param jdbcTypeCode
	 *            The type code
	 * @return <code>true</code> if the type is a text one
	 */
	public static boolean isTextType(int jdbcTypeCode) {
		Set typesInCategory = (Set) _typesPerCategory.get(JdbcTypeCategoryEnum.TEXTUAL);

		return typesInCategory == null ? false : typesInCategory.contains(new Integer(jdbcTypeCode));
	}

	/**
	 * Determines whether the given jdbc type (one of the {@link java.sql.Types}
	 * constants) is a binary type.
	 * 
	 * @param jdbcTypeCode
	 *            The type code
	 * @return <code>true</code> if the type is a binary one
	 */
	public static boolean isBinaryType(int jdbcTypeCode) {
		Set typesInCategory = (Set) _typesPerCategory.get(JdbcTypeCategoryEnum.BINARY);

		return typesInCategory == null ? false : typesInCategory.contains(new Integer(jdbcTypeCode));
	}

	/**
	 * Determines whether the given sql type (one of the {@link java.sql.Types}
	 * constants) is a special type.
	 * 
	 * @param jdbcTypeCode
	 *            The type code
	 * @return <code>true</code> if the type is a special one
	 */
	public static boolean isSpecialType(int jdbcTypeCode) {
		Set typesInCategory = (Set) _typesPerCategory.get(JdbcTypeCategoryEnum.SPECIAL);

		return typesInCategory == null ? false : typesInCategory.contains(new Integer(jdbcTypeCode));
	}
}
