package me.mogubea.jobs;

import me.mogubea.events.PlayerVeinmineEvent;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JobMiner extends Job {

    private final @NotNull Map<Player, Veinminer> veinminingPlayers = new HashMap<>();

    protected JobMiner(@NotNull JobManager manager) {
        super(manager, "Miner", "⛏", TextColor.color(0x2f6f9f), "miner", "digger", "mining", "dwarf", "minen", "diggen");
        setDescription(Component.text("Earn $$$ by excavating the world of Minecraft of its naturally generated stones and ores!"));
    }

    @Override
    public void doMiningEvent(@NotNull MoguProfile profile, @NotNull BlockBreakEvent e) {
        @Nullable final Veinminer miner = veinminingPlayers.get(e.getPlayer());
        boolean isNatural = profile.getPlugin().getBlockTracker().isBlockNatural(e.getBlock());
        double value;

        // If this event is a result of a non-cancelled PlayerVeinmineEvent..
        if (!isNatural || ((value = getValue(e.getBlock(), profile.getPlugin().getRandom())) <= 0)) {
            if (miner != null) {
                miner.blockAmount--;
                if (miner.count == miner.blockAmount)
                    doVeinmineText(profile, miner);
            }
            return;
        }

        double multiplier = miner == null ? 1 : 0.75;
        if (e.getBlock().getY() < -64)
            multiplier *= (1D + (e.getBlock().getY() + 128D) / 128D);

        value *= multiplier;

        payout(profile, (int) value, null, e.getBlock().getLocation().add(0.5, 0.5, 0.5));

        // Cumulate the message...
        if (miner != null) {
            miner.count++;
            miner.totalCash += (int)value;

            if (miner.count == miner.blockAmount)
                doVeinmineText(profile, miner);
            return;
        }

        sendActionBar(profile, (int) value, Component.text("mining ", NamedTextColor.GRAY).append(Component.translatable(e.getBlock().translationKey(), NamedTextColor.WHITE)));
    }

    @Override
    public void doVeinmineEvent(@NotNull MoguProfile profile, @NotNull PlayerVeinmineEvent e) {
        veinminingPlayers.put(e.getPlayer(), new Veinminer(e.getBlocks().size(), e.getOriginalBlock().getType()));
    }

    private void doVeinmineText(@NotNull MoguProfile profile, @NotNull Veinminer miner) {
        veinminingPlayers.remove(profile.getPlayer());
        if (miner.totalCash <= 0) return;

        sendActionBar(profile, miner.totalCash, Component.text("prospecting ", NamedTextColor.GRAY).append(Component.text(""+miner.blockAmount, NamedTextColor.WHITE)).append(Component.text("x ", NamedTextColor.GRAY)).append(Component.translatable(miner.block.translationKey(), NamedTextColor.WHITE)));
    }

    private double getValue(@NotNull Block block, Random random) {
        if (block.getType().name().endsWith("_TERRACOTTA"))
            return 2 + random.nextInt(3);
        if (block.getType().name().endsWith("_CONCRETE"))
            return 1 + random.nextInt(2);

        return switch (block.getType()) {
            case NETHERRACK, CRIMSON_NYLIUM, WARPED_NYLIUM -> 1;
            case STONE, COBBLESTONE, DEEPSLATE, COBBLED_DEEPSLATE, STONE_SLAB, BLACKSTONE, BASALT, SMOOTH_BASALT, GRAVEL -> 1 + random.nextInt(2);
            case SANDSTONE, RED_SANDSTONE, CALCITE, DIORITE, ANDESITE, GRANITE, END_STONE, TUFF, MAGMA_BLOCK, DRIPSTONE_BLOCK, POINTED_DRIPSTONE -> 2 + random.nextInt(2);
            case COAL_ORE, AMETHYST_BLOCK, AMETHYST_CLUSTER, BUDDING_AMETHYST, PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE, COPPER_ORE, DEEPSLATE_COPPER_ORE, GLOWSTONE -> 5 + random.nextInt(3);
            case OBSIDIAN, IRON_ORE, DEEPSLATE_IRON_ORE -> 7 + random.nextInt(4);
            case MOSSY_COBBLESTONE -> 8 + random.nextInt(5);
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> 9 + random.nextInt(6);
            case NETHER_QUARTZ_ORE, RAW_COPPER_BLOCK, COAL_BLOCK, RAW_IRON_BLOCK -> 12 + random.nextInt(7);
            case CRYING_OBSIDIAN, REDSTONE_BLOCK -> 14 + random.nextInt(8);
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> 15 + random.nextInt(8);
            case NETHER_GOLD_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE, DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, DEEPSLATE_COAL_ORE -> 18 + random.nextInt(9);
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE, GILDED_BLACKSTONE, RAW_GOLD_BLOCK, GOLD_BLOCK -> 26 + random.nextInt(14);
            case ANCIENT_DEBRIS -> 60 + random.nextInt(21);
            case SPAWNER -> 200 + random.nextInt(101);
            default -> 0;
        };
    }

    private static final class Veinminer {
        Material block;
        int blockAmount;
        int count;
        int totalCash;

        Veinminer(int blockAmount, Material block) {
            this.blockAmount = blockAmount;
            this.block = block;
        }
    }

}
