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
		
		if(args.length == 1) {
			plugin.getManager().linkAccount(user.getId(), args[0])
			chat.sendMessage("Linked Overwatch account")
		} else if (args.length == 2){
			plugin.manager.unLinkAccount(user.getId(), args[1])
			chat.sendMessage("Unlinked OVerwatchj account")
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
		return "Link overwatch account to discord account";
	}

	@Override
	public String getUsage() {
		return ".link <account-id>";
	}

}
