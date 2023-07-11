package dev.therealdan.exchange.main;

import dev.therealdan.exchange.commands.ExchangeCommand;
import dev.therealdan.exchange.core.Exchange;
import dev.therealdan.exchange.core.inventory.BuyUI;
import dev.therealdan.exchange.core.inventory.PreferencesUI;
import dev.therealdan.exchange.core.inventory.SellUI;
import dev.therealdan.exchange.events.ItemListener;
import dev.therealdan.exchange.events.SignListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExchangePlugin extends JavaPlugin {

    private Economy economy = null;
    private Exchange _exchange;
    private SellUI _sellUI;
    private BuyUI _buyUI;
    private PreferencesUI _preferencesUI;
    private SignListener _signListener;

    @Override
    public void onEnable() {
        setupEconomy();

        Config config = new Config(this);

        _exchange = new Exchange(this, economy, config);
        _sellUI = new SellUI(economy, _exchange);
        _buyUI = new BuyUI(economy, config, _exchange);
        _preferencesUI = new PreferencesUI(config, _exchange);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(_sellUI, this);
        manager.registerEvents(_buyUI, this);
        manager.registerEvents(_preferencesUI, this);
        manager.registerEvents(new ItemListener(_exchange), this);
        manager.registerEvents(_signListener = new SignListener(this, config, _exchange), this);

        getCommand("Exchange").setExecutor(new ExchangeCommand(config, _exchange, _sellUI, _buyUI, _preferencesUI));
    }

    @Override
    public void onDisable() {
        _sellUI.cancelAll();
        _buyUI.closeAll();
        _preferencesUI.closeAll();
        _exchange.save();
        _signListener.save();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> serviceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (serviceProvider == null) return;
        economy = serviceProvider.getProvider();
    }
}