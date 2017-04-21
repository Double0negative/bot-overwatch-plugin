package org.mcsg.bot.overwatch;

import com.fasterxml.jackson.databind.JsonNode;

public class StatCache {
	
	public long expire
	public json
	
	public StatCache(json) {
		this.json = json;
		this.expire = System.currentTimeMillis() + 30 * 60 * 1000;
	}
	
	public boolean expired() {
		return System.currentTimeMillis() > expire;
	}
	
}
