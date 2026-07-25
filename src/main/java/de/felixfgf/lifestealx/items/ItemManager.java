package de.felixfgf.lifestealx.items;

import de.felixfgf.lifestealx.LifeStealX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    private final LifeStealX plugin;
    private final NamespacedKey heartItemKey;
    private final NamespacedKey reviveItemKey;
    private final MiniMessage miniMessage;

    public static final String HEART_ITEM_KEY = "lifestealx_heart";
    public static final String REVIVE_ITEM_KEY = "lifestealx_revive";

    public ItemManager(LifeStealX plugin) {
        this.plugin = plugin;
        this.heartItemKey = new NamespacedKey(plugin, HEART_ITEM_KEY);
        this.reviveItemKey = new NamespacedKey(plugin, REVIVE_ITEM_KEY);
        this.miniMessage = MiniMessage.miniMessage();
    }

    public ItemStack createHeartItem() {
        ItemStack item = new ItemStack(Material.RED_DYE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component displayName = miniMessage.deserialize(plugin.getConfigManager().getHeartItemName());
        meta.displayName(displayName);

        List<Component> loreComponents = new ArrayList<>();
        for (String loreLine : plugin.getConfigManager().getHeartItemLore()) {
            loreComponents.add(miniMessage.deserialize(loreLine));
        }
        meta.lore(loreComponents);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(heartItemKey, PersistentDataType.STRING, HEART_ITEM_KEY);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createReviveItem() {
        ItemStack item = new ItemStack(Material.BEACON, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component displayName = miniMessage.deserialize(plugin.getConfigManager().getReviveItemName());
        meta.displayName(displayName);

        List<Component> loreComponents = new ArrayList<>();
        for (String loreLine : plugin.getConfigManager().getReviveItemLore()) {
            loreComponents.add(miniMessage.deserialize(loreLine));
        }
        meta.lore(loreComponents);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(reviveItemKey, PersistentDataType.STRING, REVIVE_ITEM_KEY);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isHeartItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String value = pdc.get(heartItemKey, PersistentDataType.STRING);
        return HEART_ITEM_KEY.equals(value);
    }

    public boolean isReviveItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String value = pdc.get(reviveItemKey, PersistentDataType.STRING);
        return REVIVE_ITEM_KEY.equals(value);
    }

    public NamespacedKey getHeartItemKey() {
        return heartItemKey;
    }

    public NamespacedKey getReviveItemKey() {
        return reviveItemKey;
    }
}