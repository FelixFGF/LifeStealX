package de.felixfgf.lifestealx.data;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int hearts;
    private boolean eliminated;
    private long eliminatedUntil;
    private UUID lastAttacker;
    private long combatTagExpiry;

    public PlayerData(UUID uuid, int defaultHearts) {
        this.uuid = uuid;
        this.hearts = defaultHearts;
        this.eliminated = false;
        this.eliminatedUntil = 0;
        this.lastAttacker = null;
        this.combatTagExpiry = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getHearts() {
        return hearts;
    }

    public void setHearts(int hearts) {
        this.hearts = hearts;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public long getEliminatedUntil() {
        return eliminatedUntil;
    }

    public void setEliminatedUntil(long eliminatedUntil) {
        this.eliminatedUntil = eliminatedUntil;
    }

    public UUID getLastAttacker() {
        return lastAttacker;
    }

    public void setLastAttacker(UUID lastAttacker) {
        this.lastAttacker = lastAttacker;
    }

    public long getCombatTagExpiry() {
        return combatTagExpiry;
    }

    public void setCombatTagExpiry(long combatTagExpiry) {
        this.combatTagExpiry = combatTagExpiry;
    }

    public boolean isInCombat() {
        return System.currentTimeMillis() < combatTagExpiry;
    }

    public void refreshCombatTag(int combatTimeSeconds) {
        this.combatTagExpiry = System.currentTimeMillis() + (combatTimeSeconds * 1000L);
    }

    public void clearCombatTag() {
        this.combatTagExpiry = 0;
    }
}
