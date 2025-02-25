package me.mogubea.items;

import me.mogubea.utils.LatinSmall;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.HashMap;

public enum ItemRarity {

    TRASH(TextColor.color(0x585858), 0, 20 * 180),
    COMMON(TextColor.color(0xFEFEFE), 40, 20 * 60),
    UNCOMMON(TextColor.color(0x6EE763), 60, 0),
    REMARKABLE(TextColor.color(0x6CAAE8), 70, 0),
    OUTSTANDING(TextColor.color(0x3232C8), 80, 0),
    EXCEPTIONAL(TextColor.color(0xD92AEC), 90, 0),
    LEGENDARY(TextColor.color(0xFFB80C), 95, 0),

    ADMIN(TextColor.color(0xff5555), 100, 0),

    ;

    private final String name;
    private final String smallName;
    private final TextColor colour;
    private final int chanceToSave;
    private final int extraTicksLived;

    ItemRarity(TextColor colour, int chanceToSave, int extraTicksLived) {
        this.name = name().toLowerCase();
        this.smallName = LatinSmall.translate(name);
        this.colour = colour;
        this.chanceToSave = chanceToSave;
        this.extraTicksLived = extraTicksLived;
    }

    public @NotNull TextColor getColour() {
        return colour;
    }

    public @NotNull String getSmallName() {
        return smallName;
    }

    public @NotNull String getName() {
        return name;
    }

    public boolean isRarity(@NotNull ItemRarity rarity) {
        return this.ordinal() >= rarity.ordinal();
    }

    public ItemRarity nextRarity() {
        if (this == ADMIN) return this;
        return values()[ordinal() + 1];
    }

    private final static HashMap<Material, ItemRarity> vanillaRarities = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 1018055300807913156L;

        {
            for (Material m : Material.values()) {
                if (m.name().endsWith("SHULKER_BOX"))
                    put(m, ItemRarity.REMARKABLE);
                else if (m.name().startsWith("NETHERITE_"))
                    put(m, ItemRarity.UNCOMMON);
                else if (m.name().contains("COMMAND_BLOCK"))
                    put(m, ItemRarity.ADMIN);
                else if (m.name().endsWith("_SPAWN_EGG"))
                    put(m, ItemRarity.ADMIN);
                else if (m.name().startsWith("MUSIC_DISC_"))
                    put(m, ItemRarity.UNCOMMON);
                else if (m.name().endsWith("_SMITHING_TEMPLATE"))
                    put(m, ItemRarity.UNCOMMON);
            }

            put(Material.NAUTILUS_SHELL, ItemRarity.UNCOMMON);
            put(Material.POISONOUS_POTATO, ItemRarity.UNCOMMON);
            put(Material.ENDER_EYE, ItemRarity.UNCOMMON);
            put(Material.ENDER_CHEST, ItemRarity.UNCOMMON);
            put(Material.END_CRYSTAL, ItemRarity.UNCOMMON);
            put(Material.SKELETON_SKULL, ItemRarity.UNCOMMON);
            put(Material.CREEPER_HEAD, ItemRarity.UNCOMMON);
            put(Material.PLAYER_HEAD, ItemRarity.UNCOMMON);
            put(Material.ZOMBIE_HEAD, ItemRarity.UNCOMMON);
            put(Material.WITHER_SKELETON_SKULL, ItemRarity.UNCOMMON);
            put(Material.HEART_OF_THE_SEA, ItemRarity.UNCOMMON);
            put(Material.ANCIENT_DEBRIS, ItemRarity.UNCOMMON);
            put(Material.ECHO_SHARD, ItemRarity.UNCOMMON);
            put(Material.RECOVERY_COMPASS, ItemRarity.UNCOMMON);
            put(Material.DRAGON_HEAD, ItemRarity.UNCOMMON);
            put(Material.TRIDENT, ItemRarity.UNCOMMON);
            put(Material.TRIAL_KEY, ItemRarity.UNCOMMON);

            put(Material.CONDUIT, ItemRarity.REMARKABLE);
            put(Material.VAULT, ItemRarity.REMARKABLE);
            put(Material.HEAVY_CORE, ItemRarity.REMARKABLE);
            put(Material.OMINOUS_TRIAL_KEY, ItemRarity.REMARKABLE);
            put(Material.PIGLIN_HEAD, ItemRarity.REMARKABLE);
            put(Material.DRAGON_EGG, ItemRarity.REMARKABLE);
            put(Material.SPAWNER, ItemRarity.REMARKABLE);
            put(Material.ENCHANTED_GOLDEN_APPLE, ItemRarity.REMARKABLE);
            put(Material.TOTEM_OF_UNDYING, ItemRarity.REMARKABLE);

            put(Material.MUSIC_DISC_PIGSTEP, ItemRarity.REMARKABLE);
            put(Material.MUSIC_DISC_OTHERSIDE, ItemRarity.REMARKABLE);
            put(Material.MUSIC_DISC_CREATOR, ItemRarity.REMARKABLE);
            put(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, ItemRarity.REMARKABLE);
            put(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemRarity.REMARKABLE);
            put(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemRarity.REMARKABLE);
            put(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, ItemRarity.REMARKABLE);

            put(Material.ELYTRA, ItemRarity.OUTSTANDING);
            put(Material.NETHER_STAR, ItemRarity.OUTSTANDING);
            put(Material.BEACON, ItemRarity.OUTSTANDING);
            put(Material.MACE, ItemRarity.OUTSTANDING);

            put(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemRarity.OUTSTANDING);

            put(Material.LIGHT, ItemRarity.ADMIN);
            put(Material.END_PORTAL_FRAME, ItemRarity.ADMIN);
            put(Material.BEDROCK, ItemRarity.ADMIN);
            put(Material.DEBUG_STICK, ItemRarity.ADMIN);
            put(Material.BARRIER, ItemRarity.ADMIN);
            put(Material.STRUCTURE_BLOCK, ItemRarity.ADMIN);
            put(Material.STRUCTURE_VOID, ItemRarity.ADMIN);
            put(Material.JIGSAW, ItemRarity.ADMIN);
            put(Material.KNOWLEDGE_BOOK, ItemRarity.ADMIN);
            put(Material.TRIAL_SPAWNER, ItemRarity.ADMIN);
            put(Material.PETRIFIED_OAK_SLAB, ItemRarity.ADMIN);
        }
    };

    public int getChanceToSave() {
        return chanceToSave;
    }

    public int getExtraTicksLived() {
        return extraTicksLived;
    }

    public static @NotNull ItemRarity getVanillaRarity(ItemStack itemStack) {
        if (itemStack == null) return COMMON;
        ItemRarity rarity = getVanillaRarity(itemStack.getType());

        if (itemStack.getItemMeta() instanceof ArmorMeta meta && meta.hasTrim())
            rarity = rarity.nextRarity();
        if (itemStack.getItemMeta().hasEnchants())
            rarity = rarity.nextRarity();

        return rarity;
    }

    public static @NotNull ItemRarity getVanillaRarity(@NotNull Material material) {
        return vanillaRarities.getOrDefault(material, ItemRarity.COMMON);
    }

}
