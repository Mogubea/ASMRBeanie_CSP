package me.mogubea.profile;

import me.mogubea.jobs.Job;
import me.mogubea.main.Main;
import me.mogubea.permissions.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * <p>Every registered player's {@link MoguProfileLite} will be stored upon server boot, assuming there's connection to the database.</p>
 * <p>Will be used in place of a {@link MoguProfile} until a full profile is required, for example when the player logs in and/or when more detailed information is required.</p>
 * <p><b>Only changes to a {@link MoguProfile} will be saved.</b></p>
 */
public class MoguProfileLite {

    private static final TextColor DEFAULT_NAMECOLOUR = TextColor.color(0x7AFFBA);

    public static @NotNull MoguProfileLite getLiteProfile(@NotNull UUID uuid) {
        return MoguProfileManager.getLiteProfile(uuid);
    }

    private final int id;
    private final @NotNull OfflinePlayer offlinePlayer;
    private final @NotNull MoguProfileManager manager;
    private final @NotNull Set<Rank> ranks; // TODO

    private @NotNull String name;
    private @Nullable String nickname;
    private @NotNull TextColor nameColour;
    protected @NotNull TextComponent colouredName;
    protected @NotNull TextComponent infoName;
    private @NotNull Job job;

    private long money;

    private boolean shouldSave = true;

    protected MoguProfileLite(@NotNull MoguProfileManager manager, final int id, final @NotNull UUID uuid, @NotNull String username, @Nullable String nickname, int nameColour, long money, Job job) {
        this.manager = manager;

        this.id = id;
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        this.name = username;
        this.nickname = name.equals(nickname) ? null : nickname;
        this.nameColour = nameColour == 0 ? DEFAULT_NAMECOLOUR : TextColor.color(nameColour);
        this.colouredName = Component.text(getDisplayName(), getNameColour());
        this.infoName = colouredName;
        this.money = money;
        this.job = job == null ? Job.getNone() : job;
        this.ranks = new HashSet<>();

        updateDisplayNames(false);
    }

    protected @NotNull MoguProfileManager getManager() {
        return manager;
    }

    public int getDatabaseId() {
        return id;
    }

    public boolean isPlayerOnline() {
        return offlinePlayer.isOnline();
    }

    public @NotNull OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public @NotNull UUID getUUID() {
        return getOfflinePlayer().getUniqueId();
    }

    public @NotNull String getName() {
        return name;
    }

    public void updateRealName(@NotNull String name) {
        this.name = name;
    }

    public @Nullable String getNickname() {
        return nickname;
    }

    public boolean hasNickname() {
        return !name.equals(nickname);
    }

    public void setNickname(@Nullable String newName) {
        nickname = getName().equals(newName) ? null : newName;
        updateDisplayNames(true);
    }

    public @NotNull String getDisplayName() {
        return nickname == null ? getName() : nickname;
    }

    public @NotNull TextComponent getColouredName() {
        return colouredName;
    }

    public @NotNull TextComponent getColouredName(boolean info) {
        return info ? infoName : colouredName;
    }

    public @NotNull TextColor getNameColour() {
        return nameColour;
    }

    public void setNameColour(@Nullable TextColor colour) {
        nameColour = colour == null ? DEFAULT_NAMECOLOUR : colour;
        updateDisplayNames(true);
    }

    public void setNameColour(int colour) {
        setNameColour(colour == 0 ? DEFAULT_NAMECOLOUR : TextColor.color(colour));
    }

    public void updateDisplayNames(boolean updateProfile) {
        colouredName = Component.text(getDisplayName(), getNameColour());
        infoName = colouredName;
    }

    protected void setMoney(long money) {
        this.money = money;
    }

    public final long getMoney() {
        return money;
    }

    public final boolean hasJob() {
        return job != Job.getNone();
    }

    public void setJob(@NotNull Job job) {
        this.job = job;
    }

    public @NotNull Job getJob() {
        return job;
    }

    public @NotNull Main getPlugin() {
        return manager.getPlugin();
    }

    protected void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    protected boolean shouldSave() {
        return shouldSave;
    }

}
