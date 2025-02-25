package me.mogubea.jobs;

import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

public class JobFisherman extends Job {

    protected JobFisherman(@NotNull JobManager manager) {
        super(manager, "Fisherman", "\uD83C\uDFA3", TextColor.color(0x4f8fdf), "fish", "fishing", "fisherman");
        setDescription(Component.text("Earn $$$ by reeling in fish from the world's lakes and oceans!"));
    }

    @Override
    public void doFishingEvent(@NotNull MoguProfile profile, @NotNull PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(e.getCaught() instanceof Item item)) return;

        double value = getValue(item.getItemStack().getType());
        if (value < 1) return;

        payout(profile, getRandom().nextInt((int) value, (int) (value * 1.5D)), Component.text("catching a ", NamedTextColor.GRAY).append(Component.translatable(item.getItemStack().translationKey(), NamedTextColor.WHITE)), e.getCaught().getLocation().add(0, 0.5, 0));
    }

    @Override
    public void doKillEntityEvent(@NotNull MoguProfile profile, @NotNull EntityDeathEvent e) {
        double value = switch (e.getEntityType()) {
            case SQUID -> 12;
            case AXOLOTL -> 15;
            case COD -> 18;
            case GLOW_SQUID -> 22;
            case PUFFERFISH -> 30;
            default -> 0;
        };

        if (value == 0) return;
        payout(profile, (int) (value * getRandom().nextDouble(0.9, 1.4)), Component.text("killing a ", NamedTextColor.GRAY).append(e.getEntity().name().color(NamedTextColor.WHITE)), e.getEntity().getLocation().add(0, e.getEntity().getHeight() / 2, 0));
    }

    private int getValue(@NotNull Material fishType) {
        return switch (fishType) {
            case COD, COOKED_COD -> 20;
            case SALMON, COOKED_SALMON -> 30;
            case PUFFERFISH -> 70;
            case TROPICAL_FISH -> 150;
            default -> 0;
        };
    }

}
