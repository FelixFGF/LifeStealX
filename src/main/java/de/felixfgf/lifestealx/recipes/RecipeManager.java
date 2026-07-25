package de.felixfgf.lifestealx.recipes;

import de.felixfgf.lifestealx.LifeStealX;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeManager {

    private final LifeStealX plugin;
    private final NamespacedKey heartRecipeKey;
    private final NamespacedKey reviveRecipeKey;

    public RecipeManager(LifeStealX plugin) {
        this.plugin = plugin;
        this.heartRecipeKey = new NamespacedKey(plugin, "heart_recipe");
        this.reviveRecipeKey = new NamespacedKey(plugin, "revive_beacon_recipe");
    }

    public void registerRecipes() {
        if (!plugin.getConfigManager().isRecipesEnabled()) return;
        registerHeartRecipe();
        registerReviveRecipe();
    }

    public void unregisterRecipes() {
        Bukkit.removeRecipe(heartRecipeKey);
        Bukkit.removeRecipe(reviveRecipeKey);
    }

    private void registerHeartRecipe() {
        ItemStack heartItem = plugin.getItemManager().createHeartItem();
        ShapedRecipe recipe = new ShapedRecipe(heartRecipeKey, heartItem);
        recipe.shape(
                "GDG",
                "DND",
                "GDG"
        );
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Registered heart recipe");
    }

    private void registerReviveRecipe() {
        ItemStack reviveItem = plugin.getItemManager().createReviveItem();
        ShapedRecipe recipe = new ShapedRecipe(reviveRecipeKey, reviveItem);
        recipe.shape(
                "DND",
                "NBN",
                "DND"
        );
        recipe.setIngredient('D', Material.DANDELION);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('B', Material.BEACON);
        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Registered revive beacon recipe");
    }
}
