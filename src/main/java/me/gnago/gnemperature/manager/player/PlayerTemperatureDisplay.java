package me.gnago.gnemperature.manager.player;

public interface PlayerTemperatureDisplay {
    void showBossBar(boolean show);
    void updateBossBar();

    void showScoreboard(boolean show);
    void updateScoreboard();
}
