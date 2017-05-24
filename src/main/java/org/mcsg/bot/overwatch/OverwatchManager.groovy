package org.mcsg.bot.overwatch;

import java.awt.Color
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mcsg.bot.DiscordChannel
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
import sx.blah.discord.util.EmbedBuilder
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

	public void linkAccount(server, user, ow, region) {
		
		try{
			def stats = getStats(server, ow, region)
			def local = this.link[server] ?: [:]
			local[user.getId()] = [id: ow, verified: false, region: region]
			saveLink()
			setRole(server, user.getId(), stats?.competitive?.overall_stats?.comprank)
			try{
				Thread.start {
					getAllStats(server)
				}
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
			def key = it.key
			Thread.start {
					getAllStats(key)
				}
		}
	}

	private saveLink() {
		linkFile.setText(JsonOutput.toJson(link))
		roleFile.setText(JsonOutput.toJson(userRoles))
	}

	public void getAllStats(server) {
		def stats = [:]
		def msgs = [:]
		link[server].each { key, value ->
			stats[key] = getStats(server, value.id, value.region)
		}

		stats = stats.sort { a, b ->
			return ((a?.value?.stats?.competitive?.overall_stats?.comprank ?: 0) > (b.value?.stats?.competitive?.overall_stats?.comprank ?: 0)) ? -1 : 1
		}

		stats.eachWithIndex { element, index ->
			println element.key
			def rank = element.value.stats?.competitive?.overall_stats?.comprank ?: 0
			def user = bot.getUser(element.key);
			if(!user)
				return
			def dname = user.getUsername()
			def verified = element.value.verified

			def role = getRole(rank)?.name
			if(!role)
				role = "unranked"
			def str = msgs[role]

			if(!str)
				str = ""

			str += "**${index + 1})** ${(rank as String).padRight(5)} ${dname}${verified ? '✓' : ''}\n"

			msgs[role] = str

			setRole(server, user.getId(), rank);
		}


		DiscordChannel chat = bot.getChat(bot.getSettings().getString("overwatch.stats-chat." + server)) as DiscordChannel
		chat.clear()

		msgs.each {key, value ->
			EmbedBuilder builder = new EmbedBuilder()
			builder.appendField("#   Rank   Name", value, false)
			def rr = getRoleByName(key)
			def color = rr ? rr.color : "#000000"
			builder.withColor(Color.decode(color))
			
			if(rr) {
				builder.withAuthorIcon(rr.img)
			}
			builder.withAuthorName(key)
			
			chat.sendMessage(builder.build())
		}
	}

	
	def getRoleByName(rank) {
		def role
		roles.each {
			if(it.name == rank || it.role == rank) {
				role = it
			}
		}
		return role
	}

	def setRole(server, user, rank) {
		IGuild guild = ((DiscordServer)bot.getServer(server)).getHandle()
		final IUser iuser = ((DiscordUser)bot.getUser(user)).getHandle()

		def newRole = getRole(rank)

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

	def getRole(rank) {
		def newRole
		for(def role : roles) {
			if(rank > role.rank) {
				newRole = role
			}
		}
		return newRole
	}

	def getStats(String server, String ow, String region) throws ProfileNotFound, RateLimitException{
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
			def platforms = ["pc", "xbl", "psn"]
			def json, platform, root
			platforms.each {
				if(!json ||root?.error){
					def url = StringUtils.replaceVars(API_BLOB, ow, it);
					println url
					json = WebClient.get(url);
					if(json)
						root = slurper.parseText(json)
					platform = it
				}
			}


			if(json) {
				if(root.error) {
					println ow
					println json
					throw new ProfileNotFound()
				}

				def stats = root.us
				println region
				if(region) {
					println "using $region"
					stats = root[region]
				}

				if(!stats)
					stats = root.eu
				if(!stats)
					stats = root.kr
				if(!stats)
					stats = root.any
				


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
