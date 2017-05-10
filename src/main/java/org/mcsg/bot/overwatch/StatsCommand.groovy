package org.mcsg.bot.overwatch;

import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotCommand;
import org.mcsg.bot.api.BotServer;
import org.mcsg.bot.api.BotUser;

import com.fasterxml.jackson.databind.JsonNode;

import sx.blah.discord.util.EmbedBuilder

public class StatsCommand implements BotCommand{

	private OverwatchPlugin plugin;

	public StatsCommand(OverwatchPlugin plugin){
		this.plugin = plugin;
	}



	@Override
	public void execute(String cmd, BotServer server, BotChannel chat, BotUser user, String[] args, String input)
	throws Exception {

		def player;
		def region;
		if(args.length == 0) {
			player = user.getUsername();
		}
		else if(args.length > 0) {
			player = args[0].replace("#", "-");
		}
		def mode = 'competitive'

		if(args.length > 1) {
			if(args[1].toLowerCase() == "quickplay")
				mode = "quickplay"
			else
				region = args[1].toLowerCase()
		}

		try{
			def root = plugin.getManager().getStats(server.getId(), player, region);
			def stats = root.stats[mode]
			def overall = stats.overall_stats
			def avg = stats.average_stats
			def game = stats.game_stats
			def heroes_stats = root.heroes.stats[mode]

			def heroes_time = root.heroes.playtime[mode]
			heroes_time = heroes_time.sort { a, b ->
				-(a.value <=> b.value)
			}

			def heroes = getHeroes(game.time_played, heroes_time, heroes_stats)

			def str = """
				```
${mode == 'competitive' ? 'Competitive' : 'Quick Play'} stats for ${root.user.split("-")[0]}
  Level: ${overall.level + 100 * overall.prestige}

${mode == 'competitive' ? 'Season 4' : 'Quick Play'}
  Rank: ${overall.comprank}
  Time: ${game.time_played as int}hrs
  Games: ${overall.games}, W: ${overall.wins}, L: ${overall.losses}, WR: ${overall.win_rate}%
  Elims: ${game.eliminations as int}, Deaths: ${game.deaths as int}, KD: ${game.kpd}
  Avg Elims: ${avg.eliminations_avg}, Avg Healing: ${avg.healing_done_avg}
  Total Damage: ${game.damage_done as int}, Most Elims: ${game.eliminations_most_in_game as int}
			

${heroes}```
			"""

			chat.sendMessage(str);
		}
		catch (ProfileNotFound e) {
			chat.sendMessage("Profile not found");
		}

	}

	private getHeroes(time, heroes, stats) {
		def str = "      Hero                            Time Win%\n"
		def max = 0;

		heroes.eachWithIndex { key, value, index ->
			if(index > 4) return
				def stat =  stats[key]
			if(stat) {
				max = Math.max(value as double, max as double)
				def amn = (25 * ((value as double) / max)) as int

				str += "${key.padLeft(10)} [${('-' * amn).padRight(25)}] ${value as int}h ${((stats[key].general_stats.win_percentage ?: 0) * 100) as int}%\n"
			}
		}

		return str;
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
