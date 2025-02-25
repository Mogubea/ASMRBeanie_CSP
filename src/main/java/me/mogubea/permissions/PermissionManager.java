package me.mogubea.permissions;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PermissionManager {
	
	private final Main plugin;
	private final HashMap<UUID, PermissionAttachment> permissions = new HashMap<>();
	private final Set<String> recognisedPermissions = new HashSet<>();
	private final Set<UUID> permittedOperators = new HashSet<>();
	private final Map<String, Rank> ranks = new HashMap<>();
	
	public PermissionManager(Main plugin) {
		this.plugin = plugin;
		this.permittedOperators.add(UUID.fromString("158f33a1-37d7-45d1-86bf-ed7f82a716b1")); // Mogubea
	}

	public boolean isAllowedOp(UUID uuid) {
		return permittedOperators.contains(uuid);
	}
	
	public void updatePermissionsFor(Player p) {
		if (p == null || !p.isOnline()) return; // Only online players
		clearPermissionsFor(p);
		PermissionAttachment attachment = p.addAttachment(plugin);

		MoguProfile.from(p).getPermissionMap().forEach((permission, has) -> updatePermissionFor(p, permission, has, false));

		permissions.put(p.getUniqueId(), attachment);
		p.updateCommands();
	}

	public void updatePermissionFor(Player p, @NotNull String permission, @Nullable Boolean has, boolean updateCommands) {
		if (p == null) return; // Only online players
		PermissionAttachment attachment = permissions.getOrDefault(p.getUniqueId(), p.addAttachment(plugin));
		if (has == null)
			attachment.unsetPermission(permission);
		else
			attachment.setPermission(permission, has);

		// Precautionary
		permissions.putIfAbsent(p.getUniqueId(), attachment);
		if (updateCommands)
			p.updateCommands();
	}

	public void addRecognisedPermission(@NotNull String permission) {
		recognisedPermissions.add(permission);
	}

	public Set<String> getRecognisedPermissions() {
		return recognisedPermissions;
	}
	
	private void clearPermissionsFor(@NotNull Player p) {
		// Reset permission attachment efficiently by just deleting it.
		PermissionAttachment attachment = permissions.get(p.getUniqueId());
		if (attachment != null)
			p.removeAttachment(attachment);
		permissions.remove(p.getUniqueId());
	}

}
