package me.duart.scoreBoardQuests;

import it.sauronsoftware.cron4j.Scheduler;
import me.duart.scoreBoardQuests.commands.CommandsManager;
import me.duart.scoreBoardQuests.manager.CustomScoreboardManager;
import me.duart.scoreBoardQuests.manager.QuestsHandler;
import me.duart.scoreBoardQuests.util.ResetHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class ScoreBoardQuests extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("ScoreBoardQuests");
    public static final MiniMessage mini = MiniMessage.miniMessage();

    private final Map<String, Component> messages = new HashMap<>();

    private static final String DEFAULT_TITLE = "<b><color:#ff5d05>Quests</color></b>";
    private static final String DEFAULT_PREFIX = "<gray>[<blue>ScoreBoardQuests</blue>]</gray>";
    private static final String DEFAULT_BOTTOM = "<color:#EC6EAD>example.server.net</color>";

    private Economy economy;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            logger.severe("Couldn't setup economy, disabling plugin...");
            return;
        }
        saveDefaultConfig();
        loadMessagesFromConfig(this);
        CustomScoreboardManager scoreboardQuests = new CustomScoreboardManager(this);
        QuestsHandler questsHandler = new QuestsHandler(scoreboardQuests, this);
        ResetHandler resetHandler = new ResetHandler(this, scoreboardQuests);

        getServer().getPluginManager().registerEvents(scoreboardQuests, this);
        getServer().getPluginManager().registerEvents(questsHandler, this);

        Scheduler cronj4Scheduler = new Scheduler();
        cronj4Scheduler.schedule("0 0 * * *", () -> // Triggers every day at midnight (Local Time)
                Bukkit.getScheduler().runTask(this, () ->
                        resetHandler.handleReset("--Daily Reset--")));

        cronj4Scheduler.start();

        CommandsManager commandsManager = new CommandsManager(this, scoreboardQuests);
        commandsManager.registerCommands(this);

        logger.info("Plugin Enabled");
        logger.info("Today is " + LocalDate.now().getDayOfWeek().name() + (isWeekend() ? ". Weekend multiplier enabled." : "."));
    }

    public void onReload() {
        reloadConfig();
        loadMessagesFromConfig(this);
    }

    private boolean setupEconomy() {
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException e) {
            getLogger().severe("Vault Economy class not found! Please ensure that the Vault plugin is installed and loaded.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault is present, but no Economy plugin was found, please ensure to install one such as EssentialsX, LiteEco, TheNewEconomy, or others.");
            return false;
        }

        economy = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public boolean isWeekend() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public void loadMessagesFromConfig(@NotNull JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        putMessage("Title", config.getString("Messages.Title", DEFAULT_TITLE));
        putMessage("Plugin_Prefix", config.getString("Messages.Plugin_Prefix", DEFAULT_PREFIX));
        putMessage("Bottom_Message", config.getString("Messages.Bottom_Message", DEFAULT_BOTTOM));
    }

    private void putMessage(String key, String message) {
        messages.put(key, mini.deserialize(message));
    }

    public Component getMessage(String key) {
        return messages.getOrDefault(key, Component.text("Message not found!", NamedTextColor.RED));
    }
}
