# ❤️ LifeStealX
![Banner](https://cdn.modrinth.com/data/cached_images/9f3ca74ec2725ec89fbef37516f1202e43a6137d_0.webp)
*A modern, lightweight and highly optimized LifeSteal plugin for Paper servers.*

LifeStealX is a clean and performance-focused implementation of the classic LifeSteal gameplay, designed for survival communities, SMPs and competitive PvP servers.

Steal hearts from your enemies, eliminate players, revive fallen teammates, trade hearts as custom items and enjoy a polished experience built specifically for modern Paper servers.

This project is actively maintained and continuously improved based on community feedback.

---

## ✨ Features

### ❤️ Heart Stealing

Every kill matters.

Whenever a player kills another player:

* ❤️ The killer gains one heart.
* 💔 The victim permanently loses one heart.
* ⚙️ Configurable minimum, default and maximum hearts.
* ☠️ Automatic elimination when no hearts remain.

---

### 💎 Heart Items

Turn your own hearts into tradable items.

```
/withdraw <amount>
```

Features:

* ❤️ Convert hearts into custom Heart Items.
* 🔒 Secure PersistentDataContainer validation.
* ✨ Custom item name and lore.
* 🖱 Right-click to consume.
* 🤝 Trade hearts with other players.

---

### 🛠 Custom Crafting Recipes

LifeStealX includes custom crafting recipes for its unique gameplay items.

Craft your own **Heart Fragments** to trade hearts with other players or create a **Revive Beacon** to bring eliminated players back into the game.

Both recipes are configurable and can be customized to fit your server's balancing.

#### ❤️ Heart Fragment Recipe

![Heart](https://cdn.modrinth.com/data/cached_images/1078cf3b72932ccda538c51ee2083f9e484412e1.png)

#### 🔥 Revive Beacon Recipe

![Beacon](https://cdn.modrinth.com/data/cached_images/19fe4b80c7092a98ef22ae77672f7ebc41946c4d.png)

> *Recipes shown above may differ if your server owner has customized them in the configuration.*

---

### ☠️ Elimination System

Running out of hearts doesn't simply kill you...

Players who reach the minimum amount of hearts and die again become **eliminated**.

Features:

* Configurable temporary ban duration.
* Custom ban message.
* Persistent elimination data.
* Fully configurable.

---

### 🔥 Revive Beacon

Bring eliminated players back into the game.

The Revive Beacon opens a professional GUI showing every eliminated player.

Features:

* 🧭 Easy-to-use revive interface.
* 👤 Player heads.
* ⚡ Instant revival.
* 🔒 Safe beacon consumption.
* 🛡 Anti-duplication protection.

---

### ⚔ Combat Logging

Built-in combat logging protection.

* Configurable combat timer.
* Logout penalties.
* Last attacker tracking.
* Environmental deaths still reward the last attacker.
* Lightweight implementation.

---

### ⚙ Highly Configurable

Almost every gameplay aspect can be customized.

Examples include:

* Heart limits
* Combat duration
* Ban duration
* Messages
* Sounds
* Item names
* Item lore
* Recipes

---

### 🛡 Built for Performance

LifeStealX was written with performance in mind.

* Lightweight architecture
* Modern Paper API
* Java 21
* Efficient player data storage
* Minimal server impact
* Production-quality code

---

# 📸 Screenshots
> **(Comming soon!)**


> Replace the placeholders below with your own screenshots before publishing.

## ❤️ Heart System

<!-- INSERT SCREENSHOT HERE -->

<!-- Example: Player receiving a heart after killing another player -->

---

## 💎 Heart Item

<!-- INSERT SCREENSHOT HERE -->

<!-- Example: Heart Item tooltip with custom name and lore -->

---

## 🔥 Revive Beacon GUI

<!-- INSERT SCREENSHOT HERE -->

<!-- Example: Revive GUI showing eliminated player heads -->

---

## ☠️ Elimination / Ban Screen

<!-- INSERT SCREENSHOT HERE -->

<!-- Example: Temporary ban message -->

---

## ⚔ Combat System

<!-- INSERT SCREENSHOT HERE -->

<!-- Example: Combat ActionBar -->

---

## ⚙ Configuration

<!-- INSERT SCREENSHOT HERE -->

<!-- Example: config.yml -->

---

# 📜 Commands

| Command                  | Description                     |
| ------------------------ | ------------------------------- |
| `/withdraw <amount>`     | Convert hearts into Heart Items |
| `/ls`                    | Plugin information              |
| `/ls health <player>`    | View a player's hearts          |
| `/ls eliminate <player>` | Eliminate a player              |
| `/ls revive <player>`    | Revive an eliminated player     |
| `/ls reset <player>`     | Reset a player's hearts         |
| `/ls reload`             | Reload the configuration        |

---

# 🔑 Permissions

```
lifestealx.withdraw

lifestealx.admin

lifestealx.admin.reload

lifestealx.admin.health

lifestealx.admin.reset

lifestealx.admin.revive

lifestealx.admin.eliminate
```

---

# 📦 Installation

1. Download the latest version.
2. Place the plugin into your `plugins` folder.
3. Start your Paper server.
4. Configure the plugin inside `config.yml`.
5. Restart or reload the server.

---

# ❤️ Open Source

LifeStealX is completely open source.

Feel free to inspect the source code, contribute or report issues.

**GitHub Repository**

https://github.com/FelixFGF/LifeStealX

---

# 🐞 Bug Reports & Support

Found a bug?

Need help?

Join the Discord server.

[![Join DC](https://cdn.modrinth.com/data/cached_images/ee9f01d2777b2b3769ba098eceeaa3d85398e594_0.webp)](https://discord.gg/mP23dpVQ6V)

---

# 🚧 Alpha Notice

This is an early **Alpha** release.

Although the core gameplay is already functional, some features may still receive improvements and balancing changes.

Your feedback helps improve future versions.

---

# ❤️ Credits

Developed with passion by **FelixFGF**.

If you enjoy the project, consider leaving a ❤️ and follow future updates on Modrinth.
