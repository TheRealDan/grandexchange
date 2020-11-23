package dev.therealdan.grandexchange.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

public class YamlFile {

    private final JavaPlugin _plugin;
    private final String _path;

    private File file;
    private FileConfiguration data;

    public YamlFile(JavaPlugin plugin, String path) {
        this(plugin, path, false);
    }

    public YamlFile(JavaPlugin plugin, String path, boolean loadDefault) {
        _plugin = plugin;
        _path = path;
        if (loadDefault) loadDefault(false);
    }

    public void save() {
        try {
            getData().save(getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDefault(boolean overwrite) {
        _plugin.saveResource(_path, overwrite);
    }

    public void saveLocation(String path, Location location) {
        if (location == null) {
            getData().set(path, null);
            return;
        }
        getData().set(path, String.format("%s;%s;%s;%s;%s;%s;",
                location.getWorld().getName(),
                Double.toString(location.getX()).replace(".", ","),
                Double.toString(location.getY()).replace(".", ","),
                Double.toString(location.getZ()).replace(".", ","),
                Float.toString(location.getYaw()).replace(".", ","),
                Float.toString(location.getPitch()).replace(".", ",")
        ));
    }

    public Location getLocation(String path) {
        if (!getData().contains(path)) return null;
        String[] args = getData().getString(path).split(";");
        return new Location(
                Bukkit.getWorld(args[0].replace(",", ".")),
                Double.parseDouble(args[1].replace(",", ".")),
                Double.parseDouble(args[2].replace(",", ".")),
                Double.parseDouble(args[3].replace(",", ".")),
                Float.parseFloat(args[4].replace(",", ".")),
                Float.parseFloat(args[5].replace(",", "."))
        );
    }

    public ArrayList<String> getKeys(String path) {
        if (!getData().contains(path)) return new ArrayList<>();
        return new ArrayList<>(getData().getConfigurationSection(path).getKeys(false));
    }

    public FileConfiguration getData() {
        if (data == null) data = YamlConfiguration.loadConfiguration(getFile());
        return data;
    }

    private File getFile() {
        if (file == null) file = new File(_plugin.getDataFolder(), _path);
        return file;
    }
}