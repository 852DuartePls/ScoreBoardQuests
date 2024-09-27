package me.duart.scoreBoardQuests.manager;

import me.duart.scoreBoardQuests.ScoreBoardQuests;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

import java.util.HashMap;
import java.util.Map;

public class ScoreboardQuestsHandler implements Listener {
    private final CustomScoreboardManager scoreboardManager;
    private final ScoreBoardQuests plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Economy economy;
    private final MultiplierHandler multiplierHandler;

    private final Map<String, Integer> blockBreakQuests = new HashMap<>() {{
        put("Mine 64 Quartz Blocks", 64);
        put("Mine 64 Coal Ores", 64);
        put("Mine 32 Iron Ores", 32);
        put("Mine 10 Diamond Ores", 10);
        put("Mine 5 Emerald Ores", 5);
        put("Break 200 Stone Blocks", 200);
        put("Break 200 End Stones", 200);
        put("Break 100 Obsidian Blocks", 100);
        put("Harvest 25 Wheat", 25);
    }};

    private final Map<String, Integer> mobKillQuests = new HashMap<>() {{
        put("Kill 50 Blazes", 50);
        put("Kill 25 Vex", 25);
        put("Kill 25 Skeletons", 25);
        put("Kill 25 Wither Skeletons", 25);
        put("Kill 25 Creepers", 25);
        put("Kill a Wither", 1);
        put("Kill 50 Cows", 50);
        put("Kill 10 Rabbits", 10);
        put("Kill 50 Sheep", 50);
        put("Kill 10 Chickens", 10);
        put("Kill 50 Pigs", 50);
    }};

    public ScoreboardQuestsHandler(CustomScoreboardManager scoreboardManager, MultiplierHandler multiplierHandler, ScoreBoardQuests plugin) {
        this.scoreboardManager = scoreboardManager;
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.multiplierHandler = multiplierHandler;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String playerId = player.getUniqueId().toString();
        String quest = scoreboardManager.getPlayerQuest(playerId);
        if (!scoreboardManager.isScoreboardVisible(player)) return;
        if (quest == null || !blockBreakQuests.containsKey(quest)) return;

        Component prefix = plugin.getMessage("Plugin_Prefix");
        Material blockType = event.getBlock().getType();
        Block block = event.getBlock();
        int increment = getBlockIncrement(quest, blockType, block);

        if (increment > 0) {
            scoreboardManager.updateProgress(player, increment);
            handleQuestCompletion(player, playerId, quest, prefix);
        }
    }

    private int getBlockIncrement(String quest, Material blockType, Block block) {
        return switch (quest) {
            case "Mine 64 Quartz Blocks" -> blockType == Material.QUARTZ_BLOCK ? 1 : 0;
            case "Mine 64 Coal Ores" -> (blockType == Material.COAL_ORE || blockType == Material.DEEPSLATE_COAL_ORE) ? 1 : 0;
            case "Mine 32 Iron Ores" -> (blockType == Material.IRON_ORE || blockType == Material.DEEPSLATE_IRON_ORE) ? 1 : 0;
            case "Mine 10 Diamond Ores" -> (blockType == Material.DIAMOND_ORE || blockType == Material.DEEPSLATE_DIAMOND_ORE) ? 1 : 0;
            case "Mine 5 Emerald Ores" -> (blockType == Material.EMERALD_ORE || blockType == Material.DEEPSLATE_EMERALD_ORE) ? 1 : 0;
            case "Break 200 Stone Blocks" -> blockType == Material.STONE ? 1 : 0;
            case "Break 200 End Stones" -> blockType == Material.END_STONE ? 1 : 0;
            case "Break 100 Obsidian Blocks" -> blockType == Material.OBSIDIAN ? 1 : 0;
            case "Harvest 25 Wheat" -> block.getBlockData() instanceof Ageable ageable && ageable.getAge() == 7 ? 1 : 0;
            default -> 0;
        };
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;

        String playerId = player.getUniqueId().toString();
        String quest = scoreboardManager.getPlayerQuest(playerId);
        if (!scoreboardManager.isScoreboardVisible(player)) return;
        if (quest == null || !mobKillQuests.containsKey(quest)) return;

        Component prefix = plugin.getMessage("Plugin_Prefix");
        EntityType entityType = event.getEntityType();
        int killCount = getMobKillIncrement(quest, entityType);

        if (killCount > 0) {
            scoreboardManager.updateProgress(player, killCount);
            handleQuestCompletion(player, playerId, quest, prefix);
        }
    }

    private int getMobKillIncrement(String quest, EntityType entityType) {
        return switch (quest) {
            case "Kill 50 Blazes" -> entityType == EntityType.BLAZE ? 1 : 0;
            case "Kill 25 Vex" -> entityType == EntityType.VEX ? 1 : 0;
            case "Kill 25 Skeletons" -> entityType == EntityType.SKELETON ? 1 : 0;
            case "Kill 25 Wither Skeletons" -> entityType == EntityType.WITHER_SKELETON ? 1 : 0;
            case "Kill 25 Creepers" -> entityType == EntityType.CREEPER ? 1 : 0;
            case "Kill a Wither" -> entityType == EntityType.WITHER ? 1 : 0;
            case "Kill 50 Cows" -> entityType == EntityType.COW ? 1 : 0;
            case "Kill 10 Rabbits" -> entityType == EntityType.RABBIT ? 1 : 0;
            case "Kill 50 Sheep" -> entityType == EntityType.SHEEP ? 1 : 0;
            case "Kill 10 Chickens" -> entityType == EntityType.CHICKEN ? 1 : 0;
            case "Kill 50 Pigs" -> entityType == EntityType.PIG ? 1 : 0;
            default -> 0;
        };
    }

    private void handleQuestCompletion(Player player, String playerId, String quest, Component prefix) {
        int progress = scoreboardManager.getPlayerProgress(playerId);
        int goal = scoreboardManager.getQuestGoal(quest);
        int completedQuests = scoreboardManager.getCompletedQuests(playerId);
        int reward = scoreboardManager.getPlayerReward(quest);
        int multiplier = multiplierHandler.getTotalMultiplier(player, completedQuests);
        double totalAmount = reward * multiplier;

        if (progress >= goal) {
            if (scoreboardManager.isScoreboardVisible(player)) {
                player.sendMessage(prefix.append(Component.text(" "))
                        .append(mini.deserialize("<reset><white>Quest: "))
                        .append(Component.text(quest)).color(NamedTextColor.GOLD)
                        .append(mini.deserialize("<white> completed!</white>")));
                scoreboardManager.getPlayerReward(playerId);
                scoreboardManager.updateCurrentQuest(player, 1);
                economy.depositPlayer(player, totalAmount);
            }
        }
    }
}