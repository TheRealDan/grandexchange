package dev.therealdan.grandexchange.core;

import dev.therealdan.grandexchange.main.Config;
import dev.therealdan.grandexchange.main.GrandExchangePlugin;
import dev.therealdan.grandexchange.models.YamlFile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrandExchange {

    private Economy _economy;
    private Config _config;
    private YamlFile _yamlFile;

    private HashMap<Material, Long> _stock = new HashMap<>();

    private GrandExchange() {
    }

    public GrandExchange(GrandExchangePlugin grandExchangePlugin, Economy economy, Config config) {
        _economy = economy;
        _config = config;
        _yamlFile = new YamlFile(grandExchangePlugin, "data/grandexchange.yml");

        if (_yamlFile.getData().contains("Stock"))
            for (String material : _yamlFile.getData().getConfigurationSection("Stock").getKeys(false))
                _stock.put(Material.valueOf(material), _yamlFile.getData().getLong("Stock." + material));
    }

    public void save() {
        for (Map.Entry<Material, Long> entry : _stock.entrySet())
            _yamlFile.getData().set("Stock." + entry.getKey().toString(), entry.getValue());
        _yamlFile.save();
    }

    public void sell(Player player, ItemStack itemStack) {
        long max = -1;
        long min = 0;
        long value = 0;
        long amount = itemStack.getAmount();
        while (amount > 0) {
            min = getSellPrice(itemStack.getType());
            value += min;
            if (max == -1) max = value;
            addStock(itemStack.getType(), 1);
            amount--;
        }

        String name = itemStack.getType().toString().substring(0, 1) + itemStack.getType().toString().substring(1).toLowerCase().replace("_", " ");
        String message = _config.secondary + itemStack.getAmount() + "x " + name + _config.primary + " sold for " + _config.secondary + "$" + value + _config.primary;
        if (max != min) message += " ($" + max + " ~ $" + min + ")";
        player.sendMessage(message);

        _economy.depositPlayer(player, value);
    }

    public void addStock(Material material, long amount) {
        _stock.put(material, getStockCount(material) + amount);
    }

    public void removeStock(Material material, long amount) {
        _stock.put(material, getStockCount(material) - amount);
    }

    public long calculateSellValue(Inventory inventory) {
        GrandExchange grandExchange = copy();
        long value = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            int amount = itemStack.getAmount();
            while (amount > 0) {
                value += grandExchange.getSellPrice(itemStack.getType());
                grandExchange.addStock(itemStack.getType(), 1);
                amount--;
            }
        }
        return value;
    }

    public long getBuyPrice(Material material) {
        return getSellPrice(material) * 2;
    }

    public long getSellPrice(Material material) {
        return Math.max(getStockCount(getMostCommonItem()) - getStockCount(material), 1);
    }

    public Material getMostCommonItem() {
        Material mostCommon = null;
        long amount = 0;
        for (Map.Entry<Material, Long> entry : _stock.entrySet()) {
            if (entry.getValue() > amount) {
                mostCommon = entry.getKey();
                amount = entry.getValue();
            }
        }
        return mostCommon;
    }

    public long getStockCount(Material material) {
        return _stock.getOrDefault(material, 0L);
    }

    public List<Material> getStock() {
        return new ArrayList<>(_stock.keySet());
    }

    private GrandExchange copy() {
        GrandExchange grandExchange = new GrandExchange();
        grandExchange._stock = new HashMap<Material, Long>(_stock);
        return grandExchange;
    }
}