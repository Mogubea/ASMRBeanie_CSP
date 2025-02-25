package me.mogubea.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import me.mogubea.main.Main;
import me.mogubea.utils.MoguTag;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class MoguItemManager {

    protected static MoguItemManager INSTANCE;

    private final Main plugin;
    private final Random random;
    private final Map<String, MoguItem> itemsByName = new LinkedHashMap<>();

    public MoguItemManager(Main plugin) {
        this.plugin = plugin;
        this.random = plugin.getRandom();
        INSTANCE = this;
    }

    public ItemStack formatItemStack(ItemStack item) {
        if (item == null || !item.getType().isItem()) return item;

        // Remove Repair Cost for now
        if (item.hasData(DataComponentTypes.REPAIR_COST))
                item.unsetData(DataComponentTypes.REPAIR_COST);

        modifyItemDurabilities(item);
        modifyStackSizes(item);
        modifyExistingEnchantments(item);
        modifyBookModelData(item);

        return item;
    }

    private void modifyStackSizes(@NotNull ItemStack item) {
        int newMax = -1;

        switch (item.getType()) {
            case HEAVY_CORE -> newMax = 1;
            case POTION, LINGERING_POTION, SPLASH_POTION, MUSIC_DISC_5, MUSIC_DISC_11, MUSIC_DISC_13, MUSIC_DISC_CAT, MUSIC_DISC_BLOCKS, MUSIC_DISC_CHIRP, MUSIC_DISC_FAR, MUSIC_DISC_MALL,
                 MUSIC_DISC_CREATOR, MUSIC_DISC_CREATOR_MUSIC_BOX, MUSIC_DISC_MELLOHI, MUSIC_DISC_RELIC, MUSIC_DISC_STAL, MUSIC_DISC_STRAD, MUSIC_DISC_WAIT, MUSIC_DISC_WARD,
                 MUSIC_DISC_OTHERSIDE, MUSIC_DISC_PIGSTEP, MUSIC_DISC_PRECIPICE, TRIAL_KEY, OMINOUS_TRIAL_KEY, MILK_BUCKET, OMINOUS_BOTTLE -> newMax = 8;
            case ENDER_EYE, WIND_CHARGE -> newMax = 16;
        }

        if (newMax == -1) return;
        if (!item.isDataOverridden(DataComponentTypes.MAX_STACK_SIZE))
            item.setData(DataComponentTypes.MAX_STACK_SIZE, newMax);
    }

    private void modifyExistingEnchantments(@NotNull ItemStack item) {
        // Add Enchant-ability to the shield.
        if (item.getType() == Material.SHIELD && !item.hasData(DataComponentTypes.ENCHANTABLE)) {
            item.setData(DataComponentTypes.ENCHANTABLE, Enchantable.enchantable(4));
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta.hasEnchants() && meta.hasEnchant(Enchantment.UNBREAKING) && meta.getEnchantLevel(Enchantment.UNBREAKING) > 1)
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);

        if (meta instanceof EnchantmentStorageMeta enchantBook && enchantBook.hasStoredEnchants() && enchantBook.hasStoredEnchant(Enchantment.UNBREAKING) && enchantBook.getStoredEnchantLevel(Enchantment.UNBREAKING) > 1)
            enchantBook.addStoredEnchant(Enchantment.UNBREAKING, 1, true);

        item.setItemMeta(meta);
    }


    private void modifyItemDurabilities(@NotNull ItemStack item) {
        @NotNull Material material = item.getType();
        if (!material.hasDefaultData(DataComponentTypes.MAX_DAMAGE) || item.isDataOverridden(DataComponentTypes.MAX_DAMAGE)) return;

        int newDurability = Objects.requireNonNull(material.getDefaultData(DataComponentTypes.MAX_DAMAGE)) * 2;
        item.setData(DataComponentTypes.MAX_DAMAGE, newDurability);
    }

    private void modifyBookModelData(@NotNull ItemStack item) {
    /*    ItemEnchantments data = item.getDataOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, null);
        if (data == null) return;

        Map<Enchantment, Integer> enchants = data.enchantments();
        ItemMeta meta = item.getItemMeta();

        if (enchants.size() == 1) {
            for (Map.Entry<Enchantment, Integer> enchantt : enchants.entrySet()) {
                Enchantment enchant = enchantt.getKey();
                int modelOffset = Math.min(enchant.getMaxLevel(), enchantt.getValue()) - 1;

                System.out.println(enchant.description());

                if (enchant == Enchantment.AQUA_AFFINITY) meta.setCustomModelData(1);
                else if (enchant == Enchantment.BANE_OF_ARTHROPODS) meta.setCustomModelData(2 + modelOffset);
                else if (enchant == Enchantment.BLAST_PROTECTION) meta.setCustomModelData(7 + modelOffset);
                else if (enchant == Enchantment.BREACH) meta.setCustomModelData(11 + modelOffset);
                else if (enchant == Enchantment.CHANNELING) meta.setCustomModelData(15);
                else if (enchant == Enchantment.VANISHING_CURSE) meta.setCustomModelData(16);
                else if (enchant == Enchantment.DENSITY) meta.setCustomModelData(18 + modelOffset);
                else if (enchant == Enchantment.DEPTH_STRIDER) meta.setCustomModelData(23 + modelOffset);
                else if (enchant == Enchantment.EFFICIENCY) meta.setCustomModelData(26 + modelOffset);

                item.setItemMeta(meta);
                System.out.println("womp: " + meta);
                return;
            }
        }*/
    }

    public void registerItems() {
        if (!MoguItems.INVISIBLE_ITEM_FRAME.getMaterial().isAir())
            plugin.getSLF4JLogger().info("Successfully registered {} custom items", itemsByName.size());
    }
    
    protected <T extends MoguItem> T register(@NotNull T item) {
        if (itemsByName.containsKey(item.getIdentifier())) throw new UnsupportedOperationException("An item with the identifier \"" + item.getIdentifier() + "\" already exists!");
        itemsByName.put(item.getIdentifier(), item);
        item.postCreation();
        return item;
    }

    public @NotNull ItemStack createEnchantBook(Enchantment enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemEnchantments data = ItemEnchantments.itemEnchantments().add(enchant, level).build();
        book.setData(DataComponentTypes.STORED_ENCHANTMENTS, data);

        return formatItemStack(book);
    }

    public @NotNull Material getReplenishableBlockSeed(Material blockType) {
        return switch (blockType) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case POTATOES -> Material.POTATO;
            case CARROTS -> Material.CARROT;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case NETHER_WART -> Material.NETHER_WART;
            case SWEET_BERRY_BUSH -> Material.SWEET_BERRIES;
            case CAVE_VINES_PLANT -> Material.GLOW_BERRIES;
            default -> Material.AIR;
        };
    }

    public @NotNull Collection<MoguItem> getItems() {
        return itemsByName.values();
    }

    /**
     * Trigger any {@link Event} code that the {@link MoguItem} may have.
     * @param customItem The {@link ItemStack}, which may or may not be a {@link MoguItem}
     * @param event The involved {@link Event}
     * @return Whether a Consumer was fired.
     */
    public <E extends Event> boolean doItemEvent(@Nullable ItemStack customItem, @NotNull E event) {
        @Nullable MoguItem item = from(customItem);
        if (item == null) return false;

        return item.doEventFunction(event);
    }

    /**
     * Attempt to grab a {@link MoguItem} instance from an {@link ItemStack}.
     * @param itemStack The ItemStack
     * @return Either the associated {@link MoguItem} or null.
     */
    public @Nullable MoguItem from(ItemStack itemStack) {
        return from(MoguTag.CUSTOM_ITEM_ID.getValue(itemStack, ""));
    }

    /**
     * Attempt to grab a {@link MoguItem} instance from a String.
     * @param identifier The identifier
     * @return Either the associated {@link MoguItem} or null.
     */
    public @Nullable MoguItem from(@NotNull String identifier) {
        return identifier.isEmpty() ? null : itemsByName.get(identifier.toLowerCase());
    }

    public @NotNull String getIdentifier(ItemStack itemStack) {
        MoguItem moguItem = from(itemStack);
        if (moguItem != null) return moguItem.getIdentifier();
        return itemStack.getType().name().toLowerCase();
    }

    public @NotNull ItemStack fromIdentifier(@NotNull String id) {
        MoguItem moguItem = from(id);
        if (moguItem != null) return moguItem.getItemStack();
        try {
            Material basicMaterial = Material.valueOf(id.toUpperCase());
            return new ItemStack(basicMaterial);
        } catch (Exception ignored) {
            return new ItemStack(Material.AIR);
        }
    }

    public @NotNull ItemRarity getItemRarity(ItemStack itemStack) {
        MoguItem moguItem = from(itemStack);
        return moguItem == null ? ItemRarity.getVanillaRarity(itemStack) : moguItem.getRarity();
    }

    public @NotNull Random getRandom() {
        return random;
    }

    protected @NotNull Main getPlugin() {
        return plugin;
    }

    protected static @Nullable MoguItem fromStack(ItemStack itemStack) {
        return INSTANCE.from(itemStack);
    }

    protected static @NotNull String getItemIdentifier(ItemStack itemStack) {
        return INSTANCE.getIdentifier(itemStack);
    }
}
