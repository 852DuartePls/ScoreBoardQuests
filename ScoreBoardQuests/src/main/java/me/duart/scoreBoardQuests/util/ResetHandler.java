package me.duart.scoreBoardQuests.util;

import me.duart.scoreBoardQuests.ScoreBoardQuests;
import me.duart.scoreBoardQuests.manager.CustomScoreboardManager;


public class ResetHandler {
    private final ScoreBoardQuests plugin;
    private final CustomScoreboardManager scoreboardManager;

    public ResetHandler(ScoreBoardQuests plugin, CustomScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    public void handleReset(String resetId) {
        scoreboardManager.resetAllQuests();
        plugin.getLogger().info("All quests have been reset (triggered by [" + resetId + "]).");
    }
}
