package net.azisaba.joinfilter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JoinFilterCommand implements TabExecutor {
    private static final List<String> COMMANDS = Arrays.asList("reload", "add", "remove", "remove-self");
    private final JoinFilter plugin;

    public JoinFilterCommand(@NotNull JoinFilter plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (args.length == 0) {
                sender.sendMessage("Usage: /joinfilter <args>");
                return;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reload();
                sender.sendMessage("Reloaded.");
                return;
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /joinfilter add <player> <server> [expireSeconds]");
                    return;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Player not found.");
                    return;
                }
                if (args.length >= 4) {
                    plugin.addPlayerWithExpire(player.getUniqueId(), args[2], Long.parseLong(args[3]));
                    sender.sendMessage("Added player " + args[1] + " (" + player.getUniqueId() + ") to server " + args[2] + " with expire time " + args[3] + " seconds");
                    return;
                }
                plugin.addPlayer(player.getUniqueId(), args[2]);
                sender.sendMessage("Added player " + args[1] + " (" + player.getUniqueId() + ") to server " + args[2]);
                return;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (args.length != 3) {
                    sender.sendMessage("Usage: /joinfilter remove <player> <server>");
                    return;
                }
                String uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
                plugin.removePlayer(Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString(), args[2]);
                sender.sendMessage("Removed player " + args[1] + " (" + uuid + ") from server " + args[2]);
                return;
            }
            if (args[0].equalsIgnoreCase("remove-self")) {
                if (args.length != 2) {
                    sender.sendMessage("Usage: /joinfilter remove-self <player>");
                    return;
                }
                String uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
                plugin.removePlayer(uuid, plugin.getServerName());
                sender.sendMessage("Removed player " + args[1] + " (" + uuid + ") from server " + plugin.getServerName());
                return;
            }
            sender.sendMessage("Usage: /joinfilter <args>");
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return COMMANDS.stream().filter(cmd -> cmd.startsWith(args[0])).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.startsWith(args[0])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
