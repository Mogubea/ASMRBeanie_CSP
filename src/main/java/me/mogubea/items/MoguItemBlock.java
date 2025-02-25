package me.mogubea.items;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public abstract class MoguItemBlock extends MoguItem {

    protected MoguItemBlock(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull Material material) {
        super(manager, identifier, displayName, material);
    }

    /**
     * @param event The BlockPlaceEvent.
     * @return If the event is continuing.
     */
    public final boolean doBlockPlace(@NotNull BlockPlaceEvent event) {
        onBlockPlace(event);
        return !event.isCancelled();
    }

    protected abstract void onBlockPlace(@NotNull  BlockPlaceEvent event);

}
