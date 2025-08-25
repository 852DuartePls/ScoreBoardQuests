package me.duart.scoreBoardQuests.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.duart.scoreBoardQuests.ScoreBoardQuests;
import me.duart.scoreBoardQuests.manager.CustomScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duart.scoreBoardQuests.ScoreBoardQuests.mini;

@SuppressWarnings("UnstableApiUsage")
public class CommandsManager {

    private final ScoreBoardQuests plugin;
    private final CustomScoreboardManager scoreboardManager;
    private final String permission = "scoreboardquests.admin";

    public CommandsManager(ScoreBoardQuests plugin, CustomScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    public void registerCommands(final @NotNull ScoreBoardQuests plugin) {
        final LifecycleEventManager<@NotNull Plugin> lifecycleManager = plugin.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var brigadier = event.registrar();

            brigadier.register(Commands.literal("sbquests")
                    // Default: show version
                    .executes(ctx -> {
                        ctx.getSource().getSender().sendMessage(plugin.getMessage("Plugin_Prefix")
                                .append(mini.deserialize("<green> v" + this.plugin.getPluginMeta().getVersion() + "</green>")));
                        return Command.SINGLE_SUCCESS;
                    })

                    // ---- TOGGLE ----
                    .then(Commands.literal("toggle")
                            .executes(ctx -> {
                                CommandSender sender = ctx.getSource().getSender();
                                if (sender instanceof Player player) {
                                    scoreboardManager.toggleScoreboardVisibility(player);
                                } else {
                                    sender.sendRichMessage("<red>You must specify a player to toggle the scoreboard visibility!</red>");
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests(new OnlinePlayersArgument())
                                    .requires(src -> src.getSender().hasPermission(permission))
                                    .executes(ctx -> {
                                        CommandSender sender = ctx.getSource().getSender();
                                        Player target = Bukkit.getPlayer(StringArgumentType.getString(ctx, "player"));
                                        if (target != null) { scoreboardManager.toggleScoreboardVisibility(target); }
                                        else { sender.sendRichMessage("<red>Player not found!</red>"); }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )

                    // ---- RELOAD ----
                    .then(Commands.literal("reload")
                            .requires(src -> src.getSender().hasPermission(permission))
                            .executes(ctx -> {
                                CommandSender sender = ctx.getSource().getSender();
                                plugin.onReload();
                                Bukkit.getScheduler().runTaskLater(plugin, () ->
                                        sender.sendMessage(plugin.getMessage("Plugin_Prefix")
                                                .append(mini.deserialize("<green>Plugin reloaded!</green>"))), 20L);
                                return Command.SINGLE_SUCCESS;
                            })
                    )

                    // ---- PAY ----
                    .then(Commands.literal("pay")
                            .requires(src -> src.getSender().hasPermission(permission))
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests(new OnlinePlayersArgument())
                                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                            .then(Commands.argument("flag", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        builder.suggest("-s");
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> handlePay(ctx, true))
                                            )
                                            .executes(ctx -> handlePay(ctx, false))
                                    )
                            )
                    )

                    // ---- FORCECOMPLETE ----
                    .then(Commands.literal("forcecomplete")
                            .requires(src -> src.getSender().hasPermission(permission))
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests(new OnlinePlayersArgument())
                                    .then(Commands.argument("quantity", IntegerArgumentType.integer(1))
                                            .then(Commands.argument("flag", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        builder.suggest("-s");
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> handleForceComplete(ctx, true))
                                            )
                                            .executes(ctx -> handleForceComplete(ctx, false))
                                    )
                                    .executes(ctx -> handleForceComplete(ctx, false))
                            )
                    )

                    // ---- HELP ----
                    .then(Commands.literal("help")
                            .executes(ctx -> {
                                handleHelp(ctx.getSource().getSender());
                                return Command.SINGLE_SUCCESS;
                            })
                    ).build()
                    , null,
                    List.of("sbq")
            );
        });
    }

    /*
    I don't know why this was withdrawing from the player's money since it's not supposed to be a basic payment command
    and just a way to deposit money to the player's account if wanted to be used as a reward command in other plugins,
    but thankfully i caught that up lol
    */
    private int handlePay(@NotNull CommandContext<CommandSourceStack> ctx, boolean silent) {
        CommandSender sender = ctx.getSource().getSender();
        String targetName = StringArgumentType.getString(ctx, "player");
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) return 0;

        if (target == sender) {
            sender.sendMessage(plugin.getMessage("Plugin_Prefix").append(mini.deserialize("<red>You can't pay yourself!</red>")));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(ctx, "amount");

        // Deposit to the target
        int completed = scoreboardManager.getCompletedQuests(target.getUniqueId().toString());
        int multiplier = CustomScoreboardManager.getMultiplier(completed);
        if (plugin.isWeekend()) multiplier *= 2;
        double total = amount * multiplier;

        plugin.getEconomy().depositPlayer(target, total);

        if (!silent) {
            sender.sendMessage(plugin.getMessage("Plugin_Prefix")
                    .append(mini.deserialize("<green> Sent <yellow>" + amount + "</yellow> to <yellow>" + target.getName() + "</yellow></green>")));
            target.sendMessage(plugin.getMessage("Plugin_Prefix")
                    .append(mini.deserialize("<green> Received <yellow>" + total + "</yellow> (multiplier x" + multiplier + ").</green>")));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int handleForceComplete(@NotNull CommandContext<CommandSourceStack> ctx, boolean silent) {
        CommandSender sender = ctx.getSource().getSender();
        String targetName = StringArgumentType.getString(ctx, "player");
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) return 0;

        int completed = 1; // default
        try {
            completed = IntegerArgumentType.getInteger(ctx, "quantity");
            if (completed < 0) completed = 1;
        } catch (IllegalArgumentException ignored) {
            // quantity argument not provided, keep default 1
        }

        scoreboardManager.updateCurrentQuest(target, completed);

        if (!silent) {
            sender.sendMessage(plugin.getMessage("Plugin_Prefix")
                    .append(mini.deserialize("<white> Completed <gold>" + completed + "</gold> quests for <green>" + target.getName() + "</green>.</white>")));
            target.sendMessage(plugin.getMessage("Plugin_Prefix")
                    .append(mini.deserialize("<white> You completed <gold>" + completed + "</gold> quests!</white>")));
        }

        return Command.SINGLE_SUCCESS;
    }

    private void handleHelp(@NotNull CommandSender sender) {
        sender.sendRichMessage("""
                <dark_aqua><st>=================</st>[ Commands ]<st>=================</st></dark_aqua>
                <aqua><dark_gray><b> | </b><yellow>Alias: /sbq</yellow><b> |      | </b><yellow>Optional: -s (silent)</yellow> <b>|</b></dark_gray>
                
                ┏ /sbq reload:
                ┗ <gold>Reloads messages.</gold>
                
                ┏ /sbq pay <player> <amount> [-s]:
                ┗ <gold>Pays a player.</gold>
                
                ┏ /sbq forcecomplete <player> [amount] [-s]:
                ┗ <gold>Forces quest completion.</gold>
                
                ┏ /sbq toggle [player]:
                ┗ <gold>Toggles scoreboard visibility.</gold>
                
                <dark_aqua><st>=============================================</st></dark_aqua>
                """);
    }
}
