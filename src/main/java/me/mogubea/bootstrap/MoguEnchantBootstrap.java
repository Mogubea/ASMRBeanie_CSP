package me.mogubea.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEntryAddEvent;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public final class MoguEnchantBootstrap {

    private final @NotNull LifecycleEventManager<@NotNull BootstrapContext> manager;
    private final @NotNull Map<TagKey<Enchantment>, Collection<TypedKey<Enchantment>>> map;
    private final @NotNull Map<TagKey<ItemType>, Collection<TypedKey<ItemType>>> itemTypeKeys;

    MoguEnchantBootstrap(@NotNull BootstrapContext context) {
        this.manager = context.getLifecycleManager();
        this.itemTypeKeys = new HashMap<>();
        this.map = new HashMap<>();

        // Tags
        TagKey<ItemType> SHIELDS = itemTypeTagKey("shields", Set.of(ItemTypeKeys.SHIELD));
        TagKey<ItemType> PICK_AND_AXES = itemTypeTagKey("pick_and_axes", Set.of(ItemTypeKeys.NETHERITE_PICKAXE, ItemTypeKeys.DIAMOND_PICKAXE, ItemTypeKeys.GOLDEN_PICKAXE, ItemTypeKeys.IRON_PICKAXE, ItemTypeKeys.STONE_PICKAXE, ItemTypeKeys.WOODEN_PICKAXE, ItemTypeKeys.NETHERITE_AXE, ItemTypeKeys.DIAMOND_AXE, ItemTypeKeys.GOLDEN_AXE, ItemTypeKeys.IRON_AXE, ItemTypeKeys.STONE_AXE, ItemTypeKeys.WOODEN_AXE));

        // Register New Enchantments
        registerEnchantment("prospector", Component.text("§f\uDBA0\uDC08 §rProspector§r"), ItemTypeTagKeys.PICKAXES,
                Set.of(EnchantmentTagKeys.IN_ENCHANTING_TABLE, EnchantmentTagKeys.TREASURE, EnchantmentTagKeys.DOUBLE_TRADE_PRICE, EnchantmentTagKeys.ON_TRADED_EQUIPMENT),
                builder -> builder.activeSlots(EquipmentSlotGroup.MAINHAND).weight(2).maxLevel(5).anvilCost(2).minimumCost(cost(22, 5)).maximumCost(cost(60, 5)));

        registerEnchantment("treecapitator", Component.text("§f\uDBA0\uDC08 §rTreecapitator§r"), ItemTypeTagKeys.AXES,
                Set.of(EnchantmentTagKeys.IN_ENCHANTING_TABLE, EnchantmentTagKeys.TREASURE, EnchantmentTagKeys.DOUBLE_TRADE_PRICE, EnchantmentTagKeys.ON_TRADED_EQUIPMENT),
                builder -> builder.activeSlots(EquipmentSlotGroup.MAINHAND).weight(2).maxLevel(5).anvilCost(2).minimumCost(cost(22, 5)).maximumCost(cost(60, 5)));

        registerEnchantment("throwaxe", Component.text("§f\uDBA0\uDC29 §rThrowaxe§r"), PICK_AND_AXES,
                Set.of(EnchantmentTagKeys.NON_TREASURE),
                builder -> builder.activeSlots(EquipmentSlotGroup.HAND).weight(1).maxLevel(1).anvilCost(25).minimumCost(cost(60, 10)).maximumCost(cost(100, 10)));

        registerEnchantment("swift_sprint", Component.text("§f\uDBA0\uDC25 §rSwift Sprint§r"), ItemTypeTagKeys.ENCHANTABLE_LEG_ARMOR,
                Set.of(EnchantmentTagKeys.IN_ENCHANTING_TABLE, EnchantmentTagKeys.TREASURE, EnchantmentTagKeys.ON_TRADED_EQUIPMENT, EnchantmentTagKeys.ON_RANDOM_LOOT),
                builder -> builder.activeSlots(EquipmentSlotGroup.LEGS).weight(2).maxLevel(3).anvilCost(3).minimumCost(cost(15, 5)).maximumCost(cost(35, 5))
                        .exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.SWIFT_SNEAK)));

        registerEnchantment("incendiary", Component.text("§f\uDBA0\uDC0B §rIncendiary§r"), SHIELDS,
                Set.of(EnchantmentTagKeys.IN_ENCHANTING_TABLE),
                builder -> builder.activeSlots(EquipmentSlotGroup.HAND).weight(3).maxLevel(1).anvilCost(2).minimumCost(cost(20, 5)).maximumCost(cost(50, 5)));

        registerEnchantment("replenish", Component.text("§f\uDBA0\uDC14 §rReplenish§r"), ItemTypeTagKeys.HOES,
                Set.of(EnchantmentTagKeys.DOUBLE_TRADE_PRICE, EnchantmentTagKeys.ON_TRADED_EQUIPMENT, EnchantmentTagKeys.ON_RANDOM_LOOT),
                builder -> builder.activeSlots(EquipmentSlotGroup.HAND).weight(1).maxLevel(1).anvilCost(5).minimumCost(cost(40, 5)).maximumCost(cost(50, 5)));

        registerEnchantment("soft_soles", Component.text("§f\uDBA0\uDC14 §rSoft Soles§r"), ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR,
                Set.of(EnchantmentTagKeys.ON_TRADED_EQUIPMENT, EnchantmentTagKeys.TRADEABLE, EnchantmentTagKeys.IN_ENCHANTING_TABLE, EnchantmentTagKeys.ON_RANDOM_LOOT, EnchantmentTagKeys.TREASURE),
                builder -> builder.activeSlots(EquipmentSlotGroup.FEET).weight(6).maxLevel(1).anvilCost(1).minimumCost(cost(6, 10)).maximumCost(cost(16, 10)));

        registerEnchantment("spiked", Component.text("§f\uDBA0\uDC26 §rSpiked§r"), SHIELDS,
                Set.of(EnchantmentTagKeys.ON_TRADED_EQUIPMENT, EnchantmentTagKeys.TRADEABLE, EnchantmentTagKeys.IN_ENCHANTING_TABLE, EnchantmentTagKeys.ON_RANDOM_LOOT, EnchantmentTagKeys.TREASURE),
                builder -> builder.activeSlots(EquipmentSlotGroup.HAND).weight(3).maxLevel(3).anvilCost(1).minimumCost(cost(20, 5)).maximumCost(cost(30, 5)));

//        registerEnchantment("experienced", Component.text("§f\uDBA0\uDC16 §rExperienced§r"), ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR,
//                Set.of(EnchantmentTagKeys.NON_TREASURE),
//                builder -> builder.activeSlots(EquipmentSlotGroup.HAND).weight(1).maxLevel(3).anvilCost(5).minimumCost(cost(30, 10)).maximumCost(cost(80, 10)));

        registerEnchantment("disrepair_curse", Component.text("§f\uDBA0\uDC04 §rCurse of Disrepair§r"), ItemTypeTagKeys.ENCHANTABLE_DURABILITY,
                Set.of(EnchantmentTagKeys.CURSE, EnchantmentTagKeys.DOUBLE_TRADE_PRICE, EnchantmentTagKeys.ON_TRADED_EQUIPMENT, EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT, EnchantmentTagKeys.ON_RANDOM_LOOT),
                builder -> builder.activeSlots(EquipmentSlotGroup.ANY).weight(3).maxLevel(1).anvilCost(2).minimumCost(cost(20, 10)).maximumCost(cost(30, 10))
                        .exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.MENDING)));

        // Update Enchantments
        updateEnchantment(EnchantmentKeys.UNBREAKING, builder -> builder.builder().weight(4).maxLevel(1).anvilCost(5).minimumCost(cost(15, 15)).maximumCost(cost(45, 15)));
        updateEnchantment(EnchantmentKeys.MENDING, builder -> builder.builder().weight(1));

        // Register tags
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT), event -> map.forEach(event.registrar()::addToTag));
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ITEM), event -> itemTypeKeys.forEach(event.registrar()::addToTag));
    }

    private void registerEnchantment(@NotNull String key, @NotNull Component displayName, @NotNull TagKey<ItemType> supportedItems, @Nullable Set<TagKey<Enchantment>> tags, @NotNull Consumer<EnchantmentRegistryEntry.Builder> builder) {
        // Register the enchantment
        final var enchantKey = TypedKey.create(RegistryKey.ENCHANTMENT, Main.key(key));
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> event.registry().register(enchantKey, builder.andThen(b1 -> b1.description(displayName).supportedItems(event.getOrCreateTag(supportedItems))))));

        // Store tags in a map for efficient registration of enchantment tags
        if (tags == null || tags.isEmpty()) return;
        tags.forEach(enchantmentTagKey -> {
            var keys = map.getOrDefault(enchantmentTagKey, new ArrayList<>());
            keys.add(enchantKey);
            map.put(enchantmentTagKey, keys);
        });
    }

    private void updateEnchantment(@NotNull TypedKey<Enchantment> enchantment, @NotNull LifecycleEventHandler<? super RegistryEntryAddEvent<Enchantment, EnchantmentRegistryEntry.@NotNull Builder>> builder) {
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(builder).filter(enchantment));
    }

    private @NotNull TagKey<ItemType> itemTypeTagKey(@NotNull String key, @NotNull Set<TypedKey<ItemType>> items) {
        TagKey<ItemType> tagKey = ItemTypeTagKeys.create(Main.key(key));
        itemTypeKeys.put(tagKey, items);
        return tagKey;
    }

    private @NotNull EnchantmentRegistryEntry.EnchantmentCost cost(int level, int increasePerLevel) {
        return EnchantmentRegistryEntry.EnchantmentCost.of(level, increasePerLevel);
    }


    // .exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.SILK_TOUCH)));

}