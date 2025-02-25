package me.mogubea.items;

import org.bukkit.Material;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class MoguItems {

    private static final MoguItemManager manager = MoguItemManager.INSTANCE;

    public static final @NotNull MoguItem INVISIBLE_ITEM_FRAME, INVISIBLE_GLOWING_ITEM_FRAME;

    static {
        INVISIBLE_ITEM_FRAME = registerItem(new MoguItem(manager, "invisible_item_frame", "Invisible Item Frame", Material.ITEM_FRAME)
                .addEvent(HangingPlaceEvent.class, (event) -> event.getEntity().setInvisible(true)));
        INVISIBLE_GLOWING_ITEM_FRAME = registerItem(new MoguItem(manager, "invisible_glowing_item_frame", "Invisible Glowing Item Frame", Material.GLOW_ITEM_FRAME)
                .addEvent(HangingPlaceEvent.class, (event) -> event.getEntity().setInvisible(true)));
    }

    private static @NotNull <T extends MoguItem> T registerItem(@NotNull T item) {
        return manager.register(item);
    }

}
