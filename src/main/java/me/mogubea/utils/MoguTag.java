package me.mogubea.utils;

import me.mogubea.main.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MoguTag<T> {

    public static final MoguTag<Boolean> UNNATURAL_KILL, WANDERING_TRADER_LOCK;
    public static final MoguTag<Integer> ENTITY_OWNER_ID;
    public static final MoguTag<String> CUSTOM_ITEM_ID;

    static {
        UNNATURAL_KILL = tag("unnatural_kill", PersistentDataType.BOOLEAN);
        WANDERING_TRADER_LOCK = tag("wandering_trader_lock", PersistentDataType.BOOLEAN);
        ENTITY_OWNER_ID = tag("vehicle_owner_id", PersistentDataType.INTEGER);
        CUSTOM_ITEM_ID = tag("custom_item_id", PersistentDataType.STRING);
    }

    private static <T> MoguTag<T> tag(@NotNull String key, @NotNull PersistentDataType<?, T> type) {
        return new MoguTag<>(Main.key(key), type);
    }

    private final @NotNull NamespacedKey key;
    private final @NotNull PersistentDataType<?, T> dataType;

    private MoguTag(@NotNull NamespacedKey key, @NotNull PersistentDataType<?, T> type) {
        this.key = key;
        this.dataType = type;
    }

    public @NotNull NamespacedKey key() {
        return key;
    }

    public @NotNull PersistentDataType<?, T> type() {
        return dataType;
    }

    public void applyTag(@NotNull PersistentDataHolder holder, T value) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        container.set(key(), type(), value);
    }

    public @Nullable T getValue(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;
        return getValue(itemStack.getItemMeta());
    }

    public @NotNull T getValue(ItemStack itemStack, T defaultValue) {
        if (itemStack == null || !itemStack.hasItemMeta()) return defaultValue;
        T value = getValue(itemStack);
        return value != null ? value : getValue(itemStack.getItemMeta(), defaultValue);
    }

    public @Nullable T getValue(@NotNull PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().get(key(), type());
    }

    public @NotNull T getValue(@NotNull PersistentDataHolder holder, T defaultValue) {
        return holder.getPersistentDataContainer().getOrDefault(key(), type(), defaultValue);
    }

    public boolean hasTag(@NotNull PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().has(key(), type());
    }

}
