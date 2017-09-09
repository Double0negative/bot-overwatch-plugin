package org.mcsg.bot.overwatch;


import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;
import org.mcsg.bot.util.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.imageio.ImageIO;


public class StatsCommand implements BotCommand{

	private OverwatchPlugin plugin;

	public StatsCommand(OverwatchPlugin plugin){
		this.plugin = plugin;
	}



	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
			throws Exception {

		String player = "";
		String region = "";
		boolean search = false;
		if(args.length == 0) {
			player = user.getUsername();
			search = true;
		}
		else if(args.length > 0) {
			player = args[0].replace("#", "-");
		}
		if(search || !player.contains("-")) {
			System.out.println("Searching for " + player);
			BotUser searchUser = server.getUserByName(player);
			if(searchUser != null) {
				System.out.println(String.format(plugin.GET_USER, searchUser.getId(), server.getId()));
				JsonNode node = WebClient.getJson(String.format(plugin.GET_USER, searchUser.getId(), server.getId()));
				if(!node.has("error") && node.has("overwatch_id")) {
					player = node.get("overwatch_id").asText();
				}
			}
		}

		File file = new File(server.getBot().getSettings().getDataFolder(), "stats_" + new Date().getTime() + ".png");
		BufferedImage image = null;
		try {
			URL url = new URL(String.format(plugin.LIVE_STATS_IMAGE, player));
			image = ImageIO.read(url);

			ImageIO.write( image, "png",file);
			//chat.sendMessage("http://localhost:3000/screenshot/live-stats+id=${player}");
			chat.sendFile(file);
		} catch (IOException e) {
		}


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
