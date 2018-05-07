package org.mcsg.bot.overwatch;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;
import org.mcsg.bot.overwatch.OwUtil.OverwatchUser;

public class GraphCommand implements BotCommand{

	private OverwatchPlugin plugin;

	private Map<String, Integer> modifier = new HashMap(){{
		put("h", 1);
		put("d", 24);
		put("w", 168);
		put("m", 720);
	}};

	public GraphCommand (OverwatchPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
			throws Exception {
		OverwatchUser player = null;
		int index = 0;
		if(args.length > 0) {
			player = OwUtil.searchUser(args[0], server);
			if(player != null)
				chat.debug("Found player searching " + args[0] + ",  " + player.id);
		} 
		if(player == null){
			player = OwUtil.getUser(user.getId(), server);
			if(player != null)
				chat.debug("Found player getting user profile " + player.id);
			else {
				chat.sendMessage("Account not found");
				return;
			}
		} else {
			index = 1;
		}
		
		int count = 24;
		int period = 168;
		if(args.length > index) {

			String time = args[index].substring(0, args[index].length() - 1);
			String mod = args[index].substring(args[index].length() - 1);

			try{
				chat.debug("Parsing time " + time + ":" + mod);

				int dur = Integer.parseInt(time);
				if(modifier.containsKey(mod)) {
					dur = dur * modifier.get(mod);
					period = dur;
					index++;
				}
			} catch (Exception e) {
				chat.debug("Time parse failed");

			}
		}
		int startPeriod = period;

		period = Math.max(1, period / count);
		count = Math.min(count, startPeriod);
		
		args = Arrays.copyOfRange(args, index,  args.length);
		String keys = "rank";
		
		if(args.length > 0) {
			chat.debug("Keys: " + Arrays.toString(args));
			keys = String.join(",", args);
		}

		chat.debug("Requesting graph with player=" + player.id + ", period=" + period + ", keys=" + keys );

		String url = String.format(OverwatchPlugin.GRAPH, player.id, period, count, keys);
		chat.sendFile(plugin.getImage(player.id, url));

	}

	@Override
	public String getPermission() {
		return "ow.graph";
	}

	@Override
	public String[] getCommand() {
		return a("graph");
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
