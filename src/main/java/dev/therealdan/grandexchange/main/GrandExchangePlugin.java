package dev.therealdan.grandexchange.main;

import dev.therealdan.grandexchange.commands.GrandExchangeCommand;
import dev.therealdan.grandexchange.core.GrandExchange;
import dev.therealdan.grandexchange.core.inventory.SellUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrandExchangePlugin extends JavaPlugin {

    private Economy economy = null;
    private GrandExchange _grandExchange;
    private SellUI _sellUI;

    @Override
    public void onEnable() {
        setupEconomy();

        Config config = new Config(this);

        _grandExchange = new GrandExchange(this, economy, config);
        _sellUI = new SellUI(economy, _grandExchange);

        getServer().getPluginManager().registerEvents(_sellUI, this);

        getCommand("GrandExchange").setExecutor(new GrandExchangeCommand(config, _sellUI));
    }

    @Override
    public void onDisable() {
        _sellUI.cancelAll();
        _grandExchange.save();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> serviceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (serviceProvider == null) return;
        economy = serviceProvider.getProvider();
    }
}