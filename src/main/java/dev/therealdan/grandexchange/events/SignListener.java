package dev.therealdan.grandexchange.events;

import dev.therealdan.grandexchange.core.GrandExchange;
import dev.therealdan.grandexchange.main.Config;
import dev.therealdan.grandexchange.main.GrandExchangePlugin;
import dev.therealdan.grandexchange.models.YamlFile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class SignListener implements Listener {

    private GrandExchangePlugin _grandExchangePlugin;
    private Config _config;
    private YamlFile _yamlFile;
    private GrandExchange _grandExchange;

    private HashMap<Location, UUID> _signs = new HashMap<>();

    public SignListener(GrandExchangePlugin grandExchangePlugin, Config config, GrandExchange grandExchange) {
        _grandExchangePlugin = grandExchangePlugin;
        _config = config;
        _yamlFile = new YamlFile(grandExchangePlugin, "data/signs.yml");
        _grandExchange = grandExchange;

        for (String key : _yamlFile.getKeys("Signs")) {
            Location location = _yamlFile.getLocation("Signs." + key + ".Location");
            UUID owner = UUID.fromString(_yamlFile.getData().getString("Signs." + key + ".Owner"));
            _signs.put(location, owner);
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(grandExchangePlugin, this::task, 20 * 60, 20 * 30);
    }

    public void save() {
        _yamlFile.getData().set("Signs", null);

        for (Map.Entry<Location, UUID> entry : _signs.entrySet()) {
            String key = UUID.randomUUID().toString();
            _yamlFile.saveLocation("Signs." + key + ".Location", entry.getKey());
            _yamlFile.getData().set("Signs." + key + ".Owner", entry.getValue().toString());
        }

        _yamlFile.save();
    }

    private void task() {
        HashSet<Location> toRemove = new HashSet<>();

        for (Map.Entry<Location, UUID> entry : _signs.entrySet()) {
            Sign sign = getSign(entry.getKey());
            if (sign == null || !isAutoSell(sign.getLine(0))) {
                toRemove.add(entry.getKey());
                continue;
            }

            boolean all = isSellEverything(sign.getLine(2));
            Material material = getMaterial(sign.getLine(2));

            if (!all && material == null) {
                toRemove.add(entry.getKey());
                continue;
            }

            BlockData blockData = sign.getBlockData();
            if (!(blockData instanceof WallSign)) continue;
            WallSign wallSign = (WallSign) blockData;
            BlockState blockState = sign.getBlock().getRelative(wallSign.getFacing().getOppositeFace()).getState();
            if (!(blockState instanceof Chest)) continue;
            Chest chest = (Chest) blockState;

            for (ItemStack itemStack : chest.getInventory().getContents()) {
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
                if (all || itemStack.getType().equals(material)) {
                    if (!_grandExchange.canBeSold(itemStack)) continue;
                    _grandExchange.sell(entry.getValue(), itemStack);
                    chest.getInventory().remove(itemStack);
                }
            }
        }

        for (Location location : toRemove)
            _signs.remove(location);
    }

    @EventHandler
    public void onSign(SignChangeEvent event) {
        Location location = event.getBlock().getLocation();

        if (!isAutoSell(event.getLine(0))) return;

        boolean all = isSellEverything(event.getLine(2));
        Material material = getMaterial(event.getLine(2));

        if (!all && material == null) {
            event.getPlayer().sendMessage(_config.primary + "Please specify a material on line 3 (or type 'all')");
            return;
        }

        _signs.put(location, event.getPlayer().getUniqueId());

        event.setLine(0, _config.primary + "Auto Sell");
        event.setLine(1, _config.primary + event.getPlayer().getName());
        event.setLine(2, _config.primary + (all ? "All Items" : GrandExchange.getName(material)));
        event.setLine(3, "");
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Sign sign = getSign(event.getBlock());
        if (sign == null) return;
        if (!isAutoSell(sign.getLine(0))) return;

        boolean all = isSellEverything(sign.getLine(2));
        Material material = getMaterial(sign.getLine(2));

        if (!all && material == null) return;

        OfflinePlayer owner = getPlayer(sign.getLine(1));
        if (event.getPlayer().getUniqueId().equals(owner.getUniqueId())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(_config.primary + "You can't break someone else's Auto Sell");
    }

    public boolean isAutoSell(String line) {
        return line.toLowerCase().replace(" ", "").contains("autosell");
    }

    public OfflinePlayer getPlayer(String line) {
        try {
            return Bukkit.getOfflinePlayer(ChatColor.stripColor(line));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isSellEverything(String line) {
        return line.toLowerCase().contains("all") || line.toLowerCase().contains("everything");
    }

    public Material getMaterial(String line) {
        try {
            return Material.valueOf(ChatColor.stripColor(line.toUpperCase()).replace(" ", "_"));
        } catch (Exception e) {
            return null;
        }
    }

    public Sign getSign(Location location) {
        return getSign(location.getBlock());
    }

    public Sign getSign(Block block) {
        if (!(block.getState() instanceof Sign)) return null;
        return (Sign) block.getState();
    }
}