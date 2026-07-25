package de.felixfgf.lifestealx.listeners;

import de.felixfgf.lifestealx.LifeStealX;
import de.felixfgf.lifestealx.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final LifeStealX plugin;
    private final MiniMessage miniMessage;

    public PlayerListener(LifeStealX plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());

        if (data.isEliminated()) {
            long now = System.currentTimeMillis();
            if (data.getEliminatedUntil() > now) {
                String banMessage = plugin.getConfigManager().getBanMessage()
                        .replace("%duration%", formatRemaining(data.getEliminatedUntil() - now));
                player.kick(miniMessage.deserialize(banMessage));
                return;
            } else {
                data.setEliminated(false);
                data.setEliminatedUntil(0);
                data.setHearts(plugin.getConfigManager().getDefaultHearts());
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(player.getName());
            }
        }

        plugin.getHeartManager().applyHearts(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());

        if (data.isInCombat()) {
            plugin.getCombatManager().handleCombatLogout(player);
        }

        plugin.getStorageManager().saveData();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        PlayerData victimData = plugin.getStorageManager().getPlayerData(victim.getUniqueId());
        if (victimData.isEliminated()) return;

        boolean atMinHearts = plugin.getHeartManager().isAtMinimumHearts(victim);

        Player killer = null;
        Player lastAttacker = plugin.getCombatManager().getLastAttacker(victim);
        if (lastAttacker != null && lastAttacker.isOnline()) {
            killer = lastAttacker;
        }

        if (killer == null && victim.getKiller() != null) {
            killer = victim.getKiller();
        }

        // B1 FIX: Remove early return on killer == null so elimination always runs
        // If there is a valid killer (not self), transfer heart
        if (killer != null && !killer.equals(victim)) {
            plugin.getHeartManager().transferHeart(victim, killer);
        }

        if (atMinHearts) {
            plugin.getReviveManager().eliminatePlayer(victim);
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getHeartManager().applyHearts(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.equals(victim)) return;
        if (attacker.getGameMode() == GameMode.CREATIVE ||
                attacker.getGameMode() == GameMode.SPECTATOR) return;
        if (victim.getGameMode() == GameMode.CREATIVE ||
                victim.getGameMode() == GameMode.SPECTATOR) return;

        plugin.getCombatManager().tagPlayers(attacker, victim);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().name().contains("RIGHT_")) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (plugin.getItemManager().isHeartItem(item)) {
            event.setCancelled(true);
            handleHeartItemUse(player, item);
            return;
        }

        if (plugin.getItemManager().isReviveItem(item)) {
            event.setCancelled(true);
            handleReviveItemUse(player);
        }
    }

    private void handleHeartItemUse(Player player, ItemStack item) {
        if (plugin.getHeartManager().isAtMaximumHearts(player)) {
            Component message = miniMessage.deserialize(
                    plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getItemMaxHearts());
            player.sendMessage(message);
            return;
        }

        item.setAmount(item.getAmount() - 1);
        plugin.getHeartManager().addHearts(player, 1);

        int hearts = plugin.getHeartManager().getHearts(player);
        String message = plugin.getConfigManager().getItemConsumed()
                .replace("%hearts%", String.valueOf(hearts));
        Component component = miniMessage.deserialize(
                plugin.getConfigManager().getPrefix() + message);
        player.sendMessage(component);

        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundHeartGain());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundHeartGain());
        }
    }

    private void handleReviveItemUse(Player player) {
        plugin.getReviveGUI().openGUI(player);

        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundGuiOpen());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundGuiOpen());
        }
    }

    private String formatRemaining(long millis) {
        long days = millis / (24 * 60 * 60 * 1000);
        long hours = (millis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        if (hours > 0) sb.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        if (minutes > 0) sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        if (sb.length() == 0) sb.append("a few moments");

        return sb.toString().trim();
    }
}