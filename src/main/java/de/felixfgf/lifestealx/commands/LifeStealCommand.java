package de.felixfgf.lifestealx.commands;

import de.felixfgf.lifestealx.LifeStealX;
import de.felixfgf.lifestealx.revive.ReviveManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LifeStealCommand implements CommandExecutor, TabCompleter {

    private final LifeStealX plugin;
    private final MiniMessage miniMessage;

    private static final List<String> SUBCOMMANDS = List.of(
            "eliminate", "revive", "reset", "health", "reload"
    );

    public LifeStealCommand(LifeStealX plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "eliminate" -> handleEliminate(sender, args);
            case "revive" -> handleRevive(sender, args);
            case "reset" -> handleReset(sender, args);
            case "health" -> handleHealth(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleEliminate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifestealx.admin.eliminate")) {
            sendMessage(sender, plugin.getConfigManager().getNoPermission());
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "<red>Usage: /lifesteal eliminate <player></red>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sendMessage(sender, plugin.getConfigManager().getPlayerNotFound());
            return;
        }

        plugin.getReviveManager().eliminatePlayer(target);

        String message = plugin.getConfigManager().getPlayerEliminated()
                .replace("%player%", target.getName());
        sendMessage(sender, message);
    }

    private void handleRevive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifestealx.admin.revive")) {
            sendMessage(sender, plugin.getConfigManager().getNoPermission());
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "<red>Usage: /lifesteal revive <player></red>");
            return;
        }

        UUID targetUuid = findPlayerUuid(args[1]);
        if (targetUuid == null) {
            sendMessage(sender, plugin.getConfigManager().getPlayerNotFound());
            return;
        }

        if (!plugin.getReviveManager().isEliminated(targetUuid)) {
            sendMessage(sender, "<red>That player is not eliminated!</red>");
            return;
        }

        plugin.getReviveManager().revivePlayer(targetUuid);

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUuid);
        if (offlinePlayer.getName() != null) {
            playerName = offlinePlayer.getName();
        }

        String message = plugin.getConfigManager().getPlayerRevived()
                .replace("%player%", playerName);
        sendMessage(sender, message);
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifestealx.admin.reset")) {
            sendMessage(sender, plugin.getConfigManager().getNoPermission());
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "<red>Usage: /lifesteal reset <player></red>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sendMessage(sender, plugin.getConfigManager().getPlayerNotFound());
            return;
        }

        plugin.getHeartManager().resetHearts(target);

        String message = plugin.getConfigManager().getHeartReset()
                .replace("%player%", target.getName());
        sendMessage(sender, message);
    }

    private void handleHealth(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifestealx.admin.health")) {
            sendMessage(sender, plugin.getConfigManager().getNoPermission());
            return;
        }

        if (args.length < 2) {
            if (sender instanceof Player player) {
                int hearts = plugin.getHeartManager().getHearts(player);
                String message = plugin.getConfigManager().getHeartCheck()
                        .replace("%player%", player.getName())
                        .replace("%hearts%", String.valueOf(hearts));
                sendMessage(sender, message);
            } else {
                sendMessage(sender, "<red>Usage: /lifesteal health <player></red>");
            }
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sendMessage(sender, plugin.getConfigManager().getPlayerNotFound());
            return;
        }

        int hearts = plugin.getHeartManager().getHearts(target);
        String message = plugin.getConfigManager().getHeartCheck()
                .replace("%player%", target.getName())
                .replace("%hearts%", String.valueOf(hearts));
        sendMessage(sender, message);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("lifestealx.admin.reload")) {
            sendMessage(sender, plugin.getConfigManager().getNoPermission());
            return;
        }

        plugin.getConfigManager().loadConfig();
        sendMessage(sender, plugin.getConfigManager().getConfigReloaded());
    }

    @Nullable
    private UUID findPlayerUuid(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) return online.getUniqueId();

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for (OfflinePlayer offline : offlinePlayers) {
            String offlineName = offline.getName();
            if (offlineName != null && offlineName.equalsIgnoreCase(name)) {
                return offline.getUniqueId();
            }
        }

        return null;
    }

    private void sendUsage(CommandSender sender) {
        List<String> usage = List.of(
                "<gold>=== LifeStealX Commands ===</gold>",
                "<gold>/lifesteal eliminate <player></gold> <gray>- Eliminate a player</gray>",
                "<gold>/lifesteal revive <player></gold> <gray>- Revive a player</gray>",
                "<gold>/lifesteal reset <player></gold> <gray>- Reset hearts to default</gray>",
                "<gold>/lifesteal health [player]</gold> <gray>- Check hearts</gray>",
                "<gold>/lifesteal reload</gold> <gray>- Reload config</gray>"
        );

        for (String line : usage) {
            sender.sendMessage(miniMessage.deserialize(line));
        }
    }

    private void sendMessage(CommandSender sender, String message) {
        Component component = miniMessage.deserialize(
                plugin.getConfigManager().getPrefix() + message);
        sender.sendMessage(component);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                 @NotNull Command command,
                                                 @NotNull String alias,
                                                 @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("eliminate") || subcommand.equals("reset") ||
                    subcommand.equals("health")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (subcommand.equals("revive")) {
                return plugin.getReviveManager().getEliminatedPlayers().stream()
                        .map(ReviveManager.EliminatedPlayerInfo::getPlayerName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}