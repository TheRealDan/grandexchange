package dev.therealdan.exchange.core.inventory;

import dev.therealdan.exchange.core.Exchange;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class SellUI implements Listener {

    private Economy _economy;
    private Exchange _exchange;

    private int _inventorySize = 27;

    private HashSet<UUID> _uiOpen = new HashSet<>();

    public SellUI(Economy economy, Exchange exchange) {
        _economy = economy;
        _exchange = exchange;
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

        if (event.getInventory().firstEmpty() == -1) return;

        player.getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
        event.getInventory().addItem(itemStack);
        Inventory inventory = Bukkit.createInventory(null, _inventorySize, "Exchange Sell - $" + _exchange.calculateSellValue(Arrays.asList(event.getInventory().getContents())));
        for (int i = 0; i < _inventorySize; i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item == null || item.getType().equals(Material.AIR)) continue;
            inventory.setItem(i, item);
        }
        _uiOpen.remove(player.getUniqueId());
        player.openInventory(inventory);
        _uiOpen.add(player.getUniqueId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        boolean open = _uiOpen.contains(player.getUniqueId());
        _uiOpen.remove(player.getUniqueId());
        if (!open) return;

        _exchange.sell(player, event.getInventory().getContents());
    }

    public void open(Player player) {
        player.openInventory(Bukkit.createInventory(null, _inventorySize, "Exchange Sell"));
        _uiOpen.add(player.getUniqueId());
    }

    public void cancelAll() {
        for (UUID uuid : _uiOpen) {
            Player player = Bukkit.getPlayer(uuid);
            for (ItemStack itemStack : player.getOpenInventory().getTopInventory().getContents()) {
                if (itemStack == null) continue;
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
            player.closeInventory();
        }
    }
}