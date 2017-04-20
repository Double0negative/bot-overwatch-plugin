package org.mcsg.bot.overwatch;

import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;

public class StatsCommand implements BotCommand{

	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
			throws Exception {
		
		chat.sendMessage("It works");
		
	}

	@Override
	public String getPermission() {
		return "ow.stats";
	}


	@Override
	public String[] getCommand() {
		return a("owstats");
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
