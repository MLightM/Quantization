package com.demo;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.demo.dataprocess.HistoryMiner;
import com.demo.strategy.AUD_USD_strategy;
import com.fxcm.fix.FXCMTimingIntervalFactory;
import com.fxcm.fix.IFXCMTimingInterval;

/**
 * main entrance
 * @author w
 *
 */
public class AppEntrance {
	
	public static AppEntrance gApp=null;
	
	public final IFXCMTimingInterval gTimeInterval = FXCMTimingIntervalFactory.MIN15; // bin data time interval
	public final String gRelatedSymbol = "AUD/USD"; // data type
	public final String gTimeZone = "EST"; // time zone
	public final String gUsername = "rkichenama"; // FXCM username
	public final String gPassword = "1311016"; // FXCM password
	public final String gTerminal = "Demo"; // FXCM terminal
	
	public final boolean useCsv = false; // use csv files save data
	public final String csvSavePath = "./csv"; // save path

	public static void main(String[] args) {
		gApp = new AppEntrance();
		
		// start history miner
//		gApp.startMiner();
		
		// start simulation
		gApp.startSimulation();
	}
	
	/**
	 * start history miner
	 */
	public void startMiner() {
		try {
			// create an instance of the JavaFixHistoryMiner
			HistoryMiner miner = new HistoryMiner(gApp.gUsername, gApp.gPassword, gApp.gTerminal);
			// login to the api
			miner.login();
			// retrieve the trader accounts to ensure login process is complete
			miner.retrieveAccounts();
			// display nore that the history display is delayed
			miner.getOutput().println("\t\tGet history data finish.");
			// storage data
			if(useCsv) {
				if(miner.dataProcessAsCSV()) {
					miner.getOutput().println("\t\tStorage history data success.");
				} else {
					miner.getOutput().println("\t\tStorage history data failed.");
				}
			} else {
				if(miner.dataProcess()) {
					miner.getOutput().println("\t\tStorage history data success.");
				} else {
					miner.getOutput().println("\t\tStorage history data failed.");
				}
			}
			// log out of the api
			miner.logout();
			miner.getOutput().println("\t\tLog out of the api and Exit the system.");
			System.exit(0);
		} catch (Exception e) { 
			Logger.getLogger(HistoryMiner.class.getName()).log(Level.SEVERE, null, e);
		}  
	}
	
	/**
	 * start simulation
	 */
	public void startSimulation() {
		AUD_USD_strategy strategy=new AUD_USD_strategy();
		strategy.runSimulation();
	}
	
	/**
	 * start realtime
	 */
	public void startRealtime() {
		
	}
	
}
