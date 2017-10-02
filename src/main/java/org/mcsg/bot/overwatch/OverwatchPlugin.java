package org.mcsg.bot.overwatch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.mcsg.bot.DiscordBot;
import org.mcsg.bot.DiscordServer;
import org.mcsg.bot.DiscordUser;
import org.mcsg.bot.api.Bot;
import org.mcsg.bot.api.BotChannel;
import org.mcsg.bot.api.BotUser;
import org.mcsg.bot.plugin.BotPlugin;
import org.mcsg.bot.util.FileUtils;
import org.mcsg.bot.util.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.exceptions.UnirestException;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.RequestBuffer;

public class OverwatchPlugin implements BotPlugin {

	DiscordBot bot;

	public static final String HOST = "http://localhost:3000";

	public static final String ADD_USER = HOST + "/add-user?discord=%s&server=%s&id=%s&region=%s&platform=%s";
	public static final String GET_STATS = HOST + "/raw-stats?id=%s&";
	public static final String GET_USER = HOST + "/get-user?discord=%s&server=%s";
	public static final String LIVE_STATS_IMAGE = HOST + "/screenshot/live-stats+id=%s?width=824&height=90";
	public static final String LIVE_STATS_IMAGE_CARD = HOST + "/screenshot/live-stats+id=%s&card=true";

	public static final String GET_USERS = HOST + "/get-users/%s";

	private Gson gson = new Gson();
	Runnable updater;

	@Override
	public void onEnable(Bot bot) {
		this.bot = (DiscordBot)bot;

		bot.getCommandHandler().registerCommand(new StatsCommand(this));
		bot.getCommandHandler().registerCommand(new LinkCommand(this));


		updater = () ->{
			System.out.println("Starting Overwatch Updater");

			try {
				updateRankings();

				updateRoles(bot);
			} catch (IOException | UnirestException e) {
				e.printStackTrace();
			}
			
			System.out.println("Completed Overwatch Updater");



		};

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(updater, 0, 1, TimeUnit.HOURS);

	}


	public void updateRankings() {
		String[] urls = { 
				HOST + "/screenshot/rankings+server=%s&min=3500&max=6000&header=true?width=1355&height=520",
				HOST + "/screenshot/rankings+server=%s&min=3000&max=3500?width=1355&height=420",
				HOST + "/screenshot/rankings+server=%s&min=2500&max=3000?width=1355&height=420",
				HOST + "/screenshot/rankings+server=%s&min=2000&max=2500?width=1355&height=420",
				HOST + "/screenshot/rankings+server=%s&min=1500&max=2000?width=1355&height=420",
				HOST + "/screenshot/rankings+server=%s&min=0&max=1500?width=1355&height=420",
		};

		//Map<String, String> chats = this.bot.getSettings().getMap("overwatch.stats-chat");

		BotChannel chat = bot.getChat("337787121005756426");
		chat.clear();
		for(String url : urls) {
			File file = getImage(new Date().getTime()  + ".png", String.format(url, "304016633783910411"));
			try {
				chat.sendFile(file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void updateRoles(Bot bot) throws IOException, UnirestException {
		File rolesFile = new File(bot.getSettings().getSettingsFolder(), "roles.json");
		String json = FileUtils.readFile(rolesFile);

		Map<String, Map<String, String>> roles = gson.fromJson(json, new TypeToken<Map<String, Map<String, String>>>() {}.getType());

		if(roles == null) {
			roles = new HashMap<>();
		}

		String server =  "304016633783910411";
		DiscordServer botServer = (DiscordServer) bot.getServer(server);
		JsonNode userList = WebClient.getJson(String.format(GET_USERS, server));

		for(JsonNode user : userList) {
			Map<String, String> serverRoles = roles.getOrDefault(server, new HashMap<>());
			String discordId = user.get("discord_id").asText();
			String currentRole = serverRoles.get(discordId);
			String newRole = user.get("ranking").get("role").asText();
			if(currentRole == null || !currentRole.equals(newRole)) {
				DiscordUser dUser = (DiscordUser) bot.getUser(discordId);
				setRole(botServer, dUser, currentRole, newRole);

				serverRoles.put(discordId, newRole);
			}
			roles.put(server,  serverRoles);
		}

		FileUtils.writeFile(rolesFile, gson.toJson(roles));
	}

	public void setRole(DiscordServer server, DiscordUser user, String remove, String add) {
		if(user == null) return;

		IGuild guild = server.getHandle();


		List<IRole> removeRole = guild.getRolesByName(remove);
		List<IRole> addRole = guild.getRolesByName(add);

		RequestBuffer.request(() -> {

			if(addRole.size() > 0){
				user.addRole(addRole.get(0));
			} else {
				System.out.println(remove + " " + add + " " + removeRole + "  " + addRole);
			}
			if(removeRole.size() > 0) {
				user.removeRole(removeRole.get(0));
			}
		});
	}

	public File getImage(String name, String url) {
		File file = new File(bot.getSettings().getDataFolder(), "stats_" + new Date().getTime() + ".png");
		BufferedImage image = null;
		try {
			image = ImageIO.read(new URL(url));

			ImageIO.write( image, "png",file);
			//chat.sendMessage("http://localhost:3000/screenshot/live-stats+id=${player}");
			return file;
		} catch (IOException e) {
			return file;
		}
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

}

