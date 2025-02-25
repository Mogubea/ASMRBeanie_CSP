package me.mogubea.profile;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoguProfileManager {

    private static MoguProfileManager INSTANCE;

    private final @NotNull Main plugin;
    private final @NotNull MoguProfileDatasource datasource;
    private final @NotNull PlayerTeamManager teamManager;
    private final @NotNull LoadingCache<UUID, MoguProfile> moguProfiles;
    private final @NotNull Map<Object, MoguProfileLite> liteProfiles;

    public MoguProfileManager(@NotNull Main plugin) {
        INSTANCE = this;
        this.plugin = plugin;
        this.liteProfiles = new HashMap<>();
        this.datasource = new MoguProfileDatasource(plugin, this);
        this.teamManager = new PlayerTeamManager(plugin);
        this.moguProfiles = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(Duration.ofMinutes(6))
                .removalListener((RemovalListener<UUID, MoguProfile>) (uuid, moguProfile, reason) -> {
                    if (moguProfile == null) return; // huh
                    moguProfile.createLite();
                })
                .build(uuid -> datasource.getOrCreateProfile(uuid, null));

        this.datasource.loadAll();
    }

    // Unregister them on disable...
    public void onDisable() {
        teamManager.onDisable();
    }

    public void initScoreboard(@NotNull Player player) {
        teamManager.initScoreboard(player);
    }

    public void resetDailyStats() {
        this.datasource.saveAndResetDailyStats();
    }

    /**
     * @return The player's {@link MoguProfileLite} unless the player's {@link MoguProfile} is loaded, in which case it will return the latter.
     */
    public @NotNull MoguProfileLite getProfileLite(@NotNull UUID uuid) {
        MoguProfile profile = moguProfiles.getIfPresent(uuid);
        MoguProfileLite lite = liteProfiles.get(uuid);

        if (profile != null)
            return profile;

        // Precautionary
        if (lite == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (datasource.isOnline()) {
                if (offlinePlayer.getPlayer() != null)
                    offlinePlayer.getPlayer().kick(Component.text("There was an error loading your Player Profile.", NamedTextColor.RED));
                throw new NullPointerException("MoguProfileLite can't be null while server is connected to the database.");
            }
            lite = new MoguProfileLite(this, -1, uuid, offlinePlayer.getName() == null ? "Null" : offlinePlayer.getName(), null, 0, 0, null);
            lite.setShouldSave(false);

            registerLite(lite);
        }
        return lite;
    }

    protected void registerLite(@NotNull MoguProfileLite lite) {
        liteProfiles.put(lite.getUUID(), lite);
        liteProfiles.put(lite.getName(), lite);
    }

    protected void clearLiteProfiles() {
        liteProfiles.clear();
    }

    public @NotNull Collection<MoguProfileLite> getLiteProfiles() {
        return liteProfiles.values();
    }

    public @NotNull MoguProfile getMoguProfile(@NotNull UUID uuid) {
        return getMoguProfile(uuid, null);
    }

    /**
     * Attempts to get or create a {@link MoguProfile} for the provided player {@link UUID}.
     * @return The {@link MoguProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link MoguProfile} for this player.
     */
    public @NotNull MoguProfile getMoguProfile(@NotNull UUID uuid, @Nullable String username) {
        try {
            // If a username is not provided or the cache already has an entry, just grab the entry.
            MoguProfile profile = moguProfiles.getIfPresent(uuid);
            if (profile != null) return profile;

            // If a username is provided and the cache doesn't have an entry, go through the precautionary creation method, which requires the username.
            profile = datasource.getOrCreateProfile(uuid, username);
            moguProfiles.put(uuid, profile);
            return profile;
        } catch (Exception e) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getPlayer() != null)
                offlinePlayer.getPlayer().kick(Component.text("There was an error loading your Player Profile.", NamedTextColor.RED));
            throw new RuntimeException(e);
        }
    }

    public @Nullable MoguProfile getIfExists(@NotNull UUID uuid) {
        return datasource.getProfile(uuid, false);
    }

    public @Nullable MoguProfile getIfExists(@NotNull String name) {
        return datasource.getProfileIfExists(name);
    }

    protected @NotNull Main getPlugin() {
        return plugin;
    }

    protected @NotNull LoadingCache<UUID, MoguProfile> getCache() {
        return moguProfiles;
    }

    protected @NotNull MoguProfileDatasource getDatasource() { return datasource; }

    protected static @Nullable MoguProfile getProfileIfExists(@NotNull UUID uuid) {
        return INSTANCE.getIfExists(uuid);
    }

    protected static @Nullable MoguProfile getProfileIfExists(@NotNull String name) {
        return INSTANCE.getIfExists(name);
    }

    /**
     * Helper static method for {@link MoguProfileManager#getMoguProfile(UUID)}.
     * @return The {@link MoguProfile} associated with the provided player.
     * @throws RuntimeException - If there was a problem getting or creating a {@link MoguProfile} for this player.
     */
    protected static @NotNull MoguProfile getProfile(@NotNull OfflinePlayer p) {
        return getProfile(p.getUniqueId());
    }

    /**
     * Helper static method for {@link MoguProfileManager#getMoguProfile(UUID)}.
     *
     * @return The {@link MoguProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link MoguProfile} for this player.
     */
    protected static @NotNull MoguProfile getProfile(@NotNull UUID uuid) {
        return INSTANCE.getMoguProfile(uuid);
    }

    /**
     * Helper static method for {@link MoguProfileManager#getProfileLite(UUID)}.
     * @return The {@link MoguProfileLite} associated with the provided player {@link UUID}.
     */
    protected static @NotNull MoguProfileLite getLiteProfile(@NotNull UUID uuid) {
        return INSTANCE.getProfileLite(uuid);
    }

}
