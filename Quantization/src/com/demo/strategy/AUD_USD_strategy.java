package com.demo.strategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.demo.AppEntrance;
import com.demo.dataprocess.DataHandler;
import com.demo.model.Bin;
import com.demo.model.DataRepository;
import com.demo.model.Trade;

/**
 * AUD/USD strategy
 * @author w
 *
 */
public class AUD_USD_strategy {
	
	public DataRepository dataRes; // data repository
	public DataHandler dataHandler; // data processing tool
	public ExecutorService fixedThreadPool; // fixed thread pool
	
//	private boolean isON=false; // realtime switch
	private int sameTimeThreads=4; // same time run thread counts
	private long cycleSec = 4*60*60; // cycle time
	private long RSec = 48*60*60; // R's parameter, hours to seconds
	private long R2Sec = 72*60*60; // R2's parameter, hours to seconds
	private double longlimitPara = 0.12; // long limit's parameter
	private double stoplossPara = 50*0.0001; // stop loss's parameter
	private double profittargetPara = 0.32; // profit target's parameter
	private ArrayList<double[]> para1and2 = new ArrayList<double[]>(){
		private static final long serialVersionUID = 841241954048446770L;{
		add(new double[]{0.013,0.019}); add(new double[]{0.007,0.015});
//		add(new double[]{0.013,0.019}); add(new double[]{0.012,0.018});
//		add(new double[]{0.013,0.019}); add(new double[]{0.012,0.018});
	}};
	
	public ArrayList<Trade> trades;
	
	public AUD_USD_strategy() {
		dataRes = DataRepository.getInstance();
		dataHandler = new DataHandler();
	    dataHandler.initTable();
	}

	public void runSimulation() {
		// get bins data
		if(!AppEntrance.gApp.useCsv) {
		    if (!dataHandler.bTableExist) {
			    dataHandler.finishDataPorcess();
			    return;
			}
			if(!dataHandler.getBinData(dataRes)) {
			    dataHandler.finishDataPorcess();
				return;
			} else {
			    dataHandler.finishDataPorcess();
			}
			
		} else {
			
		}
		
		if(dataRes.bins==null || dataRes.bins.size()<=0) return;
		
		trades = new ArrayList<Trade>();
		fixedThreadPool = Executors.newFixedThreadPool(sameTimeThreads); 
		// start a thread to back test
		for(int i=0; i<para1and2.size(); i++) {
			fixedThreadPool.execute(new strategySimulation(para1and2.get(i)[0], para1and2.get(i)[1]));
		}
	}
	
	/**
	 * strategy thread
	 * @author w
	 *
	 */
	class strategySimulation implements Runnable {
		
		private double para1; // condition para1
		private double para2; // condition para2
		private Calendar lastTime; // last check transaction time
		
		public strategySimulation(double para1, double para2) {
			if(para1>para2) {
				double tmp=para1;
				para1=para2;
				para2=tmp;
			}
			this.para1=para1;
			this.para2=para2;
		}
		
		public void run() {
			double R=0, R2=0;
			int point=0;
			Bin bin = null;
			Bin binPrevious = null;
			// set the first last check transaction time
			synchronized (dataRes.bins) {
				lastTime = dataRes.bins.get(0).getSnapshotdate();
			}

			// find the first need to be check transaction time
			for(int startIndex=0; startIndex<dataRes.bins.size(); startIndex++) {
				synchronized (dataRes.bins) {
					bin = dataRes.bins.get(startIndex);
				}
				if( (bin.getSnapshotdate().getTimeInMillis()-lastTime.getTimeInMillis()) >= (R2Sec-cycleSec)*1000 ) {
					lastTime=bin.getSnapshotdate();
					point = startIndex;
					break;
				}
			}
			if(point<=0) return;
			
			double longlimit = 0;
			// start check transaction time
			for(int index=point; index<dataRes.bins.size(); index++) {
				if(index <= 0) continue;
				synchronized (dataRes.bins) {
					// current bin
					bin = dataRes.bins.get(index);
					// previous bin
					binPrevious = dataRes.bins.get(index-1);
				}
				
				// whether to reach the cycle
				if( (bin.getSnapshotdate().getTimeInMillis()-lastTime.getTimeInMillis()) >= cycleSec*1000 ) {
					lastTime = bin.getSnapshotdate();
					point = index;
					
					// get R2
					R2 = getHighestAsk(point, R2Sec) - getLowestAsk(point, R2Sec);
					// total condition: para1<=R2<=para2
					if(R2<para1 || R2>para2) continue;
					
					// get R
					R = getHighestAsk(point, RSec) - getLowestAsk(point, RSec);
					// get long limit
					longlimit = bin.getOpenAsk() - R*longlimitPara;
					// whether to buy
					if(bin.getLowAsk()<=longlimit && binPrevious.getCloseAsk()>longlimit) {
//						new Trade()...
						
					}
				}
				
				Trade trade = null;
				// traversal trades, determine whether need to sell
				for(int tdIndex=0; tdIndex<trades.size(); tdIndex++) {
					trade = trades.get(tdIndex);
					if( Math.abs(bin.getLowAsk()-trade.getEntryPrice()) > stoplossPara
							|| Math.abs(bin.getCloseAsk()-trade.getEntryPrice()) > R*profittargetPara ) {
						//sold out
						
					}
				}
				
			}
		}
		
	}
	
	/**
	 * get the highest ask
	 * @param point current bin index
	 * @param limitSec the limit seconds
	 * @return
	 */
	private double getHighestAsk(int point, long limitSec) {
		double dHighestAsk=0;
		for(int i=point; i>=0; i--) {
			Bin bin = dataRes.bins.get(i);
			
			// determine whether the time exceeds the limitSec
			long dvalue = dataRes.bins.get(point).getSnapshotdate().getTimeInMillis() - bin.getSnapshotdate().getTimeInMillis();
			if(dvalue >= limitSec*1000)
				break;
			
			if(dHighestAsk == 0) {
				dHighestAsk=bin.getHighAsk();
				continue;
			}
			
			if(bin.getHighAsk() > dHighestAsk)
				dHighestAsk=bin.getHighAsk();
		}
		return dHighestAsk;
	}

	/**
	 * get the lowest ask
	 * @param point current bin index
	 * @param limitSec the limit seconds
	 * @return
	 */
	private double getLowestAsk(int point, long limitSec) {
		double dLowestAsk=0;
		for(int i=point; i>=0; i--) {
			Bin bin = dataRes.bins.get(i);
			
			// determine whether the time exceeds the limitSec
			long dvalue = dataRes.bins.get(point).getSnapshotdate().getTimeInMillis() - bin.getSnapshotdate().getTimeInMillis();
			if(dvalue >= limitSec*1000)
				break;
			
			if(dLowestAsk == 0) {
				dLowestAsk=bin.getLowAsk();
				continue;
			}
			
			if(bin.getLowAsk() < dLowestAsk)
				dLowestAsk=bin.getLowAsk();
		}
		return dLowestAsk;
	}
	
}
