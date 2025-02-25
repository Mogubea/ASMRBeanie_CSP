package me.mogubea.main;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.mogubea.listeners.EventListener;
import me.mogubea.listeners.ListenerManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InfinityCaveLogic extends EventListener {

    private final short MIN_DEPTH = -32;
    private final int INFINITY_LAYER = -62;
    private final short MAX_DEPTH = -192;

    private final Biome BIOME_MOLTEN_CAVES, BIOME_FROZEN_CAVES;

    private final Random rand;

    private final NamespacedKey BBS_KEY = Main.key("DEEP_MINING_PENALTY");
    private final Map<Player, Miner> deepMiners = new HashMap<>();
    private final List<Entity> undergroundCreatures = new ArrayList<>();

    public InfinityCaveLogic(@NotNull ListenerManager manager) {
        super(manager);
        this.rand = plugin.getRandom();

        var reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        BIOME_MOLTEN_CAVES = reg.get(new NamespacedKey("infinity_cave", "molten_caves"));
        BIOME_FROZEN_CAVES = reg.get(new NamespacedKey("infinity_cave", "frozen_caves"));

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkPositions, 20L, 20L);
    }

    private void tick(Player player) {
        Miner miner = deepMiners.get(player);

        // Only fire if Y depth has changed since last tick
        if (miner.getOldY() != y(player))
            doPenalties(player);

        // Do Biome stuff
        doBiomeEffects(miner);

        // Spawn Entities
        doEntitySpawning(miner);

        // Update current depth for the next tick to compare
        miner.update(player);
    }

    private void doEntitySpawning(@NotNull Miner miner) {


    }

    private Location findSpawnLocation(Location playerLocation) {
        int MIN_RADIUS = 32; // Safety range
        int MAX_RADIUS = 80; // Maximum spawn distance
        int MAX_Y_CHECK = 16;

        Location location = null;
        while (location == null) {
            int xCheck = rand.nextInt(MAX_RADIUS * 2) - MAX_RADIUS;
            if (Math.abs(xCheck) < MIN_RADIUS) xCheck = xCheck < 0 ? -MIN_RADIUS : MIN_RADIUS;
            int zCheck = rand.nextInt(MAX_RADIUS * 2) - MAX_RADIUS;
            if (Math.abs(zCheck) < MIN_RADIUS) zCheck = zCheck < 0 ? -MIN_RADIUS : MIN_RADIUS;

            Block blockCheck = playerLocation.getBlock().getRelative(xCheck, - MAX_Y_CHECK + rand.nextInt(MAX_Y_CHECK * 2), zCheck);
            Block solidBlock = null;
            int remainingChecks = MAX_Y_CHECK * 2;

            // Locate an air pocket
            while (!blockCheck.getType().isAir() && (remainingChecks-=2) > 0) {
                solidBlock = blockCheck; // If not air, update the last solid block
                blockCheck = blockCheck.getRelative(0, -2, 0);
            }

            // Restart if failed
            if (!blockCheck.getType().isAir()) continue;

            // Find ground location from air pocket
            // If our solid block is above our air pocket, or null, loop down until we find a floor.
            if (solidBlock == null || solidBlock.getY() > blockCheck.getY()) {
                while (!blockCheck.isSolid())
                    blockCheck = blockCheck.getRelative(0 , -1, 0);
                location = blockCheck.getLocation();
            } else {
                location = solidBlock.getLocation();
            }

            blockCheck = location.getBlock();

            if (!blockCheck.getRelative(1, 0, 0).getType().isAir() &&
                    !blockCheck.getRelative(-1, 0, 0).getType().isAir() &&
                    !blockCheck.getRelative(0, 0, 1).getType().isAir() &&
                    !blockCheck.getRelative(0, 0, -1).getType().isAir()) {
                location = null; // Skip this spawn, as it's too enclosed
            }
        }

        return location;
    }

    private void doBiomeEffects(@NotNull Miner miner) {
        if (miner.getBiome() != miner.getOldBiome())
            onChangeBiome(miner);

        if (miner.getBiome() == BIOME_FROZEN_CAVES) {

            if (!miner.getPlayer().isFrozen() && miner.getPlayer().getFreezeTicks() < miner.getPlayer().getMaxFreezeTicks())
                miner.getPlayer().setFreezeTicks(miner.getPlayer().getFreezeTicks() + 2);
        }
    }

    private void onChangeBiome(@NotNull Miner miner) {
        if (miner.getOldBiome() == BIOME_FROZEN_CAVES) {
            miner.getPlayer().lockFreezeTicks(false);
        } else if (miner.getBiome() == BIOME_FROZEN_CAVES) {
            miner.getPlayer().lockFreezeTicks(true);
        }
    }

    private void doPenalties(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).removeModifier(BBS_KEY);
        Objects.requireNonNull(player.getAttribute(Attribute.MINING_EFFICIENCY)).removeModifier(BBS_KEY);

        double penalty_bbs = Math.max(-0.95, depth(player) * -0.0085);
        double penalty_efficiency = Math.max(-0.95, depth(player) * -0.012);
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).addTransientModifier(new AttributeModifier(BBS_KEY, penalty_bbs, AttributeModifier.Operation.ADD_SCALAR));
        Objects.requireNonNull(player.getAttribute(Attribute.MINING_EFFICIENCY)).addTransientModifier(new AttributeModifier(BBS_KEY, penalty_efficiency, AttributeModifier.Operation.ADD_SCALAR));
    }

    private void onLeave(Player player) {
        deepMiners.remove(player);
        player.lockFreezeTicks(false);
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).removeModifier(BBS_KEY);
        Objects.requireNonNull(player.getAttribute(Attribute.MINING_EFFICIENCY)).removeModifier(BBS_KEY);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        deepMiners.remove(event.getPlayer());
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        DamageType type = event.getDamageSource().getDamageType();
        if (type != DamageType.ON_FIRE && type != DamageType.IN_FIRE) return;
        if (event.getEntity().getLocation().getBlock().getBiome() != BIOME_MOLTEN_CAVES) return;

        event.setDamage(event.getDamage() * 3);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!isDeepMining(event.getPlayer())) return;
        if (event.getDamage() <= 0) return;

        int bonusDmgMult = (depth(event.getPlayer()) * 3); // -32 would be 0%, -62 would be 90%, -92 would be 180%
        int totalDmg = event.getDamage() * (Math.floorDiv(bonusDmgMult, 100) + ((plugin.getRandom().nextInt(100) < (bonusDmgMult % 100)) ? 1 : 0));

        event.setDamage(totalDmg);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMineBlock(BlockBreakEvent event) {
        if (!isBlockNatural(event.getBlock())) return;
        if (!isDeepMining(event.getPlayer())) return;
        if (event.getExpToDrop() <= 0) return;
        double bonusAndMult = (1D + (depth(event.getPlayer()) / 30D)); // Every 30 blocks deeper increments the minimum drop AND multiplier by 1.
        double xp = Math.max(event.getExpToDrop(), bonusAndMult) * bonusAndMult;

        event.setExpToDrop((int) xp);

        if (rand.nextInt(10) == 0)
            event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation().add(0.5, 0.05, 0.5), EntityType.SILVERFISH, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!isDeepMining(event.getPlayer())) return;
        if (event.getItems().isEmpty()) return;
        if (event.getBlockState().getBlock().getBiome() != BIOME_MOLTEN_CAVES) return;
        event.getItems().forEach(item -> item.setItemStack(plugin.getRecipeManager().getSmeltedVersion(item.getItemStack())));
    }

    private void checkPositions() {
        plugin.getServer().getOnlinePlayers().forEach((player) -> {
            if (isDeepMining(player)) {
                if (y(player) > MIN_DEPTH)
                    onLeave(player);
            } else if (y(player) <= MIN_DEPTH) {
                deepMiners.put(player, new Miner(player));
                doPenalties(player);
            }
        });

        deepMiners.forEach((player, miner) -> tick(player));
    }

    private int y(Player player) {
        return player.getLocation().getBlockY();
    }

    private int depth(Player player) {
        return -y(player) + MIN_DEPTH;
    }

    private boolean isDeepMining(Player player) {
        return deepMiners.containsKey(player);
    }

    private static class Miner {
        private Player player;
        private Biome biome;
        private int y;

        private Miner(Player player) {
            update(player);
        }

        private void update(Player player) {
            this.player = player;
            this.biome = getBiome();
            this.y = getY();
        }

        private Biome getOldBiome() {
            return biome;
        }

        private Biome getBiome() {
            return player.getLocation().getBlock().getBiome();
        }

        private Player getPlayer() {
            return player;
        }

        private int getOldY() {
            return y;
        }

        private int getY() {
            return player.getLocation().getBlockY();
        }

    }

}
