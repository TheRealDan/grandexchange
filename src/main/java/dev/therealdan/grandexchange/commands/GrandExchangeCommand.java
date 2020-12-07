package dev.therealdan.grandexchange.commands;

import dev.therealdan.grandexchange.core.GrandExchange;
import dev.therealdan.grandexchange.core.inventory.BuyUI;
import dev.therealdan.grandexchange.core.inventory.SellUI;
import dev.therealdan.grandexchange.main.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class GrandExchangeCommand implements CommandExecutor {

    private DecimalFormat _decimalFormat = new DecimalFormat("#.##");

    private Config _config;
    private GrandExchange _grandExchange;
    private SellUI _sellUI;
    private BuyUI _buyUI;

    public GrandExchangeCommand(Config config, GrandExchange grandExchange, SellUI sellUI, BuyUI buyUI) {
        _config = config;
        _grandExchange = grandExchange;
        _sellUI = sellUI;
        _buyUI = buyUI;
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

            } else if (args[0].equalsIgnoreCase("King") && sender.isOp()) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(_config.primary + "Invalid Player");
                    return true;
                }

                _grandExchange.setKing(target);
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

                _grandExchange.setTaxRate(taxRate * 0.01);
                Bukkit.broadcastMessage(_config.primary + "King " + _config.secondary + player.getName() + _config.primary + " has adjusted the Tax Rate to " + _config.secondary + taxRate + "%");
                return true;
            }
        }

        sender.sendMessage("");
        sender.sendMessage(_config.primary + "/GE Sell " + _config.secondary + "Open the GE Sell menu");
        sender.sendMessage(_config.primary + "/GE Buy [Search] " + _config.secondary + "Open the GE Buy menu");
        if (isKing(player)) sender.sendMessage(_config.primary + "/GE TaxRate [TaxRate] " + _config.secondary + "Set the tax rate");
        if (sender.isOp()) sender.sendMessage(_config.primary + "/GE King [Player] " + _config.secondary + "Elect a King");
        return true;
    }

    private boolean isKing(OfflinePlayer player) {
        return player != null && _grandExchange.getKing() != null && _grandExchange.getKing().getUniqueId().equals(player.getUniqueId());
    }
}