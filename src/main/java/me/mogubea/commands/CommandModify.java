package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import me.mogubea.profile.MoguProfileLite;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandModify extends MoguCommand {

	private final String ACTION_NICKNAME = "nickname";
	private final String ACTION_NAMECOLOUR = "namecolour";
	private final String ACTION_SET_PERMISSION = "setpermission";

	private final String[] actions = { ACTION_NICKNAME, ACTION_NAMECOLOUR, ACTION_SET_PERMISSION};
	private final String[] permissionActions = { "true", "false", "null" };
	private final List<String> permissionAlts;

	public CommandModify(Main plugin) {
		super(plugin, true, 2, "modify");
		setDescription("Modify player profiles");
		addArgumentHelp("profile", Component.text("The player profile to modify"));
		addArgumentHelp("action", Component.text("The action to be taken"));
		addArgumentHelp("value", Component.text("Value of the action"));

		permissionAlts = List.of(ACTION_SET_PERMISSION, "sp", "perm", "permission", "setperm");
	}

	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		MoguProfile tp = toProfile(sender, args[0]);
		String value = args.length >= 3 ? args[2] : null;
		NamedTextColor gray = NamedTextColor.GRAY;

		switch (args[1].toLowerCase()) {
			case ACTION_NICKNAME, "name", "nick" -> {
				if (args.length >= 3) {
					Component oldName = tp.getColouredName();
					tp.setNickname(value);
					sender.sendMessage(oldName.append(Component.text("'s nickname is now ", gray).append(tp.getColouredName()).append(Component.text(".", gray))));
				} else {
					sender.sendMessage(tp.getColouredName().append(tp.hasNickname() ? Component.text("'s real name is ", gray).append(Component.text(tp.getName(), tp.getNameColour()).append(Component.text(".", gray))) : Component.text(" does not have a nickname.", gray)));
				}
			}
			case ACTION_NAMECOLOUR, "colour", "namecolor", "color" -> {
				if (args.length >= 3) {
					Component oldName = tp.getColouredName();
					tp.setNameColour((int)Long.parseLong(value, 16));
					sender.sendMessage(oldName.append(Component.text("'s name colour is now ", gray).append(Component.text(tp.getNameColour().value(), tp.getNameColour())).append(Component.text(".", gray))));
				} else {
					sender.sendMessage(tp.getColouredName().append(Component.text("'s name colour is currently ", gray).append(Component.text(tp.getNameColour().value(), tp.getNameColour()))).append(Component.text(".", gray)));
				}
			}
			case ACTION_SET_PERMISSION, "sp", "perm", "permission", "setperm" -> {
				if (args.length >= 3) {
					value = value.toLowerCase();
					Boolean has = tp.getPermission(value);
					Component permComp = Component.text(value.toLowerCase(), NamedTextColor.WHITE);

					sender.sendMessage(args.length == 3 ? getPermissionStatusMsg(tp, has, permComp) : getNewPermissionStatusMsg(tp, tp.setHasPermission(value, args[3].equalsIgnoreCase("null") ? null : Boolean.valueOf(args[3])), permComp));
				} else {
					sender.sendMessage(Component.text("Specify a permission.", NamedTextColor.RED));
				}
			}
		}

		return true;
	}

	@NotNull
	private Component getPermissionStatusMsg(MoguProfile tp, Boolean has, Component permComp) {
		Component message = tp.getColouredName();
		if (has == null)
			message = message.append(Component.text(" doesn't have the ", NamedTextColor.GRAY)).append(permComp).append(Component.text(" permission.", NamedTextColor.GRAY));
		else
			message = message.append(Component.text(" has the ", NamedTextColor.GRAY)).append(permComp).append(Component.text(" permission ", NamedTextColor.GRAY))
					.append(Component.text(has ? "granted" : "blocked", has ? NamedTextColor.GREEN : NamedTextColor.RED)).append(Component.text(".", NamedTextColor.GRAY));
		return message;
	}

	@NotNull
	private Component getNewPermissionStatusMsg(MoguProfile tp, Boolean has, Component permComp) {
		Component message = tp.getColouredName();
		if (has == null)
			message = message.append(Component.text(" no longer has the ", NamedTextColor.GRAY)).append(permComp).append(Component.text(" permission.", NamedTextColor.GRAY));
		else
			message = message.append(Component.text(" now has the ", NamedTextColor.GRAY)).append(permComp).append(Component.text(" permission ", NamedTextColor.GRAY))
					.append(Component.text(has ? "granted" : "blocked", has ? NamedTextColor.GREEN : NamedTextColor.RED)).append(Component.text(".", NamedTextColor.GRAY));
		return message;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return switch (args.length) {
			case 1 -> getTabCompleter().completeObject(args[0], MoguProfileLite::getName, getPlugin().getProfileManager().getLiteProfiles());
			case 2 -> getTabCompleter().completeString(args[1], actions);
			case 3 -> permissionAlts.contains(args[1]) ? getTabCompleter().completeString(args[2], getPlugin().getPermissionManager().getRecognisedPermissions()) : Collections.emptyList();
			case 4 -> permissionAlts.contains(args[2]) ? getTabCompleter().completeString(args[3], permissionActions) : Collections.emptyList();
			default -> Collections.emptyList();
		};
	}

}
