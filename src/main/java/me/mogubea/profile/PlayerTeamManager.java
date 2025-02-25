package me.mogubea.profile;

import me.mogubea.items.ItemRarity;
import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerTeamManager {

	private final Main plugin;

	public PlayerTeamManager(Main plugin) {
		this.plugin = plugin;
	}

	public void onDisable() {
		// Clean up all entries from this run cycle
		for (Objective entry : plugin.getServer().getScoreboardManager().getMainScoreboard().getObjectives())
			entry.unregister();
	}
	
	public void initScoreboard(Player p) {
		createScoreboard(p);
		loadTeamsFor(p);
		updateTeam(p);
//		MoguProfile.from(p).getSidebar().refreshSidebar();
	}
	
	private void createScoreboard(Player p) {
		Scoreboard playerBoard = plugin.getServer().getScoreboardManager().getNewScoreboard();

		if (plugin.hasProtocolManager()) {
			for (ItemRarity rarity : ItemRarity.values()) {
				if (rarity.ordinal() < ItemRarity.UNCOMMON.ordinal()) continue;

				Team team = playerBoard.registerNewTeam("itemRarity_" + rarity.name());
				team.color(NamedTextColor.nearestTo(rarity.getColour()));
			}
		}

		p.setScoreboard(playerBoard);
	}
	
	/**
	 * Send an update to all online players to update their team information about this player.
	 */
	public void updateTeam(Player p) {
		final MoguProfile pp = MoguProfile.from(p);

		final NamedTextColor color = NamedTextColor.nearestTo(pp.getNameColour());
		
		// This is required due to how scoreboards function per player
		// Team colouration is exclusive per scoreboard and must be redefined for every single player's scoreboard.
		// It is uncertain how memory intensive this can become the more players get online... But thankfully Scoreboards are WeakReferenced.
		plugin.getServer().getOnlinePlayers().forEach((player) -> {
			String id = "id" + pp.getDatabaseId() + "-" + MoguProfile.from(player).getDatabaseId();
			Team team = player.getScoreboard().getTeam(id);
			if (team == null) team = player.getScoreboard().registerNewTeam(id);
			if (!team.hasEntry(p.getName()))
				team.addEntry(p.getName());
			
			team.color(color);
			team.prefix(pp.getJob().getIcon());
		});

		// Cannot use p.teamDisplayName()
		p.playerListName(pp.getColouredName());
	}
	
	/**
	 * Generate a team for each of the currently online players
	 */
	private void loadTeamsFor(Player p) {
		plugin.getServer().getOnlinePlayers().forEach((player) -> {
			final MoguProfile pp = MoguProfile.from(player);

			final NamedTextColor color = NamedTextColor.nearestTo(pp.getNameColour());

			String id = "id" + pp.getDatabaseId() + "-" + pp.getDatabaseId();
			Team team = p.getScoreboard().getTeam(id);
			if (team == null) team = p.getScoreboard().registerNewTeam(id);
			if (!team.hasEntry(player.getName()))
				team.addEntry(player.getName());
			
			team.color(color);
			team.prefix(pp.getJob().getIcon().append(Component.text(" ")));
		});
	}
	
}
