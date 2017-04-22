package org.mcsg.bot.overwatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mcsg.bot.DiscordUser
import org.mcsg.bot.api.Bot
import org.mcsg.bot.api.BotChannel
import org.mcsg.bot.util.StringUtils;
import org.mcsg.bot.util.WebClient;
import sx.blah.discord.handle.obj.IUser
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class OverwatchManager {

	private static final String API_BLOB = "http://localhost:4444/api/v3/u/{0}/blob";

	public Map link = [:]
	public Map cache = [:]

	private Bot bot
	private File linkFile
	private JsonSlurper slurper

	public OverwatchManager(Bot bot) {
		this.bot = bot;
		this.linkFile = new File(bot.getSettings().getSettingsFolder(), "ow_linked.json");
		this.slurper = new JsonSlurper()
		loadLink()
	}

	public void linkAccount(user, ow) {
		this.link[user] = [id: ow, verified: false]
		saveLink()
		getAllStats()
	}

	public void unLinkAccount(user) {
		this.link.remove(user)
		saveLink()
	}
	
	public void verify(user) {
		println user
		this.link[user].verified = true;
		saveLink()
		getAllStats()
	}

	private loadLink() {
		if(!linkFile.exists())
			linkFile.createNewFile()
		else
			this.link = slurper.parse(linkFile)
		getAllStats()
	}

	private saveLink() {
		linkFile.setText(JsonOutput.toJson(link))
	}

	public void getAllStats() {
		def stats = [:]
		link.each { key, value ->
			stats[key] = getStats(value.id)
		}
		
		stats = stats.sort { a, b ->
			return ((a?.value?.stats?.competitive?.overall_stats?.comprank ?: 0) > (b.value?.stats?.competitive?.overall_stats?.comprank ?: 0)) ? -1 : 1
		}		
		
		def str = "```"
		
		stats.eachWithIndex { element, index ->
			def rank = element.value.stats?.competitive?.overall_stats?.comprank ?: 0
			def dname = bot.getUser(element.key).getUsername()
			def verified = link[element.key].verified
			str += "${index + 1}. ${(rank as String).padRight(5)} ${dname}${verified ? '✓' : ''}\n"
		}

		str += "```"
		BotChannel chat = bot.getChat(bot.getSettings().get("overwatch.stats-chat"))
		chat.clear()
		chat.sendMessage(str)
		chat.sendMessage("To link accout, use the command .link <account>")
	}


	def getStats(ow) throws ProfileNotFound, RateLimitException{
		def statsc = cache[ow]
		if(statsc != null && !statsc.expired()) {
			return statsc.json;
		}
		try {
			def json = WebClient.get(StringUtils.replaceVars(API_BLOB, ow));

			if(json) {
				def root = slurper.parseText(json)
				def stats = root.us

				if(!stats)
					stats = root.eu
				if(!stats)
					stats = root.kr



				statsc = new StatCache(stats)
				cache[ow] = statsc

				return stats;
			}
		} catch (IOException | UnirestException e) {
			e.printStackTrace();

			throw new ProfileNotFound();
		}
		return null;
	}
}
