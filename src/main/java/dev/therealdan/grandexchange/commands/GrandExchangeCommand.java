package dev.therealdan.grandexchange.commands;

import dev.therealdan.grandexchange.core.inventory.SellUI;
import dev.therealdan.grandexchange.main.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GrandExchangeCommand implements CommandExecutor {

    private Config _config;
    private SellUI _sellUI;

    public GrandExchangeCommand(Config config, SellUI sellUI) {
        _config = config;
        _sellUI = sellUI;
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
            }
        }

        sender.sendMessage(_config.primary + "/GE Sell " + _config.secondary + "Open the GE Sell menu");
        sender.sendMessage(_config.primary + "/GE Buy [Search] " + _config.secondary + "Open the GE Buy menu");
        return true;
    }
}