package me.mogubea.events;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

public class WorldDayChangeEvent extends WorldEvent {
    private static final HandlerList handlers = new HandlerList();

    private final World world;
    private final long previousTime;
    private final long newTime;

    public WorldDayChangeEvent(@NotNull World world, long previousTime, long newTime) {
        super(world, true);
        this.world = world;
        this.previousTime = previousTime;
        this.newTime = newTime;
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
