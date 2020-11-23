package dev.therealdan.grandexchange.core;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrandExchange {

    private HashMap<Material, Long> _stock = new HashMap<>();

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