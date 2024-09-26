package me.duart.scoreBoardQuests.manager;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import me.duart.scoreBoardQuests.ScoreBoardQuests;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CustomScoreboardManager implements Listener {
    private final Random random = new Random();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ScoreBoardQuests plugin;

    private final Map<String, String> playerQuests = new HashMap<>();
    private final Map<String, Integer> playerProgress = new HashMap<>();
    private final Map<String, Integer> playerQuestCount = new HashMap<>();

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
    private final Component VEX_QUEST = createQuestComponent("ᴋɪʟʟ ᴠᴇx");
    private final Component SKELETON_QUEST = createQuestComponent("ᴋɪʟʟ sᴋᴇʟᴇᴛᴏɴs");
    private final Component WITHER_SKELETON_QUEST = createQuestComponent("ᴋɪʟʟ ᴡɪᴛʜᴇʀ sᴋᴇʟᴇᴛᴏɴs");
    private final Component CREEPER_QUEST = createQuestComponent("ᴋɪʟʟ ᴄʀᴇᴇᴘᴇʀs");
    private final Component WITHER_QUEST = createQuestComponent("ᴋɪʟʟ ᴀ ᴡɪᴛʜᴇʀ");
    private final Component COW_QUEST = createQuestComponent("ᴋɪʟʟ ᴄᴏᴡs");
    private final Component RABBIT_QUEST = createQuestComponent("ᴋɪʟʟ ʀᴀʙʙɪᴛs");
    private final Component SHEEP_QUEST = createQuestComponent("ᴋɪʟʟ sʜᴇᴇᴘs");
    private final Component CHICKEN_QUEST = createQuestComponent("ᴋɪʟʟ ᴄʜɪᴄᴋᴇɴs");
    private final Component PIG_QUEST = createQuestComponent("ᴋɪʟʟ ᴘɪɢs");

    private Component createQuestComponent(String questDisplayName) {
        return miniMessage.deserialize("<gradient:#11998E:#38EF7D>" + questDisplayName + "</gradient>");
    }

    private final Map<String, QuestData> quests = new HashMap<>() {{
        put("Mine 64 Quartz Blocks", new QuestData(QUARTZ_QUEST, 64, 300, ""));
        put("Mine 64 Coal Ores", new QuestData(COAL_QUEST, 64, 200, ""));
        put("Mine 32 Iron Ores", new QuestData(IRON_QUEST, 32, 400, ""));
        put("Mine 10 Diamond Ores", new QuestData(DIAMOND_QUEST, 10, 800 , ""));
        put("Mine 5 Emerald Ores", new QuestData(EMERALD_QUEST, 5, 1000 , ""));
        put("Break 200 Stone Blocks", new QuestData(STONE_QUEST, 200, 100 , ""));
        put("Break 200 End Stones", new QuestData(END_STONE_QUEST, 200, 150 , ""));
        put("Break 100 Obsidian Blocks", new QuestData(OBSIDIAN_QUEST, 100, 700 , ""));
        put("Harvest 25 Wheat", new QuestData(WHEAT_QUEST, 25, 100, ""));
        put("Kill 50 Blazes", new QuestData(BLAZE_QUEST, 50, 900, ""));
        put("Kill 25 Vex", new QuestData(VEX_QUEST, 25, 1200, ""));
        put("Kill 25 Skeletons", new QuestData(SKELETON_QUEST, 25, 500, ""));
        put("Kill 25 Wither Skeletons", new QuestData(WITHER_SKELETON_QUEST, 25, 1300, ""));
        put("Kill 25 Creepers", new QuestData(CREEPER_QUEST, 25, 600, ""));
        put("Kill a Wither", new QuestData(WITHER_QUEST, 1, 2000, ""));
        put("Kill 50 Cows", new QuestData(COW_QUEST, 50, 150, ""));
        put("Kill 10 Rabbits", new QuestData(RABBIT_QUEST, 10, 200, ""));
        put("Kill 50 Sheep", new QuestData(SHEEP_QUEST, 50, 150, ""));
        put("Kill 10 Chickens", new QuestData(CHICKEN_QUEST, 10, 100, ""));
        put("Kill 50 Pigs", new QuestData(PIG_QUEST, 50, 150, ""));
    }};


    public CustomScoreboardManager(ScoreBoardQuests plugin) {
        this.plugin = plugin;
    }

    private void updateScores(Player player) {
        Component questTitle = plugin.getMessage("Title");
        Component advertisement = plugin.getMessage("Bottom_Message");
        Scoreboard playerScoreboard = player.getScoreboard();

        if (playerScoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            playerScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(playerScoreboard);
        }

        Objective objective = playerScoreboard.getObjective("Quests");
        if (objective == null) {
            objective = playerScoreboard.registerNewObjective("Quests", Criteria.DUMMY, questTitle);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.numberFormat(NumberFormat.blank());
        }

        objective.getScore("1").customName(Component.empty());
        int scoreIndex = 2;

        String playerId = player.getUniqueId().toString();
        String quest = playerQuests.get(playerId);

        if (quest != null) {
            QuestData questData = quests.get(quest);
            if (questData == null) {
                plugin.getLogger().warning("Quest data for quest '" + quest + "' not found. Skipping...");
                return;
            }

            objective.getScore(String.valueOf(scoreIndex)).customName(questData.displayName());

            int progress = playerProgress.getOrDefault(playerId, 0);
            objective.getScore(String.valueOf(scoreIndex + 1)).customName(miniMessage.deserialize("<gray>ᴘʀᴏɢʀᴇss:</gray> " + progress + "/" + questData.goal()));
            scoreIndex += 2;

            Component rewardText = miniMessage.deserialize("<gray>ʀᴇᴡᴀʀᴅ: </gray><color:#80ff88>"+ questData.reward() + "$</color>");
            objective.getScore(String.valueOf(scoreIndex)).customName(rewardText);
            scoreIndex++;
        }

        int completedQuests = playerQuestCount.getOrDefault(playerId, 0);
        int multiplier = getMultiplier(completedQuests);

        Component streakComponent = miniMessage.deserialize("<gray>sᴛʀᴇᴀᴋ: </gray>" + completedQuests + (multiplier >= 2 ? " <color:#80ff88>(x" + multiplier + "$)</color>" : ""));
        objective.getScore(String.valueOf(scoreIndex)).customName(streakComponent);
        scoreIndex++;

        objective.getScore(String.valueOf(scoreIndex)).customName(Component.empty());

        objective.getScore(String.valueOf(scoreIndex + 1)).customName(advertisement);
    }

    public void createScoreboard(Player player) {
        updateScores(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerId = player.getUniqueId().toString();

        if (!playerQuests.containsKey(playerId)) {
            String quest = (String) quests.keySet().toArray()[random.nextInt(quests.size())];
            playerQuests.put(playerId, quest);
            playerProgress.put(playerId, 0);
        }

        createScoreboard(player);
    }

    public void updateCurrentQuest(Player player, int completedCount) {
        String playerId = player.getUniqueId().toString();
        String newQuest = (String) quests.keySet().toArray()[random.nextInt(quests.size())];

        playerQuests.put(playerId, newQuest);
        playerProgress.put(playerId, 0);

        int completedQuests = playerQuestCount.getOrDefault(playerId, 0) + completedCount;
        playerQuestCount.put(playerId, completedQuests);

        updateScores(player);
    }

    public void updateProgress(Player player, int amount) {
        String playerId = player.getUniqueId().toString();
        playerProgress.put(playerId, playerProgress.getOrDefault(playerId, 0) + amount);
        updateScores(player);
    }

    public String getPlayerQuest(String playerId) {
        return playerQuests.getOrDefault(playerId, "No quest assigned");
    }

    public int getPlayerProgress(String playerId) {
        return playerProgress.getOrDefault(playerId, 0);
    }

    public int getQuestGoal(String quest) {
        QuestData questData = quests.get(quest);
        return (questData != null) ? questData.goal() : -1;
    }

    public int getPlayerReward(String quest) {
        QuestData questData = quests.get(quest);
        return (questData != null) ? questData.reward() : -1;
    }

    public static int getMultiplier(int completedQuests) {
        int[] thresholds = {10, 30, 65, 125, 200, 350, 600, 1000, 2000, 4000};
        for (int i = 0; i < thresholds.length; i++) {
            if (completedQuests < thresholds[i]) return i + 1;
        }
        return thresholds.length + 1;
    }

    public int getCompletedQuests(String playerId) {
        return playerQuestCount.getOrDefault(playerId, 0);
    }

    public record QuestData(Component displayName, int goal, int reward, String optionalReward) { }
}