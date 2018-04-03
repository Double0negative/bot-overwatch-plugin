package org.mcsg.bot.overwatch;


import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;
import org.mcsg.bot.overwatch.OwUtil.OverwatchUser;
import org.mcsg.bot.util.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
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

		OverwatchUser player = OwUtil.parseInput(user, server, args);

		if(player != null) {
			String url = String.format(cmd.equalsIgnoreCase("statscard") ? plugin.LIVE_STATS_IMAGE_CARD : plugin.LIVE_STATS_IMAGE, URLEncoder.encode(player.id), player.region);

			File file = plugin.getImage(player.id, url);

			if(file != null) {
				chat.sendFile(file);
			} else {
				chat.sendMessage("Error showing stats.");
			}
		} else {
			chat.sendMessage("Account not found");
		}

	}

	@Override
	public String getPermission() {
		return "ow.user.stats";
	}


	@Override
	public String[] getCommand() {
		return a("owstats", "stats", "statscard");
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
