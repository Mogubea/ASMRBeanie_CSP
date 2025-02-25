package me.mogubea.commands;

import me.mogubea.main.Main;
import me.mogubea.profile.MoguProfile;
import me.mogubea.statistics.DirtyInteger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandDebug extends MoguCommand {

	public CommandDebug(Main plugin) {
		super(plugin, false, 1, "debug");
		setDescription("Debug commands.");
	}
	
	final String[] gamemodes = { "updateNearbyVillagers", "resetDailyStats" };
	
	@Override
	public boolean runCommand(MoguProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		switch (args[0].toLowerCase()) {
			case "updatenearbyvillagers" -> {
				Player player = (Player)sender;

				int dist = args.length > 1 ? toIntDef(args[1], 0) : 0;
				if (dist < 1) dist = 1;
				DirtyInteger totalVillagers = new DirtyInteger(0);
				DirtyInteger totalTradesModified = new DirtyInteger(0);
				player.sendMessage(Component.text("Formatting all villagers within a " + dist + " Chunk Radius..."));

				player.getWorld().getNearbyEntitiesByType(Villager.class, player.getLocation(), dist * 16).forEach(villager -> {
					totalVillagers.addToValue(1);
					if (villager.getProfession() != Villager.Profession.NONE) {
						int count = villager.getRecipeCount();
						for (int x = count; --x > -1;) {
							MerchantRecipe recipe = villager.getRecipe(x);

							if (recipe.getResult().getType() == Material.ENCHANTED_BOOK) {
								List<ItemStack> ingredients = recipe.getIngredients();
								int emeraldCost = ingredients.getFirst().getAmount();
								if (ingredients.getFirst().getType() == Material.EMERALD)
									emeraldCost /= 2;

								ingredients.set(0, new ItemStack(Material.EMERALD_BLOCK, Math.max(2, emeraldCost)));
								ingredients.set(1, new ItemStack(Material.DIAMOND, Math.max(2, emeraldCost)));

								recipe.setIngredients(ingredients);
								recipe.setIgnoreDiscounts(true);
								recipe.setMaxUses(2);

								villager.setRecipe(x, recipe);
								totalTradesModified.addToValue(1);
							}
						}
					}
				});
				sender.sendMessage(Component.text("Done. Updated "+totalTradesModified.getValue()+" trades among " + totalVillagers.getValue() + " Villagers."));
				return true;
			}
			case "resetdailystats" -> {
				getPlugin().getProfileManager().resetDailyStats();
				sender.sendMessage(Component.text("Reset daily stats."));
				return true;
			}
		}

		
		// Gamemode doesn't exist
		throw new CommandException(sender, "'"+ args[0] + "' is not a valid subcommand.");
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
            return getTabCompleter().completeString(args[0], gamemodes);
		
		return Collections.emptyList();
	}

}
