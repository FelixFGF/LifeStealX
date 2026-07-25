package de.felixfgf.lifestealx.combat;

import de.felixfgf.lifestealx.LifeStealX;
import de.felixfgf.lifestealx.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatManager {

    private final LifeStealX plugin;
    private final MiniMessage miniMessage;
    private final Set<UUID> combatPlayers;
    private final CombatCheckRunnable runnable;

    public CombatManager(LifeStealX plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.combatPlayers = new HashSet<>();
        this.runnable = new CombatCheckRunnable();
        this.runnable.runTaskTimer(plugin, 0L, 20L);
    }

    public void tagPlayers(Player attacker, Player victim) {
        int combatTime = plugin.getConfigManager().getCombatTime();
        PlayerData attackerData = plugin.getStorageManager().getPlayerData(attacker.getUniqueId());
        PlayerData victimData = plugin.getStorageManager().getPlayerData(victim.getUniqueId());

        victimData.setLastAttacker(attacker.getUniqueId());
        attackerData.setLastAttacker(victim.getUniqueId());

        attackerData.refreshCombatTag(combatTime);
        victimData.refreshCombatTag(combatTime);

        combatPlayers.add(attacker.getUniqueId());
        combatPlayers.add(victim.getUniqueId());

        sendMessage(attacker, plugin.getConfigManager().getCombatTagged());
        sendMessage(victim, plugin.getConfigManager().getCombatTagged());

        playCombatSound(attacker);
        playCombatSound(victim);
    }

    public Player getLastAttacker(Player victim) {
        PlayerData data = plugin.getStorageManager().getPlayerData(victim.getUniqueId());
        UUID attackerUuid = data.getLastAttacker();
        if (attackerUuid == null) return null;
        return Bukkit.getPlayer(attackerUuid);
    }

    public boolean isInCombat(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        return data.isInCombat();
    }

    public void handleCombatLogout(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        UUID lastAttackerUuid = data.getLastAttacker();

        if (lastAttackerUuid == null) return;

        // If the player is already dead (e.g. died and logged out simultaneously),
        // the death event already handled the heart transfer. Skip to prevent double transfer.
        if (player.isDead() || player.getHealth() <= 0.0) {
            data.clearCombatTag();
            combatPlayers.remove(player.getUniqueId());
            return;
        }

        Player attacker = Bukkit.getPlayer(lastAttackerUuid);
        if (attacker == null || !attacker.isOnline()) return;

        plugin.getHeartManager().transferHeart(player, attacker);

        if (plugin.getConfigManager().isBroadcastCombatLog()) {
            String message = plugin.getConfigManager().getCombatLog()
                    .replace("%player%", player.getName());
            Component component = miniMessage.deserialize(
                    plugin.getConfigManager().getPrefix() + message);
            Bukkit.broadcast(component);
        }

        if (plugin.getHeartManager().isAtMinimumHearts(player)) {
            plugin.getReviveManager().eliminatePlayer(player);
        }

        data.clearCombatTag();
        PlayerData attackerData = plugin.getStorageManager().getPlayerData(lastAttackerUuid);
        if (attackerData != null) {
            attackerData.clearCombatTag();
        }
        combatPlayers.remove(player.getUniqueId());
        combatPlayers.remove(lastAttackerUuid);
    }

    public void removeCombatTag(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        if (data.isInCombat()) {
            sendMessage(player, plugin.getConfigManager().getCombatUntagged());
            playCombatEndSound(player);
        }
        data.clearCombatTag();
        combatPlayers.remove(player.getUniqueId());
    }

    public void shutdown() {
        runnable.cancel();
        combatPlayers.clear();
    }

    private void sendMessage(Player player, String message) {
        Component component = miniMessage.deserialize(
                plugin.getConfigManager().getPrefix() + message);
        player.sendMessage(component);
    }

    private void playCombatSound(Player player) {
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundCombatStart());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundCombatStart());
        }
    }

    private void playCombatEndSound(Player player) {
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundCombatEnd());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundCombatEnd());
        }
    }

    private class CombatCheckRunnable extends BukkitRunnable {
        @Override
        public void run() {
            Set<UUID> toRemove = new HashSet<>();

            for (UUID uuid : combatPlayers) {
                PlayerData data = plugin.getStorageManager().getPlayerData(uuid);
                if (!data.isInCombat()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        sendMessage(player, plugin.getConfigManager().getCombatUntagged());
                        playCombatEndSound(player);
                    }
                    toRemove.add(uuid);
                }
            }

            combatPlayers.removeAll(toRemove);
        }
    }
}