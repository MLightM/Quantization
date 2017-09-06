package com.demo.util;

import java.sql.*;

/**
 * mysql utility
 * @author w
 *
 */
public class DBHelper {
	
	private static String driver = "com.mysql.jdbc.Driver"; // mysql driver
	private static String url = "jdbc:mysql://127.0.0.1:3306/quantization"; // mysql database path
	private static String user = "root", pwd = "root"; // username and password
	private static Connection con = null;
	private static Statement cmd = null;
	
	public static boolean isInit = false; // sql driver is initialized
	
	static {
		try {
			Class.forName(driver);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * initialization
	 * @return
	 */
	public static boolean init() {
		close();
		try {
			con = DriverManager.getConnection(url, user, pwd);
			cmd = con.createStatement();
		} catch (Exception ex) {
			ex.printStackTrace();
			isInit = false;
			return false;
		}
		isInit = true;
		return true;
	}

	/**
	 * sql to update
	 * @param sql
	 * @return
	 */
	public static boolean executeUpdate(String sql) {
		if(con==null || cmd==null) init();
		try {
			cmd.executeUpdate(sql);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * sql to query
	 * @param sql
	 * @return
	 */
	public static ResultSet executeQuery(String sql) {
		if(con==null || cmd==null) init();
		ResultSet rs = null;
		try {
			rs = cmd.executeQuery(sql);
		} catch (Exception ex) {
			rs = null;
			ex.printStackTrace();
		}
		return rs;
	}

	/**
	 * close sql connection
	 */
	public static void close() {
		try {
			if(con!=null && !con.isClosed()) con.close();
			con = null;
			cmd = null;
			isInit = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
