package de.felixfgf.lifestealx;

import de.felixfgf.lifestealx.commands.LifeStealCommand;
import de.felixfgf.lifestealx.commands.WithdrawCommand;
import de.felixfgf.lifestealx.config.ConfigManager;
import de.felixfgf.lifestealx.gui.ReviveGUI;
import de.felixfgf.lifestealx.hearts.HeartManager;
import de.felixfgf.lifestealx.items.ItemManager;
import de.felixfgf.lifestealx.listeners.PlayerListener;
import de.felixfgf.lifestealx.recipes.RecipeManager;
import de.felixfgf.lifestealx.revive.ReviveManager;
import de.felixfgf.lifestealx.storage.StorageManager;
import de.felixfgf.lifestealx.combat.CombatManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LifeStealX extends JavaPlugin {

    private ConfigManager configManager;
    private StorageManager storageManager;
    private ItemManager itemManager;
    private HeartManager heartManager;
    private CombatManager combatManager;
    private ReviveManager reviveManager;
    private ReviveGUI reviveGUI;
    private RecipeManager recipeManager;
    private BukkitRunnable autoSaveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.storageManager = new StorageManager(this);
        this.itemManager = new ItemManager(this);
        this.heartManager = new HeartManager(this);
        this.combatManager = new CombatManager(this);
        this.reviveManager = new ReviveManager(this);
        this.reviveGUI = new ReviveGUI(this);
        this.recipeManager = new RecipeManager(this);

        registerCommands();
        registerListeners();

        if (configManager.isRecipesEnabled()) {
            recipeManager.registerRecipes();
        }

        // Auto-save every 5 minutes (6000 ticks) to prevent data loss on crash
        this.autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                storageManager.saveData();
            }
        };
        this.autoSaveTask.runTaskTimer(this, 6000L, 6000L);

        getLogger().info("LifeStealX has been enabled!");
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (combatManager != null) {
            combatManager.shutdown();
        }
        if (storageManager != null) {
            storageManager.saveData();
        }
        if (recipeManager != null) {
            recipeManager.unregisterRecipes();
        }
        getLogger().info("LifeStealX has been disabled!");
    }

    private void registerCommands() {
        LifeStealCommand lifeStealCommand = new LifeStealCommand(this);
        getCommand("withdraw").setExecutor(new WithdrawCommand(this));
        getCommand("lifesteal").setExecutor(lifeStealCommand);
        getCommand("lifesteal").setTabCompleter(lifeStealCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public HeartManager getHeartManager() {
        return heartManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public ReviveManager getReviveManager() {
        return reviveManager;
    }

    public ReviveGUI getReviveGUI() {
        return reviveGUI;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}