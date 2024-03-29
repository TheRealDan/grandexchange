package dev.therealdan.exchange.core;

import dev.therealdan.exchange.main.Config;
import dev.therealdan.exchange.main.ExchangePlugin;
import dev.therealdan.exchange.models.Icon;
import dev.therealdan.exchange.models.YamlFile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Exchange {

    private Economy _economy;
    private Config _config;
    private YamlFile _yamlFile;

    private double _baseCost = 1000;
    private double _stretch = 130;
    private double _processingFee = 0.05;
    private double _taxRate = 0;
    private double _findingBonus = 25000;

    private HashMap<Material, Long> _stock = new HashMap<>();
    private HashMap<UUID, SortPreference> _sortPreference = new HashMap<>();
    private OfflinePlayer _king = null;

    private Exchange() {
    }

    public Exchange(ExchangePlugin exchangePlugin, Economy economy, Config config) {
        _economy = economy;
        _config = config;
        _yamlFile = new YamlFile(exchangePlugin, "data/exchange.yml");

        if (_yamlFile.getData().contains("King"))
            _king = Bukkit.getOfflinePlayer(UUID.fromString(_yamlFile.getData().getString("King")));

        if (_yamlFile.getData().contains("Stock"))
            for (String material : _yamlFile.getData().getConfigurationSection("Stock").getKeys(false))
                _stock.put(Material.valueOf(material), _yamlFile.getData().getLong("Stock." + material));

        if (_yamlFile.getData().contains("Preferences"))
            for (String uuid : _yamlFile.getData().getConfigurationSection("Preferences").getKeys(false))
                _sortPreference.put(UUID.fromString(uuid), SortPreference.valueOf(_yamlFile.getData().getString("Preferences." + uuid + ".SortPreference")));
    }

    public void save() {
        if (_king != null) _yamlFile.getData().set("King", _king.getUniqueId().toString());
        for (Map.Entry<Material, Long> entry : _stock.entrySet())
            _yamlFile.getData().set("Stock." + entry.getKey().toString(), entry.getValue());
        for (Map.Entry<UUID, SortPreference> entry : _sortPreference.entrySet())
            _yamlFile.getData().set("Preferences." + entry.getKey().toString() + ".SortPreference", entry.getValue().toString());
        _yamlFile.save();
    }

    public void sell(Player player, ItemStack[] itemStacks) {
        HashSet<Material> cantSell = new HashSet<>();
        HashSet<Material> newToMarket = new HashSet<>();
        HashMap<Material, Long> min = new HashMap<>();
        HashMap<Material, Long> max = new HashMap<>();
        HashMap<Material, Long> count = new HashMap<>();
        HashMap<Material, Long> cash = new HashMap<>();

        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null) continue;
            if (!canBeSold(itemStack)) {
                cantSell.add(itemStack.getType());
                player.getInventory().addItem(itemStack);
                continue;
            }
            if (newToMarket(itemStack) && _findingBonus > 0) {
                newToMarket.add(itemStack.getType());
            }

            long value = 0;
            long amount = itemStack.getAmount();
            count.put(itemStack.getType(), count.getOrDefault(itemStack.getType(), 0L) + itemStack.getAmount());
            while (amount > 0) {
                min.put(itemStack.getType(), getBaseSellPrice(itemStack.getType()));
                value += min.get(itemStack.getType());
                if (!max.containsKey(itemStack.getType())) max.put(itemStack.getType(), value);
                addStock(itemStack.getType(), 1);
                amount--;
            }

            cash.put(itemStack.getType(), cash.getOrDefault(itemStack.getType(), 0L) + value);
        }

        for (Map.Entry<Material, Long> entry : count.entrySet()) {
            String message = _config.secondary + entry.getValue() + "x " + getName(entry.getKey()) + _config.primary + " sold for " + _config.secondary + _economy.format(cash.get(entry.getKey()));
            if (!max.get(entry.getKey()).equals(min.get(entry.getKey()))) message += _config.primary + " (" + max.get(entry.getKey()) + " ~ " + min.get(entry.getKey()) + ")";
            player.sendMessage(message);
            _economy.depositPlayer(player, cash.get(entry.getKey()));
        }

        if (newToMarket.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Material material : newToMarket)
                stringBuilder.append(", ").append(getName(material));
            String items = stringBuilder.toString().replaceFirst(", ", "");
            double findingBonus = _findingBonus * newToMarket.size();
            _economy.depositPlayer(player, findingBonus);
            Bukkit.broadcastMessage(_config.secondary + player.getName() + _config.primary + " has received a finding bonus of " + _config.secondary + _economy.format(findingBonus) + _config.primary + " for introducing " + _config.secondary + items + _config.primary + " to the GE for the first time!");
        }

        if (cantSell.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Material material : cantSell)
                stringBuilder.append(", ").append(getName(material));
            String items = stringBuilder.toString().replaceFirst(", ", "");
            player.sendMessage(_config.secondary + items + _config.primary + " can not be sold");
        }
    }

    public void sell(Player player, ItemStack itemStack) {
        if (!canBeSold(itemStack)) {
            player.sendMessage(_config.secondary + getName(itemStack.getType()) + _config.primary + " can not be sold");
            player.getInventory().addItem(itemStack);
            return;
        }

        if (newToMarket(itemStack) && _findingBonus > 0) {
            _economy.depositPlayer(player, _findingBonus);
            Bukkit.broadcastMessage(_config.secondary + player.getName() + _config.primary + " has received a finding bonus of " + _config.secondary + _economy.format(_findingBonus) + _config.primary + " for introducing " + _config.secondary + getName(itemStack.getType()) + _config.primary + " to the GE for the first time!");
        }

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

    public void sell(UUID uuid, ItemStack itemStack) {
        if (!canBeSold(itemStack)) return;

        long value = 0;
        long amount = itemStack.getAmount();
        while (amount > 0) {
            value += getBaseSellPrice(itemStack.getType());
            addStock(itemStack.getType(), 1);
            amount--;
        }

        _economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), value);
    }

    public void addStock(Material material, long amount) {
        _stock.put(material, getStockCount(material) + amount);
    }

    public void removeStock(Material material, long amount) {
        _stock.put(material, getStockCount(material) - amount);
    }

    public long calculateSellValue(List<ItemStack> itemStacks) {
        Exchange simulation = getSimulation();
        long value = 0;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR) || !canBeSold(itemStack)) continue;
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
        Exchange simulation = getSimulation();
        long value = 0;
        for (int i = 0; i < stackSize; i++) {
            simulation.removeStock(material, 1);
            value += simulation.getFinalBuyPrice(material);
        }
        return value;
    }

    public long calculateTax(Material material, int stackSize) {
        Exchange simulation = getSimulation();
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

    public boolean canBeSold(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.hasEnchants()) {
                return false;
            }
        }

        return true;
    }

    public boolean newToMarket(ItemStack itemStack) {
        return !_stock.containsKey(itemStack.getType());
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

    public List<Material> getStock(String search, SortPreference sortPreference, int max) {
        if (search.length() == 0) return getStock(sortPreference);

        List<Material> materials = getStock(sortPreference);
        List<Material> filtered = new ArrayList<>();
        for (Material material : materials) {
            if (getName(material).toLowerCase().contains(search.toLowerCase()))
                filtered.add(material);
            if (filtered.size() >= max) return filtered;
        }
        return filtered;
    }

    public List<Material> getStock(SortPreference sortPreference) {
        switch (sortPreference) {
            default:
            case STOCK_COUNT:
                return getStockByStockCount(false);
            case STOCK_COUNT_ASCENDING:
                return getStockByStockCount(true);
            case ALPHABETICAL:
                return getStockByAlphabetical(false);
            case ALPHABETICAL_DESCENDING:
                return getStockByAlphabetical(true);
        }
    }

    public List<Material> getStockByStockCount(boolean ascending) {
        List<Material> materials = getStock();
        List<Material> sorted = new ArrayList<>();
        Material next;
        while (materials.size() > 0) {
            next = null;
            for (Material material : materials) {
                if (next == null || (ascending ? getStockCount(material) < getStockCount(next) : getStockCount(material) > getStockCount(next))) {
                    next = material;
                }
            }
            sorted.add(next);
            materials.remove(next);
        }
        return sorted;
    }

    public List<Material> getStockByAlphabetical(boolean descending) {
        List<Material> materials = getStock();
        List<Material> sorted = new ArrayList<>();
        Material next;
        while (materials.size() > 0) {
            next = null;
            for (Material material : materials) {
                if (next == null || (descending ? getName(material).compareTo(getName(next)) > 0 : getName(material).compareTo(getName(next)) < 0)) {
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

    private Exchange getSimulation() {
        Exchange exchange = new Exchange();
        exchange._stock = new HashMap<>(_stock);
        exchange._taxRate = _taxRate;
        return exchange;
    }

    public Icon getIcon(Material material) {
        int stackSize = (int) Math.min(getStockCount(material), material.getMaxStackSize());
        return new Icon(material, "",
                _config.primary + "Stock: " + _config.secondary + getStockCount(material),
                _config.primary + "Price: " + _config.secondary + "$" + getFinalBuyPrice(material),
                _config.primary + "Stack Price: " + _config.secondary + "$" + calculateBuyStackPrice(material, stackSize) + _config.primary + " (" + _config.secondary + stackSize + "x" + _config.primary + ", shift click)"
        );
    }

    public void setSortPreference(UUID uuid, SortPreference sortPreference) {
        _sortPreference.put(uuid, sortPreference);
    }

    public SortPreference getSortPreference(UUID uuid) {
        return _sortPreference.getOrDefault(uuid, SortPreference.STOCK_COUNT);
    }

    public enum SortPreference {
        STOCK_COUNT, STOCK_COUNT_ASCENDING, ALPHABETICAL, ALPHABETICAL_DESCENDING;

        public String getName() {
            return this.toString().substring(0, 1) + this.toString().substring(1).toLowerCase().replace("_", " ");
        }
    }

    public static String getName(Material material) {
        return material.toString().substring(0, 1) + material.toString().substring(1).toLowerCase().replace("_", " ");
    }
}
