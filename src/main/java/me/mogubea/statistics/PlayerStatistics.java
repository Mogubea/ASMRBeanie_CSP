package me.mogubea.statistics;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlayerStatistics {

	private final @NotNull SimpleStatHolder<Long> stats = new SimpleStatHolder<>();
	private final @NotNull SimpleStatHolder<Long> dailyStats = new SimpleStatHolder<>();

	public <T extends Number> T getStat(@NotNull SimpleStatType type, @NotNull String name) {
		return (T) stats.getStat(type, name);
	}

	public <T extends Number> T getDailyStat(@NotNull SimpleStatType type, @NotNull String name) {
		return (T) dailyStats.getStat(type, name);
	}

	public void setStat(@NotNull SimpleStatType type, @NotNull String name, long value) {
		setStat(type, name, value, true);
	}

	public void setStat(@NotNull SimpleStatType type, @NotNull String name, long value, boolean dirty) {
		stats.setStat(type, name, value, dirty);
	}

	public void setDailyStat(@NotNull SimpleStatType type, @NotNull String name, long value) {
		setDailyStat(type, name, value, true);
	}

	public void setDailyStat(@NotNull SimpleStatType type, @NotNull String name, long value, boolean dirty) {
		dailyStats.setStat(type, name, value, dirty);
	}

	public void addToStat(@NotNull SimpleStatType type, @NotNull String name, long add) {
		stats.addToStat(type, name, add);
		dailyStats.addToStat(type, name, add);
	}

	public void incrementStat(@NotNull SimpleStatType type, @NotNull String name) {
		addToStat(type, name, 1);
	}

	public @NotNull Map<SimpleStatType, Map<String, DirtyVal<Long>>> getMap() {
		return stats.getMap();
	}

	public @NotNull Map<SimpleStatType, Map<String, DirtyVal<Long>>> getDailyMap() {
		return dailyStats.getMap();
	}

	public void clearDailyStats() {
		this.dailyStats.clear();
	}
	
}
