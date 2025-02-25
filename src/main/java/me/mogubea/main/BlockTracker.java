package me.mogubea.main;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class specifically designed to track if a block has been placed by a player as efficiently as possible
 * 16 Bits are allocated to Y co-ordinate.
 * 24 Bits are allocated to X co-ordinate.
 * 24 Bits are allocated to Z co-ordinate.
 */
public class BlockTracker implements Listener {

    private final Main plugin;
    private final Map<World, LongSet> trackedBlocks = new HashMap<>();

    protected BlockTracker(Main plugin) {
        this.plugin = plugin;
        load();
    }

    /**
     * Check if this block is being tracked, and therefore has been placed before.
     * @return true or false
     */
    public boolean isBlockNatural(Block block) {
        return !trackedBlocks.get(block.getWorld()).contains(getBlockKey(block));
    }

    /**
     * Start tracking this block.
     */
    public void trackBlock(Block block) {
        trackedBlocks.get(block.getWorld()).add(getBlockKey(block));
    }

    /**
     * Stop tracking this block.
     */
    public void untrackBlock(Block block) {
        trackedBlocks.get(block.getWorld()).remove(getBlockKey(block));
    }

    protected void save() {
        File trackingFile = getTrackingFile();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(trackingFile);
        trackedBlocks.forEach((world, set) -> cfg.set("worlds." + world.getUID() + ".blocks", set.toLongArray()));
        try {
            cfg.save(trackingFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the list of block keys that have been saved previously
     */
    private void load() {
        File trackingFile = getTrackingFile();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(trackingFile);
        plugin.getServer().getWorlds().forEach(world -> trackedBlocks.put(world, new LongOpenHashSet(cfg.getLongList("worlds." + world.getUID() + ".blocks"))));
    }

    /**
     * Get the block key for the provided block, this is unique for every single block at any position
     * @return a long key.
     */
    public long getBlockKey(Block block) {
        long yBits = block.getY() & 0xFFFL; // 16
        long xBits = block.getX() & 0xFFFFFFL; // 24
        long zBits = block.getZ() & 0xFFFFFFL; // 24, each F represents 4 bits
        return (yBits << 48) | (xBits << 24) | (zBits);
    }

    private File getTrackingFile() {
        File trackingFile = new File(plugin.getDataFolder() + "/PlacedBlocks.yml");
        if (!trackingFile.exists()) {
            try {
                if (trackingFile.createNewFile())
                    plugin.getSLF4JLogger().info("Created PlacedBlocks.yml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return trackingFile;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        trackBlock(e.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        untrackBlock(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakBlock(BlockBreakBlockEvent e) {
        untrackBlock(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakNaturally(BlockDestroyEvent e) {
        untrackBlock(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        for (int i = -1, n = e.blockList().size(); ++i < n;)
            untrackBlock(e.blockList().get(i));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (int i = -1, n = e.blockList().size(); ++i < n;)
            untrackBlock(e.blockList().get(i));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockMove(BlockPistonRetractEvent e) {
        doPistonUpdateTracking(e.getBlocks(), e.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockMove(BlockPistonExtendEvent e) {
        doPistonUpdateTracking(e.getBlocks(), e.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e) {
        // In order to prevent the creation of stone grinders, we will track the creation of all formed stones.
        switch (e.getBlock().getType()) {
            case COBBLESTONE, COBBLED_DEEPSLATE, DEEPSLATE, STONE -> trackBlock(e.getBlock());
        }

    }

    private void doPistonUpdateTracking(List<Block> blocks, BlockFace direction) {
        // All blocks that are moved by pistons become unnatural to prevent things like piston wood and stone grinders.
        for (int i = -1, n = blocks.size(); ++i < n;) {
            Block block = blocks.get(i);

            untrackBlock(block); // Un-track the old position of the block
            trackBlock(block.getLocation().add(direction.getDirection()).getBlock()); // Re-apply non-natural marker at the new position of the block
        }
    }

    /**
     * The maximum vertical world size allowed
     */
    public static int getMaximumVertical() {
        return 0xFFFF - 1;
    }

    /**
     * The maximum horizontal world size allowed
     */
    public static int getMaximumHorizontal() {
        return 0xFFFFFF - 1;
    }

}