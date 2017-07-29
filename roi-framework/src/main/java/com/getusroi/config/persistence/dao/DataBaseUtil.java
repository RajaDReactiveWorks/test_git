package com.getusroi.config.persistence.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DataBaseUtil {

	private static String URL = null;
	private static String DRIVER_CLASS = null;
	private static String USER = null;
	private static String PASSWORD = null;

	/**
	 * 
	 * @return JDBCconnection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static Connection getConnection() throws ClassNotFoundException, SQLException, IOException {

		if (URL == null) {
			loadConfigrationDbPropertyFile();
		}
		Class.forName(DRIVER_CLASS);
		Connection connection = (Connection) DriverManager.getConnection(URL, USER, PASSWORD);
		return connection;
	}

	public static void dbCleanup(Connection con, PreparedStatement ptst, ResultSet rs) {
		close(con);
		close(ptst);
		close(rs);
	}

	public static void dbCleanUp(Connection conn, PreparedStatement ps) {
		close(conn);
		close(ps);
	}

	public static void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException sqlexp) {
				sqlexp.printStackTrace();
			}
		}
	}

	public static void close(PreparedStatement pStatement) {
		if (pStatement != null) {
			try {
				pStatement.close();
			} catch (SQLException sqlexp) {
				sqlexp.printStackTrace();
			}
		}
	}

	public static void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException sqlexp) {
				sqlexp.printStackTrace();
			}
		}
	}


	private synchronized static void loadConfigrationDbPropertyFile() throws IOException {
		Properties properties = new Properties();
		properties.load(DataBaseUtil.class.getClassLoader().getResourceAsStream("configartionDB.properties"));
		URL = properties.getProperty("DB_URL");
		DRIVER_CLASS = properties.getProperty("DB_DRIVER_CLASS");
		USER = properties.getProperty("DB_USER");
		PASSWORD = properties.getProperty("DB_PASSWORD");

	}

}
