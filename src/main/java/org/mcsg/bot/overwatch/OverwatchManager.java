package org.mcsg.bot.overwatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mcsg.bot.util.StringUtils;
import org.mcsg.bot.util.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;

public class OverwatchManager {

	private static final String API_BLOB = "https://owapi.net/api/v3/u/{0}/blob";

	public Map<String, String> link = new HashMap<>();
	public Map<String, StatCache> statsCache = new HashMap<>();

	private ObjectMapper mapper = new ObjectMapper();

	public void linkAccount(String user, String ow) {
		this.link.put(user, ow);
	}

	public void unLinkAccount(String user) {
		this.link.remove(user);
	}


	public JsonNode getStats(String ow) throws ProfileNotFound, RateLimitException{
		StatCache cache = statsCache.get(ow);
		if(cache != null && !cache.expired()) {
			return cache.json;
		}
		try {
			String json = WebClient.get(StringUtils.replaceVars(API_BLOB, ow));
			JsonNode root = mapper.readTree(json);

			if(root.has("error")) {
				int error = root.get("error").asInt();
				if(error == 404) {
					throw new ProfileNotFound();
				} else if(error == 429) {
					throw new RateLimitException();
				}
			} else {
				JsonNode stats = null;
				stats = root.get("us");
				if(stats == null) {
					stats = root.get("eu");
				}
				if(stats == null) {
					stats = root.get("kr");
				}
				if(stats != null) {
					statsCache.put(ow, new StatCache(stats));
					return stats;
				} else
					return null;
			}
		} catch (IOException | UnirestException e) {
			e.printStackTrace();

			throw new ProfileNotFound();
		}
		return null;
	}
}
