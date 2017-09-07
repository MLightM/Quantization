package com.demo.util;

import java.util.UUID;

public class IDGenerator {
	
	public static String getId() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
}
