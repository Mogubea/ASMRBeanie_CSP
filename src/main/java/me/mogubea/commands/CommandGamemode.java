package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandGamemode extends MoguCommand {

	public CommandGamemode(Main plugin) {
		super(plugin, true, 1, "gamemode", "gm");
		setDescription("Update your, or another players, gamemode.");
		addArgumentHelp("gamemode", Component.text("The desired gamemode"));
		addArgumentHelp("player", Component.text("Optional: The player who's having their gamemode swapped"));
	}
	
	final String[] gamemodes = { "survival", "creative", "adventure", "spectator" };
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player target = (hasSubPerm(sender, "others", false) && args.length > 1) ? toPlayer(sender, args[1]) : (isPlayer(sender) ? ((Player)sender) : null);
		if (target == null)
			throw new CommandException(sender, "Please specify a player!");
		
		// Allow the usage of /gm <number> instead of the traditional /gamemode <gamemode>.
		String gm = args[0].toLowerCase();
		final int intAlt = toIntDef(args[0], -1);
		if (intAlt > -1 && intAlt < gamemodes.length)
			gm = gamemodes[intAlt];
		
		// Allow the usage of shortened gamemode identifiers (s, c, a).
		for (int x = -1; ++x < gamemodes.length;) {
			if (gm.equals(gamemodes[x]) || gamemodes[x].startsWith(gm)) {
				final MoguProfile tp = MoguProfile.from(target);
				target.setGameMode(GameMode.valueOf(gamemodes[x].toUpperCase()));
				if (target != sender)
					sender.sendMessage(tp.getColouredName().append(Component.text(" is now in " + target.getGameMode().name() + " Mode.", NamedTextColor.GRAY)));
				return true;
			}
		}
		
		// Gamemode doesn't exist
		throw new CommandException(sender, "'"+ args[0] + "' is not a valid gamemode.");
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			List<String> gms = getTabCompleter().completeString(args[0], gamemodes);
			if (!sender.hasPermission("mogu.gm.creative"))
				gms.remove("creative");
			if (!sender.hasPermission("mogu.gm.adventure"))
				gms.remove("adventure");
			return gms;
		}
		if (args.length == 2)
			return getTabCompleter().completeOnlinePlayer(sender, args[1]);
		
		return Collections.emptyList();
	}

}
