package me.mogubea.jobs;

import io.papermc.paper.event.block.PlayerShearBlockEvent;
import me.mogubea.events.PlayerVeinmineEvent;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Job {

    private static Job NONE;

    private final @NotNull JobManager manager;
    private final @NotNull List<String> aliases;
    private final @NotNull Component displayName;
    private final @NotNull String displayString;
    private final @NotNull String lowercaseString;
    private final @NotNull Component icon;

    private Component description;

    protected Job(@NotNull JobManager manager, @NotNull String displayString, @NotNull TextColor color, @NotNull String... aliases) {
        this(manager, displayString, "\uD83E\uDE93", color, aliases);
    }

    protected Job(@NotNull JobManager manager, @NotNull String displayName, @NotNull String icon, @NotNull TextColor color, @NotNull String... aliases) {
        List<String> list = new ArrayList<>();
        for (String s : aliases)
            list.add(s.toLowerCase());

        this.manager = manager;
        this.aliases = List.copyOf(list);
        this.displayName = Component.text(displayName, color);
        this.displayString = displayName;
        this.lowercaseString = displayString.toLowerCase();
        this.description = Component.empty();
        this.icon = Component.empty().append(Component.text(icon, color).hoverEvent(HoverEvent.showText(getDisplayName())));
    }

    protected void setDescription(Component description) {
        this.description = description;
    }

    protected @NotNull List<String> getAliases() {
        return aliases;
    }

    public @NotNull Component getDisplayName() {
        return displayName;
    }

    public @NotNull Component getDescription() {
        return description;
    }

    public @NotNull String getName() {
        return displayString;
    }

    public @NotNull String getLowerName() {
        return lowercaseString;
    }

    public @NotNull Component getIcon() {
        return icon;
    }

    public void doMiningEvent(@NotNull MoguProfile profile, @NotNull BlockBreakEvent e) {}

    public void doVeinmineEvent(@NotNull MoguProfile profile, @NotNull PlayerVeinmineEvent e) {}

    public void doFishingEvent(@NotNull MoguProfile profile, @NotNull PlayerFishEvent e) {}

    public void doKillEntityEvent(@NotNull MoguProfile profile, @NotNull EntityDeathEvent e) {}

    public void doBuildEvent(@NotNull MoguProfile profile, @NotNull BlockPlaceEvent e) {}

    public void doHarvestBlockEvent(@NotNull MoguProfile profile, @NotNull PlayerHarvestBlockEvent e) {}

    public void doShearBlockEvent(@NotNull MoguProfile profile, @NotNull PlayerShearBlockEvent e) {}

    public void doShearEntityEvent(@NotNull MoguProfile profile, @NotNull PlayerShearEntityEvent e) {}

    public void doEnchantEvent(@NotNull MoguProfile profile, @NotNull EnchantItemEvent e) {}

    public void doFreshPotionEvent(@NotNull MoguProfile profile, @NotNull ItemStack itemStack, Location blockLocation) {}

    /**
     * @param block The block being checked whether it is natural or not.
     * @return Whether the block is natural or not.
     */
    protected boolean isBlockNatural(Block block) {
        return getPlugin().getBlockTracker().isBlockNatural(block);
    }

    protected @NotNull Random getRandom() {
        return manager.getPlugin().getRandom();
    }

    protected @NotNull Main getPlugin() { return manager.getPlugin(); }

    protected void payout(@NotNull MoguProfile profile, int money, @Nullable Component reasoning, @Nullable Location entityLocation) {
        if (money <= 0) return;

        profile.addMoney(money, "job_" + getName());

//        if (entityLocation != null)
//            spawnEarningIndicator(money, entityLocation);

        if (reasoning != null)
            sendActionBar(profile, money, reasoning);
    }

    protected void sendActionBar(@NotNull MoguProfile profile, int money, @NotNull Component reasoning) {
        if (!profile.getPlayer().isOnline()) return;
        profile.getPlayer().sendActionBar(Component.text("+", NamedTextColor.GRAY).append(Component.text(money,NamedTextColor.GOLD)).append(Component.text("$", NamedTextColor.GREEN)).append(Component.text(" from ", NamedTextColor.GRAY)).append(reasoning).append(Component.text(" as a ", NamedTextColor.GRAY)).append(getDisplayName()));
    }

//    protected void spawnEarningIndicator(int money, @NotNull Location entityLocation) {
//        CustomEntityType.TEXT_INDICATOR.spawn(entityLocation, (text) -> {
//            text.setTTL(15);
//            text.setText(Component.text("+", NamedTextColor.DARK_GREEN).append(Component.text(money, NamedTextColor.GOLD)).append(Component.text("$", NamedTextColor.GREEN)));
//        });
//    }

    public static @NotNull Job getNone() {
        if (NONE == null) NONE = new JobNone();
        return NONE;
    }

}
