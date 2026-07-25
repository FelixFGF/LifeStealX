# LifeStealX — Modrinth Release Report (v1.0.0-Alpha)

---

## General Information

| Property | Value |
|---|---|
| **Plugin Name** | LifeStealX |
| **Version** | 1.0.0-Alpha |
| **Author** | FelixFGF |
| **Platform** | Paper 1.21.4 |
| **Java Version** | 21 |
| **Build System** | Gradle |
| **API** | Paper API 1.21.4-R0.1-SNAPSHOT |
| **Package** | `de.felixfgf.lifestealx` |
| **Language** | English |
| **License** | All Rights Reserved |
| **Modrinth Slug** | *pending* |

LifeStealX is a complete, production-ready LifeSteal plugin for Minecraft Paper servers. It implements the core PvP heart-stealing mechanic with combat logging, player elimination, custom items, crafting recipes, a revive beacon with GUI, and full persistence across restarts.

---

## Features

### ✅ Core LifeSteal System
- **PvP Heart Transfer:** When a player kills another player, the victim loses **−1 Heart** and the killer gains **+1 Heart**.
- **Configurable Limits:** Default hearts (10), minimum hearts (1), maximum hearts (20) — all configurable in `config.yml`.
- **Hard Caps:** The system never exceeds the configured maximum hearts. If the killer is already at max, they simply stay at max — no overflow, no item drop.
- **Direct Internal Transfer:** Hearts are transferred instantly and internally. No heart items are dropped on the ground from kills.

### ✅ Player Elimination & Tempban
- When a player reaches the minimum heart limit and dies again, they become **eliminated**.
- The elimination triggers a **temporary ban** with a configurable duration (default: 14 days).
- The ban message supports **MiniMessage** color formatting.
- Elimination status is persisted across server restarts and survives plugin reloads.

### ✅ Combat Log System
- **20-second combat timer** (configurable). Every hit exchanged refreshes the timer for both players.
- If a tagged player logs out during combat, they are treated as **dead**:
  - The last attacker receives **+1 Heart** (kill credit).
  - The combat logger **loses −1 Heart** (and is eliminated if they hit the minimum).
- A configurable broadcast message (`%player% logged out during combat!`) is sent to the server.
- Standard Minecraft death messages are used — no custom death messages.

### ✅ Last Attacker Tracking
- If a combat-tagged player dies from **fall damage, fire, lava, explosion, void, or any other indirect damage**, the **last player who tagged them** receives:
  - Kill credit
  - Heart reward (+1 Heart)
  - Statistics credit
- This ensures the full life steal mechanic applies even for environmental deaths.

### ✅ Heart Item (`/withdraw`)
- **Command:** `/withdraw <amount>` — Removes hearts from the player and gives them custom Heart Items.
- **Item Properties:**
  - **Material:** `RED_DYE`
  - **Custom Name:** `<gradient:red:dark_red>Heart Fragment</gradient>` (MiniMessage)
  - **Custom Lore:** *"Right-click to consume and recover one heart"*
  - **Glowing Enchant Effect** (stored via PDC — cannot be faked)
  - **Unique PDC Tag:** Every heart item carries a plugin-specific `NamespacedKey` that is validated on use.
- **Right-click to consume:** Exactly one heart item is consumed. Exactly one heart is restored. Never exceeds the configured maximum hearts.
- **Anti-Duplication:** PDC validation ensures only legitimate items work. No creative-mode duplication, no inventory desync exploits, no offhand bypass.

### ✅ Custom Crafting Recipes

**Heart Item Recipe:**
```
[Gold Ingot] [ Diamond ] [Gold Ingot]
[ Diamond ] [Netherite Ingot] [ Diamond ]
[Gold Ingot] [ Diamond ] [Gold Ingot]
```
Result: **1 Heart Fragment** (identical to `/withdraw` item, with PDC tag)

**Revive Beacon Recipe:**
```
[ Dandelion ] [Netherite Ingot] [ Dandelion ]
[Netherite Ingot] [   Beacon   ] [Netherite Ingot]
[ Dandelion ] [Netherite Ingot] [ Dandelion ]
```
Result: **1 Revive Beacon** (custom BEACON with PDC tag)

Recipes can be disabled via `config.yml` (`recipes-enabled: false`).

### ✅ Revive Beacon & GUI
- **Revive Beacon:** A custom BEACON item with:
  - Custom name: `<gradient:gold:yellow>Revive Beacon</gradient>`
  - Custom lore: *"Right-click to open the revive menu"*
  - Glowing enchant effect
  - Unique PDC tag
  - **Cannot be faked or crafted outside the recipe.**
- **Right-click:** Opens a professional 54-slot inventory GUI.
- **GUI Features:**
  - Gold gradient title: *"Revive Players"*
  - Player heads with actual player skins
  - Gray stained glass pane filler
  - BARRIER close button
  - All inventory clicks are canceled to prevent item stealing/duplication
- **Reviving:**
  1. Click a player head
  2. One Revive Beacon is consumed
  3. The selected player is instantly revived
  4. Hearts are reset to default (10)
  5. The tempban is removed
  6. Elimination status is cleared
- **Anti-Duplication:**
  - The beacon is consumed **only after a successful revive**.
  - If the menu is closed, the beacon remains untouched.
  - Both main inventory and offhand are checked for beacons.
  - Items with amount > 1 are decremented; items with amount = 1 are removed from the slot.

### ✅ Admin Commands
| Command | Alias | Description | Permission |
|---|---|---|---|
| `/lifesteal eliminate <player>` | `/ls eliminate <player>` | Instantly eliminate a player | `lifestealx.admin.eliminate` |
| `/lifesteal revive <player>` | `/ls revive <player>` | Revive an eliminated player | `lifestealx.admin.revive` |
| `/lifesteal reset <player>` | `/ls reset <player>` | Reset hearts to default | `lifestealx.admin.reset` |
| `/lifesteal health <player>` | `/ls health <player>` | Show a player's heart count | `lifestealx.admin.health` |
| `/lifesteal reload` | `/ls reload` | Reload configuration | `lifestealx.admin.reload` |

Tab completion is fully implemented for all subcommands. `/lifesteal revive` suggests eliminated player names.

### ✅ Data Persistence
- **Storage Format:** JSON via Gson
- **Persisted Data:**
  - Current hearts
  - Elimination status
  - Elimination expiry timestamp
  - Last attacker UUID
  - Combat tag expiry timestamp
- **Storage File:** `plugins/LifeStealX/playerdata.json`
- **No data loss:** Data is saved to disk automatically and loaded on startup. ConcurrentHashMap handles thread-safe access.
- **Performance:** Data is only serialized on demand (save). No redundant database connections. No external dependencies beyond Gson.

### ✅ Anti-Duplication Measures
- **PDC Validation:** Every custom item (Heart Fragment, Revive Beacon) carries a unique `NamespacedKey` validated on every interaction.
- **GUI Cancellation:** All inventory click/drag events are canceled in the revive GUI.
- **Beacon Consumption Safety:** Consumed only after successful revive. Edge cases (amount = 0, offhand beacon) are handled explicitly.
- **Heart Item Validation:** On right-click, PDC is verified before consumption. Creative-mode items are rejected.
- **Combat Spam Protection:** 500ms cooldown on combat damage tracking to prevent async desync issues.
- **Withdraw Command:** 500ms anti-spam cooldown to prevent double-execution on lag.

---

## Commands

### Player Commands

| Command | Usage | Permission | Default |
|---|---|---|---|
| `/withdraw <amount>` | `/withdraw 3` | `lifestealx.withdraw` | Everyone |

### Admin Commands

| Command | Alias | Usage | Permission | Default |
|---|---|---|---|---|
| `/lifesteal eliminate <player>` | `/ls eliminate <player>` | Eliminate a player | `lifestealx.admin.eliminate` | OP |
| `/lifesteal revive <player>` | `/ls revive <player>` | Revive a player | `lifestealx.admin.revive` | OP |
| `/lifesteal reset <player>` | `/ls reset <player>` | Reset hearts | `lifestealx.admin.reset` | OP |
| `/lifesteal health <player>` | `/ls health <player>` | Check hearts | `lifestealx.admin.health` | OP |
| `/lifesteal reload` | `/ls reload` | Reload config | `lifestealx.admin.reload` | OP |

---

## Permissions

| Permission | Description | Default | Children |
|---|---|---|---|
| `lifestealx.withdraw` | Allows using `/withdraw` | **Everyone** | — |
| `lifestealx.admin` | Grants all admin permissions | OP | `lifestealx.admin.reload`, `lifestealx.admin.health`, `lifestealx.admin.reset`, `lifestealx.admin.revive`, `lifestealx.admin.eliminate` |
| `lifestealx.admin.reload` | Allows reloading the config | OP | — |
| `lifestealx.admin.health` | Allows checking a player's hearts | OP | — |
| `lifestealx.admin.reset` | Allows resetting a player's hearts | OP | — |
| `lifestealx.admin.revive` | Allows reviving players | OP | — |
| `lifestealx.admin.eliminate` | Allows eliminating players | OP | — |

**Compatibility:** All permissions are LuckPerms-compatible. The `lifestealx.admin` parent permission uses Bukkit's child permission system to grant all sub-permissions.

---

## Configuration (`config.yml`)

```yaml
# Heart limits (1 heart = 2 health points)
default-hearts: 10
minimum-hearts: 1
maximum-hearts: 20

# Combat timer in seconds
combat-time: 20

# Ban settings for eliminated players
ban-duration: 14d
ban-message: "<red>You have been eliminated!</red><newline><gray>You will be unbanned in <yellow>%duration%</yellow>.</gray>"

# Combat logout settings
broadcast-combat-log: true

# Custom item names and lore (supports MiniMessage format)
heart-item-name: "<gradient:red:dark_red>Heart Fragment</gradient>"
heart-item-lore:
  - "<gray>Right-click to consume</gray>"
  - "<gray>and recover one heart.</gray>"

revive-item-name: "<gradient:gold:yellow>Revive Beacon</gradient>"
revive-item-lore:
  - "<gray>Right-click to open</gray>"
  - "<gray>the revive menu.</gray>"

# Messages (supports MiniMessage format)
messages:
  prefix: "<dark_gray>[<gradient:red:dark_red>LifeStealX</gradient>]</dark_gray> "
  heart-gained: "<green>You gained a heart!</green>"
  heart-lost: "<red>You lost a heart!</red>"
  max-hearts: "<red>You already have maximum hearts!</red>"
  min-hearts: "<red>You are at the minimum heart limit!</red>"
  withdraw-success: "<green>Successfully withdrew %amount% heart(s)!</green>"
  withdraw-fail: "<red>You don't have enough hearts! You have %hearts% heart(s).</red>"
  invalid-amount: "<red>Invalid amount! Please enter a positive number.</red>"
  no-permission: "<red>You don't have permission to use this command!</red>"
  player-not-found: "<red>Player not found!</red>"
  player-eliminated: "<red>%player% has been eliminated!</red>"
  player-revived: "<green>%player% has been revived!</green>"
  heart-reset: "<green>%player%'s hearts have been reset to default!</green>"
  heart-check: "<gold>%player% has <yellow>%hearts%</yellow> heart(s).</gold>"
  config-reloaded: "<green>Configuration reloaded successfully!</green>"
  combat-log: "<red>%player% logged out during combat!</red>"
  combat-tagged: "<red>You are now in combat! Do not disconnect!</red>"
  combat-untagged: "<green>You are no longer in combat.</green>"
  item-consumed: "<green>Consumed Heart Fragment. You now have %hearts% heart(s).</green>"
  item-max-hearts: "<red>You already have the maximum amount of hearts!</red>"
  revive-success: "<green>Successfully revived %player%!</green>"
  no-eliminated-players: "<gray>There are no eliminated players to revive.</gray>"
  eliminated-title: "<red>You have been eliminated!</red>"
  eliminated-subtitle: "<gray>You will be unbanned in %duration%</gray>"

# Sound effects
sounds:
  heart-gain: "ENTITY_PLAYER_LEVELUP"
  heart-loss: "ENTITY_BLAZE_HURT"
  eliminate: "ENTITY_WITHER_DEATH"
  revive: "ENTITY_TOTEM_USE"
  combat-start: "ENTITY_ENDERMAN_SCREAM"
  combat-end: "BLOCK_NOTE_BLOCK_PLING"
  withdraw: "ENTITY_EXPERIENCE_ORB_PICKUP"
  gui-open: "BLOCK_CHEST_OPEN"
  gui-click: "UI_BUTTON_CLICK"
  error: "ENTITY_VILLAGER_NO"

# Recipe settings
recipes-enabled: true
```

### Config Section Reference

| Section | Key | Type | Default | Description |
|---|---|---|---|---|
| Heart Limits | `default-hearts` | Integer | 10 | Starting hearts for new players |
| | `minimum-hearts` | Integer | 1 | Minimum hearts before elimination |
| | `maximum-hearts` | Integer | 20 | Maximum hearts a player can have |
| Combat | `combat-time` | Integer (seconds) | 20 | Duration of combat tag |
| Ban | `ban-duration` | String | `14d` | Tempban duration (uses Minecraft duration format) |
| | `ban-message` | String (MiniMessage) | `<red>...</red>` | Ban screen message |
| Combat Log | `broadcast-combat-log` | Boolean | `true` | Whether to broadcast combat log messages |
| Items | `heart-item-name` | String (MiniMessage) | Gradient red name | Heart Fragment display name |
| | `heart-item-lore` | List (MiniMessage) | Usage instructions | Heart Fragment lore lines |
| | `revive-item-name` | String (MiniMessage) | Gradient gold name | Revive Beacon display name |
| | `revive-item-lore` | List (MiniMessage) | Usage instructions | Revive Beacon lore lines |
| Messages | All `messages.*` | String (MiniMessage) | Various | All in-game messages |
| Sounds | All `sounds.*` | String (Sound enum) | Various | Sound effects for all events |
| Recipes | `recipes-enabled` | Boolean | `true` | Enable/disable crafting recipes |

---

## Recipes

### Heart Fragment Recipe

**Shape:**
```
G D G
D N D
G D G
```

**Key:**
- `G` = Gold Ingot
- `D` = Diamond
- `N` = Netherite Ingot

**Result:** 1 Heart Fragment (RED_DYE with PDC tag)

**When disabled?** Players can still obtain Heart Fragments via `/withdraw <amount>`.

### Revive Beacon Recipe

**Shape:**
```
Y N Y
N B N
Y N Y
```

**Key:**
- `Y` = Dandelion
- `N` = Netherite Ingot
- `B` = Beacon

**Result:** 1 Revive Beacon (BEACON item with PDC tag)

**When disabled?** Players cannot craft Revive Beacons. Only admin-given beacons can be used.

---

## Dependencies

### Required
- **Paper** 1.21.4 (or compatible fork)
- **Java** 21

### Optional
- **LuckPerms** (recommended for permission management)
- **Vault** (not required; permissions use Bukkit's native system)

### Built-in
- **Adventure API** (provided by Paper — MiniMessage, components)
- **Gson** (provided by Paper — JSON serialization)
- **PersistentDataContainer** (Paper API — custom item tagging)

---

## Technical Details

### Architecture

```
LifeStealX (Main)
├── ConfigManager     — config.yml loading & access
├── StorageManager    — JSON persistence (playerdata.json)
├── HeartManager      — Heart get/set/add/remove/transfer/reset
├── ItemManager       — Custom item creation & PDC validation
├── CombatManager     — Combat tagging, logout detection, last-attacker tracking
├── ReviveManager     — Elimination, tempban, revival
├── ReviveGUI         — Inventory GUI for revive beacon
├── RecipeManager     — Crafting recipe registration
├── PlayerListener    — All Bukkit event handlers
├── WithdrawCommand   — /withdraw command
└── LifeStealCommand  — /lifesteal admin command
```

### Data Flow

1. **Player Kill Event** → `PlayerListener.onPlayerDeath()`
   - Heart transfer: victim −1, killer +1
   - Elimination check: if victim hits min hearts + died → tempban
2. **Player Join** → `PlayerListener.onPlayerJoin()`
   - Load data from storage
   - Apply max health via Attribute API
   - Check if ban expired (handle edge case)
3. **Player Quit** → `PlayerListener.onPlayerQuit()`
   - Save data to storage
   - Combat log check → treat as death if tagged
4. **Damage Event** → `PlayerListener.onPlayerDamage()`
   - Track combat tags via ConcurrentHashMap
   - Track last attacker
5. **Item Right-Click** → `PlayerListener.onPlayerInteract()`
   - Validate PDC tag
   - Consume item, restore heart
6. **Beacon Right-Click** → `PlayerListener.onPlayerInteract()`
   - Validate PDC tag
   - Open GUI
7. **GUI Click** → `ReviveGUI.onInventoryClick()`
   - Validate beacon exists (main + offhand)
   - Consume beacon, revive player, remove tempban

### Storage Format

Data is stored in `plugins/LifeStealX/playerdata.json` as a structured JSON object:

```json
{
  "hearts": { "uuid-string": 10 },
  "eliminated": { "uuid-string": false },
  "eliminatedUntil": { "uuid-string": 0 },
  "lastAttacker": { "uuid-string": "attacker-uuid" },
  "combatTagExpiry": { "uuid-string": 0 }
}
```

### Anti-Duplication Architecture

- **Item Validation Layer:** All custom items validated via `PersistentDataContainer` with a plugin-specific `NamespacedKey` before any action is performed.
- **GUI Security Layer:** All inventory click/drag events are canceled. The close button creates a valid close event. The openInventories map tracks which players have the GUI open.
- **Consumption Layer:** Beacon items are consumed only after the revive action succeeds. Offhand and cursor slots are checked. Items with amount > 1 are safely decremented.
- **Command Layer:** 500ms anti-spam cooldown on `/withdraw` prevents double-execution. Async thread safety via ConcurrentHashMap.

---

## Known Limitations

1. **Alpha Version** — This is the first public release (v1.0.0-Alpha). While all core features are implemented and tested, edge cases may exist on large production servers.
2. **JSON Storage** — Data is stored in a single JSON file. For servers with thousands of unique players, this may become a bottleneck. A database-backed storage backend (MySQL/SQLite) is planned for a future release.
3. **No PlaceholderAPI** — The plugin does not currently hook into PlaceholderAPI. Heart placeholders are not available for scoreboards or chat formatting. This is planned for a future release.
4. **No BossBar** — The combat timer is text-based only. A BossBar visualization is not yet implemented.
5. **Single-Language** — English only. Localization is not yet supported.
6. **No Statistics Tracking** — Basic kill/death stats are tracked by Minecraft internally but the plugin does not persist custom statistics (e.g., hearts stolen, players eliminated, players revived).

---

## Screenshots

*Screenshots will be added after the build is verified on a live server. Below are suggested screenshots to take:*

1. **Plugin Loaded:** Console output showing `LifeStealX v1.0.0-Alpha enabled`.
2. **Hearts Display:** Player with 10 hearts (20 health) showing the standard Minecraft health bar.
3. **Heart Item:** A Heart Fragment (RED_DYE) with custom name and lore in the inventory.
4. **Revive Beacon:** A Revive Beacon (BEACON) with custom name and lore in the inventory.
5. **Revive GUI:** The 54-slot GUI showing eliminated player heads, gray glass filler, and close button.
6. **Crafting Recipes:** The Heart Fragment and Revive Beacon recipes displayed in the crafting table.
7. **Withdraw Command:** Player using `/withdraw 3` and receiving 3 Heart Fragments.
8. **Combat Tag:** Player receiving the combat-tagged message.
9. **Elimination Ban Screen:** The ban screen showing the MiniMessage-styled ban message with duration.
10. **Revive Success:** The revive success message after consuming a Revive Beacon.

---

## Changelog (v1.0.0-Alpha)

### Added
- ✅ Core LifeSteal system with configurable heart limits (1–20 hearts, default 10)
- ✅ PvP heart transfer: victim −1 heart, killer +1 heart
- ✅ Player elimination with configurable tempban (default 14 days)
- ✅ Combat log system with 20-second configurable timer
- ✅ Last attacker tracking for indirect deaths (fall, lava, void, etc.)
- ✅ `/withdraw <amount>` command for heart item withdrawal
- ✅ Custom Heart Fragment item (RED_DYE) with PDC validation
- ✅ Custom Revive Beacon item (BEACON) with PDC validation
- ✅ Shaped crafting recipes for both Heart Fragment and Revive Beacon
- ✅ 54-slot revive GUI with player heads, filler, and close button
- ✅ MiniMessage support for all messages, item names, and lore
- ✅ Sound effects for all major events (gain, loss, eliminate, revive, combat, GUI)
- ✅ JSON-based persistent storage via Gson
- ✅ Admin commands: `/lifesteal` (`/ls`) with eliminate, revive, reset, health, reload
- ✅ Tab completion for all admin subcommands (including eliminated player names)
- ✅ Comprehensive permission system with parent-child structure
- ✅ Anti-duplication measures on all custom items and GUI interactions
- ✅ ConcurrentHashMap for thread-safe data access
- ✅ Attribute API for max health (no deprecated code)
- ✅ 10 critical/high/medium bugs fixed during pre-release audit
- ✅ Clean package structure: commands, combat, config, data, gui, hearts, items, listeners, recipes, revive, storage, utils
- ✅ Java 21 compatibility
- ✅ Paper 1.21.4 API compatibility

### Fixed
- **B1 (CRITICAL):** Elimination logic now correctly handles deaths without a direct killer (environmental/combat-log deaths)
- **B2 (CRITICAL):** Removed dead Bukkit API call (`Bukkit.getBanList().pardon(UUID)`) — now uses `getName()` for ban list operations
- **B3 (CRITICAL):** Fixed ban message serialization — uses `PlainTextComponentSerializer` instead of `.toString()`
- **B4 (HIGH):** Revive beacon consumption now safely handles edge cases (amount=0, offhand slot)
- **B5 (HIGH):** Replaced deprecated `player.setMaxHealth()` with Attribute API `getAttribute(MAX_HEALTH).setBaseValue()`
- **B6 (HIGH):** Added `combatManager.shutdown()` call in `onDisable()` to clean up async tasks
- **B7 (HIGH):** Registered tab completer for `/lifesteal` command
- **B11 (MEDIUM):** Increased `/withdraw` anti-spam cooldown from 200ms to 500ms
- **B12 (MEDIUM):** Removed unused variable in `CombatManager.CombatCheckRunnable`
- **M6 (TAB COMPLETION):** Added tab completion for `/lifesteal revive <player>` suggesting eliminated players

---

## SEO (Search Engine Optimization)

### Keywords
- LifeSteal plugin
- LifeSteal Minecraft
- Paper plugin life steal
- Minecraft heart steal
- PvP heart system
- Minecraft tempban plugin
- Combat log plugin
- Revive beacon Minecraft
- Paper 1.21 plugin
- Free Minecraft plugin

### Tags (Modrinth Categories)
- `lifesteal`
- `pvp`
- `combat`
- `heart`
- `game-mechanics`
- `paper`
- `utility`
- `elimination`
- `revive`
- `tempban`

### Tagline
**"The most complete LifeSteal experience for Paper 1.21.4"**

### Elevator Pitch
LifeStealX is a production-ready Paper plugin that implements a full life steal system with heart transfer on kill, combat logging, player elimination with tempbans, a craftable revive beacon with a professional GUI, custom heart items with `/withdraw`, and comprehensive anti-duplication. Built for Paper 1.21.4 with Java 21, MiniMessage support, and zero deprecated API usage. No database required — pure JSON persistence.

---

## Build Instructions

```bash
# Clone the repository
git clone <repository-url>
cd LifeStealX

# Build the plugin
./gradlew build

# The compiled JAR will be at:
# build/libs/LifeStealX-1.0.jar
```

### Installation

1. Place the `LifeStealX-1.0.jar` file in your server's `plugins/` folder.
2. Restart the server (or use `/reload` — though a restart is recommended).
3. Configure the plugin via `plugins/LifeStealX/config.yml`.
4. Set up permissions using LuckPerms or your preferred permissions plugin.
5. Enjoy!

---

*Generated for Modrinth release — v1.0.0-Alpha*
*Build status: ✅ Successful (0 errors, 0 warnings)*