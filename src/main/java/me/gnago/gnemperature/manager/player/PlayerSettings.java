package me.gnago.gnemperature.manager.player;

public abstract class PlayerSettings {
    boolean useCelsius;
    boolean showFromInventory;
    boolean showActual;
    boolean lockSettings;

    boolean debugModeOn;
    boolean debugDisableDebuffs;
    boolean enabled;

    public PlayerSettings() {
        useCelsius = false;
        showFromInventory = false;
        showActual = false;
        lockSettings = false;

        debugModeOn = false;
        debugDisableDebuffs = false;
        enabled = true;
    }
}
