package dev.therealdan.grandexchange.main;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {

    public final String primary;
    public final String secondary;

    public Config(JavaPlugin plugin) {
        plugin.saveDefaultConfig();

        primary = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Colors.Primary"));
        secondary = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Colors.Secondary"));
    }
}