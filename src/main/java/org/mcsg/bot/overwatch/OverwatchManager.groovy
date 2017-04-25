package org.mcsg.bot.overwatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mcsg.bot.DiscordServer
import org.mcsg.bot.DiscordUser
import org.mcsg.bot.api.Bot
import org.mcsg.bot.api.BotChannel
import org.mcsg.bot.api.BotServer
import org.mcsg.bot.api.BotUser
import org.mcsg.bot.util.StringUtils;
import org.mcsg.bot.util.WebClient;
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuffer.IVoidRequest
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class OverwatchManager {

	private static String API_BLOB;

	public Map link = [:]
	public Map cache = [:]
	public List roles = []
	public Map userRoles = [:]

	private Bot bot
	private File linkFile
	private File roleFile
	private JsonSlurper slurper

	public OverwatchManager(Bot bot) {
		this.bot = bot;
		API_BLOB = bot.getSettings().getString("overwatch.apiurl")
		this.linkFile = new File(bot.getSettings().getSettingsFolder(), "ow_linked.json");
		this.roleFile = new File(bot.getSettings().getSettingsFolder(), "ow_roles.json");

		this.slurper = new JsonSlurper()

		roles = bot.getSettings().get("overwatch.ranks")
		roles.sort { a, b ->
			return a.rank <=> b.rank
		}
		loadLink()
	}

	public void linkAccount(server, user, ow) {
		try{getStats(server, ow)
			def local = this.link[server] ?: [:]
			local[user.getId()] = [id: ow, verified: false]
			saveLink()
			try{
				getAllStats(server)
			} catch (Exception e){}
		}catch(ProfileNotFound e) {
			throw new ProfileNotFound()
		}
	}

	public void unLinkAccount(server, user) {
		this.link?.get(server)?.remove(user)
		saveLink()
	}

	public void verify(server, user) {
		this.link[server][user].verified = true;
		saveLink()
		try{
			getAllStats(server)
		} catch (Exception e){}
	}

	private loadLink() {
		if(!linkFile.exists())
			linkFile.createNewFile()
		else
			this.link = slurper.parse(linkFile)

		if(!roleFile.exists())
			roleFile.createNewFile()
		else
			this.userRoles = slurper.parse(roleFile)

		this.link.each{
			getAllStats(it.key)
		}
	}

	private saveLink() {
		linkFile.setText(JsonOutput.toJson(link))
		roleFile.setText(JsonOutput.toJson(userRoles))
	}

	public void getAllStats(server) {
		def stats = [:]
		link[server].each { key, value ->
			stats[key] = getStats(server, value.id)
		}

		stats = stats.sort { a, b ->
			return ((a?.value?.stats?.competitive?.overall_stats?.comprank ?: 0) > (b.value?.stats?.competitive?.overall_stats?.comprank ?: 0)) ? -1 : 1
		}

		def str = "```"

		stats.eachWithIndex { element, index ->
			println element.key
			def rank = element.value.stats?.competitive?.overall_stats?.comprank ?: 0
			def user = bot.getUser(element.key);
			def dname = user.getUsername()
			def verified = element.value.verified
			if(index < 25)
				str += "${index + 1}. ${(rank as String).padRight(5)} ${dname}${verified ? '✓' : ''}\n"

			setRole(server, user.getId(), rank);
		}

		str += "```"

		BotChannel chat = bot.getChat(bot.getSettings().getString("overwatch.stats-chat." + server))
		chat.clear()
		chat.sendMessage(str)
	}


	def setRole(server, user, rank) {
		IGuild guild = ((DiscordServer)bot.getServer(server)).getHandle()
		final IUser iuser = ((DiscordUser)bot.getUser(user)).getHandle()
		def newRole
		for(def role : roles) {
			if(rank > role.rank) {
				newRole = role
			}
		}

		if(!newRole) {
			println "Could not find role for ${user}, ${rank}"
			return
		}

		if(!userRoles[server])
			userRoles[server] = [:]

		def prevRole = userRoles[server][user];

		if(prevRole != newRole.role) {
			IRole prole = guild.getRolesByName(prevRole)[0]
			IRole nrole = guild.getRolesByName(newRole.role)[0]

			def req = {
				if (prole)
					iuser.removeRole(prole)
				iuser.addRole(nrole)
			} as IVoidRequest;

			RequestBuffer.request req
		}

		userRoles[server][user] = newRole.role;
		saveLink()

	}

	def getStats(String server, String ow) throws ProfileNotFound, RateLimitException{
		if(!ow.contains("#") && !ow.contains("-")) {
			def searchUser = bot.getServer(server).getUserByName(ow);
			if(searchUser) {
				ow = link[server][searchUser.getId()]?.id
			}
		}

		if(ow == null) {
			throw new ProfileNotFound()
		}

		def statsc = cache[ow]
		if(statsc != null && !statsc.expired()) {
			return statsc.json;
		}
		try {
			def platforms = ["pc", "xbl", "ps4"]
			def json, platform
			platforms.each {
				if(!json){
					json = WebClient.get(StringUtils.replaceVars(API_BLOB, ow, it));
					platform = it
				}
			}


			if(json) {
				def root = slurper.parseText(json)

				if(root.error) {
					println ow
					println json
					throw new ProfileNotFound()
				}

				def stats = root.us

				if(!stats)
					stats = root.eu
				if(!stats)
					stats = root.kr



				statsc = new StatCache(stats)
				cache[ow] = statsc

				stats.user = ow;
				stats.platform = platform
				return stats;
			}
		} catch (IOException | UnirestException e) {
			e.printStackTrace();

			throw new ProfileNotFound();
		}
		return null;
	}
}
