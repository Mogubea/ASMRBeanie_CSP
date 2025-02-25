package me.mogubea.main;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.mogubea.commands.CommandManager;
import me.mogubea.data.DatasourceCore;
import me.mogubea.entities.CustomEntityManager;
import me.mogubea.events.PlayerToggleShieldEvent;
import me.mogubea.events.WorldDayChangeEvent;
import me.mogubea.items.MoguItemManager;
import me.mogubea.jobs.JobManager;
import me.mogubea.listeners.ListenerManager;
import me.mogubea.permissions.PermissionManager;
import me.mogubea.profile.MoguProfile;
import me.mogubea.profile.MoguProfileManager;
import me.mogubea.utils.WordFilterTrie;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Main extends JavaPlugin {

    private BlockTracker blockTracker;
    private DatasourceCore datasourceCore;
    private MoguProfileManager profileManager;
    private MoguItemManager itemManager;
    private CustomEntityManager customEntityManager;
    private CommandManager commandManager;
    private PermissionManager permissionManager;
    private JobManager jobManager;
    private WordFilterTrie wordFilter;
    private CustomRecipes customRecipes;

    private final Random random = new Random();
    private long lastWorldTime;

    private ProtocolManager protocolManager;

    private int currentDay; // Days since equinox

    @Override
    public void onLoad() {
        customEntityManager = new CustomEntityManager(this);
    }

    @Override
    public void onEnable() {
        // Create data folder.
        if (!getDataFolder().exists())
            if (!getDataFolder().mkdir())
                getSLF4JLogger().error("There was a problem creating the plugin Data Folder.");

        Plugin protocolLib = getServer().getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib != null && protocolLib.isEnabled()) {
            protocolManager = ProtocolLibrary.getProtocolManager();
        } else {
            getSLF4JLogger().warn("ProtocolLib was not found! Continuing without it~");
        }

        wordFilter = new WordFilterTrie(this);

        // Initialise stuff
        blockTracker = new BlockTracker(this);
        permissionManager = new PermissionManager(this);
        new ListenerManager(this);
        jobManager = new JobManager(this);
        datasourceCore = new DatasourceCore(this);
        profileManager = new MoguProfileManager(this);
        itemManager = new MoguItemManager(this);
        itemManager.registerItems();

        commandManager = new CommandManager(this);

        customRecipes = new CustomRecipes(this);

        startMainLoop();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        getDatasourceCore().saveAll();
        blockTracker.save();
        commandManager.unregisterCommands();
        profileManager.onDisable();
    }

    private Map<Player, Long> blockingSince;

    private void startMainLoop() {
        blockingSince = new HashMap<>();

        getServer().getScheduler().runTaskTimer(this, () -> getServer().getOnlinePlayers().forEach(player -> {
            boolean was = blockingSince.containsKey(player);
            if (was && !player.isBlocking()) {
                blockingSince.remove(player);
            } else if (!was && player.isBlocking()) {
                blockingSince.put(player, System.currentTimeMillis());
            } else {
                return;
            }

            getServer().getPluginManager().callEvent(new PlayerToggleShieldEvent(player));
        }), 1L, 1L);

        getServer().getScheduler().runTaskTimer(this, () -> getServer().getOnlinePlayers().forEach((player) -> {
            // While sleeping, regen
            if (player.isSleeping() && player.getSleepTicks() >= 40)
                player.heal(1);
        }), 20L, 20L);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {

            // EVERY NEW IRL DAY
            checkForDayChange();

            // EVERY NEW IN-GAME DAY
            World world = getServer().getWorlds().getFirst();
            long worldTime = world.getFullTime();
            if (lastWorldTime != 0 && Math.floorDiv(lastWorldTime, 24000) != Math.floorDiv(worldTime, 24000))
                getServer().getPluginManager().callEvent(new WorldDayChangeEvent(world, lastWorldTime, worldTime));
            lastWorldTime = worldTime;

            getServer().getOnlinePlayers().forEach((player) -> {
                // Operator check
                if (player.isOp() && !permissionManager.isAllowedOp(player.getUniqueId())) {
                    player.kick(Component.text("There was an error loading your Player Profile.", NamedTextColor.RED));
                    player.setOp(false);
                    return;
                }

                MoguProfile.from(player); // keep alive
            });
        }, 20L, 20L);
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public int getCurrentGameDay() {
        World world = getServer().getWorlds().getFirst();
        long worldTime = world.getFullTime();
        return (int) Math.floorDiv(worldTime, 24000);
    }

    public boolean checkForDayChange() {
        int day = (int) Math.floorDiv(System.currentTimeMillis(), 86400000);
        boolean change = getCurrentDay() != day;

        if (change) {
            currentDay = day;
            getProfileManager().resetDailyStats();
            getServer().broadcast(Component.text("Daily Stats have been reset."));
        }

        return change;
    }

    /**
     * Get an online player whether it's by nickname or username.
     */
    public @Nullable Player searchForPlayer(String name) {
        final Collection<? extends Player> online = getServer().getOnlinePlayers();
        List<Player> targets = new ArrayList<>(online);
        int size = targets.size();

        for (int x = -1; ++x < size;) {
            Player p = targets.get(x);
            MoguProfile profile = MoguProfile.from(p);
            if (profile.getDisplayName().equalsIgnoreCase(name) || profile.getName().equalsIgnoreCase(name))
                return p;
        }

        String lowerName = name.toLowerCase();
        if (lowerName.length() >= 2) {
            for (int x = -1; ++x < size;) {
                Player p = targets.get(x);
                MoguProfile profile = MoguProfile.from(p);
                if (profile.getDisplayName().toLowerCase().contains(lowerName) || profile.getName().toLowerCase().contains(lowerName))
                    return p;
            }
        }
        return null;
    }

    public @NotNull Random getRandom() {
        return random;
    }

    public @NotNull BlockTracker getBlockTracker() {
        return blockTracker;
    }

    public @NotNull DatasourceCore getDatasourceCore() {
        return datasourceCore;
    }

    public @NotNull MoguProfileManager getProfileManager() {
        return profileManager;
    }

    public @NotNull MoguItemManager getItemManager() {
        return itemManager;
    }

    public @NotNull CustomEntityManager getCustomEntityManager() {
        return customEntityManager;
    }

    public @NotNull CommandManager getCommandManager() {
        return commandManager;
    }

    public @NotNull PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public @NotNull JobManager getJobManager() {
        return jobManager;
    }

    public @NotNull WordFilterTrie getWordFilter() {
        return wordFilter;
    }

    public @NotNull CustomRecipes getRecipeManager() { return customRecipes; }

    public boolean hasProtocolManager() {
        return protocolManager != null;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    private static final String NAMESPACE = "mogu"; // NEVER CHANGE THIS EVER.

    public static NamespacedKey key(@NotNull String key) {
        return new NamespacedKey(NAMESPACE, key.toLowerCase());
    }

}
