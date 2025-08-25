package me.duart.scoreBoardQuests.manager;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import me.duart.scoreBoardQuests.ScoreBoardQuests;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.duart.scoreBoardQuests.ScoreBoardQuests.mini;

public class CustomScoreboardManager implements Listener {

    private static final int[] THRESHOLDS = {10, 30, 65, 125, 200, 350, 600, 1000, 2000, 4000};
    private static final Component EMPTY = Component.empty();
    private static final Component WEEKEND_BONUS = Component.text("[ᴡᴇᴇᴋᴇɴᴅ ʙᴏɴᴜs!]", TextColor.color(0xffcc00));
    private static final StringBuilder PROGRESS_BUILDER = new StringBuilder(32);

    private final Random random = new Random();
    private final ScoreBoardQuests plugin;

    private final Map<String, PlayerQuestData> playerQuestDataMap = new HashMap<>();
    private final Map<String, Integer> playerQuestCount = new HashMap<>();
    private final Map<Player, Boolean> scoreboardVisible = new WeakHashMap<>();

    private final String[] questArray;

    private final Component QUARTZ_QUEST = createQuestComponent("ᴍɪɴᴇ ǫᴜᴀʀᴛᴢ ʙʟᴏᴄᴋs");
    private final Component COAL_QUEST = createQuestComponent("ᴍɪɴᴇ ᴄᴏᴀʟ ᴏʀᴇs");
    private final Component IRON_QUEST = createQuestComponent("ᴍɪɴᴇ ɪʀᴏɴ ᴏʀᴇs");
    private final Component DIAMOND_QUEST = createQuestComponent("ᴍɪɴᴇ ᴅɪᴀᴍᴏɴᴅ ᴏʀᴇs");
    private final Component EMERALD_QUEST = createQuestComponent("ᴍɪɴᴇ ᴇᴍᴇʀᴀʟᴅ ᴏʀᴇs");
    private final Component STONE_QUEST = createQuestComponent("ʙʀᴇᴀᴋ sᴛᴏɴᴇ ʙʟᴏᴄᴋs");
    private final Component END_STONE_QUEST = createQuestComponent("ʙʀᴇᴀᴋ ᴇɴᴅ sᴛᴏɴᴇs");
    private final Component OBSIDIAN_QUEST = createQuestComponent("ʙʀᴇᴀᴋ ᴏʙsɪᴅɪᴀɴ ʙʟᴏᴄᴋs");
    private final Component WHEAT_QUEST = createQuestComponent("ʜᴀʀᴠᴇsᴛ ᴡʜᴇᴀᴛ");
    private final Component BLAZE_QUEST = createQuestComponent("ᴋɪʟʟ ʙʟᴀᴢᴇs");
    private final Component SKELETON_QUEST = createQuestComponent("ᴋɪʟʟ sᴋᴇʟᴇᴛᴏɴs");
    private final Component WITHER_SKELETON_QUEST = createQuestComponent("ᴋɪʟʟ ᴡɪᴛʜᴇʀ sᴋᴇʟᴇᴛᴏɴs");
    private final Component CREEPER_QUEST = createQuestComponent("ᴋɪʟʟ ᴄʀᴇᴇᴘᴇʀs");
    private final Component WITHER_QUEST = createQuestComponent("ᴋɪʟʟ ᴀ ᴡɪᴛʜᴇʀ");
    private final Component COW_QUEST = createQuestComponent("ᴋɪʟʟ ᴄᴏᴡs");
    private final Component RABBIT_QUEST = createQuestComponent("ᴋɪʟʟ ʀᴀʙʙɪᴛs");
    private final Component SHEEP_QUEST = createQuestComponent("ᴋɪʟʟ sʜᴇᴇᴘs");
    private final Component CHICKEN_QUEST = createQuestComponent("ᴋɪʟʟ ᴄʜɪᴄᴋᴇɴs");
    private final Component PIG_QUEST = createQuestComponent("ᴋɪʟʟ ᴘɪɢs");

    private final Map<String, QuestData> quests = new HashMap<>() {{
        put("Mine 64 Quartz Blocks", new QuestData(QUARTZ_QUEST, 64, 1000, ""));
        put("Mine 64 Coal Ores", new QuestData(COAL_QUEST, 64, 500, ""));
        put("Mine 32 Iron Ores", new QuestData(IRON_QUEST, 32, 2000, ""));
        put("Mine 10 Diamond Ores", new QuestData(DIAMOND_QUEST, 10, 2000, ""));
        put("Mine 5 Emerald Ores", new QuestData(EMERALD_QUEST, 5, 500, ""));
        put("Break 200 Stone Blocks", new QuestData(STONE_QUEST, 200, 100, ""));
        put("Break 200 End Stones", new QuestData(END_STONE_QUEST, 200, 5000, ""));
        put("Break 100 Obsidian Blocks", new QuestData(OBSIDIAN_QUEST, 100, 100, ""));
        put("Harvest 25 Wheat", new QuestData(WHEAT_QUEST, 25, 2000, ""));
        put("Kill 50 Blazes", new QuestData(BLAZE_QUEST, 50, 500, ""));
        put("Kill 25 Skeletons", new QuestData(SKELETON_QUEST, 25, 1000, ""));
        put("Kill 25 Wither Skeletons", new QuestData(WITHER_SKELETON_QUEST, 25, 500, ""));
        put("Kill 25 Creepers", new QuestData(CREEPER_QUEST, 25, 1000, ""));
        put("Kill a Wither", new QuestData(WITHER_QUEST, 1, 10000, ""));
        put("Kill 50 Cows", new QuestData(COW_QUEST, 50, 500, ""));
        put("Kill 10 Rabbits", new QuestData(RABBIT_QUEST, 10, 1000, ""));
        put("Kill 50 Sheep", new QuestData(SHEEP_QUEST, 50, 500, ""));
        put("Kill 10 Chickens", new QuestData(CHICKEN_QUEST, 10, 1000, ""));
        put("Kill 50 Pigs", new QuestData(PIG_QUEST, 50, 1000, ""));
    }};

    public CustomScoreboardManager(ScoreBoardQuests plugin) {
        this.plugin = plugin;
        this.questArray = quests.keySet().toArray(new String[0]);
    }

    public void toggleScoreboardVisibility(Player player) {
        boolean isVisible = scoreboardVisible.getOrDefault(player, true);
        scoreboardVisible.put(player, !isVisible);
        player.sendMessage(mini.deserialize("<green>Quests scoreboard " + (isVisible ? "hidden." : "visible.")));

        if (isVisible) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        } else {
            createScoreboard(player);
        }
    }

    private void ensurePlayerScoreboard(@NotNull Player player) {
        if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    private void updateScores(@NotNull Player player) {
        if (!scoreboardVisible.getOrDefault(player, true)) return;

        ensurePlayerScoreboard(player);

        Scoreboard playerScoreboard = player.getScoreboard();
        Component questTitle = plugin.getMessage("Title");
        Component advertisement = plugin.getMessage("Bottom_Message");

        Objective objective = playerScoreboard.getObjective("Quests");
        if (objective != null) {
            objective.unregister();
        }
        objective = playerScoreboard.registerNewObjective("Quests", Criteria.DUMMY, questTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());

        int scoreIndex = 2;
        String playerId = player.getUniqueId().toString();
        PlayerQuestData questData = playerQuestDataMap.get(playerId);

        if (questData != null) {
            QuestData questDetails = quests.get(questData.getQuestName());
            if (questDetails == null) {
                plugin.getLogger().warning("Quest data for quest '" + questData.getQuestName() + "' not found. Skipping...");
                return;
            }

            objective.getScore(String.valueOf(scoreIndex)).customName(questDetails.displayName());

            int progress = questData.getProgress();
            PROGRESS_BUILDER.setLength(0);
            PROGRESS_BUILDER.append("ᴘʀᴏɢʀᴇss: ").append(progress).append('/').append(questDetails.goal());
            objective.getScore(String.valueOf(scoreIndex + 1)).customName(Component.text(PROGRESS_BUILDER.toString(), NamedTextColor.GRAY));
            scoreIndex += 2;

            Component rewardText = Component.text("ʀᴇᴡᴀʀᴅ: ", NamedTextColor.GRAY).append(Component.text(questDetails.reward() + "$", TextColor.color(0x80ff88)));
            objective.getScore(String.valueOf(scoreIndex)).customName(rewardText);
            scoreIndex++;
        }

        int completedQuests = playerQuestCount.getOrDefault(playerId, 0);
        int multiplier = getMultiplier(completedQuests);

        Component streakComponent = mini.deserialize("<gray>sᴛʀᴇᴀᴋ: </gray>" + completedQuests +
                (multiplier >= 2 ? " <color:#80ff88>(x" + multiplier + "$)</color>" : ""));
        objective.getScore(String.valueOf(scoreIndex)).customName(streakComponent);
        scoreIndex++;

        if (plugin.isWeekend()) {
            objective.getScore(String.valueOf(scoreIndex)).customName(WEEKEND_BONUS);
            scoreIndex++;
        }

        objective.getScore(String.valueOf(scoreIndex)).customName(EMPTY);
        objective.getScore(String.valueOf(scoreIndex + 1)).customName(advertisement);
    }

    public void createScoreboard(Player player) {
        ensurePlayerScoreboard(player);
        updateScores(player);
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerId = player.getUniqueId().toString();

        playerQuestDataMap.computeIfAbsent(playerId, id
                -> new PlayerQuestData(questArray[random.nextInt(questArray.length)]));
        scoreboardVisible.putIfAbsent(player, true);
        createScoreboard(player);
    }

    public void updateCurrentQuest(@NotNull Player player, int completedCount) {
        String playerId = player.getUniqueId().toString();
        PlayerQuestData playerQuestData = playerQuestDataMap.get(playerId);

        if (playerQuestData == null) {
            plugin.getLogger().warning("No quest data found for player " + playerId);
            return;
        }

        String newQuest;
        do {
            newQuest = questArray[random.nextInt(questArray.length)];
        } while (newQuest.equals(playerQuestData.getQuestName()));

        playerQuestData.setProgress(0);
        playerQuestDataMap.put(playerId, new PlayerQuestData(newQuest));

        int completedQuests = getCompletedQuests(playerId) + completedCount;
        playerQuestCount.put(playerId, completedQuests);

        updateScores(player);
    }

    public void updateProgress(@NotNull Player player, int amount) {
        if (isScoreboardHidden(player)) return;
        String playerId = player.getUniqueId().toString();
        PlayerQuestData playerQuestData = playerQuestDataMap.get(playerId);

        if (playerQuestData != null) {
            playerQuestData.setProgress(playerQuestData.getProgress() + amount);
        }

        updateScores(player);
    }

    public String getPlayerQuest(String playerId) {
        PlayerQuestData questData = playerQuestDataMap.get(playerId);
        return (questData != null) ? questData.getQuestName() : "No quest assigned";
    }

    public int getPlayerProgress(String playerId) {
        PlayerQuestData questData = playerQuestDataMap.get(playerId);
        return (questData != null) ? questData.getProgress() : 0;
    }

    public int getQuestGoal(String quest) {
        QuestData questData = quests.get(quest);
        return (questData != null) ? questData.goal() : -1;
    }

    public int getPlayerReward(String quest) {
        QuestData questData = quests.get(quest);
        return (questData != null) ? questData.reward() : -1;
    }

    public void giveOptionalReward(Player player, String questName) {
        QuestData questData = quests.get(questName);
        if (questData == null) {
            plugin.getLogger().warning("Quest not found: " + questName);
            return;
        }

        String optionalReward = questData.optionalReward();
        if (optionalReward == null || optionalReward.isBlank()) return;

        if (optionalReward.startsWith("item:")) {
            String[] parts = optionalReward.substring(5).split(":");
            String itemName = parts[0];
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid number format for reward: " + optionalReward);
                    return;
                }
            }
            giveItemReward(player, itemName, amount);
        } else if (optionalReward.startsWith("command:")) {
            String command = optionalReward.substring(8);
            CommandSender silent = Bukkit.createCommandSender(m -> {});
            Bukkit.dispatchCommand(silent, command.replace("%player%", player.getName()));
        } else {
            plugin.getLogger().warning("Unknown optional reward type: " + optionalReward);
        }
    }

    private void giveItemReward(Player player, @NotNull String itemName, int amount) {
        Material material = Material.matchMaterial(itemName.toUpperCase());
        if (material != null) {
            ItemStack itemStack = new ItemStack(material, amount);
            player.getInventory().addItem(itemStack);
        } else {
            plugin.getLogger().warning("Invalid reward item type: " + itemName);
        }
    }

    public boolean isScoreboardHidden(Player player) {
        return !scoreboardVisible.getOrDefault(player, true);
    }

    public static int getMultiplier(int completedQuests) {
        int idx = Arrays.binarySearch(THRESHOLDS, completedQuests);
        return (idx < 0 ? -idx - 1 : idx + 1) + 1;
    }

    public int getCompletedQuests(String playerId) {
        return playerQuestCount.getOrDefault(playerId, 0);
    }

    private static @NotNull Component createQuestComponent(String questDisplayName) {
        return mini.deserialize("<gradient:#11998E:#38EF7D>" + questDisplayName + "</gradient>");
    }

    public record QuestData(Component displayName, int goal, int reward, String optionalReward) { }

    public static class PlayerQuestData {
        private final String questName;
        private int progress;

        public PlayerQuestData(String questName) {
            this.questName = questName;
            this.progress = 0;
        }

        public String getQuestName() {
            return questName;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }
    }
}
