package me.mogubea.jobs;

import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class JobBuilder extends Job {

    private final Component why = Component.text("building", NamedTextColor.GRAY);

    protected JobBuilder(@NotNull JobManager manager) {
        super(manager, "Builder", "\uD83D\uDD14", TextColor.color(0x6fdf3f), "builder", "build", "building");
        setDescription(Component.text("Earn some $$$ by constructing things!"));
    }

    @Override
    public void doBuildEvent(@NotNull MoguProfile profile, @NotNull BlockPlaceEvent e) {
        float hardness = e.getBlock().getType().getHardness();
        if (hardness <= 0) return;
        byte maxPayout = 1;

        if (hardness >= 10) maxPayout = 4;
        else if (hardness >= 4) maxPayout = 3;
        else if (hardness >= 1) maxPayout = 2;

        int value = maxPayout == 1 ? 1 : profile.getPlugin().getRandom().nextInt(1, maxPayout + 1);

        payout(profile, value, why, e.getBlock().getLocation().add(0.5, 0.5, 0.5));
    }
}
