package org.mcsg.bot.overwatch;

import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;

import com.fasterxml.jackson.databind.JsonNode;

import sx.blah.discord.util.EmbedBuilder

public class StatsCommand implements BotCommand{

	private OverwatchPlugin plugin;

	public StatsCommand(OverwatchPlugin plugin){
		this.plugin = plugin;
	}



	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
	throws Exception {

		

	}


	@Override
	public String getPermission() {
		return "ow.user.stats";
	}


	@Override
	public String[] getCommand() {
		return a("owstats", "stats");
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return null;
	}

}
