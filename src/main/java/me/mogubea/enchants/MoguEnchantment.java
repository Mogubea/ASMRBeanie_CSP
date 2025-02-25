package me.mogubea.enchants;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.mogubea.main.Main;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public abstract class MoguEnchantment extends Enchantment {

    public static final @NotNull Enchantment PROSPECTOR = get("prospector");
    public static final @NotNull Enchantment INCENDIARY = get("incendiary");
    public static final @NotNull Enchantment SWIFT_SPRINT = get("swift_sprint");
    public static final @NotNull Enchantment TREECAPITATOR = get("treecapitator");
    public static final @NotNull Enchantment THROWAXE = get("throwaxe");
    public static final @NotNull Enchantment SOFT_SOLES = get("soft_soles");
    public static final @NotNull Enchantment REPLENISH = get("replenish");
    public static final @NotNull Enchantment SPIKED = get("spiked");

    public static final @NotNull Enchantment DISREPAIR_CURSE = get("disrepair_curse");

    private static @NotNull Enchantment get(@NotNull String key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(Main.key(key));
    }

}