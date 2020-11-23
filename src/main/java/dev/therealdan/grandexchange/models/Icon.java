package dev.therealdan.grandexchange.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Icon extends ItemStack {

    public Icon(Material material, String... text) {
        this(material, 1, text);
    }

    public Icon(Material material, int amount, String... text) {
        super(material, amount);

        if (text.length > 0) {
            ItemMeta itemMeta = getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (text[0].length() > 0) itemMeta.setDisplayName(text[0]);
            if (text.length > 1) {
                List<String> lore = new ArrayList<>();
                for (int i = 1; i < text.length; i++) lore.add(text[i]);
                itemMeta.setLore(lore);
            }
            setItemMeta(itemMeta);
        }
    }
}