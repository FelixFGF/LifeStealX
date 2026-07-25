package de.felixfgf.lifestealx.revive;

import de.felixfgf.lifestealx.LifeStealX;
import de.felixfgf.lifestealx.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReviveManager {

    private final LifeStealX plugin;
    private final MiniMessage miniMessage;
    private final PlainTextComponentSerializer plainSerializer;

    public ReviveManager(LifeStealX plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.plainSerializer = PlainTextComponentSerializer.plainText();
    }

    public void eliminatePlayer(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        data.setEliminated(true);

        long banDurationMillis = parseDuration(plugin.getConfigManager().getBanDuration());
        long banUntil = System.currentTimeMillis() + banDurationMillis;
        data.setEliminatedUntil(banUntil);

        String banMessage = plugin.getConfigManager().getBanMessage()
                .replace("%duration%", formatDuration(banDurationMillis));
        Component banComponent = miniMessage.deserialize(banMessage);
        // B3 FIX: Use PlainTextComponentSerializer instead of Component.toString()
        // Component.toString() produces internal Adventure serialized format, not readable text
        String plainMessage = plainSerializer.serialize(banComponent);

        Date banExpiry = new Date(banUntil);
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                plainMessage,
                banExpiry,
                "LifeStealX"
        );

        player.kick(banComponent);

        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundEliminate());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundEliminate());
        }

        plugin.getStorageManager().saveData();
    }

    public void revivePlayer(UUID uuid) {
        PlayerData data = plugin.getStorageManager().getPlayerData(uuid);
        if (!data.isEliminated()) return;

        data.setEliminated(false);
        data.setEliminatedUntil(0);
        data.setHearts(plugin.getConfigManager().getDefaultHearts());

        // B2 FIX: Remove UUID-based pardon - BanList.Type.NAME stores bans by player name, not UUID
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.getName() != null) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(offlinePlayer.getName());
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            plugin.getHeartManager().applyHearts(player);

            try {
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                        plugin.getConfigManager().getSoundRevive());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound: " +
                        plugin.getConfigManager().getSoundRevive());
            }
        }

        plugin.getStorageManager().saveData();
    }

    public boolean isEliminated(Player player) {
        PlayerData data = plugin.getStorageManager().getPlayerData(player.getUniqueId());
        return data.isEliminated();
    }

    public boolean isEliminated(UUID uuid) {
        PlayerData data = plugin.getStorageManager().getPlayerData(uuid);
        return data.isEliminated();
    }

    public List<EliminatedPlayerInfo> getEliminatedPlayers() {
        return plugin.getStorageManager().getEliminatedPlayers().stream()
                .map(data -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(data.getUuid());
                    String name = offlinePlayer.getName() != null ?
                            offlinePlayer.getName() : "Unknown";
                    return new EliminatedPlayerInfo(data.getUuid(), name);
                })
                .collect(Collectors.toList());
    }

    private long parseDuration(String duration) {
        try {
            if (duration.endsWith("d")) {
                int days = Integer.parseInt(duration.substring(0, duration.length() - 1));
                return (long) days * 24 * 60 * 60 * 1000;
            } else if (duration.endsWith("h")) {
                int hours = Integer.parseInt(duration.substring(0, duration.length() - 1));
                return (long) hours * 60 * 60 * 1000;
            } else if (duration.endsWith("m")) {
                int minutes = Integer.parseInt(duration.substring(0, duration.length() - 1));
                return (long) minutes * 60 * 1000;
            } else if (duration.endsWith("s")) {
                int seconds = Integer.parseInt(duration.substring(0, duration.length() - 1));
                return (long) seconds * 1000;
            } else {
                int days = Integer.parseInt(duration);
                return (long) days * 24 * 60 * 60 * 1000;
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid ban duration format: " + duration +
                    ". Using default 14d.");
            return 14L * 24 * 60 * 60 * 1000;
        }
    }

    private String formatDuration(long millis) {
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

    public static class EliminatedPlayerInfo {
        private final UUID uuid;
        private final String playerName;

        public EliminatedPlayerInfo(UUID uuid, String playerName) {
            this.uuid = uuid;
            this.playerName = playerName;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getPlayerName() {
            return playerName;
        }
    }
}