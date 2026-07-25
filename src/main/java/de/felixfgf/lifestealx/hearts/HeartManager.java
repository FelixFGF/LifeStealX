package de.felixfgf.lifestealx.hearts;

import de.felixfgf.lifestealx.LifeStealX;
import de.felixfgf.lifestealx.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HeartManager {

    private final LifeStealX plugin;
    private final MiniMessage miniMessage;

    public HeartManager(LifeStealX plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public int getHearts(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        return data.getHearts();
    }

    public void setHearts(Player player, int hearts) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        int clamped = Math.max(plugin.getConfigManager().getMinimumHearts(),
                Math.min(plugin.getConfigManager().getMaximumHearts(), hearts));
        data.setHearts(clamped);
        applyHearts(player);
    }

    public boolean addHearts(Player player, int amount) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        int current = data.getHearts();
        int max = plugin.getConfigManager().getMaximumHearts();
        if (current >= max) {
            return false;
        }
        int newHearts = Math.min(max, current + amount);
        data.setHearts(newHearts);
        applyHearts(player);
        return true;
    }

    public boolean removeHearts(Player player, int amount) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        int current = data.getHearts();
        int min = plugin.getConfigManager().getMinimumHearts();
        int newHearts = Math.max(min, current - amount);
        data.setHearts(newHearts);
        applyHearts(player);
        return newHearts > min;
    }

    public void transferHeart(Player victim, Player killer) {
        PlayerData victimData = plugin.getStorageManager().getPlayerData(victim.getUniqueId());
        PlayerData killerData = plugin.getStorageManager().getPlayerData(killer.getUniqueId());

        int victimHearts = victimData.getHearts();
        int killerHearts = killerData.getHearts();
        int maxHearts = plugin.getConfigManager().getMaximumHearts();
        int minHearts = plugin.getConfigManager().getMinimumHearts();

        victimHearts = Math.max(minHearts, victimHearts - 1);
        victimData.setHearts(victimHearts);
        applyHearts(victim);

        if (killerHearts < maxHearts) {
            killerHearts = Math.min(maxHearts, killerHearts + 1);
            killerData.setHearts(killerHearts);
            applyHearts(killer);
        }

        playSound(victim, plugin.getConfigManager().getSoundHeartLoss());
        playSound(killer, plugin.getConfigManager().getSoundHeartGain());

        sendMessage(victim, plugin.getConfigManager().getHeartLost());
        sendMessage(killer, plugin.getConfigManager().getHeartGained());
    }

    public void resetHearts(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        data.setHearts(plugin.getConfigManager().getDefaultHearts());
        applyHearts(player);
    }

    public void applyHearts(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        double health = data.getHearts() * 2.0;

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) {
            plugin.getLogger().severe("Cannot apply hearts to " + player.getName() + ": MAX_HEALTH attribute is null");
            return;
        }
        maxHealth.setBaseValue(health);

        if (player.getHealth() > health) {
            player.setHealth(health);
        }
    }

    public boolean isAtMinimumHearts(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        return data.getHearts() <= plugin.getConfigManager().getMinimumHearts();
    }

    public boolean isAtMaximumHearts(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        return data.getHearts() >= plugin.getConfigManager().getMaximumHearts();
    }

    private void playSound(Player player, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name in config: " + soundName);
        }
    }

    private void sendMessage(Player player, String message) {
        Component component = miniMessage.deserialize(
                plugin.getConfigManager().getPrefix() + message);
        player.sendMessage(component);
    }
}