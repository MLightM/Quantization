package com.demo.strategy;

import java.util.Calendar;

import com.demo.dataprocess.DataHandler;
import com.demo.model.Bin;
import com.demo.model.DataRepository;

/**
 * AUD/USD strategy
 * @author w
 *
 */
public class AUD_USD_strategy {
	
	public DataRepository dataRes; // data repository
	public DataHandler dataHandler; // data processing tool
	
	private boolean isON=false; // realtime switch
	private long cycleSec = 4*60*60; // cycle time
	private long RSec = 48*60*60; // R's parameter, hours to seconds
	private long R2Sec = 72*60*60; // R2's parameter, hours to seconds
	private double longlimitPara = 0.12; // long limit's parameter
	private double stoplossPara = 50*0.0001; // stop loss's parameter
	private double profittargetPara = 0.32; // profit target's parameter
	
	public AUD_USD_strategy() {
		dataRes = DataRepository.getInstance();
		dataHandler = new DataHandler();
	}

	public void runSimulation() {
		// get bins data
		if(!dataHandler.getBinData(dataRes))
			return;
		
		// start a thread to back test
		new Thread(new strategy(0.013, 0.019)).start();
	}
	
	/**
	 * strategy thread
	 * @author w
	 *
	 */
	class strategy implements Runnable {
		
		private double para1; // condition para1
		private double para2; // condition para2
		private Calendar lastTime; // last check transaction time
		
		public strategy(double para1, double para2) {
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
			// set the first last check transaction time
			lastTime = dataRes.bins.get(0).getSnapshotdate();
			
			// find the first need to be check transaction time
			for(int startIndex=0; startIndex<dataRes.bins.size(); startIndex++) {
				Bin bin = dataRes.bins.get(startIndex);
				if( (bin.getSnapshotdate().getTimeInMillis()-lastTime.getTimeInMillis()) >= (R2Sec-cycleSec)*1000 ) {
					lastTime=bin.getSnapshotdate();
					point = startIndex;
					break;
				}
			}
			
			double longlimit = 0;
			// start check transaction time
			for(int index=point; index<dataRes.bins.size(); index++) {
				if(index <= 0) continue;
				// current bin
				Bin bin = dataRes.bins.get(index);
				// previous bin
				Bin binPrevious = dataRes.bins.get(index-1);
				
				// traversal trades, determine whether need to sell
				// for(Trade)...
				
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
