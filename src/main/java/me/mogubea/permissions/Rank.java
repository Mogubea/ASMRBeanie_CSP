package me.mogubea.permissions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class Rank {

    private final @NotNull String id;

    private @NotNull String name;
    private @NotNull TextColor colour;
    private @NotNull TextComponent component;

    protected Rank(@NotNull String id, @NotNull String name, TextColor colour) {
        this.id = id;
        this.name = name;
        this.colour = colour;

        updateComponent();
    }

    private void updateComponent() {
        this.component = Component.text(name, colour);
    }

    private @NotNull TextComponent getComponent() {
        return component;
    }

}
