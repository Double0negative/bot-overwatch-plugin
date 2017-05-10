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

		try{
			if(args.length == 1) {

				if(args[0] == "delete") {
					plugin.manager.unLinkAccount(user.getId())
					chat.sendMessage("Unlinked OVerwatch account")
				} else {
					plugin.getManager().linkAccount(server.getId(), user, args[0].replace("#", "-"), "")
					chat.sendMessage("Linked Overwatch account")
				}
			} else if (args.length > 1){
				if(args[0] == "verify" && server.getBot().getPermissionManager().hasPermission(server, user,  "ow.verify")){
					plugin.manager.verify(server, args[1])
					chat.sendMessage("Verified account")
				}
				if(args[0] == "delete") {
					plugin.manager.unLinkAccount(server.getId(), args[0], args[1])
					chat.sendMessage("Unlinked OVerwatch account")
				}
				if(args[0] == "other" && server.getBot().getPermissionManager().hasPermission(server,user,  "ow.admin.link")) {
					plugin.getManager().linkAccount(server.getId(), args[1], args[2])
					chat.sendMessage("Linked overwatch account")
				} else {
					plugin.getManager().linkAccount(server.getId(), user, args[0].replace("#", "-"), args[1].toLowerCase())
					chat.sendMessage("Linked Overwatch account")
				}
			}
		}catch (ProfileNotFound e) {
			chat.sendMessage("Profile not found");
			e.printStackTrace()
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
