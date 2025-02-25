package me.mogubea.profile;

import me.mogubea.gui.MoguGui;
import me.mogubea.jobs.Job;
import me.mogubea.statistics.PlayerStatistics;
import me.mogubea.statistics.SimpleStatType;
import me.mogubea.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * <p>The player's profile, holding all of their server stats and information.</p>
 * <p>Will be replaced with a {@link MoguProfileLite} upon expiring from the profile cache.</p>
 */
public class MoguProfile extends MoguProfileLite {

    public static @NotNull MoguProfile from(@NotNull UUID uuid) {
        return MoguProfileManager.getProfile(uuid);
    }

    public static @NotNull MoguProfile from(@NotNull OfflinePlayer player) {
        return MoguProfileManager.getProfile(player);
    }

    public static @Nullable MoguProfile fromIfExists(@NotNull UUID uuid) { return MoguProfileManager.getProfileIfExists(uuid); }

    public static @Nullable MoguProfile fromIfExists(@NotNull String name) { return MoguProfileManager.getProfileIfExists(name); }

    private final @NotNull Map<String, Long>        cooldowns;
    private final @NotNull Map<String, Boolean>     permissions;
    private final @NotNull PlayerStatistics         stats;
    private final @NotNull PlayerPickupFilter       pickupFilter;

    private @Nullable MoguGui                       currentGui;
    private int                                     dayOfJobChange;

    protected MoguProfile(@NotNull MoguProfileLite liteProfile) {
        this(liteProfile.getManager(), liteProfile.getDatabaseId(), liteProfile.getUUID(), liteProfile.getName(), liteProfile.getNickname(), liteProfile.getNameColour().value(), liteProfile.getMoney(), liteProfile.getJob());
    }

    protected MoguProfile(@NotNull MoguProfileManager manager, int id, @NotNull UUID uuid, @NotNull String name) {
        this(manager, id, uuid, name, null, 0, 0, Job.getNone());
    }

    protected MoguProfile(@NotNull MoguProfileManager manager, int id, @NotNull UUID uuid, @NotNull String name, @Nullable String nickname, int nameColour, long money, @NotNull Job job) {
        super(manager, id, uuid, name, nickname, nameColour, money, job);
        this.cooldowns = new HashMap<>();
        this.permissions = new TreeMap<>();
        this.stats = getManager().getDatasource().loadPlayerStats(this);
        this.pickupFilter = new PlayerPickupFilter(this);
    }

    /**
     * Create and set the {@link MoguProfileLite} for this profile.
     */
    protected void createLite() {
        MoguProfileLite liteProfile = new MoguProfileLite(getManager(), getDatabaseId(), getUUID(), getName(), getNickname(), getNameColour().value(), getMoney(), getJob());
        getManager().registerLite(liteProfile);
    }

    public Player getPlayer() {
        return getOfflinePlayer().getPlayer();
    }

    @Override
    @SuppressWarnings("removal")
    public void updateDisplayNames(boolean updateProfile) {
        colouredName = Component.text(getDisplayName(), getNameColour());
        updateInfoName();

        if (isPlayerOnline()) {
            if (updateProfile) {
                com.destroystokyo.paper.profile.PlayerProfile prof = getPlayer().getPlayerProfile();
                prof.setName(getDisplayName());
                getPlayer().setPlayerProfile(prof);
                prof.complete();
                prof.update();
            }
            updateBoardNames();
        }
    }

    protected void updateBoardNames() {
        if (!isPlayerOnline()) return;
        getPlayer().displayName(getColouredName()); // Display Name
        getManager().initScoreboard(getPlayer()); // Team, Tab list etc.
    }

    public void updateInfoName() {
        infoName = Component.empty().append(getColouredName(false).hoverEvent(
                HoverEvent.showText(Component.text(getDisplayName() + "\n", getNameColour())
                        .append(Component.text(" - Mogucoins: " + getMoney() + "$\n", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(" - Playtime: " + Utils.timeStringFromMillis(getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L) + "\n", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(" - Job: ", NamedTextColor.GRAY).append(getJob().getDisplayName())).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("\n\n"))
                        .append(Component.text(getOfflinePlayer().getUniqueId() + ""))
                )));
    }

    @Override
    public @NotNull TextComponent getColouredName() {
        updateInfoName();
        return infoName;
    }

    /**
     * Set a cooldown to this player.
     * @param id The identifier of the cooldown
     * @param milli The length in milliseconds
     */
    public void addCooldown(String id, int milli) {
        this.cooldowns.put(id, System.currentTimeMillis() + milli);
    }

    /**
     * Remove a cooldown
     * @param id The identifier of the cooldown
     */
    public void clearCooldown(String id) {
        this.cooldowns.remove(id);
    }

    /**
     * Check if the player is on cooldown for the specified identifier
     * @param id The identifier of the cooldown
     * @return If the player is on cooldown
     */
    public boolean onCooldown(String id) {
        final long dura = this.cooldowns.getOrDefault(id, 0L);
        final boolean isCd = System.currentTimeMillis() < dura;
        if (dura > 0 && !isCd)
            this.cooldowns.remove(id);
        return isCd;
    }

    /**
     * Check if the player is on cooldown, and if not, put them on cooldown.
     * @param id The identifier of the cooldown
     * @param milli The length in milliseconds
     * @return If the player is on cooldown
     */
    public boolean onCdElseAdd(String id, int milli) {
        boolean onCd = onCooldown(id);
        if (!onCd)
            this.cooldowns.put(id, System.currentTimeMillis() + milli);
        return onCd;
    }

    /**
     * Get the remaining cooldown for the specified identifier
     * @param id The identifier of the cooldown
     * @return The time in milliseconds when this cooldown expires
     */
    public long getCooldown(String id) {
        return this.cooldowns.getOrDefault(id, 0L);
    }

    public Map<String, Boolean> getPermissionMap() {
        return permissions;
    }

    /**
     * Set whether a player has a permission or not.
     * @param permission The permission string.
     * @param has True/False/Null, Can be set to null to remove from the map.
     */
    public Boolean setHasPermission(@NotNull String permission, @Nullable Boolean has) {
        if (has == null)
            this.permissions.remove(permission);
        else
            this.permissions.put(permission, has);

        getManager().getPlugin().getPermissionManager().updatePermissionFor(getPlayer(), permission, has, true);
        return has;
    }

    public @Nullable Boolean getPermission(@NotNull String permission) {
        return permissions.get(permission);
    }

    /**
     * Update the player's permissions, assuming they are online.
     */
    public void updatePermissions() {
        getManager().getPlugin().getPermissionManager().updatePermissionsFor(getPlayer());
    }

    public @NotNull PlayerStatistics getStats() {
        return stats;
    }

    public void addToStat(@NotNull SimpleStatType statType, @NotNull String subStat, long amount) {
        getStats().addToStat(statType, subStat, amount);
    }

    public @NotNull PlayerPickupFilter getPickupFilter() {
        return pickupFilter;
    }

    public void addMoney(long amount, String moneySource) {
        addToStat(SimpleStatType.MONEY_SOURCES, moneySource, amount);
        setMoney(getMoney() + amount);
    }

    public void changeJob(@NotNull Job job) {
        setJob(job);
        this.dayOfJobChange = getPlugin().getCurrentGameDay();
    }

    @Override
    public void setJob(@NotNull Job job) {
        super.setJob(job);
        updateBoardNames();
    }

    public int getDayOfLastJobChange() {
        return dayOfJobChange;
    }

    /**
     * @return The {@link MoguGui} currently being viewed by the player.
     */
    public @Nullable MoguGui getMoguGui() {
        if (!isPlayerOnline()) currentGui = null;
        return currentGui;
    }

    /**
     * Set the currently viewed {@link MoguGui}.
     * This method should only ever be called by the abstract {@link MoguGui}.
     * @param gui The {@link MoguGui}.
     */
    public void setMoguGui(@Nullable MoguGui gui) {
        if (!isPlayerOnline()) gui = null;
        this.currentGui = gui;
    }

}
