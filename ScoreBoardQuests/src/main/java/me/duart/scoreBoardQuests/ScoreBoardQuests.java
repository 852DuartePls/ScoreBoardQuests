package me.duart.scoreBoardQuests;

import me.duart.scoreBoardQuests.commands.CommandsManager;
import me.duart.scoreBoardQuests.manager.CustomScoreboardManager;
import me.duart.scoreBoardQuests.manager.MultiplierHandler;
import me.duart.scoreBoardQuests.manager.QuestsHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class ScoreBoardQuests extends JavaPlugin {

    private final Logger logger = getLogger();
    private Economy economy;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, Component> messages = new HashMap<>();
    private final MultiplierHandler multiplierHandler = new MultiplierHandler(this);
    private static final String DEFAULT_TITLE = "<b><color:#ff5d05>Quests</color></b>";
    private static final String DEFAULT_PREFIX = "<gray>[<blue>ScoreBoardQuests</blue>]</gray>";
    private static final String DEFAULT_BOTTOM = "<color:#EC6EAD>example.server.net</color>";

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            logger.severe("Vault plugin not found. Disabling plugin...");
            return;
        } else {
            logger.info("Vault plugin found. Hooking into it...");
        }
        saveDefaultConfig();
        loadMessagesFromConfig(this);
        CustomScoreboardManager scoreboardQuests = new CustomScoreboardManager(this);
        QuestsHandler questsHandler = new QuestsHandler(scoreboardQuests, multiplierHandler, this);
        getServer().getPluginManager().registerEvents(scoreboardQuests, this);
        getServer().getPluginManager().registerEvents(questsHandler, this);
        PluginCommand command = getCommand("sbquests");
        if (command != null) {
            command.setExecutor(new CommandsManager(this, multiplierHandler, scoreboardQuests));
            command.setTabCompleter(new CommandsManager(this, multiplierHandler, scoreboardQuests));
        }
        logger.info("Plugin Enabled");

        logger.info("Today is " + LocalDate.now().getDayOfWeek().name() + (isWeekend() ? ". Weekend multiplier enabled." : "."));
    }

    public void onReload() {
        reloadConfig();
        loadMessagesFromConfig(this);
    }

    public void loadMessagesFromConfig(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        putMessage("Title", config.getString("Messages.Title", DEFAULT_TITLE));
        putMessage("Plugin_Prefix", config.getString("Messages.Plugin_Prefix", DEFAULT_PREFIX));
        putMessage("Bottom_Message", config.getString("Messages.Bottom_Message", DEFAULT_BOTTOM));
    }

    private void putMessage(String key, String message) {
        messages.put(key, miniMessage.deserialize(message));
    }

    public Component getMessage(String key) {
        return messages.getOrDefault(key, miniMessage.deserialize("<red>Message not found!</red>"));
    }

    private boolean setupEconomy() {
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException e) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            return true;
        }
        return false;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isWeekend() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}