package me.duart.scoreBoardQuests.commands;

import me.duart.scoreBoardQuests.ScoreBoardQuests;
import me.duart.scoreBoardQuests.manager.CustomScoreboardManager;
import me.duart.scoreBoardQuests.manager.MultiplierHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandsManager implements CommandExecutor, TabCompleter {

    private final ScoreBoardQuests plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Economy economy;
    private final CustomScoreboardManager scoreboardManager;
    private final MultiplierHandler multiplierHandler;

    public CommandsManager(@NotNull ScoreBoardQuests plugin, MultiplierHandler multiplierHandler, CustomScoreboardManager scoreboardManager ) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
        this.multiplierHandler = multiplierHandler;
        this.economy = plugin.getEconomy();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        String pluginVersion = plugin.getPluginMeta().getVersion();
        var pluginVersionComponent = mini.deserialize("<green> v" + pluginVersion + "</green>");
        var pluginPrefix = plugin.getMessage("Plugin_Prefix");
        var noPermission = mini.deserialize("<red> You do not have permission to use this command.</red>");
        var missingArgsPayment = mini.deserialize("<red> Please specify a player and an amount to pay.</red>");
        var missingArgsForceComplete = mini.deserialize("<red> Usage /scoreboardquests forcecomplete <player>");
        var playerNotFound = MiniMessage.miniMessage().deserialize("<red> Player not found!</red>");
        var invalidAmount = MiniMessage.miniMessage().deserialize("<red> Invalid amount!</red>");
        var notEnoughBalance = MiniMessage.miniMessage().deserialize("<red> Not enough balance to complete this transaction!</red>");

        if (args.length == 0) {
            sender.sendMessage(pluginPrefix.append(pluginVersionComponent));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "toggle" -> handleToggleCommand(sender, pluginPrefix, noPermission, args);
            case "reload" -> handleReloadCommand(sender);
            case "pay" -> handlePaymentCommand(sender, missingArgsPayment, pluginPrefix, noPermission, playerNotFound, invalidAmount, notEnoughBalance, args);
            case "forcecomplete" -> handleForceCompleteCommand(sender, pluginPrefix, noPermission, playerNotFound, missingArgsForceComplete, args);
            case "help" -> handleHelpCommand(sender, pluginPrefix, pluginVersionComponent, args);
            default -> false;
        };
    }

    private boolean handleReloadCommand(@NotNull CommandSender sender) {
        plugin.onReload();
        var PluginPrefix = plugin.getMessage("Plugin_Prefix");
        sender.sendMessage(PluginPrefix.append(mini.deserialize("<reset><green> Plugin reloaded!</green>")));
        return false;
    }

    private boolean handlePaymentCommand(CommandSender sender, Component missingArgsPayment, Component pluginPrefix, Component noPermission, Component playerNotFound, Component invalidAmount, Component notEnoughBalance, String @NotNull [] args) {
        boolean silentMode = false;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-s")) {
                silentMode = true;
                break;
            }
        }

        if (!sender.hasPermission("scoreboardquests.pay")) {
            sender.sendMessage(pluginPrefix.append(noPermission));
            return false;
        }
        if (args.length < 3) {
            sender.sendMessage(pluginPrefix.append(missingArgsPayment));
            return false;
        }

        Player targetPlayer = getTargetPlayer(sender, args);
        if (targetPlayer == null) {
            sender.sendMessage(pluginPrefix.append(playerNotFound));
            return false;
        }

        double balance;
        try {
            balance = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(pluginPrefix.append(invalidAmount));
            return false;
        }

        OfflinePlayer senderPlayer = (sender instanceof Player) ? (Player) sender : Bukkit.getOfflinePlayer(sender.getName());

        if (!(sender instanceof ConsoleCommandSender) && !economy.has(senderPlayer, balance)) {
            sender.sendMessage(pluginPrefix.append(notEnoughBalance));
            return false;
        }

        int completedQuests = scoreboardManager.getCompletedQuests(targetPlayer.getUniqueId().toString());
        int totalMultiplier = multiplierHandler.getTotalMultiplier(targetPlayer, completedQuests);
        double totalAmount = balance * totalMultiplier;

        economy.withdrawPlayer(senderPlayer, balance);
        economy.depositPlayer(targetPlayer, totalAmount);

        if (!silentMode) {
            sender.sendMessage(pluginPrefix.append(mini.deserialize("<green> Sent <yellow>" + balance + "</yellow> to " + targetPlayer.getName() + "</green>")));
            targetPlayer.sendMessage(pluginPrefix.append(mini.deserialize("<green> You have completed <dark_green>" + completedQuests + "</dark_green> quests and have a total multiplier of <gold>" + totalMultiplier + "</gold></green>")));
            targetPlayer.sendMessage(pluginPrefix.append(mini.deserialize("<green> Your total amount received is <yellow>" + totalAmount + "</yellow></green>")));
        }

        return true;
    }

    private boolean handleForceCompleteCommand(CommandSender sender, Component pluginPrefix, Component noPermission, Component playerNotFound, Component missingArgsForceComplete, String @NotNull [] args) {
        boolean silentMode = false;
        int numQuests = 1;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-s")) {
                silentMode = true;
                break;
            }
        }

        if (!sender.hasPermission("scoreboardquests.admin")) {
            sender.sendMessage(pluginPrefix.append(noPermission));
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(pluginPrefix.append(missingArgsForceComplete));
            return false;
        }

        Player targetPlayer = getTargetPlayer(sender, args);
        if (targetPlayer == null) {
            sender.sendMessage(pluginPrefix.append(playerNotFound));
            return false;
        }

        if (args.length >= 3) {
            try {
                numQuests = Integer.parseInt(args[2]);
                if (numQuests <= 0) {
                    numQuests = 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        scoreboardManager.updateCurrentQuest(targetPlayer, numQuests);

        if (!silentMode) {
            sender.sendMessage(pluginPrefix.append(Component.text(" "))
                    .append(MiniMessage.miniMessage().deserialize("<white>Completed: <gold>" + numQuests + "</gold> quests! for the player: </white>"))
                    .append(Component.text(targetPlayer.getName()).color(NamedTextColor.GREEN)));

            targetPlayer.sendMessage(pluginPrefix.append(Component.text(" "))
                    .append(MiniMessage.miniMessage().deserialize("<white>You have just completed:<gold> " + numQuests + "</gold> quests!</white>")));
        }
        return true;
    }

    private boolean handleHelpCommand(@NotNull CommandSender sender, Component pluginPrefix, Component pluginVersionComponent, String[] args) {
        if (!sender.hasPermission("scoreboardquests.admin") || !sender.hasPermission("scoreboardquests.pay")) {
            sender.sendMessage(pluginPrefix.append(pluginVersionComponent));
            return false;
        }

        if (args.length == 1) {
            sender.sendMessage(mini.deserialize("""
        <dark_aqua><st>=================</st>[ Commands ]<st>=================</st></dark_aqua>
        <aqua><aqua><dark_gray><b> | </b><yellow><aqua>Alias:</aqua> /sbq</yellow><b> |      | </b><yellow><aqua>Optional:</aqua> -s (Disables Output)</yellow> <b>|</b></dark_gray>
        
        ┏ /sbquests reload:
        ┗<gold> Reloads the plugin messages.</gold>
        
        ┏ /sbquests pay <player> <amount> "-s":
        ┗<gold> Processes a player's payment.</gold>
        
        ┏ /sbquests forcecomplete <dark_gray><player> <amount></dark_gray> "-s":
        ┗<gold> Completes the current quest for the player and optionally adds the specified amount to the streak.</gold>
        
        ┏ /sbquests toggle <dark_gray><player></dark_gray>:
        ┗<gold> Toggles the visibility of the quests scoreboard.</gold>
        </aqua>
        <dark_aqua><st>=============================================</st></dark_aqua>
        """));
        }
        return true;
    };

    private boolean handleToggleCommand(CommandSender sender, Component pluginPrefix, Component playerNotFound, String @NotNull [] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player targetPlayer)) {
                sender.sendMessage(mini.deserialize("<red>You must be a player to toggle your own scoreboard visibility!</red>"));
                return false;
            }

            scoreboardManager.toggleScoreboardVisibility(targetPlayer);
            return true;
        }

        if (args.length == 2) {
            if (!sender.hasPermission("scoreboardquests.admin")) {
                sender.sendMessage(mini.deserialize("<red>You do not have permission to toggle another player's scoreboard visibility.</red>"));
                return false;
            }

            Player targetPlayer = getTargetPlayer(sender, args);
            if (targetPlayer == null) {
                sender.sendMessage(pluginPrefix.append(playerNotFound));
                return false;
            }

            scoreboardManager.toggleScoreboardVisibility(targetPlayer);
            return true;
        }

        return false;
    }

    private @Nullable Player getTargetPlayer(CommandSender sender, String @NotNull [] args) {
        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(mini.deserialize("<red>Player " + args[1] + " not found!</red>"));
            }
            return target;
        }
        if (sender instanceof Player) return (Player) sender;
        else {
            sender.sendMessage(mini.deserialize("<red>You must specify a player when running this command from the console!</red>"));
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("toggle");
            if (sender.hasPermission("scoreboardquests.pay")) {
                completions.add("pay");
                completions.add("help");
            }
            if (sender.hasPermission("scoreboardquests.admin")) {
                completions.add("reload");
                completions.add("pay");
                completions.add("forcecomplete");
                completions.add("help");
            }
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("pay") && sender.hasPermission("scoreboardquests.pay") || sender.hasPermission("scoreboardquests.admin")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("forcecomplete") && sender.hasPermission("scoreboardquests.admin")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
        }
        return completions;
    }
}

