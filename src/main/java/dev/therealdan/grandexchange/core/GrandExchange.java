package dev.therealdan.grandexchange.core;

import dev.therealdan.grandexchange.main.Config;
import dev.therealdan.grandexchange.main.GrandExchangePlugin;
import dev.therealdan.grandexchange.models.Icon;
import dev.therealdan.grandexchange.models.YamlFile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GrandExchange {

    private Economy _economy;
    private Config _config;
    private YamlFile _yamlFile;

    private double _baseCost = 1000;
    private double _stretch = 130;
    private double _processingFee = 0.65;
    private double _taxRate = 0;

    private HashMap<Material, Long> _stock = new HashMap<>();
    private OfflinePlayer _king = null;

    private GrandExchange() {
    }

    public GrandExchange(GrandExchangePlugin grandExchangePlugin, Economy economy, Config config) {
        _economy = economy;
        _config = config;
        _yamlFile = new YamlFile(grandExchangePlugin, "data/grandexchange.yml");

        if (_yamlFile.getData().contains("King"))
            _king = Bukkit.getOfflinePlayer(UUID.fromString(_yamlFile.getData().getString("King")));

        if (_yamlFile.getData().contains("Stock"))
            for (String material : _yamlFile.getData().getConfigurationSection("Stock").getKeys(false))
                _stock.put(Material.valueOf(material), _yamlFile.getData().getLong("Stock." + material));
    }

    public void save() {
        if (_king != null) _yamlFile.getData().set("King", _king.getUniqueId().toString());
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
            min = getBaseSellPrice(itemStack.getType());
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
                value += simulation.getBaseSellPrice(itemStack.getType());
                simulation.addStock(itemStack.getType(), 1);
                amount--;
            }
        }
        return value;
    }

    public long calculateBuyStackPrice(Material material, int stackSize) {
        GrandExchange simulation = getSimulation();
        long value = 0;
        for (int i = 0; i < stackSize; i++) {
            value += simulation.getFinalBuyPrice(material);
            simulation.removeStock(material, 1);
        }
        return value;
    }

    public long calculateTax(Material material, int stackSize) {
        GrandExchange simulation = getSimulation();
        long value = 0;
        for (int i = 0; i < stackSize; i++) {
            value += simulation.getTax(material);
            simulation.removeStock(material, 1);
        }
        return value;
    }

    public long getBaseBuyPrice(Material material) {
        return getBaseSellPrice(material);
    }

    public long getTax(Material material) {
        return (long) (getBaseBuyPrice(material) * _taxRate);
    }

    public long getFinalBuyPrice(Material material) {
        return (long) (getBaseSellPrice(material) * (1.0 + _processingFee + _taxRate));
    }

    public long getBaseSellPrice(Material material) {
        return (long) (_baseCost * Math.exp(-(getStockCount(material) / _stretch)) + 1);
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

    public long getTotalStock() {
        long totalStock = 0;
        for (long stock : _stock.values())
            totalStock += stock;
        return totalStock;
    }

    public void setTaxRate(double taxRate) {
        _taxRate = taxRate;
    }

    public double getTaxRate() {
        return _taxRate;
    }

    public void setKing(OfflinePlayer king) {
        _king = king;
    }

    public OfflinePlayer getKing() {
        return _king;
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
        grandExchange._taxRate = _taxRate;
        return grandExchange;
    }

    public Icon getIcon(Material material) {
        int stackSize = (int) Math.min(getStockCount(material), material.getMaxStackSize());
        return new Icon(material, "",
                _config.primary + "Stock: " + _config.secondary + getStockCount(material),
                _config.primary + "Price: " + _config.secondary + "$" + getFinalBuyPrice(material),
                _config.primary + "Stack Price: " + _config.secondary + "$" + calculateBuyStackPrice(material, stackSize) + _config.primary + " (" + _config.secondary + stackSize + "x" + _config.primary + ", shift click)"
        );
    }

    public static String getName(Material material) {
        return material.toString().substring(0, 1) + material.toString().substring(1).toLowerCase().replace("_", " ");
    }
}