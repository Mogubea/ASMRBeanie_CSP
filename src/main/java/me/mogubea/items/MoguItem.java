package me.mogubea.items;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.mogubea.main.Main;
import me.mogubea.utils.MoguTag;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class MoguItem {

    private final @NotNull MoguItemManager      manager;
    private final @NotNull String               identifier;
    private final @NotNull String               displayName;
    private final @NotNull Material             material;
    private final @NotNull ItemStack            itemStack;

    private final @NotNull Set<EventHandler<?>> eventFunctions = new HashSet<>();
    private final @NotNull Set<DataValue<?>>    overriddenValues = new HashSet<>();

    private @NotNull ItemRarity                 rarity = ItemRarity.COMMON;

    private boolean                             enabled = true;
    private boolean                             trackCreationTime;

    protected MoguItem(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull Material material) {
        this.manager = manager;
        this.identifier = identifier.toLowerCase();
        this.displayName = displayName;
        this.material = material;
        this.trackCreationTime = material.hasDefaultData(DataComponentTypes.MAX_DAMAGE);
        itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> MoguTag.CUSTOM_ITEM_ID.applyTag(meta, identifier));

        addData(DataComponentTypes.ITEM_NAME, Component.text(getDisplayName(itemStack)));
    }

    /**
     * @return The identifier of this item.
     */
    public @NotNull String getIdentifier() {
        return identifier;
    }

    /**
     * @return The display name of this item.
     */
    public @NotNull String getDisplayName(ItemStack item) {
        return displayName;
    }

    /**
     * @return The material of this item.
     */
    public @NotNull Material getMaterial() {
        return material;
    }

    /**
     * @return The rarity of this item.
     */
    public @NotNull ItemRarity getRarity() {
        return rarity;
    }

    protected @NotNull <T extends MoguItem> T rarity(@NotNull ItemRarity rarity) {
        this.rarity = rarity;
        return (T) this;
    }

    protected final @NotNull <T extends MoguItem, O> T addData(DataComponentType.@NotNull Valued<O> type, @Nullable O value) {
        overriddenValues.add(new DataValue<>(type, value));
        return (T) this;
    }

    protected final <T extends MoguItem, E extends Event> T addEvent(@NotNull Class<E> event, @NotNull Consumer<E> consumer) {
        this.eventFunctions.add(new EventHandler<>(event, consumer));
        return (T) this;
    }

    /**
     * @return The ItemStack
     */
    public @NotNull ItemStack getItemStack() {
        return getItemStack(1);
    }

    /**
     * @return The ItemStack
     */
    public @NotNull ItemStack getItemStack(int quantity) {
        if (quantity > itemStack.getMaxStackSize() || quantity < 1)
            quantity = 1;

        ItemStack itemStack = this.itemStack.clone();
        itemStack.setAmount(quantity);

        return itemStack;
    }

    protected @NotNull <T extends MoguItem> T setCustomModelData(int data) {
        itemStack.editMeta(meta -> meta.setCustomModelData(data));
        return (T) this;
    }

    protected @NotNull MoguItem setTrackCreation() {
        this.trackCreationTime = true;
        return this;
    }

    public boolean shouldTrackCreation() {
        return trackCreationTime;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected @NotNull Random getRandom() {
        return manager.getRandom();
    }

    protected @NotNull Main getPlugin() {
        return manager.getPlugin();
    }

    protected @NotNull MoguItemManager getManager() {
        return manager;
    }

    protected <E extends Event> boolean doEventFunction(@NotNull E event) {
        for (EventHandler<?> handler : eventFunctions) {
            if (!handler.type().isInstance(event)) continue;

            ((Consumer<Event>)handler.consumer()).accept(event);
            return true;
        }
        return false;
    }

    protected void postCreation() {
        overriddenValues.forEach(value -> value.applyTo(itemStack));
        manager.formatItemStack(itemStack);
    }

    private record DataValue<T>(@NotNull DataComponentType.Valued<T> key, T value) {
        public void applyTo(@NotNull ItemStack itemStack) {
            itemStack.setData(key, value);
        }

        public boolean equals(Object obj) {
            return this == obj || (obj instanceof DataValue<?> other && other.key.equals(key));
        }

        public int hashCode() {
            return key.hashCode();
        }
    }

    private record EventHandler<E extends Event>(@NotNull Class<E> type, @NotNull Consumer<E> consumer) {
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof EventHandler<?> other && other.type.equals(type));
        }

        public int hashCode() {
            return type.hashCode();
        }
    }

    public static @Nullable MoguItem from(ItemStack itemStack) {
        return MoguItemManager.fromStack(itemStack);
    }

    public static @NotNull String identifier(ItemStack itemStack) {
        return MoguItemManager.getItemIdentifier(itemStack);
    }

}
