package dev.therealdan.grandexchange.core.inventory;

import dev.therealdan.grandexchange.core.GrandExchange;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.UUID;

public class SellUI implements Listener {

    private Economy _economy;
    private GrandExchange _grandExchange;

    private int _inventorySize = 27;

    private HashSet<UUID> _uiOpen = new HashSet<>();

    public SellUI(Economy economy, GrandExchange grandExchange) {
        _economy = economy;
        _grandExchange = grandExchange;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!_uiOpen.contains(player.getUniqueId())) return;

        if (!event.getClick().equals(ClickType.LEFT)) {
            event.setCancelled(true);
            return;
        }

        if (event.getSlot() == event.getRawSlot()) return;
        if (!event.getCursor().getType().equals(Material.AIR)) return;

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null) return;

        player.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
        event.getInventory().addItem(itemStack);
        Inventory inventory = Bukkit.createInventory(null, _inventorySize, "Grand Exchange Sell - $" + _grandExchange.calculateSellValue(event.getInventory()));
        for (int i = 0; i < _inventorySize; i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item == null || item.getType().equals(Material.AIR)) continue;
            inventory.setItem(i, item);
        }
        player.openInventory(inventory);
        _uiOpen.add(player.getUniqueId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        boolean open = _uiOpen.contains(player.getUniqueId());
        _uiOpen.remove(player.getUniqueId());
        if (!open) return;

        // TODO - sell
    }

    public void open(Player player) {
        player.openInventory(Bukkit.createInventory(null, _inventorySize, "Grand Exchange Sell"));
        _uiOpen.add(player.getUniqueId());
    }
}