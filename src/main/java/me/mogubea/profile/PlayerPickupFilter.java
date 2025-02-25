package me.mogubea.profile;

import me.mogubea.items.MoguItemManager;
import me.mogubea.utils.Dirty;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PlayerPickupFilter implements Dirty<Boolean> {

    private static MoguItemManager itemManager;
    private static final int MAX_SIZE = 45 * 3;

    private final @NotNull List<@NotNull String> filteredIdentifiers;
    private boolean dirty;

    protected PlayerPickupFilter(@NotNull MoguProfile profile) {
        filteredIdentifiers = profile.getManager().getDatasource().loadPickupBlacklist(profile.getDatabaseId());
        if (itemManager == null) itemManager = profile.getPlugin().getItemManager();
    }

    public boolean removeEntry(@NotNull ItemStack item) {
        boolean makeDirty = filteredIdentifiers.remove(getIdentifier(item));
        if (!isDirty() && makeDirty)
            setDirty(true);

        return makeDirty;
    }

    public boolean addEntry(@NotNull ItemStack item) {
        if (!canPickupItem(item) || size() >= getMaxSize()) return false;
        filteredIdentifiers.add(getIdentifier(item));
        if (!isDirty())
            setDirty(true);

        return true;
    }

    public boolean canPickupItem(@NotNull ItemStack item) {
        return !isFiltering(getIdentifier(item));
    }

    public int size() {
        return filteredIdentifiers.size();
    }

    public int getMaxSize() {
        return MAX_SIZE;
    }

    private boolean isFiltering(@NotNull String id) {
        return filteredIdentifiers.contains(id.toLowerCase());
    }

    private @NotNull String getIdentifier(@NotNull ItemStack item) {
        return itemManager.getIdentifier(item);
    }

    public @UnmodifiableView @NotNull List<@NotNull String> getList() {
        return Collections.unmodifiableList(filteredIdentifiers);
    }

    protected @Unmodifiable @NotNull Set<@NotNull String> getSnapshotSet() {
        return Set.copyOf(filteredIdentifiers);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public Boolean setDirty(boolean dirty) {
        return this.dirty = dirty;
    }

}
