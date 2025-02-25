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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JobLumberjack extends Job {

    private final @NotNull Map<Player, Veinminer> veinminingPlayers;

    protected JobLumberjack(@NotNull JobManager manager) {
        super(manager, "Lumberjack", "\uD83E\uDE93", TextColor.color(0x8f5f3f), "lumberjack", "treechopper", "treecutter", "choppen", "wooden");
        setDescription(Component.text("Earn $$$ by chopping trees and collecting leaves!"));

        this.veinminingPlayers = new HashMap<>();
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

        payout(profile, (int) value, null, e.getBlock().getLocation().add(0.5, 0.5, 0.5));

        // Cumulate the message...
        if (miner != null) {
            miner.count++;
            miner.totalCash += (int)value;

            if (miner.count == miner.blockAmount)
                doVeinmineText(profile, miner);
            return;
        }

        sendActionBar(profile, (int) value, Component.text("chopping ", NamedTextColor.GRAY).append(Component.translatable(e.getBlock().translationKey(), NamedTextColor.WHITE)));
    }

    @Override
    public void doVeinmineEvent(@NotNull MoguProfile profile, @NotNull PlayerVeinmineEvent e) {
        veinminingPlayers.put(e.getPlayer(), new Veinminer(e.getBlocks().size(), e.getOriginalBlock().getType()));
    }

    private void doVeinmineText(@NotNull MoguProfile profile, @NotNull Veinminer miner) {
        veinminingPlayers.remove(profile.getPlayer());
        if (miner.totalCash <= 0) return;

        sendActionBar(profile, miner.totalCash, Component.text("treecapitating ", NamedTextColor.GRAY).append(Component.text(""+miner.blockAmount, NamedTextColor.WHITE)).append(Component.text("x ", NamedTextColor.GRAY)).append(Component.translatable(miner.block.translationKey(), NamedTextColor.WHITE)));
    }

    private int getValue(@NotNull Block block, Random random) {
        return switch (block.getType()) {
            case ACACIA_LEAVES, AZALEA_LEAVES, BIRCH_LEAVES, CHERRY_LEAVES, DARK_OAK_LEAVES, FLOWERING_AZALEA_LEAVES, JUNGLE_LEAVES, MANGROVE_LEAVES, OAK_LEAVES, PALE_OAK_LEAVES, SPRUCE_LEAVES -> 1;
            case MANGROVE_ROOTS, MUDDY_MANGROVE_ROOTS -> 2;
            case ACACIA_WOOD, BIRCH_WOOD, CHERRY_WOOD, DARK_OAK_WOOD, JUNGLE_WOOD, MANGROVE_WOOD, OAK_WOOD, PALE_OAK_WOOD, SPRUCE_WOOD, DARK_OAK_LOG, OAK_LOG, ACACIA_LOG, BIRCH_LOG, CHERRY_LOG,
                    JUNGLE_LOG, MANGROVE_LOG, PALE_OAK_LOG, SPRUCE_LOG, STRIPPED_ACACIA_LOG, STRIPPED_BIRCH_LOG, STRIPPED_CHERRY_LOG, STRIPPED_DARK_OAK_LOG, STRIPPED_OAK_LOG, STRIPPED_JUNGLE_LOG,
                    STRIPPED_MANGROVE_LOG, STRIPPED_PALE_OAK_LOG, STRIPPED_SPRUCE_LOG -> random.nextInt(4, 8);
            case CRIMSON_STEM, STRIPPED_CRIMSON_STEM, STRIPPED_WARPED_STEM, WARPED_STEM -> random.nextInt(8, 13);
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
