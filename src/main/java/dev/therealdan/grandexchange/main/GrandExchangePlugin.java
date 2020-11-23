package dev.therealdan.grandexchange.main;

import dev.therealdan.grandexchange.commands.GrandExchangeCommand;
import dev.therealdan.grandexchange.core.GrandExchange;
import dev.therealdan.grandexchange.core.inventory.SellUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrandExchangePlugin extends JavaPlugin {

    private Economy economy = null;

    @Override
    public void onEnable() {
        setupEconomy();

        Config config = new Config(this);

        GrandExchange grandExchange = new GrandExchange(economy, config);
        SellUI sellUI = new SellUI(economy, grandExchange);

        getServer().getPluginManager().registerEvents(sellUI, this);

        getCommand("GrandExchange").setExecutor(new GrandExchangeCommand(config, sellUI));
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> serviceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (serviceProvider == null) return;
        economy = serviceProvider.getProvider();
    }
}