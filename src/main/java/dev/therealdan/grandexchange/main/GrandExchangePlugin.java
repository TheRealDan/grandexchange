package dev.therealdan.grandexchange.main;

import dev.therealdan.grandexchange.commands.GrandExchangeCommand;
import dev.therealdan.grandexchange.core.GrandExchange;
import dev.therealdan.grandexchange.core.inventory.BuyUI;
import dev.therealdan.grandexchange.core.inventory.PreferencesUI;
import dev.therealdan.grandexchange.core.inventory.SellUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrandExchangePlugin extends JavaPlugin {

    private Economy economy = null;
    private GrandExchange _grandExchange;
    private SellUI _sellUI;
    private BuyUI _buyUI;
    private PreferencesUI _preferencesUI;

    @Override
    public void onEnable() {
        setupEconomy();

        Config config = new Config(this);

        _grandExchange = new GrandExchange(this, economy, config);
        _sellUI = new SellUI(economy, _grandExchange);
        _buyUI = new BuyUI(economy, config, _grandExchange);
        _preferencesUI = new PreferencesUI(config, _grandExchange);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(_sellUI, this);
        manager.registerEvents(_buyUI, this);
        manager.registerEvents(_preferencesUI, this);

        getCommand("GrandExchange").setExecutor(new GrandExchangeCommand(config, _grandExchange, _sellUI, _buyUI, _preferencesUI));
    }

    @Override
    public void onDisable() {
        _sellUI.cancelAll();
        _buyUI.closeAll();
        _preferencesUI.closeAll();
        _grandExchange.save();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> serviceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (serviceProvider == null) return;
        economy = serviceProvider.getProvider();
    }
}