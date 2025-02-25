package me.mogubea.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called whenever a player with the PROSPECTOR enchantment triggers a Veinmine that includes a block other than the original.
 */
public class PlayerVeinmineEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Block originalBlock;
    private final List<Block> blocks;
    private boolean cancelled ;

    public PlayerVeinmineEvent(@NotNull Player player, @NotNull Block original, @NotNull List<Block> blocks) {
        super(player);
        this.originalBlock = original;
        this.blocks = blocks;
        this.cancelled = false;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Block getOriginalBlock() {
        return originalBlock;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
