package me.duart.scoreBoardQuests.manager;

import me.duart.scoreBoardQuests.ScoreBoardQuests;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class QuestsHandler implements Listener {

    private static final Map<String, Integer> blockBreakQuests = Map.of(
        "Mine 64 Quartz Blocks", 64,
        "Mine 64 Coal Ores", 64,
        "Mine 32 Iron Ores", 32,
        "Mine 10 Diamond Ores", 10,
        "Mine 5 Emerald Ores", 5,
        "Break 200 Stone Blocks", 200,
        "Break 200 End Stones", 200,
        "Break 100 Obsidian Blocks", 100,
        "Harvest 25 Wheat", 25
    );

    private static final Map<String, Integer> mobKillQuests = Map.of(
            "Kill 50 Blazes", 50,
            "Kill 25 Skeletons", 25,
            "Kill 25 Wither Skeletons", 25,
            "Kill 25 Creepers", 25,
            "Kill a Wither", 1,
            "Kill 50 Cows", 50,
            "Kill 10 Rabbits", 10,
            "Kill 50 Sheep", 50,
            "Kill 10 Chickens", 10,
            "Kill 50 Pigs", 50
    );

    private final CustomScoreboardManager scoreboardManager;
    private final ScoreBoardQuests plugin;
    private final Economy economy;

    @Contract(pure = true)
    public QuestsHandler(CustomScoreboardManager scoreboardManager, @NotNull ScoreBoardQuests plugin) {
        this.scoreboardManager = scoreboardManager;
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        String playerId = player.getUniqueId().toString();
        String quest = scoreboardManager.getPlayerQuest(playerId);

        if (scoreboardManager.isScoreboardHidden(player) || quest == null) return;

        int increment = getBlockIncrement(quest, event.getBlock());

        if (increment > 0) {
            scoreboardManager.updateProgress(player, increment);
            handleQuestCompletion(player, playerId, quest);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;

        String playerId = player.getUniqueId().toString();
        String quest = scoreboardManager.getPlayerQuest(playerId);
        if (scoreboardManager.isScoreboardHidden(player) || quest == null) return;

        int killCount = getMobKillIncrement(quest, event.getEntityType());

        if (killCount > 0) {
            scoreboardManager.updateProgress(player, killCount);
            handleQuestCompletion(player, playerId, quest);
        }
    }

    private static int getBlockIncrement(String quest, Block block) {
        Integer goal = blockBreakQuests.get(quest);
        if (goal == null) return 0;

        return switch (quest) {
            case "Mine 64 Quartz Blocks" -> block.getType() == Material.QUARTZ_BLOCK ? 1 : 0;
            case "Mine 64 Coal Ores" -> (block.getType() == Material.COAL_ORE || block.getType() == Material.DEEPSLATE_COAL_ORE) ? 1 : 0;
            case "Mine 32 Iron Ores" -> (block.getType() == Material.IRON_ORE || block.getType() == Material.DEEPSLATE_IRON_ORE) ? 1 : 0;
            case "Mine 10 Diamond Ores" -> (block.getType() == Material.DIAMOND_ORE || block.getType() == Material.DEEPSLATE_DIAMOND_ORE) ? 1 : 0;
            case "Mine 5 Emerald Ores" -> (block.getType() == Material.EMERALD_ORE || block.getType() == Material.DEEPSLATE_EMERALD_ORE) ? 1 : 0;
            case "Break 200 Stone Blocks" -> block.getType() == Material.STONE ? 1 : 0;
            case "Break 200 End Stones" -> block.getType() == Material.END_STONE ? 1 : 0;
            case "Break 100 Obsidian Blocks" -> block.getType() == Material.OBSIDIAN ? 1 : 0;
            case "Harvest 25 Wheat" -> (block.getBlockData() instanceof Ageable ageable && ageable.getAge() == 7) ? 1 : 0;
            default -> 0;
        };
    }

    @Contract(pure = true)
    private static int getMobKillIncrement(@NotNull String quest, EntityType type) {
        Integer goal = mobKillQuests.get(quest);
        if (goal == null) return 0;
        return switch (quest) {
            case "Kill 50 Blazes" -> type == EntityType.BLAZE ? 1 : 0;
            case "Kill 25 Skeletons" -> type == EntityType.SKELETON ? 1 : 0;
            case "Kill 25 Wither Skeletons" -> type == EntityType.WITHER_SKELETON ? 1 : 0;
            case "Kill 25 Creepers" -> type == EntityType.CREEPER ? 1 : 0;
            case "Kill a Wither" -> type == EntityType.WITHER ? 1 : 0;
            case "Kill 50 Cows" -> type == EntityType.COW ? 1 : 0;
            case "Kill 10 Rabbits" -> type == EntityType.RABBIT ? 1 : 0;
            case "Kill 50 Sheep" -> type == EntityType.SHEEP ? 1 : 0;
            case "Kill 10 Chickens" -> type == EntityType.CHICKEN ? 1 : 0;
            case "Kill 50 Pigs" -> type == EntityType.PIG ? 1 : 0;
            default -> 0;
        };
    }

    private void handleQuestCompletion(Player player, String playerId, String quest) {
        int progress = scoreboardManager.getPlayerProgress(playerId);
        int goal = scoreboardManager.getQuestGoal(quest);
        if (progress < goal) return;

        int completedQuests = scoreboardManager.getCompletedQuests(playerId);
        int reward = scoreboardManager.getPlayerReward(quest);
        int multiplier = CustomScoreboardManager.getMultiplier(completedQuests);
        if (plugin.isWeekend()) multiplier *= 2;
        double total = reward * multiplier;

        player.sendMessage(plugin.getMessage("Plugin_Prefix")
                .append(Component.text(" Quest: ", NamedTextColor.WHITE))
                .append(Component.text(quest, NamedTextColor.GOLD))
                .append(Component.text(" completed!", NamedTextColor.WHITE)));

        economy.depositPlayer(player, total);
        scoreboardManager.giveOptionalReward(player, quest);
        scoreboardManager.updateCurrentQuest(player, 1);
    }
}
