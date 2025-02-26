package me.mogubea.jobs;

import me.mogubea.profile.MoguProfile;
import me.mogubea.utils.MoguTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

public class JobHunter extends Job {

    protected JobHunter(@NotNull JobManager manager) {
        super(manager, "Hunter", "\uD83C\uDFF9", TextColor.color(0x9f2f3f), "hunter", "fighter", "butcher", "murderer", "killer", "hunt", "fight", "hunteren", "fighten");
        setDescription(Component.text("Earn $$$ for slaying the hostile creatures that plague these lands!"));
    }

    @Override
    public void doKillEntityEvent(@NotNull MoguProfile profile, @NotNull EntityDeathEvent e) {
        double value = getValue(e.getEntity());
        if (value <= 0) return;

        if (MoguTag.UNNATURAL_KILL.hasTag(e.getEntity())) return;

        if (e.getEntity().getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER)
            value *= 0.33;

        payout(profile, (int) (value * getRandom().nextDouble(0.9, 1.25)), Component.text("slaying a ", NamedTextColor.GRAY).append(e.getEntity().name().color(NamedTextColor.RED)), e.getEntity().getLocation().add(0, e.getEntity().getHeight() / 2, 0));
    }

    private double getValue(@NotNull LivingEntity entity) {
        return switch (entity.getType()) {
            case MAGMA_CUBE, SLIME -> 6 * ((Slime)entity).getSize();
            case ZOMBIE, SPIDER, CAVE_SPIDER, SILVERFISH -> 12;
            case ZOMBIE_VILLAGER, HUSK -> 14;
            case PIGLIN, CREEPER, DROWNED, HOGLIN, POLAR_BEAR -> 15;
            case SKELETON, PHANTOM, BLAZE, BOGGED, GUARDIAN, STRAY -> 18;
            case PILLAGER, ZOMBIFIED_PIGLIN, CREAKING -> 22;
            case WOLF -> ((Wolf)entity).getOwner() == null ? 12 : 0;
            case WITHER_SKELETON, ENDERMAN, ENDERMITE -> 25;
            case SHULKER, BREEZE -> 30;
            case WITCH -> 40;
            case ZOGLIN -> 50;
            case PIGLIN_BRUTE, VINDICATOR, VEX -> 65;
            case GHAST, RAVAGER, EVOKER -> 85;
            case WARDEN, ELDER_GUARDIAN, ILLUSIONER -> 150;
            case ENDER_DRAGON -> 4000;
            case WITHER -> 5000;
            default -> 0;
        };
    }

}
