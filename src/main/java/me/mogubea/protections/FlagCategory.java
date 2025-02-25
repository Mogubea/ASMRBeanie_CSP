package me.mogubea.protections;

public enum FlagCategory {
    BLOCKS("Block Flags"),
    TELEPORTING("Teleportation Flags"),
    ENTITIES("Entity Flags"),
    NOTIFICATION("Notification Flags"),
    MISCELLANEOUS("Miscellaneous Flags");

    private final String title;
    FlagCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}