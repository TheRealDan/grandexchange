package dev.therealdan.grandexchange.core.inventory;

import dev.therealdan.grandexchange.core.GrandExchange;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BuyUI implements Listener {

    private Economy _economy;
    private GrandExchange _grandExchange;

    private HashMap<UUID, String> _uiOpen = new HashMap<>();

    public BuyUI(Economy economy, GrandExchange grandExchange) {
        _economy = economy;
        _grandExchange = grandExchange;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!_uiOpen.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        _uiOpen.remove(event.getPlayer().getUniqueId());
    }

    public void open(Player player, String search) {
        List<Material> materials = _grandExchange.getStock(search, 54);
        int size = Math.max(9, materials.size());
        while (size % 9 != 0) size++;

        Inventory inventory = Bukkit.createInventory(null, size, "Grand Exchange" + (search.length() > 0 ? " - " + search : ""));
        for (Material material : materials)
            inventory.addItem(_grandExchange.getIcon(material));

        player.openInventory(inventory);
        _uiOpen.put(player.getUniqueId(), search);
    }
}