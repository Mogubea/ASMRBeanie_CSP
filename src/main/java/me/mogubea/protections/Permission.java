package me.mogubea.protections;

import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Permission {

    private final String identifier;
    private final String displayName;
    private final List<TextComponent> description = new ArrayList<>();

    private Consumer<Region> onUpdate;

    protected Permission(@NotNull String identifier, @NotNull String displayName) {
        this.identifier = identifier;
        this.displayName = displayName;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public Permission setDescription(List<TextComponent> desc) {
        this.description.addAll(desc);
        return this;
    }

    @NotNull
    public List<TextComponent> getDescription() {
        return description;
    }

}
