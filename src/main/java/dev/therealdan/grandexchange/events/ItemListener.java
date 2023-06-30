package dev.therealdan.grandexchange.events;

import dev.therealdan.grandexchange.core.GrandExchange;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ItemListener implements Listener {

    private GrandExchange _grandExchange;

    public ItemListener(GrandExchange grandExchange) {
        _grandExchange = grandExchange;
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (!_grandExchange.canBeSold(event.getEntity().getItemStack())) return;

        _grandExchange.addStock(event.getEntity().getItemStack().getType(), event.getEntity().getItemStack().getAmount());
    }
}