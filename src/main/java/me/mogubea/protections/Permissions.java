package me.mogubea.protections;

public class Permissions {

    public static final Permission EDIT_FLAGS, EDIT_MEMBERS, EDIT_PERMISSIONS;

    static {
        EDIT_FLAGS = new Permission("edit-flags", "Edit Region Flags");
        EDIT_MEMBERS = new Permission("edit-members", "Edit Region Members");
        EDIT_PERMISSIONS = new Permission("edit-permissions", "Edit Region Permissions");
    }

}
