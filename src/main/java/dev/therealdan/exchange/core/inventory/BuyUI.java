package dev.therealdan.exchange.core.inventory;

import dev.therealdan.exchange.core.Exchange;
import dev.therealdan.exchange.main.Config;
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BuyUI implements Listener {

    private Economy _economy;
    private Config _config;
    private Exchange _exchange;

    private HashMap<UUID, String> _uiOpen = new HashMap<>();

    public BuyUI(Economy economy, Config config, Exchange exchange) {
        _economy = economy;
        _config = config;
        _exchange = exchange;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!_uiOpen.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);

        if (event.getClick().equals(ClickType.DOUBLE_CLICK)) return;
        if (event.getSlot() != event.getRawSlot()) return;

        if (event.getCurrentItem() == null) return;
        Material material = event.getCurrentItem().getType();

        if (_exchange.getStockCount(material) == 0) return;

        if (event.getClick().equals(ClickType.SHIFT_LEFT)) {
            int stackSize = (int) Math.min(_exchange.getStockCount(material), material.getMaxStackSize());
            long price = _exchange.calculateBuyStackPrice(material, stackSize);
            if (_economy.has(player, price)) {
                if (_exchange.getKing() != null) {
                    long tax = _exchange.calculateTax(material, stackSize);
                    _economy.depositPlayer(_exchange.getKing(), tax);
                }
                _economy.withdrawPlayer(player, price);
                _exchange.removeStock(material, stackSize);
                player.getInventory().addItem(new ItemStack(material, stackSize));
                player.sendMessage(_config.primary + "Purchased " + _config.secondary + stackSize + "x " + _exchange.getName(material) + _config.primary + " for " + _config.secondary + "$" + price);
                open(player, _uiOpen.getOrDefault(player.getUniqueId(), ""));
            }

        } else {
            long price = _exchange.getFinalBuyPrice(material);
            if (_economy.has(player, price)) {
                if (_exchange.getKing() != null) {
                    long tax = _exchange.getTax(material);
                    _economy.depositPlayer(_exchange.getKing(), tax);
                }
                _economy.withdrawPlayer(player, price);
                _exchange.removeStock(material, 1);
                player.getInventory().addItem(new ItemStack(material));
                player.sendMessage(_config.primary + "Purchased " + _config.secondary + _exchange.getName(material) + _config.primary + " for " + _config.secondary + "$" + price);
                open(player, _uiOpen.getOrDefault(player.getUniqueId(), ""));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        _uiOpen.remove(event.getPlayer().getUniqueId());
    }

    public void open(Player player, String search) {
        List<Material> materials = _exchange.getStock(search, _exchange.getSortPreference(player.getUniqueId()), 54);
        int size = Math.min(54, Math.max(9, materials.size()));
        while (size % 9 != 0) size++;

        Inventory inventory = Bukkit.createInventory(null, size, "Exchange" + (search.length() > 0 ? " - " + search : ""));
        for (Material material : materials)
            inventory.addItem(_exchange.getIcon(material));

        player.openInventory(inventory);
        _uiOpen.put(player.getUniqueId(), search);
    }

    public void closeAll() {
        for (UUID uuid : _uiOpen.keySet())
            Bukkit.getPlayer(uuid).closeInventory();
    }
}