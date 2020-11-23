package dev.therealdan.grandexchange.core.inventory;

import dev.therealdan.grandexchange.core.GrandExchange;
import dev.therealdan.grandexchange.main.Config;
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
    private GrandExchange _grandExchange;

    private HashMap<UUID, String> _uiOpen = new HashMap<>();

    public BuyUI(Economy economy, Config config, GrandExchange grandExchange) {
        _economy = economy;
        _config = config;
        _grandExchange = grandExchange;
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

        if (event.getClick().equals(ClickType.SHIFT_LEFT)) {
            long price = _grandExchange.calculateBuyStackPrice(material, material.getMaxStackSize());
            if (_economy.has(player, price) && _grandExchange.getStockCount(material) >= material.getMaxStackSize()) {
                _economy.withdrawPlayer(player, price);
                _grandExchange.removeStock(material, material.getMaxStackSize());
                player.getInventory().addItem(new ItemStack(material, material.getMaxStackSize()));
                player.sendMessage(_config.primary + "Purchased " + _config.secondary + material.getMaxStackSize() + "x " + _grandExchange.getName(material) + _config.primary + " for " + _config.secondary + "$" + price);
                open(player, _uiOpen.getOrDefault(player.getUniqueId(), ""));
            }

        } else {
            long price = _grandExchange.getBuyPrice(material);
            if (_economy.has(player, price) && _grandExchange.getStockCount(material) >= 1) {
                _economy.withdrawPlayer(player, price);
                _grandExchange.removeStock(material, 1);
                player.getInventory().addItem(new ItemStack(material));
                player.sendMessage(_config.primary + "Purchased " + _config.secondary + _grandExchange.getName(material) + _config.primary + " for " + _config.secondary + "$" + price);
                open(player, _uiOpen.getOrDefault(player.getUniqueId(), ""));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        _uiOpen.remove(event.getPlayer().getUniqueId());
    }

    public void open(Player player, String search) {
        List<Material> materials = _grandExchange.getStock(search, 54);
        int size = Math.min(54, Math.max(9, materials.size()));
        while (size % 9 != 0) size++;

        Inventory inventory = Bukkit.createInventory(null, size, "Grand Exchange" + (search.length() > 0 ? " - " + search : ""));
        for (Material material : materials)
            inventory.addItem(_grandExchange.getIcon(material));

        player.openInventory(inventory);
        _uiOpen.put(player.getUniqueId(), search);
    }

    public void closeAll() {
        for (UUID uuid : _uiOpen.keySet())
            Bukkit.getPlayer(uuid).closeInventory();
    }
}