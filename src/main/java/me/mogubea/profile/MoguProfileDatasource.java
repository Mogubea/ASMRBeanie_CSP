package me.mogubea.profile;

import me.mogubea.data.PrivateDatasource;
import me.mogubea.main.Main;
import me.mogubea.statistics.DirtyVal;
import me.mogubea.statistics.PlayerStatistics;
import me.mogubea.statistics.SimpleStatType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

public class MoguProfileDatasource extends PrivateDatasource {

    private final @NotNull MoguProfileManager manager;
    private final String TABLE_PROFILE, TABLE_BLACKLIST, TABLE_STATS, TABLE_STATS_DAILY;

    protected MoguProfileDatasource(@NotNull Main plugin, @NotNull MoguProfileManager manager) {
        super(plugin);
        this.manager = manager;

        this.TABLE_PROFILE = getDatasourceConfig().getString("tables.players.profiles");
        this.TABLE_BLACKLIST = getDatasourceConfig().getString("tables.players.blacklist");
        this.TABLE_STATS = getDatasourceConfig().getString("tables.players.stats.main");
        this.TABLE_STATS_DAILY = getDatasourceConfig().getString("tables.players.stats.daily");
    }

    @Override
    public void loadAll() {
        if (!isOnline()) return;

        manager.getCache().invalidateAll();
        manager.clearLiteProfiles();

        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM " + TABLE_PROFILE); ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                UUID uuid;

                try {
                    uuid = UUID.fromString(rs.getString("uuid"));
                } catch (Exception e) {
                    getPlugin().getSLF4JLogger().warn("Profile with id {} has an invalid UUID. Skipping..", id);
                    continue;
                }

                manager.registerLite(new MoguProfileLite(manager, id, uuid, rs.getString("name"), rs.getString("nickname"), rs.getInt("nameColour"), rs.getLong("money"), getPlugin().getJobManager().getJob(rs.getString("job"))));
            }
        } catch (SQLException | NullPointerException e) {
            getPlugin().getSLF4JLogger().trace("There was a problem with fetching player profile information.", e);
        }
    }

    @Override
    public void saveAll() {
        Collection<MoguProfile> profiles = Collections.unmodifiableCollection(manager.getCache().asMap().values());
        for (MoguProfile profile : profiles) {
            if (!profile.shouldSave()) continue;

            try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE " + TABLE_PROFILE + " SET name = ?, nickname = ?, nameColour = ?, money = ?, job = ? WHERE id = ?")) {
                int idx = 0;

                s.setString(++idx, profile.getName());
                s.setString(++idx, profile.getNickname());
                s.setInt(++idx, profile.getNameColour().value());
                s.setLong(++idx, profile.getMoney());
                s.setString(++idx, profile.getJob().getLowerName());
                s.setInt(++idx, profile.getDatabaseId());
                s.executeUpdate();
            } catch (SQLException | NullPointerException e) {
                getPlugin().getSLF4JLogger().trace("There was a problem saving {}'s PlayerProfile;", profile.getName(), e);
            }

            savePlayerStats(profile);
            savePickupBlacklist(profile);
        }
    }

    protected @NotNull MoguProfile getOrCreateProfile(UUID playerUUID, @Nullable String username) throws Exception {
        MoguProfile profile = getProfile(playerUUID, true);
        if (profile != null) return profile;
        ResultSet rs = null;

        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + TABLE_PROFILE + " (uuid, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, playerUUID.toString());
            s.setString(2, username);
            s.executeUpdate();

            rs = s.getGeneratedKeys();

            if (rs.next()) {
                int id = rs.getInt(1);
                getPlugin().getSLF4JLogger().info("Player Profile has been created for {} (#{}).", username, id);
                profile = new MoguProfile(manager, id, playerUUID, username == null ? "Null" : username);
                return profile;
            }
        } catch (SQLException | NullPointerException e) {
            throw new Exception("There was a problem creating a profile for " + playerUUID, e);
        } finally {
            close(rs);
        }

        throw new Exception("There was an unknown problem creating a profile for " + playerUUID);
    }

    protected @Nullable MoguProfile getProfile(@NotNull UUID playerUUID, boolean makeOfflineProfile) {
        if (!isOnline() && makeOfflineProfile) {
            getPlugin().getSLF4JLogger().debug("Created an offline profile for {}", playerUUID);
            MoguProfile profile = new MoguProfile(manager.getProfileLite(playerUUID));
            profile.setShouldSave(false);
            return profile;
        }

        ResultSet rs = null;

        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM " + TABLE_PROFILE + " WHERE uuid = ?")) {
            s.setString(1, playerUUID.toString());
            rs = s.executeQuery();
            if (rs.next()) {
                return new MoguProfile(manager, rs.getInt("id"), playerUUID, rs.getString("name"), rs.getString("nickname"), rs.getInt("nameColour"), rs.getLong("money"), getPlugin().getJobManager().getJob(rs.getString("job")));
            }
        } catch (Exception e) {
            plugin.getSLF4JLogger().error("Loading Player Profile", e);
        } finally {
            close(rs);
        }
        return null;
    }

    protected @Nullable MoguProfile getProfileIfExists(@NotNull String playerName) {
        ResultSet rs = null;

        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM " + TABLE_PROFILE + " WHERE name = ? OR nickname = ?")) {
            s.setString(1, playerName);
            s.setString(2, playerName);
            rs = s.executeQuery();
            if (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                return new MoguProfile(manager, rs.getInt("id"), uuid, rs.getString("name"), rs.getString("nickname"), rs.getInt("nameColour"), rs.getLong("money"), getPlugin().getJobManager().getJob(rs.getString("job")));
            }
        } catch (Exception e) {
            plugin.getSLF4JLogger().error("Loading Player Profile", e);
        } finally {
            close(rs);
        }
        return null;
    }

    /**
     * Save the player's {@link PlayerStatistics}.
     */
    protected void savePlayerStats(@NotNull MoguProfile profile) {
        int statDay = plugin.getCurrentDay();
        PlayerStatistics stats = profile.getStats();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + TABLE_STATS + " (id, category, stat, value) VALUES (?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
            s.setInt(1, profile.getDatabaseId());

            Map<SimpleStatType, Map<String, DirtyVal<Long>>> map = stats.getMap();

            for (Map.Entry<SimpleStatType, Map<String, DirtyVal<Long>>> ent : map.entrySet()) {
                s.setString(2, ent.getKey().getIdentifier());

                for(Map.Entry<String, DirtyVal<Long>> entry : ent.getValue().entrySet()) {
                    if (!entry.getValue().isDirty()) continue;

                    s.setString(3, entry.getKey());
                    s.setLong(4, entry.getValue().getValue());
                    s.addBatch();
                    entry.getValue().setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException | NullPointerException e) {
            plugin.getSLF4JLogger().error("Saving Player Stats", e);
        }

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + TABLE_STATS_DAILY + " (day, id, category, stat, value) VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
            s.setInt(1, statDay);
            s.setInt(2, profile.getDatabaseId());

            Map<SimpleStatType, Map<String, DirtyVal<Long>>> map = stats.getDailyMap();

            for (Map.Entry<SimpleStatType, Map<String, DirtyVal<Long>>> ent : map.entrySet()) {
                s.setString(3, ent.getKey().getIdentifier());

                for(Map.Entry<String, DirtyVal<Long>> entry : ent.getValue().entrySet()) {
                    if (!entry.getValue().isDirty()) continue;

                    s.setString(4, entry.getKey());
                    s.setLong(5, entry.getValue().getValue());
                    s.addBatch();
                    entry.getValue().setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException | NullPointerException e) {
            plugin.getSLF4JLogger().error("Saving Player Stats", e);
        }
    }

    /**
     * Load the player's {@link PlayerStatistics}.
     */
    protected @NotNull PlayerStatistics loadPlayerStats(@NotNull final MoguProfile profile) {
        int statDay = plugin.getCurrentDay();
        final PlayerStatistics stats = new PlayerStatistics();
        ResultSet rs = null;

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT category,stat,value FROM " + TABLE_STATS + " WHERE id = ?")) {
            s.setInt(1, profile.getDatabaseId());
            rs = s.executeQuery();

            while(rs.next()) {
                SimpleStatType type = SimpleStatType.fromIdentifier(rs.getString(1));
                if (type != null)
                    stats.setStat(type, rs.getString(2), rs.getLong(3), false);
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().error("Loading Player Stats", e);
        } finally {
            close(rs);
        }

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT category,stat,value FROM " + TABLE_STATS_DAILY + " WHERE id = ? AND day = ?")) {
            s.setInt(1, profile.getDatabaseId());
            s.setInt(2, statDay);
            rs = s.executeQuery();

            while(rs.next()) {
                SimpleStatType type = SimpleStatType.fromIdentifier(rs.getString(1));
                if (type != null)
                    stats.setDailyStat(type, rs.getString(2), rs.getInt(3), false);
            }
        } catch (SQLException | NullPointerException e) {
            plugin.getSLF4JLogger().error("Loading Player Stats", e);
        } finally {
            close(rs);
        }

        return stats;
    }

    protected @NotNull List<String> loadPickupBlacklist(int playerId) {
        ResultSet rs = null;
        List<String> array = new ArrayList<>();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT blacklist FROM " + TABLE_BLACKLIST + " WHERE playerId = ?")) {
            s.setInt(1, playerId);

            rs = s.executeQuery();

            while(rs.next()) {
                String string = rs.getString("blacklist");
                if (string == null || string.isEmpty())
                    break;
                String[] list = string.split(",");
                for (String str : list)
                    if (!str.isEmpty())
                        array.add(str);
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().error("Loading Pickup Blacklist", e);
        } finally {
            close(rs);
        }
        return array;
    }

    /**
     * Save the player's item blacklist
     */
    protected void savePickupBlacklist(@NotNull final MoguProfile profile) {
        if (!profile.getPickupFilter().isDirty()) return;

        try(Connection c = getNewConnection(); PreparedStatement statement = c.prepareStatement("INSERT INTO " + TABLE_BLACKLIST + " (playerId, blacklist) VALUES (?,?) ON DUPLICATE KEY UPDATE blacklist = VALUES(blacklist)")) {

            StringBuilder sb = new StringBuilder();
            for (String entry : profile.getPickupFilter().getSnapshotSet())
                sb.append(entry).append(",");

            statement.setInt(1, profile.getDatabaseId());
            statement.setString(2, sb.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getSLF4JLogger().error("Saving Pickup Blacklist", e);
        } finally {
            profile.getPickupFilter().setDirty(false);
        }
    }

    protected void saveAndResetDailyStats() {
        manager.getCache().asMap().forEach((uuid, profile) -> {
            savePlayerStats(profile);
            profile.getStats().clearDailyStats();
        });
    }

}
