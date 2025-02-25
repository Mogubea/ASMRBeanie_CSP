package me.mogubea.protections;

import me.mogubea.utils.Lore;

public class Flags {

    public static final FlagBoolean BUILD_ACCESS;
    public static final FlagBoolean CONTAINER_ACCESS, VILLAGER_ACCESS, ANVIL_ACCESS, DOOR_ACCESS, SMALL_CROP_ACCESS, LARGE_CROP_ACCESS, SHOP_ACCESS, PASSIVE_ENTITY_INTERACTION, ANIMAL_BREEDING_ACCESS;

    public static final FlagBoolean PVP, MOB_HOSTILE_SPAWNS, MOB_PASSIVE_SPAWNS, MOB_SPAWNER_SPAWNS, ANVIL_DEGRADATION, GRASS_SPREAD, SCULK_SPREAD, SMALL_CROP_GROWTH, LARGE_CROP_GROWTH, VINE_GROWTH, MUSHROOM_SPEAD;

    static {
        // "Plags" are Flags that can also be used as Individual Member Permissions.
        // BUILD_ACCESS is a SUPER PLAG that overrides the following Plags when set to TRUE...
        BUILD_ACCESS = new FlagBoolean("build-access", "Build Access", false).setPlag();

        CONTAINER_ACCESS = new FlagBoolean("container-access", "Container Access", false).setPlag();
        VILLAGER_ACCESS = new FlagBoolean("villager-access", "Villager Access", false).setPlag();
        ANVIL_ACCESS = new FlagBoolean("anvil-access", "Anvil Access", false).setPlag();
        DOOR_ACCESS = new FlagBoolean("door-access", "Door Access", true).setPlag();
        SMALL_CROP_ACCESS = new FlagBoolean("small-crop-access", "Small Crop Access", false).setPlag();
        LARGE_CROP_ACCESS = new FlagBoolean("large-crop-access", "Large Crop Access", false).setPlag();
        PASSIVE_ENTITY_INTERACTION = new FlagBoolean("animal-interaction-access", "Animal Interaction Access", true).setPlag();
        ANIMAL_BREEDING_ACCESS = new FlagBoolean("animal-breeding-access", "Animal Breeding Access", false).setPlag();
        SHOP_ACCESS = new FlagBoolean("shop-access", "Shop Access", true).setPlag();
        // End of Overridden Plags

        PVP = new FlagBoolean("pvp", "Player vs Player", false).setNeedsPermission();

        MOB_HOSTILE_SPAWNS = new FlagBoolean("hostile-spawning", "Hostile Monster Spawning", false).setNeedsPermission();
        MOB_PASSIVE_SPAWNS = new FlagBoolean("friendly-spawning", "Friendly Mob Spawning", true);
        MOB_SPAWNER_SPAWNS = new FlagBoolean("spawner-spawning", "Mob Spawner Spawning", true);

        ANVIL_DEGRADATION = new FlagBoolean("anvil-degradation", "Anvil Degradation", true).setNeedsPermission();
        GRASS_SPREAD = new FlagBoolean("grass-spread", "Grass Spreading", true).setDescription(Lore.fastBuild(true, 40, "Blocks such as &#aaaaaaGrass&r and &#aaaaaaMycelium&r can spread across Dirt Blocks."));
        SCULK_SPREAD = new FlagBoolean("sculk-spread", "Sculk Spreading", true).setDescription(Lore.fastBuild(true, 40, "&eSkulk Catalysts&r can spread &bSculk&r by replacing blocks such as &#444444Stone&r and &#888833Dirt&r when a nearby mob dies."));
        SMALL_CROP_GROWTH = new FlagBoolean("small-crop-growth", "Small Crop Growth", true).setDescription(Lore.fastBuild(true, 40, "Single block crops like &#998844Wheat&r or &#cccc99Potatoes&r can grow."));
        LARGE_CROP_GROWTH = new FlagBoolean("large-crop-growth", "Large Crop Growth", true).setDescription(Lore.fastBuild(true, 40, "Multi block crops like &#aaddbbSugar Cane&r, &#88cc99Bamboo&r or &aMelons&r can grow."));
        VINE_GROWTH = new FlagBoolean("vine-growth", "Vine Growth", true).setDescription(Lore.fastBuild(false, 40, "Vine based blocks can grow and yield crops."));
        MUSHROOM_SPEAD = new FlagBoolean("mushroom-spread", "Mushroom Spreading", true).setDescription(Lore.fastBuild(false, 40, "Mushrooms will grow and spread across dark areas or on Mycelium blocks."));
    }

}
