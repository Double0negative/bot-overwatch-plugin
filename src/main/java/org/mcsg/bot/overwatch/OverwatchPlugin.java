package org.mcsg.bot.overwatch;

import org.mcsg.bot.DiscordBot;
import org.mcsg.bot.api.Bot;
import org.mcsg.bot.plugin.BotPlugin;

public class OverwatchPlugin implements BotPlugin {

	DiscordBot bot;
	OverwatchManager manager;

	@Override
	public void onEnable(Bot bot) {
		this.bot = (DiscordBot)bot;
		this.manager = new OverwatchManager();

		bot.getCommandHandler().registerCommand(new StatsCommand(this));
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}
	
	public OverwatchManager getManager() {
		return manager;
	}

}
