package me.mogubea.jobs;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UnstableApiUsage")
public class JobCleric extends Job {

    public static final NamespacedKey KEY_FRESH_POTION = Main.key("FRESH_FOR_CLERIC");
    private final Component enchantComponent = Component.text("enchanting", NamedTextColor.GRAY);

    protected JobCleric(@NotNull JobManager manager) {
        super(manager, "Cleric", "⚗", TextColor.color(0xfd5599), "cleric", "enchanter", "brewer", "witch", "wizard", "potion", "clericen", "enchanten", "brewen", "librarian");
        setDescription(Component.text("Earn $$$ by brewing potions and enchanting items!"));
    }

    @Override
    public void doEnchantEvent(@NotNull MoguProfile profile, @NotNull EnchantItemEvent e) {
        AtomicInteger value = new AtomicInteger(e.getExpLevelCost() * 10);
        e.getEnchantsToAdd().forEach(((enchantment, integer) -> value.addAndGet(enchantment.getAnvilCost() * integer)));

        float enchanterLevel = e.getEnchanter().getLevel();
        float multiplier = 1F;
        if (enchanterLevel > 16) multiplier += 0.2F;
        if (enchanterLevel > 31) multiplier += 0.2F;
        if (enchanterLevel > 32) multiplier += ((enchanterLevel-32F) / 100F);
        if (e.getItem().getType() == Material.ENCHANTED_BOOK) multiplier += 0.1F;

        payout(profile, (int) (value.get() * multiplier), enchantComponent, e.getEnchantBlock().getLocation());
    }

    @Override
    public void doFreshPotionEvent(@NotNull MoguProfile profile, @NotNull ItemStack itemStack, Location blockLocation) {
        int value = (int) ((double)getPotionValue(itemStack) * getRandom().nextDouble(0.8, 1.5));
        if (value <= 0) return;

        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        if (contents == null || contents.potion() == null) return; // Isn't, but, whatever, shut up IDE

        Component key = Component.translatable(itemStack.getType().getItemTranslationKey() + ".effect." + contents.potion().key().value().toLowerCase(), NamedTextColor.WHITE);

        payout(profile, value, Component.text("brewing a ", NamedTextColor.GRAY).append(key), blockLocation);
    }

    private int getPotionValue(@NotNull ItemStack itemStack) {
        if (itemStack.getType() == Material.SPLASH_POTION) return 6; // 6 for making it splash
        if (itemStack.getType() == Material.LINGERING_POTION) return 14; // 14 for making it lingering

        PotionContents contents = itemStack.getDataOrDefault(DataComponentTypes.POTION_CONTENTS, null);
        if (contents == null) return 0;

        PotionType type = contents.potion();

        if (type == null) return 0;
        if (type.name().startsWith("LONG_") || type.name().startsWith("STRONG_")) return 6; // Give 6 for buffing the potion.

        return switch (type) {
            case MUNDANE, THICK -> 2;
            case AWKWARD -> 4;
            case INFESTED -> 8;
            case SWIFTNESS -> 12;
            case POISON, HEALING, HARMING, STRENGTH -> 14;
            case NIGHT_VISION, WEAKNESS, OOZING -> 16;
            case WEAVING -> 17;
            case INVISIBILITY -> 22;
            case FIRE_RESISTANCE -> 23;
            case SLOWNESS -> 26;
            case SLOW_FALLING -> 28;
            case WATER_BREATHING -> 33;
            case WIND_CHARGED -> 38;
            case LEAPING -> 48;
            case REGENERATION -> 57;
            case TURTLE_MASTER -> 85;
            case LUCK -> 100;
            default -> 0;
        };
    }


}
