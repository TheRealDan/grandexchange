package dev.therealdan.exchange.commands;

import dev.therealdan.exchange.core.Exchange;
import dev.therealdan.exchange.core.inventory.BuyUI;
import dev.therealdan.exchange.core.inventory.PreferencesUI;
import dev.therealdan.exchange.core.inventory.SellUI;
import dev.therealdan.exchange.main.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class ExchangeCommand implements CommandExecutor {

    private DecimalFormat _decimalFormat = new DecimalFormat("#.##");

    private Config _config;
    private Exchange _exchange;
    private SellUI _sellUI;
    private BuyUI _buyUI;
    private PreferencesUI _preferencesUI;

    public ExchangeCommand(Config config, Exchange exchange, SellUI sellUI, BuyUI buyUI, PreferencesUI preferencesUI) {
        _config = config;
        _exchange = exchange;
        _sellUI = sellUI;
        _buyUI = buyUI;
        _preferencesUI = preferencesUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("Sell") && player != null) {
                _sellUI.open(player);
                return true;

            } else if (args[0].equalsIgnoreCase("Buy") && player != null) {
                StringBuilder search = new StringBuilder();
                if (args.length > 1) {
                    search.append(args[1]);
                    for (int i = 2; i < args.length; i++)
                        search.append(" ").append(args[i]);
                }
                _buyUI.open(player, search.toString());
                return true;

            } else if (args[0].equalsIgnoreCase("Preferences") && player != null) {
                _preferencesUI.open(player);
                return true;

            } else if (args[0].equalsIgnoreCase("King") && sender.isOp()) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(_config.primary + "Invalid Player");
                    return true;
                }

                _exchange.setKing(target);
                Bukkit.broadcastMessage(_config.secondary + target.getName() + _config.primary + " has been elected as King!");
                return true;

            } else if (args[0].equalsIgnoreCase("TaxRate") && isKing(player)) {
                int taxRate;
                try {
                    taxRate = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    taxRate = -1;
                }
                if (taxRate < 0) {
                    sender.sendMessage(_config.primary + "Tax rate must be at least 0%");
                    return true;
                }
                if (taxRate > 20) {
                    sender.sendMessage(_config.primary + "Tax rate can not exceed 20%");
                    return true;
                }

                _exchange.setTaxRate(taxRate * 0.01);
                Bukkit.broadcastMessage(_config.primary + "King " + _config.secondary + player.getName() + _config.primary + " has adjusted the Tax Rate to " + _config.secondary + taxRate + "%");
                return true;
            }
        }

        sender.sendMessage("");
        sender.sendMessage(_config.primary + "/EX Sell " + _config.secondary + "Open the GE Sell menu");
        sender.sendMessage(_config.primary + "/EX Buy [Search] " + _config.secondary + "Open the GE Buy menu");
        sender.sendMessage(_config.primary + "/EX Preferences " + _config.secondary + "Set your sorting preference");
        if (isKing(player)) sender.sendMessage(_config.primary + "/EX TaxRate [TaxRate] " + _config.secondary + "Set the tax rate");
        if (sender.isOp()) sender.sendMessage(_config.primary + "/EX King [Player] " + _config.secondary + "Elect a King");
        return true;
    }

    private boolean isKing(OfflinePlayer player) {
        return player != null && _exchange.getKing() != null && _exchange.getKing().getUniqueId().equals(player.getUniqueId());
    }
}