package com.demo.model;

import java.util.Calendar;

public class Bin {

	private Calendar snapshotdate;
	private String timezone;
	
	private Calendar openTime;
	private Calendar closeTime;
	
	private double openBid;
	private double highBid;
	private double lowBid;
	private double closeBid;

	private double openAsk;
	private double highAsk;
	private double lowAsk;
	private double closeAsk;
	
	private double volume;
	
	private double pnl;
	
	private int length;

	public Bin() {
		super();
	}

	public Bin(Calendar snapshotdate, String timezone, Calendar openTime, Calendar closeTime, double openBid,
			double highBid, double lowBid, double closeBid, double openAsk, double highAsk, double lowAsk,
			double closeAsk, double volume) {
		super();
		this.snapshotdate = snapshotdate;
		this.timezone = timezone;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.openBid = openBid;
		this.highBid = highBid;
		this.lowBid = lowBid;
		this.closeBid = closeBid;
		this.openAsk = openAsk;
		this.highAsk = highAsk;
		this.lowAsk = lowAsk;
		this.closeAsk = closeAsk;
		this.volume = volume;
	}
	
	public Calendar getSnapshotdate() {
		return snapshotdate;
	}
	public void setSnapshotdate(Calendar snapshotdate) {
		this.snapshotdate = snapshotdate;
	}

	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public Calendar getOpenTime() {
		return openTime;
	}
	public void setOpenTime(Calendar openTime) {
		this.openTime = openTime;
	}

	public Calendar getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(Calendar closeTime) {
		this.closeTime = closeTime;
	}

	public double getOpenBid() {
		return openBid;
	}
	public void setOpenBid(double openBid) {
		this.openBid = openBid;
	}

	public double getHighBid() {
		return highBid;
	}
	public void setHighBid(double highBid) {
		this.highBid = highBid;
	}

	public double getLowBid() {
		return lowBid;
	}
	public void setLowBid(double lowBid) {
		this.lowBid = lowBid;
	}

	public double getCloseBid() {
		return closeBid;
	}
	public void setCloseBid(double closeBid) {
		this.closeBid = closeBid;
	}

	public double getOpenAsk() {
		return openAsk;
	}
	public void setOpenAsk(double openAsk) {
		this.openAsk = openAsk;
	}

	public double getHighAsk() {
		return highAsk;
	}
	public void setHighAsk(double highAsk) {
		this.highAsk = highAsk;
	}

	public double getLowAsk() {
		return lowAsk;
	}
	public void setLowAsk(double lowAsk) {
		this.lowAsk = lowAsk;
	}

	public double getCloseAsk() {
		return closeAsk;
	}
	public void setCloseAsk(double closeAsk) {
		this.closeAsk = closeAsk;
	}

	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getPnl() {
		return pnl;
	}
	public void setPnl(double pnl) {
		this.pnl = pnl;
	}

	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	
}
