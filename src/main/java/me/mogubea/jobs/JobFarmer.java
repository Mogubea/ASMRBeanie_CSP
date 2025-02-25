package me.mogubea.jobs;

import io.papermc.paper.event.block.PlayerShearBlockEvent;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class JobFarmer extends Job {

    private final @NotNull Component prefix = Component.text("harvesting ", NamedTextColor.GRAY);
    private final @NotNull Map<@NotNull Material, @NotNull FarmerReward> jobPayouts = new HashMap<>();
    private final @NotNull Map<@NotNull Material, @NotNull Material> similar = new HashMap<>();

    protected JobFarmer(@NotNull JobManager manager) {
        super(manager, "Farmer", "\ud83d\udd31", TextColor.color(0x6fdf3f), "farmer", "farm");
        setDescription(Component.text("Earn $$$ by tending to a variety of crops!"));

        // These blocks care about the Age-able Data Tag, but not about Natural Tag.
        makeCropPayout(Material.WHEAT, 5, 8);
        makeCropPayout(Material.CARROTS, 5, 8);
        makeCropPayout(Material.POTATOES, 5, 8);
        makeCropPayout(Material.COCOA, 6, 9);
        new FarmerReward(this, Material.SWEET_BERRY_BUSH, (block -> block.getBlockData() instanceof Ageable ageable ? ageable.getAge() >= 2 ? 4 * (ageable.getAge()-1) : 0 : 0), true, true, false, 0);
        makeCropPayout(Material.NETHER_WART, 6, 9);
        makeCropPayout(Material.BEETROOTS, 6, 9);

        // These blocks don't care about Age-able and cannot be right-clicked to be harvested. These care about Natural Tag.
        makeBlockPayout(Material.BAMBOO, 1, 1, 16, BlockFace.UP);
        makeBlockPayout(setSimilar(Material.KELP, Material.KELP_PLANT), 1, 1);
        makeBlockPayout(Material.KELP_PLANT, 1, 1, 26, BlockFace.UP);
        makeBlockPayout(Material.CHORUS_PLANT, 0, 1, 50, BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH);
        makeBlockPayout(Material.SUGAR_CANE, 3, 4, 5, BlockFace.UP);
        makeBlockPayout(setSimilar(Material.CHORUS_FLOWER, Material.CHORUS_PLANT), 3, 6);
        makeBlockPayout(Material.CACTUS, 6, 9, 5, BlockFace.UP);
        makeBlockPayout(Material.AZALEA, 7, 10);
        makeBlockPayout(Material.FLOWERING_AZALEA, 7, 10);
        makeBlockPayout(Material.PUMPKIN, 7, 10);
        makeBlockPayout(Material.MELON, 7, 10);

        // Cave Vines have a tag to be checked for.
        new FarmerReward(this, setSimilar(Material.CAVE_VINES, Material.CAVE_VINES_PLANT), (block) -> block.getBlockData() instanceof CaveVinesPlant plant && plant.isBerries() ? getRandom().nextInt(4, 6) : 0, true, true, false, 0);
        new FarmerReward(this, Material.CAVE_VINES_PLANT, (block) -> block.getBlockData() instanceof CaveVinesPlant plant && plant.isBerries() ? getRandom().nextInt(4, 6) : 0, true, true, false, 22, BlockFace.DOWN);

        // Honey Harvesting, they can be Sheared and Harvested.
        new FarmerReward(this, Material.BEEHIVE, (block -> block.getBlockData() instanceof Beehive hive && hive.getHoneyLevel() >= hive.getMaximumHoneyLevel() ? getRandom().nextInt(20, 31) : 0), false, true, true, 0);
        new FarmerReward(this, Material.BEE_NEST, (block -> block.getBlockData() instanceof Beehive hive && hive.getHoneyLevel() >= hive.getMaximumHoneyLevel() ? getRandom().nextInt(20, 31) : 0), false, true, true, 0);
    }

    @Override
    public void doMiningEvent(@NotNull MoguProfile profile, @NotNull BlockBreakEvent e) {
        FarmerReward reward = jobPayouts.get(e.getBlock().getType());
        if (reward == null || !reward.paidThroughBreaking()) return;

        doFarmingStuff(profile, reward, e.getBlock(), false);
    }

    @Override
    public void doShearBlockEvent(@NotNull MoguProfile profile, @NotNull PlayerShearBlockEvent e) {
        FarmerReward reward = jobPayouts.get(e.getBlock().getType());
        if (reward == null || !reward.paidThroughShearing()) return;

        doFarmingStuff(profile, reward, e.getBlock(), true);
    }

    @Override
    public void doHarvestBlockEvent(@NotNull MoguProfile profile, @NotNull PlayerHarvestBlockEvent e) {
        FarmerReward reward = jobPayouts.get(e.getHarvestedBlock().getType());
        if (reward == null || !reward.paidThroughHarvest()) return;

        doFarmingStuff(profile, reward, e.getHarvestedBlock(), true);
    }

    private void doFarmingStuff(@NotNull MoguProfile profile, @NotNull FarmerReward reward, @NotNull Block initialBlock, boolean ignoreMulti) {
        int value = reward.getPayout().apply(initialBlock), count = value > 0 ? 1 : 0;
        @NotNull Block block = initialBlock;
        @NotNull Component prefix = this.prefix;

        // Multi-Block Crops
        if (!ignoreMulti && reward.maxCropSize() > 1 && reward.faces() != null && reward.faces().length > 0) {
            int limit = reward.maxCropSize(); // Maximum size we're accepting.
            final Set<Block> visited = new HashSet<>();
            final Queue<Block> queue = new LinkedList<>();
            queue.add(block);
            visited.add(block);

            while (visited.size() < limit && !queue.isEmpty()) {
                block = queue.poll();

                for (BlockFace face : reward.faces()) {
                    Block neighbor = block.getRelative(face);

                    if (!visited.contains(neighbor) && matches(neighbor, reward.getBlockType())) {
                        queue.add(neighbor);
                        visited.add(neighbor);

                        int toAdd = reward.getPayout().apply(neighbor);
                        count++;
                        if (toAdd > 0)
                            value += toAdd;
                    }
                }
            }
        }

        if (count > 1) prefix = prefix.append(Component.text(count, NamedTextColor.WHITE)).append(Component.text("x ", NamedTextColor.GRAY));
        payout(profile, value, prefix.append(Component.translatable(block.translationKey(), NamedTextColor.WHITE)), block.getLocation().add(0.5, 0.5, 0.5));
    }

    private void makeCropPayout(Material material, int minPay, int maxPay) {
        new FarmerReward(this, material, (b) -> (b.getBlockData() instanceof Ageable ageable && ageable.getAge() >= (ageable.getMaximumAge())) ? getRandom().nextInt(minPay, maxPay + 1) : 0, true, true, false, 0);
    }

    private void makeBlockPayout(Material material, int minPay, int maxPay) {
        makeBlockPayout(material, minPay, maxPay, 0, BlockFace.SELF);
    }

    private void makeBlockPayout(Material material, int minPay, int maxPay, int maxSize, BlockFace... faces) {
        new FarmerReward(this, material, (b) -> isBlockNatural(b) ? getRandom().nextInt(minPay, maxPay + 1) : 0, true, false, false, maxSize, faces);
    }

    private Material setSimilar(Material a, Material b) {
        similar.put(a, b);
        similar.put(b, a);
        return a;
    }

    private boolean matches(Block block, Material material) {
        Material type = block.getType();
        return type == material || type == similar.get(material);
    }

    private record FarmerReward(JobFarmer job, @NotNull Material getBlockType, Function<Block, Integer> getPayout, boolean paidThroughBreaking, boolean paidThroughHarvest, boolean paidThroughShearing, int maxCropSize, BlockFace... faces) {
        FarmerReward {
            job.jobPayouts.put(getBlockType, this);
        }
    }

}
