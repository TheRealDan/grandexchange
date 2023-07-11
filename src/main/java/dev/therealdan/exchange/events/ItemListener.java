package dev.therealdan.exchange.events;

import dev.therealdan.exchange.core.Exchange;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ItemListener implements Listener {

    private Exchange _exchange;

    public ItemListener(Exchange exchange) {
        _exchange = exchange;
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (!_exchange.canBeSold(event.getEntity().getItemStack())) return;

        _exchange.addStock(event.getEntity().getItemStack().getType(), event.getEntity().getItemStack().getAmount());
    }
}