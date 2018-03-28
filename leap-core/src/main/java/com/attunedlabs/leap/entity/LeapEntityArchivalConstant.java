package com.attunedlabs.leap.entity;

public class LeapEntityArchivalConstant {
	public static final String CASSANDRA = "cassandra";
	public static final String JDBC = "jdbc";

	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String URL = "url";
	public static final String DRIVER_CLASS = "driver_class";
	public static final String KEYSPACE = "keyspace";
	public static final String TYPE_TOKEN = "type-token";
	
	public static final String CONFIG_PROPERTY_FILE = "cassandraDBConfig.properties";
	
	public static final String FILTER = "$filter";
	public static final String SELECT = "$select";
	public static final String ORDER_BY = "$orderby";
	public static final String SEARCH = "$search";
	public static final String EXPAND = "$expand";
	public static final String TOP = "$top";
	public static final String SKIP = "$skip";
	
	public static final String LEAP_QUERY = "leapQuery";
	public static final String DATA = "data";
	public static final String TYPE = "type";
	public static final String SPACE = "\\s+";
	public static final String ORDER_BY_ASC = "ASC";
	public static final String ORDER_BY_DESC = "DESC";
	
	public static final String LEAP_DATA_SERVICES = "leapDataServices.xml";
	public static final String COMMA = ",";
	public static final String LEAP_XA_DATASOURCE = "leapXASource";
	public static final String NO_SQL_HEADER_KEY = "NoSql";

	//Logical Operator
//	public static final String AND_OR_SPLITTER = " and | or | AND | OR |AND|OR|and|or";
	public static final String AND_OR_SPLITTER = " and | or | AND | OR ";
	public static final String EQ_SPLITTER = " eq | EQ ";
	public static final String NE_SPLITTER = " ne | NE ";
	public static final String GE_SPLITTER = " ge | GE ";
	public static final String GT_SPLITTER = " gt | GT ";
	public static final String LE_SPLITTER = " le | LE ";
	public static final String LT_SPLITTER = " lt | LT ";
	public static final String AND = " and ";
	public static final String OR = " or ";
	public static final String EQ = " eq ";
	public static final String NE = " ne ";
	public static final String GE = " ge ";
	public static final String GT = " gt ";
	public static final String LE = " le ";
	public static final String LT = " lt ";
	
	//Arithmetic Operator
	public static final String ADD_SPLITTER = " add | ADD ";
	public static final String SUB_SPLITTER = " sub | SUB ";
	public static final String MUL_SPLITTER = " mul | MUL ";
	public static final String DIV_SPLITTER = " div | DIV ";
	public static final String MOD_SPLITTER = " mod | MOD ";
	public static final String ADD = " add ";
	public static final String SUB = " sub ";
	public static final String MUL = " mul ";
	public static final String DIV = " div ";
	public static final String MOD = " mod ";
	
	//String Functions
	public static final String TO_LOWER = "tolower";
	public static final String TO_UPPER = "toupper";
	public static final String STARTS_WITH = "startswith";
	public static final String ENDS_WITH = "endswith";
	public static final String SUBSTRING_OF = "substringof";
	public static final String TRIM = "trim";
	public static final String LENGTH = "length";
	public static final String INDEX_OF = "indexof";
	public static final String REPLACE = "replace";
	public static final String SUBSTRING = "substring";
	public static final String CONCAT = "concat";

	public static final String CONTAINS = "contains";
	public static final String OPEN_BRACKET = "(";
	public static final String CLOSE_BRACKET = ")";
	public static final String PERCENTAGE = "%";
	public static final String APOSTROPHE = "'";

}
