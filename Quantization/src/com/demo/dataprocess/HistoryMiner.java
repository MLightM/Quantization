package com.demo.dataprocess;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.FXCMSample.JavaFixHistoryMiner;
import com.demo.AppEntrance;
import com.demo.model.Bin;
import com.fxcm.external.api.transport.FXCMLoginProperties;
import com.fxcm.external.api.transport.GatewayFactory;
import com.fxcm.external.api.transport.IGateway;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.external.api.transport.listeners.IStatusMessageListener;
import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.SubscriptionRequestTypeFactory;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;
import com.fxcm.fix.posttrade.CollateralReport;
import com.fxcm.fix.pretrade.MarketDataRequest;
import com.fxcm.fix.pretrade.MarketDataRequestReject;
import com.fxcm.fix.pretrade.MarketDataSnapshot;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.messaging.ISessionStatus;
import com.fxcm.messaging.ITransportable;

/**
 * the miner to get the history data
 * @author w
 *
 */
public class HistoryMiner implements IGenericMessageListener, IStatusMessageListener {

	private static final String server = "http://www.fxcorporate.com/Hosts.jsp";
	
	private FXCMLoginProperties login;
	private IGateway gateway;
	private final Object requestSync = new Object();
	private String currentRequest;
	private boolean requestComplete;

	private final ArrayList<CollateralReport> accounts = new ArrayList<CollateralReport>();
	private final HashMap<UTCDate, MarketDataSnapshot> historicalRates = new HashMap<UTCDate, MarketDataSnapshot>();
	 
	private static PrintWriter output = new PrintWriter((OutputStream)System.out, true);
	public PrintWriter getOutput() { return output; }
	public void setOutput(PrintWriter newOutput) { output = newOutput; }

	public HistoryMiner(String username, String password, String terminal, String file) {
	    // if file is not specified
	    if(file == null)
	      // create a local LoginProperty
	      this.login = new FXCMLoginProperties(username, password, terminal, server);
	    else
	      this.login = new FXCMLoginProperties(username, password, terminal, server, file);
	}
	
	public HistoryMiner(String username, String password, String terminal) {
		// call the proper constructor
		this(username, password, terminal, null);
	}
	
	public HistoryMiner(String[] args) {
		// call the proper constructor
		this(args[0], args[1], args[2], null);
	}
	
	public boolean login() {
		return this.login(this, this);
	}
	
	public boolean login(IGenericMessageListener genericMessageListener, IStatusMessageListener statusMessageListener) {
		try {
			// if the gateway has not been defined
			if(gateway == null)
				// assign it to a new gateway created by the factory
				gateway = GatewayFactory.createGateway();
			
			// register the generic message listener with the gateway
			gateway.registerGenericMessageListener(genericMessageListener);
			// register the status message listener with the gateway
			gateway.registerStatusMessageListener(statusMessageListener);
			
			// if the gateway has not been connected
			if(!gateway.isConnected()) {
				// attempt to login with the local login properties
				gateway.login(this.login);
			} else {
				// attempt to re-login to the api
				gateway.relogin();
			}
			
			synchronized(requestSync) {
				// set the state of the request to be incomplete
				requestComplete = false;
				// request the current trading session status
				currentRequest = gateway.requestTradingSessionStatus();
				// wait until the request is complete
				while(!requestComplete) {
					requestSync.wait();
				}
			}
			// return that this process was successful
			return true;
		} catch(Exception e) { 
			Logger.getLogger(JavaFixHistoryMiner.class.getName()).log(Level.SEVERE, null, e); 
		}
		// if any error occurred, then return that this process failed
		return false;
	}
	
	public void logout() {
		this.logout(this, this);
	}
	
	public void logout(IGenericMessageListener genericMessageListener, IStatusMessageListener statusMessageListener) {
		// attempt to logout of the api
//		gateway.logout();
		// remove the generic message listener, stop listening to updates
		gateway.removeGenericMessageListener(genericMessageListener);
		// remove the status message listener, stop listening to status changes
		gateway.removeStatusMessageListener(statusMessageListener);
	}
	
	public void retrieveAccounts() {
		// if the gateway is null then attempt to login
	    if(gateway == null) this.login();
	    try {
	    	synchronized ( requestSync ) {
	    		// set the state of the request to be incomplete
	    		requestComplete = false;
	    		// request the refresh of all collateral reports
	    		currentRequest = gateway.requestAccounts();
	    		// wait until all the reqports have been processed
	    		while(!requestComplete) {
	    			requestSync.wait();
	    		}
	    	}
	    } catch (InterruptedException ex) {
	    	Logger.getLogger(JavaFixHistoryMiner.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	
	public String sendRequest(ITransportable request) {
		try {
			synchronized (requestSync) {
				// set the completion status of the requst to false
				requestComplete = false;
				// send the request message to the api
				currentRequest = gateway.sendMessage(request);
				// The example code wasn't waiting here, this was commented out
				// wait until the api answers on this particular request
				// while(!requestComplete) {}
				// if there is a value to return, it will be passed by currentResult
			}
			return currentRequest;
		} catch(Exception e) { 
			Logger.getLogger(JavaFixHistoryMiner.class.getName()).log(Level.SEVERE, null, e); 
		}
		// if an error occured, return no result
		return null;
	}
	
	@Override
	public void messageArrived(ISessionStatus status) {
		// check to the status code
		if(status.getStatusCode() == ISessionStatus.STATUSCODE_ERROR ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTING ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_CONNECTING ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_CONNECTED ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_CRITICAL_ERROR ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_EXPIRED ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_LOGGINGIN ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_LOGGEDIN ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_PROCESSING ||
				status.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTED) {
	      // display status message
	      output.println("\t\t" + status.getStatusMessage());
	    }
	}
	
	@Override
	public void messageArrived(ITransportable message) {
		// decide which child function to send an cast instance of the message
	    try {
	    	// if it is an instance of CollateralReport, process the collateral report 
	    	if(message instanceof CollateralReport) 
	    		messageArrived((CollateralReport)message);
	    	
	    	// if it is an instance of MarketDataSnapshot, process the historical data 
	    	if(message instanceof MarketDataSnapshot) 
	    		messageArrived((MarketDataSnapshot)message);
	    	
	    	// if it is an instance of MarketDataRequestReject, process the historical data request error
	    	if(message instanceof MarketDataRequestReject) 
	    		messageArrived((MarketDataRequestReject)message);
	    	
	    	// if the message is an instance of TradingSessionStatus, cast it and send to child function
	    	else if(message instanceof TradingSessionStatus) 
	    		messageArrived((TradingSessionStatus)message);
	    }
	    catch(Exception e) {
	    	e.printStackTrace(output);
	    }
	}

	public void messageArrived(CollateralReport cr) {
		synchronized (requestSync) {
			// if this report is the result of a direct request by a waiting process
			if(currentRequest.equals(cr.getRequestID()) && !accounts.contains(cr)) {
				// add the trading account to the account list
				accounts.add(cr);
		        // set the state of the request to be completed only if this is the last collateral report
		        // requested
				requestComplete = cr.isLastRptRequested();
		        requestSync.notify();
			}
		}
	}
	
	public void messageArrived(TradingSessionStatus tss) {
		synchronized (requestSync) {
			// check to see if there is a request from main application for a session update
			if(currentRequest.equals(tss.getRequestID())) {
				// set that the request is complete for any waiting thread
		        requestComplete = true;
		        requestSync.notify();
		        // attempt to set up the historical market data request
		        try {
		        	// create a new market data request
		        	MarketDataRequest mdr = new MarketDataRequest();
		        	// set the subscription type to ask for only a snapshot of the history
		        	mdr.setSubscriptionRequestType(SubscriptionRequestTypeFactory.SNAPSHOT);
		        	// request the response to be formated FXCM style
		        	mdr.setResponseFormat(IFixDefs.MSGTYPE_FXCMRESPONSE);
		        	// set the intervale of the data candles
		        	mdr.setFXCMTimingInterval(AppEntrance.gApp.gTimeInterval);
		        	// set the type set for the data candles
		        	mdr.setMDEntryTypeSet(MarketDataRequest.MDENTRYTYPESET_ALL);
		        	// configure the start and end dates
		        	Date now = new Date();
		        	Calendar calendar = (Calendar)Calendar.getInstance().clone();
		        	calendar.setTime(now);
		        	calendar.add(Calendar.DAY_OF_MONTH, -20);
		        	Date beforeNow = calendar.getTime();
		        	// set the dates and times for the market data request
		        	mdr.setFXCMStartDate(new UTCDate(beforeNow));
		        	mdr.setFXCMStartTime(new UTCTimeOnly(beforeNow));
		        	mdr.setFXCMEndDate(new UTCDate(now));
		        	mdr.setFXCMEndTime(new UTCTimeOnly(now));
		        	// set the instrument on which the we want the historical data
		        	mdr.addRelatedSymbol(tss.getSecurity(AppEntrance.gApp.gRelatedSymbol));
		        	// send the request
		        	sendRequest(mdr);
		        } catch(Exception e) { 
		        	Logger.getLogger(JavaFixHistoryMiner.class.getName()).log(Level.SEVERE, null, e);
		        }
			}
		}
	}
	
	public void messageArrived(MarketDataRequestReject mdrr) {
		synchronized (requestSync) {
			// display note consisting of the reason the request was rejected
			output.println("Historical data rejected; " + mdrr.getMDReqRejReason());
			// set the state of the request to be complete
			requestComplete = true;
			requestSync.notify();
		}
	}
	
	public void messageArrived(MarketDataSnapshot mds) {
		synchronized (requestSync) {
			// if the market data snapshot is part of the answer to a specific request
			try {
				if(mds.getRequestID() != null && mds.getRequestID().equals(currentRequest)) {
					// add that snapshot to the historicalRates table
					synchronized(historicalRates) { historicalRates.put(mds.getDate(), mds); }
					// set the request to be complete only if the continuous flaf is at the end
					requestComplete = (mds.getFXCMContinuousFlag() == IFixDefs.FXCMCONTINUOUS_END);
					requestSync.notify();
				}
			} catch (Exception e) {
				Logger.getLogger(JavaFixHistoryMiner.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	}
	
	/**
	 * process the bins data
	 * @return
	 */
	public boolean dataProcess() {
		// get the keys for the historicalRates table into a sorted list
	    SortedSet<UTCDate> candle = new TreeSet<UTCDate>(historicalRates.keySet());

	    DataHandler dataHandler = new DataHandler();
	    if (!dataHandler.bTableExist) {
		    dataHandler.finishDataPorcess();
		    return false;
		}
	    
	    Bin bin = null;
	    Calendar snapshotdate = null;
	    Calendar openTime = null;
	    Calendar closeTime = null;
	    for(UTCDate date : candle) {
	    	// create a single instance of the snapshot
	        MarketDataSnapshot candleData;
	        synchronized(historicalRates) {
	        	candleData = historicalRates.get(date);
	        }
	        
	        snapshotdate = new GregorianCalendar();
	        snapshotdate.setTime(candleData.getDate().toDate());
	        openTime = new GregorianCalendar();
	        openTime.setTime(candleData.getOpenTimestamp().toDate());
	        closeTime = new GregorianCalendar();
	        closeTime.setTime(candleData.getCloseTimestamp().toDate());
	        
	        bin = new Bin(snapshotdate, AppEntrance.gApp.gTimeZone, openTime, closeTime, 
	        		candleData.getBidOpen(), candleData.getBidHigh(), candleData.getBidLow(), candleData.getBidClose(), 
	        		candleData.getAskOpen(), candleData.getAskHigh(), candleData.getAskLow(), candleData.getAskClose(), 
	        		candleData.getTickVolume());
	        if(!dataHandler.storageBinData(bin))
	        	break;
	    }
	    dataHandler.finishDataPorcess();
	    return true;
	}
	
}
