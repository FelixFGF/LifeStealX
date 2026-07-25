package de.felixfgf.lifestealx.gui;

import de.felixfgf.lifestealx.LifeStealX;
import de.felixfgf.lifestealx.items.ItemManager;
import de.felixfgf.lifestealx.revive.ReviveManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ReviveGUI implements Listener {

    private final LifeStealX plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, UUID> openInventories;

    private static final int GUI_SIZE = 54;
    private static final String GUI_TITLE = "<gradient:gold:yellow>Revive Players</gradient>";

    public ReviveGUI(LifeStealX plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.openInventories = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        List<ReviveManager.EliminatedPlayerInfo> eliminatedPlayers =
                plugin.getReviveManager().getEliminatedPlayers();

        Component title = miniMessage.deserialize(GUI_TITLE);
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, title);

        ItemStack filler = createFillerItem();
        for (int i = 0; i < GUI_SIZE; i++) {
            gui.setItem(i, filler);
        }

        int slot = 0;
        for (ReviveManager.EliminatedPlayerInfo info : eliminatedPlayers) {
            if (slot >= 45) break;

            ItemStack head = createPlayerHead(info);
            gui.setItem(slot, head);
            slot++;
        }

        ItemStack closeButton = createCloseButton();
        gui.setItem(49, closeButton);

        openInventories.put(player.getUniqueId(), UUID.randomUUID());

        player.openInventory(gui);

        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundGuiOpen());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundGuiOpen());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openInventories.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked.getType() == Material.PLAYER_HEAD && clicked.hasItemMeta()) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta instanceof SkullMeta skullMeta) {
                if (skullMeta.getOwningPlayer() != null) {
                    UUID targetUuid = skullMeta.getOwningPlayer().getUniqueId();
                    handleReviveClick(player, targetUuid);
                }
            }
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (openInventories.containsKey(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            openInventories.remove(player.getUniqueId());
        }
    }

    private void handleReviveClick(Player player, UUID targetUuid) {
        if (!plugin.getReviveManager().isEliminated(targetUuid)) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getConfigManager().getPrefix() +
                    "<red>This player is no longer eliminated!</red>"));
            player.closeInventory();
            return;
        }

        ItemStack beacon = findReviveBeacon(player);
        if (beacon == null) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getConfigManager().getPrefix() +
                    "<red>You need a Revive Beacon to revive a player!</red>"));
            playErrorSound(player);
            return;
        }

        // B4 FIX: Properly consume the beacon - if amount > 1 decrement, otherwise remove entirely
        // This prevents the item from having amount 0 (bugged state)
        if (beacon.getAmount() > 1) {
            beacon.setAmount(beacon.getAmount() - 1);
        } else {
            beacon.setAmount(0);
            // Remove the item from the slot it's in
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if (slot != null && slot.equals(beacon)) {
                    player.getInventory().setItem(i, null);
                    break;
                }
            }
        }
        player.updateInventory();

        plugin.getReviveManager().revivePlayer(targetUuid);

        String targetName = Bukkit.getOfflinePlayer(targetUuid).getName();
        if (targetName == null) targetName = targetUuid.toString();

        String message = plugin.getConfigManager().getReviveSuccess()
                .replace("%player%", targetName);
        player.sendMessage(miniMessage.deserialize(
                plugin.getConfigManager().getPrefix() + message));

        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundRevive());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundRevive());
        }

        player.closeInventory();
        openGUI(player);
    }

    private ItemStack findReviveBeacon(Player player) {
        ItemManager itemManager = plugin.getItemManager();

        // Check main inventory (getContents does NOT include offhand)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && itemManager.isReviveItem(item)) {
                return item;
            }
        }

        // B4 FIX: Also check offhand slot
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && itemManager.isReviveItem(offhand)) {
            return offhand;
        }

        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && itemManager.isReviveItem(cursor)) {
            return cursor;
        }

        return null;
    }

    private ItemStack createPlayerHead(ReviveManager.EliminatedPlayerInfo info) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(info.getUuid());
        meta.setOwningPlayer(offlinePlayer);
        meta.displayName(miniMessage.deserialize(
                "<gold>" + info.getPlayerName() + "</gold>"));
        meta.lore(List.of(
                miniMessage.deserialize("<gray>Click to revive this player</gray>")
        ));

        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createFillerItem() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            filler.setItemMeta(meta);
        }
        return filler;
    }

    private ItemStack createCloseButton() {
        ItemStack close = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = close.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<red>Close</red>"));
            close.setItemMeta(meta);
        }
        return close;
    }

    private void playErrorSound(Player player) {
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(
                    plugin.getConfigManager().getSoundError());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " +
                    plugin.getConfigManager().getSoundError());
        }
    }
}