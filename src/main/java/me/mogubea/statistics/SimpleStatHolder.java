package me.mogubea.statistics;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleStatHolder<T extends Number> {

    final private Map<SimpleStatType, Map<String, DirtyVal<T>>> stats = new ConcurrentHashMap<>();

    protected SimpleStatHolder() {
        clear();
    }

    /**
     * @param type The type of stat
     * @param name The name of the stat
     * @return The value of this stat
     */
    @SuppressWarnings("unchecked")
    public T getStat(@NotNull SimpleStatType type, @NotNull String name) {
        name = name.toUpperCase();
        if (!stats.get(type).containsKey(name))
            return (T)(Number)0;
        return stats.get(type).get(name).getValue();
    }

    public void setStat(@NotNull SimpleStatType type, @NotNull String name, T value) {
        setStat(type, name, value, true);
    }

    /**
     * @param type The type of stat
     * @param name The name of the stat
     * @param value The value to set the stat to
     * @param dirty Whether this should be flagged as dirty and saved in the next save cycle
     */
    @SuppressWarnings("unchecked")
    public void setStat(@NotNull SimpleStatType type, @NotNull String name, T value, boolean dirty) {
        name = name.toUpperCase();
        Map<String, DirtyVal<T>> ack = stats.get(type);

        boolean put = !ack.containsKey(name);
        DirtyVal<?> di = (put ? newDirty(value).setDirty(dirty) : ack.get(name).setValue(value, dirty));
        if (put)
            ack.put(name, (DirtyVal<T>) di);
    }

    /**
     * @param type The type of stat
     * @param name The name of the stat
     * @param add The amount to add to the specified stat
     */
    @SuppressWarnings("unchecked")
    public void addToStat(@NotNull SimpleStatType type, @NotNull String name, T add) {
        name = name.toUpperCase();
        Map<String, DirtyVal<T>> ack = stats.get(type);

        boolean put = !ack.containsKey(name);
        DirtyVal<?> di = put ? newDirty(add).setDirty(true) : ack.get(name).addToValue(add);
        if (put)
            ack.put(name, (DirtyVal<T>) di);
    }

    /**
     * @return The map containing all the stored values
     */
    public Map<SimpleStatType, Map<String, DirtyVal<T>>> getMap() {
        return stats;
    }

    protected void clear() {
        stats.clear();
        final SimpleStatType[] types = SimpleStatType.values();
        for (SimpleStatType type : types)
            stats.put(type, new ConcurrentHashMap<>());
    }

    private DirtyVal<?> newDirty(T value) {
        if (value instanceof Integer i)
            return new DirtyInteger(i);
        if (value instanceof Long l)
            return new DirtyLong(l);
        if (value instanceof Double d)
            return new DirtyDouble(d);
        if (value instanceof Float f)
            return new DirtyFloat(f);
        return null;
    }

}
