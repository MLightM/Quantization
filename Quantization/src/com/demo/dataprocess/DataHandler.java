package com.demo.dataprocess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.demo.AppEntrance;
import com.demo.model.Bin;
import com.demo.model.DataRepository;
import com.demo.util.DBHelper;

/**
 * data processing
 * @author w
 *
 */
public class DataHandler {

    private SimpleDateFormat sdf;
    private String sTableName; // mysql table name
    private String sCreateTable; // sql to create a table
    
    public boolean bTableExist=false; // table is existed
	
	public DataHandler() {
		// define a format for the dates
    	sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    // set time zone
	    sdf.setTimeZone(TimeZone.getTimeZone(AppEntrance.gApp.gTimeZone));
	    // initialization table
	}
	
	/**
	 * initialize the table
	 * @return
	 */
	public void initTable() {
		// get table name
		sTableName = AppEntrance.gApp.gRelatedSymbol.toLowerCase().replace("/", "-") + "_" + 
					AppEntrance.gApp.gTimeInterval.getClass().getSimpleName().toLowerCase();
		
		// create table
	    sCreateTable = "CREATE TABLE IF NOT EXISTS `"+sTableName+"` (" + 
			    		"  `snapshotdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," + 
			    		"  `timezone` varchar(10) NOT NULL DEFAULT 'EST'," + 
			    		"  `opentime` timestamp NOT NULL DEFAULT '2000-01-01 00:00:00'," + 
			    		"  `closetime` timestamp NOT NULL DEFAULT '2000-01-01 00:00:00'," + 
			    		"  `openbid` double DEFAULT 0.0," + 
			    		"  `highbid` double DEFAULT 0.0," + 
			    		"  `lowbid` double DEFAULT 0.0," + 
			    		"  `closebid` double DEFAULT 0.0," + 
			    		"  `openask` double DEFAULT 0.0," + 
			    		"  `highask` double DEFAULT 0.0," + 
			    		"  `lowask` double DEFAULT 0.0," + 
			    		"  `closeask` double DEFAULT 0.0," + 
			    		"  `volume` double DEFAULT 0.0," + 
			    		"  PRIMARY KEY (`snapshotdate`,`timezone`)" + 
			    		") ENGINE=myisam DEFAULT CHARSET=UTF8;";
	    DBHelper.init();
	    bTableExist = DBHelper.executeUpdate(sCreateTable);
	}
	
	/**
	 * close mysql driver
	 */
	public void finishDataPorcess() {
		DBHelper.close();
	}
	
	/**
	 * storage bins data
	 * @param bin
	 * @return
	 */
	public boolean storageBinData(Bin bin) {
		if(!DBHelper.isInit) DBHelper.init();
		
		String sql = "replace into `"+sTableName+"` values("
				+ " '"+ sdf.format(bin.getSnapshotdate().getTime()) +"', '"+AppEntrance.gApp.gTimeZone+"', "
				+ " '"+ sdf.format(bin.getOpenTime().getTime()) +"', '"+ sdf.format(bin.getCloseTime().getTime()) +"', "
				+ " "+bin.getOpenBid()+", "+bin.getHighBid()+", "+bin.getLowBid()+", "+bin.getCloseBid()+", "
				+ " "+bin.getOpenAsk()+", "+bin.getHighAsk()+", "+bin.getLowAsk()+", "+bin.getCloseAsk()+", "
				+ " "+bin.getVolume()+" );";
		return DBHelper.executeUpdate(sql);
	}
	
	/**
	 * get bins data from mysql
	 * @param dataRes data repository
	 * @return
	 */
	public boolean getBinData(DataRepository dataRes) {
		if(!DBHelper.isInit) DBHelper.init();
		
		String sql = "select * from `"+sTableName+"`";
		ResultSet rs=DBHelper.executeQuery(sql);
		Bin bin=null;
	    Calendar snapshotdate = null;
	    Calendar openTime = null;
	    Calendar closeTime = null;
		try {
			if(rs == null) return false;
			while(rs.next()) {
		        snapshotdate = new GregorianCalendar();
		        snapshotdate.setTime(new Date(rs.getTimestamp("snapshotdate").getTime()));
		        openTime = new GregorianCalendar();
		        openTime.setTime(new Date(rs.getTimestamp("opentime").getTime()));
		        closeTime = new GregorianCalendar();
		        closeTime.setTime(new Date(rs.getTimestamp("closetime").getTime()));
				bin = new Bin(snapshotdate, rs.getString("timezone"), openTime, closeTime, 
		        		rs.getDouble("openbid"), rs.getDouble("highbid"), rs.getDouble("lowbid"), rs.getDouble("closebid"), 
		        		rs.getDouble("openask"), rs.getDouble("highask"), rs.getDouble("lowask"), rs.getDouble("closeask"), 
						rs.getDouble("volume"));
				dataRes.bins.add(bin);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private FileWriter fileWriter = null; 
	/**
	 * storage bins data into .csv files
	 * @param bin
	 * @return
	 */
	public boolean storageBinDataAsCSV(Bin bin) {
		try {
			if(sTableName==null || sTableName.length()<=0)
				sTableName = AppEntrance.gApp.gRelatedSymbol.toLowerCase().replace("/", "-") + "_" + 
							AppEntrance.gApp.gTimeInterval.getClass().getSimpleName().toLowerCase();
			
        	File folder = new File(AppEntrance.gApp.csvSavePath);
        	if(!folder.exists()) folder.mkdirs();
        	if(!folder.exists()) return false;
        	
			if(fileWriter == null) {
				fileWriter = new FileWriter(folder.getAbsolutePath() + "/" + sTableName + ".csv", true);
				fileWriter.write("snapshotdate,timezone,opentime,closetime,openbid,highbid,lowbid,closebid,openask,highask,lowask,closeask,volume" + "\r\n");
			}
			
			String binStr = sdf.format(bin.getSnapshotdate().getTime()) +","+AppEntrance.gApp.gTimeZone+","
							+ sdf.format(bin.getOpenTime().getTime()) +","+ sdf.format(bin.getCloseTime().getTime()) +","
							+ bin.getOpenBid()+","+bin.getHighBid()+","+bin.getLowBid()+","+bin.getCloseBid()+","
							+ bin.getOpenAsk()+","+bin.getHighAsk()+","+bin.getLowAsk()+","+bin.getCloseAsk()+","
							+ bin.getVolume();
			fileWriter.write(binStr + "\r\n");
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * close file writer
	 */
	public boolean finishFileWriter() {
		try {
			if(fileWriter!=null) {
				fileWriter.flush();
				fileWriter.close();
				fileWriter = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
