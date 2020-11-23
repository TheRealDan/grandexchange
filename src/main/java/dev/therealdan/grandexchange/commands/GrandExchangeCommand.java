package dev.therealdan.grandexchange.commands;

import dev.therealdan.grandexchange.core.inventory.BuyUI;
import dev.therealdan.grandexchange.core.inventory.SellUI;
import dev.therealdan.grandexchange.main.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GrandExchangeCommand implements CommandExecutor {

    private Config _config;
    private SellUI _sellUI;
    private BuyUI _buyUI;

    public GrandExchangeCommand(Config config, SellUI sellUI, BuyUI buyUI) {
        _config = config;
        _sellUI = sellUI;
        _buyUI = buyUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("Sell")) {
                _sellUI.open(player);
                return true;
            } else if (args[0].equalsIgnoreCase("Buy")) {
                StringBuilder search = new StringBuilder();
                if (args.length > 1) {
                    search.append(args[1]);
                    for (int i = 2; i < args.length; i++)
                        search.append(" ").append(args[i]);
                }
                _buyUI.open(player, search.toString());
                return true;
            }
        }

        sender.sendMessage(_config.primary + "/GE Sell " + _config.secondary + "Open the GE Sell menu");
        sender.sendMessage(_config.primary + "/GE Buy [Search] " + _config.secondary + "Open the GE Buy menu");
        return true;
    }
}