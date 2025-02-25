package me.mogubea.commands;

import me.mogubea.items.MoguItem;
import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandI extends MoguCommand {

	public CommandI(Main plugin) {
		super(plugin, false, 1, "i");
		setDescription("Give yourself an item.");
		addArgumentHelp("item", Component.text("The item to be added to your inventory."));
		addArgumentHelp("amount", Component.text("Optional: The amount of the specified item."));
	}
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		ItemStack i = toItemStack(sender, args[0], args.length > 1 ? toIntMinMax(sender, args[1], 1, 1024) : 1);

		((Player)sender).getInventory().addItem(i);
		sender.sendMessage(Component.text("Added ", NamedTextColor.GRAY).append(toHover(i)).colorIfAbsent(NamedTextColor.WHITE).append(Component.text(" to your inventory!", NamedTextColor.GRAY)));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			List<String> mats = getTabCompleter().completeItems(args[0]);
			mats.addAll(getTabCompleter().completeObject(args[0], MoguItem::getIdentifier, getPlugin().getItemManager().getItems()));
			return mats;
		}
		if (args.length == 2)
			return getTabCompleter().completeIntegerBetween(args[1], 1, 64);
		
		return Collections.emptyList();
	}

}
