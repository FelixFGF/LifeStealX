package de.felixfgf.lifestealx.commands;

import de.felixfgf.lifestealx.LifeStealX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WithdrawCommand implements CommandExecutor {

    private final LifeStealX plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, Long> cooldownMap;

    public WithdrawCommand(LifeStealX plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.cooldownMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("lifestealx.withdraw")) {
            sendMessage(player, plugin.getConfigManager().getNoPermission());
            return true;
        }

        if (args.length < 1) {
            sendMessage(player, plugin.getConfigManager().getInvalidAmount());
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
            if (amount <= 0) {
                sendMessage(player, plugin.getConfigManager().getInvalidAmount());
                return true;
            }
        } catch (NumberFormatException e) {
            sendMessage(player, plugin.getConfigManager().getInvalidAmount());
            return true;
        }

        // Anti-spam cooldown
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastUsed = cooldownMap.get(uuid);
        if (lastUsed != null && now - lastUsed < 500) {
            return true;
        }
        cooldownMap.put(uuid, now);

        // Check if player has enough hearts
        int currentHearts = plugin.getHeartManager().getHearts(player);
        int minHearts = plugin.getConfigManager().getMinimumHearts();

        if (currentHearts - amount < minHearts) {
            String message = plugin.getConfigManager().getWithdrawFail()
                    .replace("%hearts%", String.valueOf(currentHearts - minHearts));
            sendMessage(player, message);
            return true;
        }

        // Remove hearts
        boolean success = plugin.getHeartManager().removeHearts(player, amount);
        if (!success) {
            sendMessage(player, plugin.getConfigManager().getWithdrawFail()
                    .replace("%hearts%", String.valueOf(currentHearts - minHearts)));
            return true;
        }

        // Give heart items
        for (int i = 0; i < amount; i++) {
            org.bukkit.inventory.ItemStack heartItem = plugin.getItemManager().createHeartItem();
            Map<Integer, org.bukkit.inventory.ItemStack> leftover = player.getInventory().addItem(heartItem);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
            }
        }

        String message = plugin.getConfigManager().getWithdrawSuccess()
                .replace("%amount%", String.valueOf(amount));
        sendMessage(player, message);

        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundWithdraw());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundWithdraw());
        }

        return true;
    }

    private void sendMessage(Player player, String message) {
        Component component = miniMessage.deserialize(
                plugin.getConfigManager().getPrefix() + message);
        player.sendMessage(component);
    }
}