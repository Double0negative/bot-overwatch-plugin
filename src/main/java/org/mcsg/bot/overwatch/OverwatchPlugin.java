package org.mcsg.bot.overwatch;

import org.mcsg.bot.DiscordBot;
import org.mcsg.bot.api.Bot;
import org.mcsg.bot.plugin.BotPlugin;

public class OverwatchPlugin implements BotPlugin {

	DiscordBot bot;
	
	public static final String HOST = "http://localhost:3000";
	
	public static final String ADD_USER = HOST + "/add-user?discord=%s&server=%s&id=%s&region=%s&platform=%s";
	public static final String GET_STATS = HOST + "/raw-stats?id=%s&";
	public static final String GET_USER = HOST + "/get-user?discord=%s&server=%s";
	public static final String LIVE_STATS_IMAGE = HOST + "/screenshot/live-stats+id=%s";

	@Override
	public void onEnable(Bot bot) {
		this.bot = (DiscordBot)bot;

		bot.getCommandHandler().registerCommand(new StatsCommand(this));
		bot.getCommandHandler().registerCommand(new LinkCommand(this));

	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}
	
}

