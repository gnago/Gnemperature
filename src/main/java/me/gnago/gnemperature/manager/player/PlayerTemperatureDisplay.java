package me.gnago.gnemperature.manager.player;

public interface PlayerTemperatureDisplay {
    void displayBossBar(boolean show);
    void updateBossBar();

    void displayScoreboard(boolean show);
    void updateScoreboard();
}
