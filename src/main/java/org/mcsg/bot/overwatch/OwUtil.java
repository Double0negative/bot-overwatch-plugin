package org.mcsg.bot.overwatch;

import java.io.IOException;

import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;
import org.mcsg.bot.util.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

public class OwUtil {

	public static class OverwatchUser {
		public String id;
		public String region;
	}

	public static OverwatchUser searchUser(String name, BotServer server) {
		OverwatchUser player = getUserByOverwatchId(name);
		
		if(player != null) {
			return player;
		}
		
		name = name.replace("#", "-");
		BotUser searchUser = server.getUserByName(name);

		if(searchUser != null) {
			return getUser(searchUser.getId(), server);
		} else {
			return getRawUser(name);
		}
	}
	
	public static OverwatchUser getRawUser(String owid) {
		JsonNode node = null;
		try {
			node = WebClient.getJson(String.format(OverwatchPlugin.GET_STATS, owid));
			
			if(node.has("error"))
				return null;
			
			OverwatchUser user = new OverwatchUser();
			user.id = owid;
			user.region = "";
			
			return user;
			
		} catch (IOException | UnirestException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static OverwatchUser getUserByOverwatchId(String owid) {
		JsonNode node = null;
		try {
			node = WebClient.getJson(String.format(OverwatchPlugin.GET_USER_BY_OWID, owid));
		} catch (IOException | UnirestException e) {
			e.printStackTrace();
		}
		return parseUser(node);
	}


	public static OverwatchUser getUser(String id, BotServer server) {
		JsonNode node = null;
		try {
			node = WebClient.getJson(String.format(OverwatchPlugin.GET_USER, id, server.getId()));
		} catch (IOException | UnirestException e) {
			e.printStackTrace();
		}
		
		return parseUser(node);
	}

	public static OverwatchUser parseInput(BotUser user, BotServer server, String [] args) {
		if(args.length == 0) {
			return getUser(user.getId(), server);
		} else if(args.length > 1) {
			OverwatchUser player = searchUser(args[0], server);
			if(OverwatchPlugin.REGIONS.contains(args[1])) {
				player.region = args[1];
			}
			return player;
		}else if(args.length > 0) {
			String arg = args[0];
			if(OverwatchPlugin.REGIONS.contains(arg.toLowerCase())) {
				OverwatchUser player = getUser(user.getId(), server);
				player.region = arg.toLowerCase();
				return player;
			} else {
				return searchUser(arg, server);
			}
		} 
		return null;
	}
	
	public static boolean isOverwatchUser(String id) {
		return getUserByOverwatchId(id) != null;
	}
	
	private static OverwatchUser parseUser(JsonNode node) {
		if(node != null && !node.has("error") && node.has("overwatch_id")) {
			OverwatchUser user = new OverwatchUser();

			user.id = node.get("overwatch_id").asText();
			user.region  = node.get("region").asText();
			return user;
		}
		return null;
	}
	
	

}
