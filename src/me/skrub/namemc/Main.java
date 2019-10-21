package me.skrub.namemc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {

	public void onEnable() {
		loadConfig();
		getServer().getPluginCommand("namemc").setExecutor(this);
	}

	private void loadConfig() {
		File file = new File(this.getDataFolder(), "config.yml");
		if (!file.exists()) {
			saveDefaultConfig();
		}
	}

	boolean check(UUID uuid) {
		try {
			final URL url = new URL(
					"https://api.namemc.com/server/" + this.getConfig().getString("ip") + "/likes?profile=" + uuid.toString());
			final URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			final BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			String line = null;
			boolean ret = false;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.toLowerCase();
				if (line.contains("true")) {
					ret = true;
					break;
				}
			}
			bufferedReader.close();
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("namemc.redeem")) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				if(this.check(player.getUniqueId())) {
                    List<String> commands = getConfig().getStringList("commands");
                    List<String> alreadyRedeemed = getConfig().getStringList("already-redeemed");
                    if(alreadyRedeemed.contains(player.getUniqueId().toString())) {
                    	player.sendMessage(color(getConfig().getString("redeemed")));
                    } else {
                    	alreadyRedeemed.add(player.getUniqueId().toString());
                    	this.getConfig().set("already-redeemed", alreadyRedeemed);
                    	this.saveConfig();
                    	for(String s : commands) {
                    		if(s.startsWith("/")) {
                    			s = s.substring(1);
                    		}
                    		s = s.replace("%player%", player.getName());
                    		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                    	}
                    }
				} else {
					player.sendMessage(color(getConfig().getString("no-like")));
				}
			} else {
				sender.sendMessage(color(getConfig().getString("console-exec")));
			}    
		} else {
			sender.sendMessage(color(getConfig().getString("no-perms")));
		}
		return true;
	}
	
	String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

}
