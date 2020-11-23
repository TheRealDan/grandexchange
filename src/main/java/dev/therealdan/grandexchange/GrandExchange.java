package dev.therealdan.grandexchange;

import dev.therealdan.grandexchange.commands.GrandExchangeCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrandExchange extends JavaPlugin {

    private Economy economy = null;

    @Override
    public void onEnable() {
        setupEconomy();

        getCommand("GrandExchange").setExecutor(new GrandExchangeCommand());
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> serviceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (serviceProvider == null) return;
        economy = serviceProvider.getProvider();
    }
}