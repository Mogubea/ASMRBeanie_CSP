package me.mogubea.protections;

import me.mogubea.statistics.DirtyInteger;
import me.mogubea.statistics.DirtyVal;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Region {

    private final ProtectionManager manager;
    private final int id;
    private final World world;

    private ConcurrentHashMap<Flag<?>, DirtyVal<?>> flags = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, RegionMember> members = new ConcurrentHashMap<>();
    private DirtyInteger priority = new DirtyInteger(0);

    protected Region(ProtectionManager manager, int id, World world) {
        this.manager = manager;
        this.id = id;
        this.world = world;
    }

    protected @NotNull ProtectionManager manager() {
        return manager;
    }

    public @NotNull World getWorld() {
        return world;
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority.getValue();
    }

    public @NotNull Map<Flag<?>, DirtyVal<?>> getFlags() {
        return this.flags;
    }

//    public @NotNull <T extends Flag<V>, V> V getFlag(T flag) {
//        return getFlag(flag, false);
//    }

}
