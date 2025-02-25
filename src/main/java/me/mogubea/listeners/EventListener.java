package me.mogubea.listeners;

import me.mogubea.items.MoguItem;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import me.mogubea.statistics.SimpleStatType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Random;

public abstract class EventListener implements Listener {

    protected final @NotNull ListenerManager manager;
    protected final @NotNull DecimalFormat dFormat;
    protected final @NotNull Random random;
    protected final @NotNull Main plugin;

    protected EventListener(@NotNull ListenerManager manager) {
        this.manager = manager;
        this.plugin = manager.getPlugin();
        this.random = plugin.getRandom();
        this.dFormat = manager.getDecimalFormat();
    }

    /**
     * @param block The block being checked whether it is natural or not.
     * @return Whether the block is natural or not.
     */
    protected boolean isBlockNatural(Block block) {
        return plugin.getBlockTracker().isBlockNatural(block);
    }

    protected long getBlockKey(Block block) {
        return plugin.getBlockTracker().getBlockKey(block);
    }

    protected ItemStack formatItem(ItemStack itemStack) {
        return plugin.getItemManager().formatItemStack(itemStack);
    }

    protected @Nullable MoguItem getMoguItem(@NotNull ItemStack itemStack) {
        return plugin.getItemManager().from(itemStack);
    }

    protected void addToStat(@NotNull Player player, @NotNull SimpleStatType type, @NotNull String subStat, int value) {
        MoguProfile.from(player).addToStat(type, subStat, value);
    }

    protected <E extends Event> boolean doMoguItemEvents(ItemStack itemStack, @NotNull E event) {
        return plugin.getItemManager().doItemEvent(itemStack, event);
    }

}
