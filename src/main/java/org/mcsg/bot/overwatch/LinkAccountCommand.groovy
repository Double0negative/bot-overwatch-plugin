package org.mcsg.bot.overwatch;

import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;

public class LinkAccountCommand implements BotCommand{

	private OverwatchPlugin plugin

	public LinkAccountCommand( plugin) {
		this.plugin = plugin
	}


	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
	throws Exception {

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
		return "Link overwatch account to discord account";
	}

	@Override
	public String getUsage() {
		return ".link <account-id>";
	}

}
