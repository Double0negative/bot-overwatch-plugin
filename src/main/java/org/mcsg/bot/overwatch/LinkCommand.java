package org.mcsg.bot.overwatch;

import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;
import org.mcsg.bot.util.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

public class LinkCommand  implements BotCommand{
	private OverwatchPlugin plugin;

	public LinkCommand(OverwatchPlugin plugin){
		this.plugin = plugin;
	}


	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
			throws Exception {

		String owid = args[0].replace("#", "-");
		String region = "";
		if(args.length > 1)
			region = args[1];


		String url  = String.format(OverwatchPlugin.GET_STATS, owid);
		if(region.length() > 0) {
			url += "region=" + region;
		}
		System.out.println(url);
		JsonNode stats = WebClient.getJson(url);
		if(stats.has("error")) {
			chat.sendMessage("Account not found");
			return;
		}

		JsonNode json = WebClient.getJson(String.format(OverwatchPlugin.ADD_USER, user.getId(), server.getId(), owid, region,  ""));
		if(!json.has("error")) {
			chat.sendMessage("Account linked");
		} else {
			chat.sendMessage(json.get("msg").asText());
		}
	}

	@Override
	public String getPermission() {
		return "ow.user.link";
	}

	@Override
	public String[] getCommand() {
		return a("link");
	}

	@Override
	public String getHelp() {
		return "";
	}

	@Override
	public String getUsage() {
		return "";
	}

}
