package de.felixfgf.lifestealx.config;

import de.felixfgf.lifestealx.LifeStealX;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final LifeStealX plugin;
    private FileConfiguration config;

    // Heart settings
    private int defaultHearts;
    private int minimumHearts;
    private int maximumHearts;

    // Combat settings
    private int combatTime;

    // Ban settings
    private String banDuration;
    private String banMessage;

    // Feature flags
    private boolean broadcastCombatLog;
    private boolean recipesEnabled;

    // Item config
    private String heartItemName;
    private List<String> heartItemLore;
    private String reviveItemName;
    private List<String> reviveItemLore;

    // Messages
    private String prefix;
    private String heartGained;
    private String heartLost;
    private String maxHearts;
    private String minHearts;
    private String withdrawSuccess;
    private String withdrawFail;
    private String invalidAmount;
    private String noPermission;
    private String playerNotFound;
    private String playerEliminated;
    private String playerRevived;
    private String heartReset;
    private String heartCheck;
    private String configReloaded;
    private String combatLog;
    private String combatTagged;
    private String combatUntagged;
    private String itemConsumed;
    private String itemMaxHearts;
    private String reviveSuccess;
    private String noEliminatedPlayers;
    private String eliminatedTitle;
    private String eliminatedSubtitle;

    // Sounds
    private String soundHeartGain;
    private String soundHeartLoss;
    private String soundEliminate;
    private String soundRevive;
    private String soundCombatStart;
    private String soundCombatEnd;
    private String soundWithdraw;
    private String soundGuiOpen;
    private String soundGuiClick;
    private String soundError;

    public ConfigManager(LifeStealX plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadValues();
    }

    private void loadValues() {
        // Heart settings
        this.defaultHearts = config.getInt("default-hearts", 10);
        this.minimumHearts = config.getInt("minimum-hearts", 1);
        this.maximumHearts = config.getInt("maximum-hearts", 20);

        // Clamp values to valid range
        if (defaultHearts < minimumHearts) defaultHearts = minimumHearts;
        if (defaultHearts > maximumHearts) defaultHearts = maximumHearts;
        if (minimumHearts < 1) minimumHearts = 1;
        if (maximumHearts > 20) maximumHearts = 20;
        if (minimumHearts > maximumHearts) minimumHearts = maximumHearts;

        // Combat
        this.combatTime = config.getInt("combat-time", 20);
        if (combatTime < 1) combatTime = 1;

        // Ban
        this.banDuration = config.getString("ban-duration", "14d");
        this.banMessage = config.getString("ban-message",
                "<red>You have been eliminated!</red><newline><gray>You will be unbanned in <yellow>%duration%</yellow>.</gray>");

        // Features
        this.broadcastCombatLog = config.getBoolean("broadcast-combat-log", true);
        this.recipesEnabled = config.getBoolean("recipes-enabled", true);

        // Item config
        this.heartItemName = config.getString("heart-item-name",
                "<gradient:red:dark_red>Heart Fragment</gradient>");
        this.heartItemLore = config.getStringList("heart-item-lore");
        if (heartItemLore.isEmpty()) {
            heartItemLore = List.of(
                    "<gray>Right-click to consume</gray>",
                    "<gray>and recover one heart.</gray>"
            );
        }

        this.reviveItemName = config.getString("revive-item-name",
                "<gradient:gold:yellow>Revive Beacon</gradient>");
        this.reviveItemLore = config.getStringList("revive-item-lore");
        if (reviveItemLore.isEmpty()) {
            reviveItemLore = List.of(
                    "<gray>Right-click to open</gray>",
                    "<gray>the revive menu.</gray>"
            );
        }

        // Messages
        ConfigurationSection msg = config.getConfigurationSection("messages");
        if (msg != null) {
            this.prefix = msg.getString("prefix",
                    "<dark_gray>[<gradient:red:dark_red>LifeStealX</gradient>]</dark_gray> ");
            this.heartGained = msg.getString("heart-gained", "<green>You gained a heart!</green>");
            this.heartLost = msg.getString("heart-lost", "<red>You lost a heart!</red>");
            this.maxHearts = msg.getString("max-hearts", "<red>You already have maximum hearts!</red>");
            this.minHearts = msg.getString("min-hearts", "<red>You are at the minimum heart limit!</red>");
            this.withdrawSuccess = msg.getString("withdraw-success",
                    "<green>Successfully withdrew %amount% heart(s)!</green>");
            this.withdrawFail = msg.getString("withdraw-fail",
                    "<red>You don't have enough hearts! You have %hearts% heart(s).</red>");
            this.invalidAmount = msg.getString("invalid-amount",
                    "<red>Invalid amount! Please enter a positive number.</red>");
            this.noPermission = msg.getString("no-permission",
                    "<red>You don't have permission to use this command!</red>");
            this.playerNotFound = msg.getString("player-not-found", "<red>Player not found!</red>");
            this.playerEliminated = msg.getString("player-eliminated",
                    "<red>%player% has been eliminated!</red>");
            this.playerRevived = msg.getString("player-revived",
                    "<green>%player% has been revived!</green>");
            this.heartReset = msg.getString("heart-reset",
                    "<green>%player%'s hearts have been reset to default!</green>");
            this.heartCheck = msg.getString("heart-check",
                    "<gold>%player% has <yellow>%hearts%</yellow> heart(s).</gold>");
            this.configReloaded = msg.getString("config-reloaded",
                    "<green>Configuration reloaded successfully!</green>");
            this.combatLog = msg.getString("combat-log",
                    "<red>%player% logged out during combat!</red>");
            this.combatTagged = msg.getString("combat-tagged",
                    "<red>You are now in combat! Do not disconnect!</red>");
            this.combatUntagged = msg.getString("combat-untagged",
                    "<green>You are no longer in combat.</green>");
            this.itemConsumed = msg.getString("item-consumed",
                    "<green>Consumed Heart Fragment. You now have %hearts% heart(s).</green>");
            this.itemMaxHearts = msg.getString("item-max-hearts",
                    "<red>You already have the maximum amount of hearts!</red>");
            this.reviveSuccess = msg.getString("revive-success",
                    "<green>Successfully revived %player%!</green>");
            this.noEliminatedPlayers = msg.getString("no-eliminated-players",
                    "<gray>There are no eliminated players to revive.</gray>");
            this.eliminatedTitle = msg.getString("eliminated-title",
                    "<red>You have been eliminated!</red>");
            this.eliminatedSubtitle = msg.getString("eliminated-subtitle",
                    "<gray>You will be unbanned in %duration%</gray>");
        }

        // Sounds
        ConfigurationSection sounds = config.getConfigurationSection("sounds");
        if (sounds != null) {
            this.soundHeartGain = sounds.getString("heart-gain", "ENTITY_PLAYER_LEVELUP");
            this.soundHeartLoss = sounds.getString("heart-loss", "ENTITY_BLAZE_HURT");
            this.soundEliminate = sounds.getString("eliminate", "ENTITY_WITHER_DEATH");
            this.soundRevive = sounds.getString("revive", "ENTITY_TOTEM_USE");
            this.soundCombatStart = sounds.getString("combat-start", "ENTITY_ENDERMAN_SCREAM");
            this.soundCombatEnd = sounds.getString("combat-end", "BLOCK_NOTE_BLOCK_PLING");
            this.soundWithdraw = sounds.getString("withdraw", "ENTITY_EXPERIENCE_ORB_PICKUP");
            this.soundGuiOpen = sounds.getString("gui-open", "BLOCK_CHEST_OPEN");
            this.soundGuiClick = sounds.getString("gui-click", "UI_BUTTON_CLICK");
            this.soundError = sounds.getString("error", "ENTITY_VILLAGER_NO");
        }
    }

    // Getters
    public int getDefaultHearts() { return defaultHearts; }
    public int getMinimumHearts() { return minimumHearts; }
    public int getMaximumHearts() { return maximumHearts; }
    public int getCombatTime() { return combatTime; }
    public String getBanDuration() { return banDuration; }
    public String getBanMessage() { return banMessage; }
    public boolean isBroadcastCombatLog() { return broadcastCombatLog; }
    public boolean isRecipesEnabled() { return recipesEnabled; }
    public String getHeartItemName() { return heartItemName; }
    public List<String> getHeartItemLore() { return heartItemLore; }
    public String getReviveItemName() { return reviveItemName; }
    public List<String> getReviveItemLore() { return reviveItemLore; }
    public String getPrefix() { return prefix; }
    public String getHeartGained() { return heartGained; }
    public String getHeartLost() { return heartLost; }
    public String getMaxHearts() { return maxHearts; }
    public String getMinHearts() { return minHearts; }
    public String getWithdrawSuccess() { return withdrawSuccess; }
    public String getWithdrawFail() { return withdrawFail; }
    public String getInvalidAmount() { return invalidAmount; }
    public String getNoPermission() { return noPermission; }
    public String getPlayerNotFound() { return playerNotFound; }
    public String getPlayerEliminated() { return playerEliminated; }
    public String getPlayerRevived() { return playerRevived; }
    public String getHeartReset() { return heartReset; }
    public String getHeartCheck() { return heartCheck; }
    public String getConfigReloaded() { return configReloaded; }
    public String getCombatLog() { return combatLog; }
    public String getCombatTagged() { return combatTagged; }
    public String getCombatUntagged() { return combatUntagged; }
    public String getItemConsumed() { return itemConsumed; }
    public String getItemMaxHearts() { return itemMaxHearts; }
    public String getReviveSuccess() { return reviveSuccess; }
    public String getNoEliminatedPlayers() { return noEliminatedPlayers; }
    public String getEliminatedTitle() { return eliminatedTitle; }
    public String getEliminatedSubtitle() { return eliminatedSubtitle; }
    public String getSoundHeartGain() { return soundHeartGain; }
    public String getSoundHeartLoss() { return soundHeartLoss; }
    public String getSoundEliminate() { return soundEliminate; }
    public String getSoundRevive() { return soundRevive; }
    public String getSoundCombatStart() { return soundCombatStart; }
    public String getSoundCombatEnd() { return soundCombatEnd; }
    public String getSoundWithdraw() { return soundWithdraw; }
    public String getSoundGuiOpen() { return soundGuiOpen; }
    public String getSoundGuiClick() { return soundGuiClick; }
    public String getSoundError() { return soundError; }
}