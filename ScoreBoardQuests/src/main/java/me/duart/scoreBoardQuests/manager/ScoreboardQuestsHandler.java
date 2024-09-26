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
        Component prefix = plugin.getMessage("Plugin_Prefix");

        if (quest == null || !blockBreakQuests.containsKey(quest)) return;

        Material blockType = event.getBlock().getType();
        Block block = event.getBlock();
        int increment = 0;

        switch (blockType) {
            case QUARTZ_BLOCK:
                if (quest.equals("Mine 64 Quartz Blocks")) increment = 1;
                break;
            case DEEPSLATE_COAL_ORE:
            case COAL_ORE:
                if (quest.equals("Mine 64 Coal Ores")) increment = 1;
                break;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                if (quest.equals("Mine 32 Iron Ores")) increment = 1;
                break;
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                if (quest.equals("Mine 10 Diamond Ores")) increment = 1;
                break;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                if (quest.equals("Mine 5 Emerald Ores")) increment = 1;
                break;
            case STONE:
                if (quest.equals("Break 200 Stone Blocks")) increment = 1;
                break;
            case END_STONE:
                if (quest.equals("Break 200 End Stones")) increment = 1;
                break;
            case OBSIDIAN:
                if (quest.equals("Break 100 Obsidian Blocks")) increment = 1;
                break;
            case WHEAT:
                if (quest.equals("Harvest 25 Wheat")) {
                    if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() == 7) {
                        increment = 1;
                    }
                }
                break;
            default:
                return;
        }

        scoreboardManager.updateProgress(player, increment);
        handleQuestCompletion(player, playerId, quest, prefix);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {
        Component prefix = plugin.getMessage("Plugin_Prefix");
        Player player = event.getEntity().getKiller();
        if (player == null) return;

        String playerId = player.getUniqueId().toString();
        String quest = scoreboardManager.getPlayerQuest(playerId);
        if (quest == null || !mobKillQuests.containsKey(quest)) return;

        EntityType entityType = event.getEntityType();
        int killCount = 0;

        switch (entityType) {
            case BLAZE:
                if (quest.equals("Kill 50 Blazes")) killCount = 1;
                break;
            case VEX:
                if (quest.equals("Kill 25 Vex")) killCount = 1;
                break;
            case SKELETON:
                if (quest.equals("Kill 25 Skeletons")) killCount = 1;
                break;
            case WITHER_SKELETON:
                if (quest.equals("Kill 25 Wither Skeletons")) killCount = 1;
                break;
            case CREEPER:
                if (quest.equals("Kill 25 Creepers")) killCount = 1;
                break;
            case WITHER:
                if (quest.equals("Kill a Wither")) killCount = 1;
                break;
            case COW:
                if (quest.equals("Kill 50 Cows")) killCount = 1;
                break;
            case RABBIT:
                if (quest.equals("Kill 10 Rabbits")) killCount = 1;
                break;
            case SHEEP:
                if (quest.equals("Kill 50 Sheep")) killCount = 1;
                break;
            case CHICKEN:
                if (quest.equals("Kill 10 Chickens")) killCount = 1;
                break;
            case PIG:
                if (quest.equals("Kill 50 Pigs")) killCount = 1;
                break;
            default:
                return;
        }

        if (killCount > 0) {
            scoreboardManager.updateProgress(player, killCount);
            handleQuestCompletion(player, playerId, quest, prefix);
        }
    }

    private void handleQuestCompletion(Player player, String playerId, String quest, Component prefix) {
        int progress = scoreboardManager.getPlayerProgress(playerId);
        int goal = scoreboardManager.getQuestGoal(quest);
        int comppletedQuests = scoreboardManager.getCompletedQuests(playerId);
        int reward = scoreboardManager.getPlayerReward(quest);
        int multiplier = multiplierHandler.getTotalMultiplier(player, comppletedQuests);
        double totalAmount = reward * multiplier;

        if (progress >= goal) {
            player.sendMessage(prefix.append(Component.text(" "))
                    .append(mini.deserialize("<reset><white>Quest: "))
                    .append(Component.text(quest)).color(NamedTextColor.GOLD)
                    .append(mini.deserialize("<white> completed!</white>")));
            scoreboardManager.getPlayerReward(playerId);
            scoreboardManager.updateCurrentQuest(player, 1);
            player.sendMessage(" ");
            economy.depositPlayer(player, totalAmount);
            player.sendMessage(prefix.append(Component.text(" "))
                    .append(mini.deserialize("<reset><yellow>The quest reward is: <green>" + reward
                            + "$</green>, and your total multiplier is: <gold>" + multiplier
                            + "</gold>, so your total amount received is: <gold>" + totalAmount
                            + "$</gold></yellow>")));
        }
    }
}