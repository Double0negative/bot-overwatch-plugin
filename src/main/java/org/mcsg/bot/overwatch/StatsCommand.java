package org.mcsg.bot.overwatch;

import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;

import com.fasterxml.jackson.databind.JsonNode;

public class StatsCommand implements BotCommand{

	private OverwatchPlugin plugin;
	
	public StatsCommand(OverwatchPlugin plugin){
		this.plugin = plugin;
	}
	
	
	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
			throws Exception {
		
		if(args.length == 1) {
			String player = args[0];
			
			JsonNode root = plugin.getManager().getStats(player);
			JsonNode stats = root.get("stats").get("competitive");
			JsonNode overall = stats.get("overall_stats");
			
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("```\n\n");
			sb.append("Stats for ").append(player).append("\n");
			sb.append("Level: ").append(overall.get("level").asInt() + 100 * (overall.get("prestige").asInt())).append("\n");
			sb.append("Rank: ").append(overall.get("comprank").asInt());
			sb.append("```");
			
			chat.sendMessage(sb.toString());
		}
		
	}

	@Override
	public String getPermission() {
		return "ow.user.stats";
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
