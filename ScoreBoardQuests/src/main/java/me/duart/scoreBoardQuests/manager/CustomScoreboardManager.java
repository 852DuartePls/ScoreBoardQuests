package me.duart.scoreBoardQuests.manager;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import me.duart.scoreBoardQuests.ScoreBoardQuests;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomScoreboardManager implements Listener {
    private final Random random = new Random();
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final ScoreBoardQuests plugin;
    private final CommandSender silentSender;

    private final Map<String, Integer> playerQuestCount = new ConcurrentHashMap<>();
    private final Map<String, PlayerQuestData> playerQuestDataMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, Boolean> scoreboardVisible = new ConcurrentHashMap<>();
    private final List<String> questNames;
    private static final int[] THRESHOLDS = {10, 30, 65, 125, 200, 350, 600, 1000, 2000, 4000};

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

    private final String TEST_REWARD2 = createCommandReward("minecraft:give %player% gold_ingot[custom_name='[\"\",{\"text\":\"Reward Gold\",\"italic\":false,\"bold\":true,\"color\":\"gold\"}]']");
    private final String ESSENTIALS_REWARD = createCommandReward("essentials:give %player% diamond 1 [custom_name='[\"\",{\"text\":\"Diamantin\",\"italic\":false,\"underlined\":true,\"bold\":true,\"color\":\"blue\"}]']");

    private Component createQuestComponent(String questDisplayName) {
        return mini.deserialize("<gradient:#11998E:#38EF7D>" + questDisplayName + "</gradient>");
    }

    private String createCommandReward(String reward) {
        return "command:" + reward;
    }

    private final Map<String, QuestData> quests = new HashMap<>() {{
        put("Mine 64 Quartz Blocks", new QuestData(QUARTZ_QUEST, 64, 300, ESSENTIALS_REWARD));
        put("Mine 64 Coal Ores", new QuestData(COAL_QUEST, 64, 200, ESSENTIALS_REWARD));
        put("Mine 32 Iron Ores", new QuestData(IRON_QUEST, 32, 400, ESSENTIALS_REWARD));
        put("Mine 10 Diamond Ores", new QuestData(DIAMOND_QUEST, 10, 800 , ESSENTIALS_REWARD));
        put("Mine 5 Emerald Ores", new QuestData(EMERALD_QUEST, 5, 1000 , ESSENTIALS_REWARD));
        put("Break 200 Stone Blocks", new QuestData(STONE_QUEST, 200, 100 , TEST_REWARD2));
        put("Break 200 End Stones", new QuestData(END_STONE_QUEST, 200, 150 , ESSENTIALS_REWARD));
        put("Break 100 Obsidian Blocks", new QuestData(OBSIDIAN_QUEST, 100, 700 , ESSENTIALS_REWARD));
        put("Harvest 25 Wheat", new QuestData(WHEAT_QUEST, 25, 100, ESSENTIALS_REWARD));
        put("Kill 50 Blazes", new QuestData(BLAZE_QUEST, 50, 900, ESSENTIALS_REWARD));
        put("Kill 25 Vex", new QuestData(VEX_QUEST, 25, 1200, ESSENTIALS_REWARD));
        put("Kill 25 Skeletons", new QuestData(SKELETON_QUEST, 25, 500, ESSENTIALS_REWARD));
        put("Kill 25 Wither Skeletons", new QuestData(WITHER_SKELETON_QUEST, 25, 1300, ESSENTIALS_REWARD));
        put("Kill 25 Creepers", new QuestData(CREEPER_QUEST, 25, 600, TEST_REWARD2));
        put("Kill a Wither", new QuestData(WITHER_QUEST, 1, 2000, ESSENTIALS_REWARD));
        put("Kill 50 Cows", new QuestData(COW_QUEST, 50, 150, ESSENTIALS_REWARD));
        put("Kill 10 Rabbits", new QuestData(RABBIT_QUEST, 10, 200, ESSENTIALS_REWARD));
        put("Kill 50 Sheep", new QuestData(SHEEP_QUEST, 50, 150, ESSENTIALS_REWARD));
        put("Kill 10 Chickens", new QuestData(CHICKEN_QUEST, 10, 100, ESSENTIALS_REWARD));
        put("Kill 50 Pigs", new QuestData(PIG_QUEST, 50, 150, ESSENTIALS_REWARD));
    }};

    public CustomScoreboardManager(ScoreBoardQuests plugin) {
        this.plugin = plugin;
        this.questNames = new ArrayList<>(quests.keySet());
        this.silentSender = Bukkit.createCommandSender(message -> {});
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

    private void updateScores(Player player) {
        Component questTitle = plugin.getMessage("Title");
        Component advertisement = plugin.getMessage("Bottom_Message");
        Scoreboard playerScoreboard = player.getScoreboard();
        if (!scoreboardVisible.getOrDefault(player, true)) return;

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
        PlayerQuestData questData = playerQuestDataMap.get(playerId);

        if (questData != null) {
            QuestData questDetails = quests.get(questData.getQuestName());
            if (questDetails == null) {
                plugin.getLogger().warning("Quest data for quest '" + questData.getQuestName() + "' not found. Skipping...");
                return;
            }

            objective.getScore(String.valueOf(scoreIndex)).customName(questDetails.displayName());

            int progress = questData.getProgress();
            objective.getScore(String.valueOf(scoreIndex + 1)).customName(mini.deserialize("<gray>ᴘʀᴏɢʀᴇss:</gray> " + progress + "/" + questDetails.goal()));
            scoreIndex += 2;

            Component rewardText = mini.deserialize("<gray>ʀᴇᴡᴀʀᴅ: </gray><color:#80ff88>"+ questDetails.reward() + "$</color>");
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
            Component weekendBonus = mini.deserialize("<color:#ffcc00>[ᴡᴇᴇᴋᴇɴᴅ ʙᴏɴᴜs!]</color>");
            objective.getScore(String.valueOf(scoreIndex)).customName(weekendBonus);
            scoreIndex++;
        }

        objective.getScore(String.valueOf(scoreIndex)).customName(Component.empty());
        objective.getScore(String.valueOf(scoreIndex + 1)).customName(advertisement);
    }

    public void createScoreboard(Player player) {
        if (scoreboardVisible.getOrDefault(player, true)){
            updateScores(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerId = player.getUniqueId().toString();

        playerQuestDataMap.computeIfAbsent(playerId, id -> {
            String quest = questNames.get(random.nextInt(questNames.size()));
            return new PlayerQuestData(quest);
        });

        scoreboardVisible.putIfAbsent(player, true);
        createScoreboard(player);
    }

    public void updateCurrentQuest(Player player, int completedCount) {
        String playerId = player.getUniqueId().toString();
        PlayerQuestData playerQuestData = playerQuestDataMap.get(playerId);

        if (playerQuestData == null) {
            plugin.getLogger().warning("No quest data found for player " + playerId);
            return;
        }

        String newQuest;
        do {
            newQuest = (String) quests.keySet().toArray()[random.nextInt(quests.size())];
        } while (newQuest.equals(playerQuestData.getQuestName()));

        playerQuestData.setProgress(0);
        playerQuestDataMap.put(playerId, new PlayerQuestData(newQuest));

        int completedQuests = getCompletedQuests(playerId) + completedCount;
        playerQuestCount.put(playerId, completedQuests);

        updateScores(player);
    }

    public void updateProgress(Player player, int amount) {
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
        if (optionalReward == null || optionalReward.isEmpty() || optionalReward.isBlank()) return;

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
        }
        else if (optionalReward.startsWith("command:")) {
            String command = optionalReward.substring(8);
            Bukkit.dispatchCommand(silentSender, command.replace("%player%", player.getName()));
        }
        else {
            plugin.getLogger().warning("Unknown optional reward type: " + optionalReward);
        }
    }

    private void giveItemReward(Player player, String itemName, int amount) {
        Material material = Material.matchMaterial(itemName.toUpperCase());
        if (material != null) {
            ItemStack itemStack = new ItemStack(material, amount);
            player.getInventory().addItem(itemStack);
        } else {
            plugin.getLogger().warning("Invalid reward item type: " + itemName);
        }
    }

    public boolean isScoreboardVisible(Player player) {
        return scoreboardVisible.getOrDefault(player, true);
    }

    public static int getMultiplier(int completedQuests) {
        long count = Arrays.stream(THRESHOLDS)
                .filter(threshold -> completedQuests >= threshold)
                .count();

        return (int) count + 1;
    }

    public int getCompletedQuests(String playerId) {
        return playerQuestCount.getOrDefault(playerId, 0);
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