package me.mogubea.statistics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum SimpleStatType {
	GENERIC("GENERIC", "generic"),
	BLOCK_BREAK("BLOCK_BREAK", "blockbreak", "blockbreaks", "breaks", "broken", "block_break", "brokenblocks"),
	BLOCK_PLACE("BLOCK_PLACE", "blockplace", "blockplaces", "blockplacements", "placed", "blocksplaced", "block_place"),
	NATURAL_BLOCK_BREAK("NATURAL_BLOCK_BREAK", "naturalblockbreak"),
	CHAT_EMOTE("CHAT_EMOTE", "emotessent", "chatemote"),
	COMMAND("COMMAND", "command", "commandsrun", "commands"),
	CROP_HARVEST("CROP_HARVEST", "harvestcrop", "cropharvest"),
	MONEY_SOURCES("MONEY_SOURCES", "MONEY_SOURCE", "moneysource", "moneysources"),
	INTERACTION("INTERACTION", "INTERACTIONS"),
	;

	private final String identifier;
	private final String[] strings;
	SimpleStatType(@NotNull String identifier, String... strings) {
		this.identifier = identifier.toUpperCase();
		this.strings = strings;
	}

	public @NotNull String getIdentifier() {
		return identifier;
	}

	public static @Nullable SimpleStatType fromString(@NotNull String s) {
		for (SimpleStatType type : values()) {
			if (Arrays.stream(type.strings).anyMatch(s::equalsIgnoreCase)) {
				return type;
			}
		}
		return null;
	}

	public static @Nullable SimpleStatType fromIdentifier(@NotNull String s) {
		for (SimpleStatType type : values())
			if (type.getIdentifier().equals(s.toUpperCase()))
				return type;
		return null;
	}
	
}
