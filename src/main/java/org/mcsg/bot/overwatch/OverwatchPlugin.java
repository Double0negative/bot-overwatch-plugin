package org.mcsg.bot.overwatch;

import org.mcsg.bot.DiscordBot;
import org.mcsg.bot.api.Bot;
import org.mcsg.bot.plugin.BotPlugin;

public class OverwatchPlugin implements BotPlugin {

	DiscordBot bot;

	@Override
	public void onEnable(Bot bot) {
		this.bot = (DiscordBot)bot;

		bot.getCommandHandler().registerCommand(new StatsCommand());
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

}
