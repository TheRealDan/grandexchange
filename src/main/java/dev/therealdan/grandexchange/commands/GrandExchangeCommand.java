package dev.therealdan.grandexchange.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GrandExchangeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        return true;
    }
}