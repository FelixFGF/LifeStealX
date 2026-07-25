package de.felixfgf.lifestealx.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.felixfgf.lifestealx.LifeStealX;
import de.felixfgf.lifestealx.data.PlayerData;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    private final LifeStealX plugin;
    private final Gson gson;
    private final File dataFile;
    private final Map<UUID, PlayerData> playerDataMap;

    private static class StorageData {
        Map<String, Integer> hearts = new HashMap<>();
        Map<String, Boolean> eliminated = new HashMap<>();
        Map<String, Long> eliminatedUntil = new HashMap<>();
        Map<String, String> lastAttacker = new HashMap<>();
        Map<String, Long> combatTagExpiry = new HashMap<>();
    }

    public StorageManager(LifeStealX plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.json");
        this.playerDataMap = new ConcurrentHashMap<>();
        loadData();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, key -> {
            PlayerData data = new PlayerData(uuid, plugin.getConfigManager().getDefaultHearts());
            return data;
        });
    }

    public void saveData() {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }

            StorageData storageData = new StorageData();
            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
                String uuid = entry.getKey().toString();
                PlayerData data = entry.getValue();
                storageData.hearts.put(uuid, data.getHearts());
                storageData.eliminated.put(uuid, data.isEliminated());
                if (data.getEliminatedUntil() > 0) {
                    storageData.eliminatedUntil.put(uuid, data.getEliminatedUntil());
                }
                if (data.getLastAttacker() != null) {
                    storageData.lastAttacker.put(uuid, data.getLastAttacker().toString());
                }
                if (data.getCombatTagExpiry() > 0) {
                    storageData.combatTagExpiry.put(uuid, data.getCombatTagExpiry());
                }
            }

            // Write to temp file first, then atomically rename to prevent data corruption
            File tempFile = new File(dataFile.getParentFile(), dataFile.getName() + ".tmp");
            try (Writer writer = new FileWriter(tempFile)) {
                gson.toJson(storageData, writer);
                writer.flush();
            }
            // Atomic rename: if this fails, the original file remains intact
            if (!tempFile.renameTo(dataFile)) {
                plugin.getLogger().severe("Failed to rename temp file to playerdata.json! Data may be lost.");
                // Attempt to delete temp file to avoid accumulation
                tempFile.delete();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    public void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        try {
            Type type = new TypeToken<StorageData>() {}.getType();
            StorageData storageData;
            try (Reader reader = new FileReader(dataFile)) {
                storageData = gson.fromJson(reader, type);
            }

            if (storageData == null) return;

            Set<UUID> allUuids = new HashSet<>();
            allUuids.addAll(parseUuidKeys(storageData.hearts.keySet()));
            allUuids.addAll(parseUuidKeys(storageData.eliminated.keySet()));

            for (UUID uuid : allUuids) {
                String uuidStr = uuid.toString();
                PlayerData data = getPlayerData(uuid);

                if (storageData.hearts.containsKey(uuidStr)) {
                    data.setHearts(storageData.hearts.get(uuidStr));
                }
                if (storageData.eliminated.containsKey(uuidStr)) {
                    data.setEliminated(storageData.eliminated.get(uuidStr));
                }
                if (storageData.eliminatedUntil.containsKey(uuidStr)) {
                    data.setEliminatedUntil(storageData.eliminatedUntil.get(uuidStr));
                }
                if (storageData.lastAttacker.containsKey(uuidStr)) {
                    try {
                        data.setLastAttacker(UUID.fromString(storageData.lastAttacker.get(uuidStr)));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (storageData.combatTagExpiry.containsKey(uuidStr)) {
                    data.setCombatTagExpiry(storageData.combatTagExpiry.get(uuidStr));
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
        }
    }

    private Set<UUID> parseUuidKeys(Set<String> keys) {
        Set<UUID> uuids = new HashSet<>();
        for (String key : keys) {
            try {
                uuids.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {}
        }
        return uuids;
    }

    public List<PlayerData> getEliminatedPlayers() {
        List<PlayerData> eliminated = new ArrayList<>();
        for (PlayerData data : playerDataMap.values()) {
            if (data.isEliminated()) {
                eliminated.add(data);
            }
        }
        return eliminated;
    }

    public void clearAllCombatTags() {
        for (PlayerData data : playerDataMap.values()) {
            data.clearCombatTag();
        }
    }
}