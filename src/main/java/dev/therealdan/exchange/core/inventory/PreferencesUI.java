package dev.therealdan.exchange.core.inventory;

import dev.therealdan.exchange.core.Exchange;
import dev.therealdan.exchange.main.Config;
import dev.therealdan.exchange.models.Icon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.UUID;

public class PreferencesUI implements Listener {

    private Config _config;
    private Exchange _exchange;

    private HashSet<UUID> _uiOpen = new HashSet<>();

    public PreferencesUI(Config config, Exchange exchange) {
        _config = config;
        _exchange = exchange;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!_uiOpen.contains(player.getUniqueId())) return;
        event.setCancelled(true);

        int i = 0;
        for (Exchange.SortPreference sortPreference : Exchange.SortPreference.values()) {
            if (event.getSlot() == i) {
                _exchange.setSortPreference(player.getUniqueId(), sortPreference);
                player.sendMessage(_config.primary + "Exchange sort preference set to " + _config.secondary + sortPreference.getName());
                player.closeInventory();
            }
            i++;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        _uiOpen.remove(event.getPlayer().getUniqueId());
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, "Exchange - Preferences");

        inventory.addItem(new Icon(Material.CHEST, _config.primary + "Stock Count",
                _config.secondary + "Click to sort the EX with top stocked items first"));
        inventory.addItem(new Icon(Material.HOPPER, _config.primary + "Stock Count Ascending",
                _config.secondary + "Click to sort the EX with lowest stocked items first"));

        inventory.addItem(new Icon(Material.BOOK, _config.primary + "Alphabetical",
                _config.secondary + "Click to sort the EX alphabetically from A-Z"));
        inventory.addItem(new Icon(Material.WRITABLE_BOOK, _config.primary + "Alphabetical Descending",
                _config.secondary + "Click to sort the EX descending alphabetically from Z-A"));

        player.openInventory(inventory);
        _uiOpen.add(player.getUniqueId());
    }

    public void closeAll() {
        for (UUID uuid : _uiOpen)
            Bukkit.getPlayer(uuid).closeInventory();
    }
}