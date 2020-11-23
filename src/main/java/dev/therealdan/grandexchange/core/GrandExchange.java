package dev.therealdan.grandexchange.core;

import dev.therealdan.grandexchange.main.Config;
import dev.therealdan.grandexchange.main.GrandExchangePlugin;
import dev.therealdan.grandexchange.models.Icon;
import dev.therealdan.grandexchange.models.YamlFile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

        String message = _config.secondary + itemStack.getAmount() + "x " + getName(itemStack.getType()) + _config.primary + " sold for " + _config.secondary + "$" + value + _config.primary;
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

    public long calculateSellValue(List<ItemStack> itemStacks) {
        GrandExchange simulation = getSimulation();
        long value = 0;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            int amount = itemStack.getAmount();
            while (amount > 0) {
                value += simulation.getSellPrice(itemStack.getType());
                simulation.addStock(itemStack.getType(), 1);
                amount--;
            }
        }
        return value;
    }

    public long calculateBuyStackPrice(Material material, int stackSize) {
        GrandExchange simulation = getSimulation();
        long value = 0;
        for (int i = 0; i <= stackSize; i++) {
            value += simulation.getBuyPrice(material);
            simulation.removeStock(material, 1);
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

    public List<Material> getStock(String search, int max) {
        if (search.length() == 0) return getStockByStockCount();

        List<Material> materials = getStockByAlphabetical();
        List<Material> filtered = new ArrayList<>();
        for (Material material : materials) {
            if (getName(material).toLowerCase().contains(search.toLowerCase()))
                filtered.add(material);
            if (filtered.size() >= max) return filtered;
        }
        return filtered;
    }

    public List<Material> getStockByStockCount() {
        List<Material> materials = getStock();
        List<Material> sorted = new ArrayList<>();
        Material next;
        while (materials.size() > 0) {
            next = null;
            for (Material material : materials) {
                if (next == null || getStockCount(material) > getStockCount(next)) {
                    next = material;
                }
            }
            sorted.add(next);
            materials.remove(next);
        }
        return sorted;
    }

    public List<Material> getStockByAlphabetical() {
        List<Material> materials = getStock();
        List<Material> sorted = new ArrayList<>();
        Material next;
        while (materials.size() > 0) {
            next = null;
            for (Material material : materials) {
                if (next == null || getName(material).compareTo(getName(next)) < 0) {
                    next = material;
                }
            }
            sorted.add(next);
            materials.remove(next);
        }
        return sorted;
    }

    public List<Material> getStock() {
        return new ArrayList<>(_stock.keySet());
    }

    private GrandExchange getSimulation() {
        GrandExchange grandExchange = new GrandExchange();
        grandExchange._stock = new HashMap<>(_stock);
        return grandExchange;
    }

    public Icon getIcon(Material material) {
        return new Icon(material, "",
                _config.primary + "Stock: " + _config.secondary + getStockCount(material),
                _config.primary + "Price: " + _config.secondary + "$" + getBuyPrice(material),
                _config.primary + "Stack Price: " + _config.secondary + (getStockCount(material) < material.getMaxStackSize() ? "Not enough stock" : "$" + calculateBuyStackPrice(material, material.getMaxStackSize()) + _config.primary + " (shift click)")
        );
    }

    public static String getName(Material material) {
        return material.toString().substring(0, 1) + material.toString().substring(1).toLowerCase().replace("_", " ");
    }
}