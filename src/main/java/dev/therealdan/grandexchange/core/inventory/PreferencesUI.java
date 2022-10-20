package dev.therealdan.grandexchange.core.inventory;

import dev.therealdan.grandexchange.core.GrandExchange;
import dev.therealdan.grandexchange.main.Config;
import dev.therealdan.grandexchange.models.Icon;
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
    private GrandExchange _grandExchange;

    private HashSet<UUID> _uiOpen = new HashSet<>();

    public PreferencesUI(Config config, GrandExchange grandExchange) {
        _config = config;
        _grandExchange = grandExchange;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!_uiOpen.contains(player.getUniqueId())) return;
        event.setCancelled(true);

        int i = 0;
        for (GrandExchange.SortPreference sortPreference : GrandExchange.SortPreference.values()) {
            if (event.getSlot() == i) {
                _grandExchange.setSortPreference(player.getUniqueId(), sortPreference);
                player.sendMessage(_config.primary + "Grand Exchange sort preference set to " + _config.secondary + sortPreference.getName());
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
        Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, "Grand Exchange - Preferences");

        inventory.addItem(new Icon(Material.CHEST, _config.primary + "Stock Count",
                _config.secondary + "Click to sort the GE with top stocked items first"));
        inventory.addItem(new Icon(Material.HOPPER, _config.primary + "Stock Count Ascending",
                _config.secondary + "Click to sort the GE with lowest stocked items first"));

        inventory.addItem(new Icon(Material.BOOK, _config.primary + "Alphabetical",
                _config.secondary + "Click to sort the GE alphabetically from A-Z"));
        inventory.addItem(new Icon(Material.WRITABLE_BOOK, _config.primary + "Alphabetical Descending",
                _config.secondary + "Click to sort the GE descending alphabetically from Z-A"));

        player.openInventory(inventory);
        _uiOpen.add(player.getUniqueId());
    }

    public void closeAll() {
        for (UUID uuid : _uiOpen)
            Bukkit.getPlayer(uuid).closeInventory();
    }
}