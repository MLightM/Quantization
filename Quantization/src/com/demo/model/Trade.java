package com.demo.model;

import java.util.Calendar;
import java.util.UUID;

public class Trade {
	
	private UUID uuid;
	
	private String instrument;
	
	private double entryPrice;
	private double exitPrice;
	
	private double stoplossPrice;
	private double profitTargetPrice;

	private double commissionrate;
	private double commission;
	
	private double profit;

	private int quantity;

	private Calendar entryTime;
	private Calendar exitTime;

//	private DirectionType direction;

	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	public String getInstrument() {
		return instrument;
	}
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	
	public double getEntryPrice() {
		return entryPrice;
	}
	public void setEntryPrice(double entryPrice) {
		this.entryPrice = entryPrice;
	}
	
	public double getExitPrice() {
		return exitPrice;
	}
	public void setExitPrice(double exitPrice) {
		this.exitPrice = exitPrice;
	}
	
	public double getStoplossPrice() {
		return stoplossPrice;
	}
	public void setStoplossPrice(double stoplossPrice) {
		this.stoplossPrice = stoplossPrice;
	}
	
	public double getProfitTargetPrice() {
		return profitTargetPrice;
	}
	public void setProfitTargetPrice(double profitTargetPrice) {
		this.profitTargetPrice = profitTargetPrice;
	}
	
	public double getCommissionrate() {
		return commissionrate;
	}
	public void setCommissionrate(double commissionrate) {
		this.commissionrate = commissionrate;
	}
	
	public double getCommission() {
		return commission;
	}
	public void setCommission(double commission) {
		this.commission = commission;
	}
	
	public double getProfit() {
		return profit;
	}
	public void setProfit(double profit) {
		this.profit = profit;
	}
	
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public Calendar getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(Calendar entryTime) {
		this.entryTime = entryTime;
	}
	
	public Calendar getExitTime() {
		return exitTime;
	}
	public void setExitTime(Calendar exitTime) {
		this.exitTime = exitTime;
	}
	
}
