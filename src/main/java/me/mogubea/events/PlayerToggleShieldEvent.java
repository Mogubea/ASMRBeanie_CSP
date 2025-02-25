package me.mogubea.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerToggleShieldEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerToggleShieldEvent(@NotNull Player player) {
        super(player);
    }

    public boolean isBlocking() {
        return player.isBlocking();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
