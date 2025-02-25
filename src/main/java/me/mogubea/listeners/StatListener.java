package me.mogubea.listeners;

import me.mogubea.statistics.SimpleStatType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.NotNull;

public class StatListener extends EventListener {

    protected StatListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCropHarvest(PlayerHarvestBlockEvent e) {
        if (e.getItemsHarvested().isEmpty() || plugin.getItemManager().getReplenishableBlockSeed(e.getHarvestedBlock().getType()).isAir()) return;
        addToStat(e.getPlayer(), SimpleStatType.CROP_HARVEST, e.getHarvestedBlock().getType().name(), 1);
    }

}
