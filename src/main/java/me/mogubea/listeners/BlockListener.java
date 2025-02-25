package me.mogubea.listeners;

import me.mogubea.items.MoguItem;
import me.mogubea.items.MoguItemBlock;
import me.mogubea.profile.MoguProfile;
import me.mogubea.statistics.SimpleStatType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlockListener extends EventListener {

    protected BlockListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        String blockIdentifier = e.getBlock().getType().name();
        MoguItem moguItem = plugin.getItemManager().from(e.getItemInHand());
        if (moguItem instanceof MoguItemBlock moguItemBlock) {
            if (!moguItemBlock.doBlockPlace(e)) return;
            blockIdentifier = moguItemBlock.getIdentifier();
        }

        MoguProfile.from(e.getPlayer()).addToStat(SimpleStatType.BLOCK_PLACE, blockIdentifier, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        MoguProfile profile = MoguProfile.from(e.getPlayer());
        String blockIdentifier = e.getBlock().getType().name();

        profile.addToStat(SimpleStatType.BLOCK_BREAK, blockIdentifier, 1);
        if (isBlockNatural(e.getBlock()))
            profile.addToStat(SimpleStatType.NATURAL_BLOCK_BREAK, blockIdentifier, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreakLow(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if (e.getBlock().getType() == Material.VAULT || e.getBlock().getType() == Material.TRIAL_SPAWNER) {
            if (isBlockNatural(e.getBlock()))
                e.setCancelled(true);
        } else if ((e.getBlock().getState() instanceof Container container && container instanceof Lootable && isBlockNatural(e.getBlock()))) {
            if (container instanceof ShulkerBox box) {
                e.setDropItems(false);
            }
            e.setExpToDrop(40);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeafDecay(LeavesDecayEvent e) {
        List<Block> toDecay = new ArrayList<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            scanNearbyLeaves(toDecay, e.getBlock());

            plugin.getServer().getScheduler().runTaskTimer(plugin, (task) -> {
                if (toDecay.isEmpty()) {
                    task.cancel();
                } else {
                    int index = toDecay.size() == 1 ? 0 : plugin.getRandom().nextInt(toDecay.size());
                    Block block = toDecay.get(index);
                    block.breakNaturally(false);
                    toDecay.remove(index);
                }
            }, 1L, 1L);
        });
    }

    private void scanNearbyLeaves(List<Block> set, Block block) {
        int checks = 0;

        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++)
                for (int z = -1; z <= 1; z++) {
                    Block nearbyBlock = block.getRelative(x, y, z);
                    if (!(nearbyBlock.getBlockData() instanceof Leaves leaves) || leaves.isPersistent() || leaves.getDistance() < leaves.getMaximumDistance()) continue;
                    set.add(nearbyBlock);

                    leaves.setPersistent(true);
                    nearbyBlock.setBlockData(leaves);

                    if (++checks >= 100 /* Maximum amount of leaves checked before stopping the recurrence */)
                        break;

                    scanNearbyLeaves(set, nearbyBlock);
                }
    }

}
