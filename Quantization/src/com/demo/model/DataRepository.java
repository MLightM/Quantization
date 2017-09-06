package com.demo.model;

import java.util.ArrayList;
import java.util.List;

public class DataRepository {

	private static DataRepository dataRes = null;

	private DataRepository() {
		bins = new ArrayList<Bin>();
	}
	
	public static synchronized DataRepository getInstance() {
		if(dataRes == null)
			dataRes = new DataRepository();
		
		return dataRes;
	}

	public List<Bin> bins;
	
	public void clearAll() {
		bins.clear();
	}

}
