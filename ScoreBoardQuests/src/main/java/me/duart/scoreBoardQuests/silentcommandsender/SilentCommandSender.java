package me.duart.scoreBoardQuests.silentcommandsender;

import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class SilentCommandSender implements ConsoleCommandSender {
    @Override
    public void sendMessage(@NotNull String message) {}

    @Override
    public void sendMessage(@NotNull String[] messages) {}

    @Override
    public void sendMessage(UUID sender, @NotNull String message) {}

    @Override
    public void sendMessage(UUID sender, @NotNull String[] messages) {}
}
