package me.mogubea.listeners;

import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class ConnectionListener extends EventListener {

    protected ConnectionListener(@NotNull ListenerManager manager) {
        super(manager);
    }

    @EventHandler
    @SuppressWarnings("removal")
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        try {
            MoguProfile profile = plugin.getProfileManager().getMoguProfile(e.getUniqueId(), e.getName());
            profile.updateRealName(e.getName());
            e.getPlayerProfile().setName(profile.getDisplayName());
        } catch (Exception ex) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("There was a problem loading your Player Profile.", NamedTextColor.RED));
            plugin.getSLF4JLogger().error("Error Loading Profile", ex);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
        MoguProfile profile = MoguProfile.from(e.getPlayer());
        plugin.getProfileManager().initScoreboard(e.getPlayer());
        e.joinMessage(profile.getColouredName().append(Component.text(" joined the game, nya.", NamedTextColor.YELLOW)));

        plugin.getRecipeManager().keys().forEach(key -> {
            if (!e.getPlayer().hasDiscoveredRecipe(key))
                e.getPlayer().discoverRecipe(key);
        });

        // Format Inventory
        e.getPlayer().getInventory().forEach(this::formatItem);
        e.getPlayer().getEnderChest().forEach(this::formatItem);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        e.getPlayer().closeInventory(InventoryCloseEvent.Reason.DISCONNECT);
        MoguProfile profile = MoguProfile.from(e.getPlayer());
        e.quitMessage(profile.getColouredName().append(Component.text(" left the game, nya.", NamedTextColor.YELLOW)));
    }

}
