package me.mogubea.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.player.PlayerTradeEvent;
import me.mogubea.enchants.MoguEnchantment;
import me.mogubea.entities.CustomEntityType;
import me.mogubea.items.ItemRarity;
import me.mogubea.profile.MoguProfile;
import me.mogubea.utils.MoguTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class EntityListener extends EventListener {

    private final Set<Wither> withers = new HashSet<>();
    private final List<Enchantment> wanderingEnchants = List.of(Enchantment.BANE_OF_ARTHROPODS, Enchantment.THORNS, Enchantment.EFFICIENCY, Enchantment.FEATHER_FALLING, Enchantment.BLAST_PROTECTION, Enchantment.PROJECTILE_PROTECTION, Enchantment.FIRE_PROTECTION, Enchantment.SWIFT_SNEAK, Enchantment.PUNCH, MoguEnchantment.PROSPECTOR, MoguEnchantment.TREECAPITATOR, MoguEnchantment.SWIFT_SPRINT, Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT, Enchantment.SWEEPING_EDGE, Enchantment.SMITE);
    private Location netherCenter;

    protected EntityListener(@NotNull ListenerManager manager) {
        super(manager);

        plugin.getServer().getWorlds().forEach(world -> {
            if (world.getEnvironment() == World.Environment.NETHER)
                netherCenter = new Location(world, 0, 64, 0);
        });

        // WITHER LOCATION CHECK
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> withers.forEach(wither -> {
            if (wither.getLocation().distanceSquared(netherCenter) < 6666 * 6666 || wither.getLocation().getNearbyPlayers(100).isEmpty())
                wither.remove();
        }), 20L, 20L);

        if (!plugin.hasProtocolManager()) return;

        plugin.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player p = event.getPlayer();
                Entity e = p.getWorld().getEntity(event.getPacket().getUUIDs().read(0));
                if (!(e instanceof Item item)) return;
                if (!item.hasMetadata("rarity")) return;

                String rarityName = ItemRarity.valueOf(item.getMetadata("rarity").getFirst().asString()).name();
                Team team = p.getScoreboard().getTeam("itemRarity_" + rarityName);
                if (team != null)
                    team.addEntity(item);
            }
        });
    }

    @EventHandler
    public void onPotion(PotionSplashEvent e) {
        if (!(e.getPotion().getShooter() instanceof Player thrower)) return;

        e.getPotion().getEffects().forEach(effect -> {
            PotionEffectType type = effect.getType();

            if (type.equals(PotionEffectType.INVISIBILITY)) { // Make all nearby item frames invisible!
                e.getEntity().getLocation().getNearbyEntitiesByType(ItemFrame.class, 3, 2, 3).forEach(frame -> frame.setInvisible(true));
            } else if (type.getCategory() == PotionEffectTypeCategory.HARMFUL) { // Prevent players harming each other with potions.
                e.getAffectedEntities().forEach(entity -> {
                    if (entity instanceof Player player && player != thrower)
                        e.setIntensity(player, 0);
                });
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.WITHER) {
            boolean cancel = false;

            if (e.getLocation().getWorld().getEnvironment() != World.Environment.NETHER) {
                cancel = true;
                if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER)
                    e.getLocation().getNearbyPlayers(6).forEach(player -> player.sendMessage(Component.text("Try somewhere warmer...", NamedTextColor.RED)));
            } else {
                if (e.getLocation().distanceSquared(netherCenter) < 7500 * 7500) {
                    e.getLocation().getNearbyPlayers(6).forEach(player -> player.sendMessage(Component.text("Further.. out..", NamedTextColor.RED)));
                    cancel = true;
                }
            }

            if (!cancel && !withers.isEmpty()) {
                e.getLocation().getNearbyPlayers(6).forEach(player -> player.sendMessage(Component.text("One at a time...", NamedTextColor.RED)));
                cancel = true;
            }

            e.setCancelled(cancel);
        }
    }

    // Add Wither to list
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWitherSpawnHigh(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Wither wither) {
            withers.add(wither);
            wither.setCanTravelThroughPortals(false);
            wither.setPersistent(false);
            wither.setGlowing(true);
            return;
        }

        switch (e.getSpawnReason()) {
            case NETHER_PORTAL, EGG, DISPENSE_EGG, BUILD_IRONGOLEM, BUILD_SNOWMAN -> MoguTag.UNNATURAL_KILL.applyTag(e.getEntity(), true);
        }
    }

    // Remove Wither from list
    @EventHandler(ignoreCancelled = true)
    public void onCreatureDespawn(EntityRemoveEvent e) {
        if (e.getEntity() instanceof Wither wither) {
            withers.remove(wither);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function wither:wither/fix");
        }
    }

    // Stop Wither breaking blocks within 6666 radius
    @EventHandler(ignoreCancelled = true)
    public void onWitherBreakBlock(EntityChangeBlockEvent e) {
        if (e.getBlock().getWorld().getEnvironment() != World.Environment.NORMAL) {
            if (e.getEntityType() == EntityType.WITHER || e.getEntityType() == EntityType.WITHER_SKULL)
                if (e.getBlock().getLocation().distanceSquared(netherCenter) < 6666 * 6666) {
                    e.setCancelled(true);
                }
        }
    }

    // Stop Wither breaking blocks within 6666 radius and prevent explosions breaking blocks on surface.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureExplode(EntityExplodeEvent e) {
        if (e.getLocation().getWorld().getEnvironment() != World.Environment.NORMAL) {
            if (e.getEntityType() == EntityType.WITHER || e.getEntityType() == EntityType.WITHER_SKULL)
                if (e.getLocation().distanceSquared(netherCenter) < 6666 * 6666) {
                    e.blockList().clear();
                    return;
                }
        }
        e.blockList().removeIf(block -> block.getLocation().getBlockY() >= 32 || block.getState() instanceof Container);
    }

    // Prevent Endermen targeting the dragon
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent e) {
        if (!(e.getEntity() instanceof Enderman)) return;
        if (e.getTarget() instanceof EnderDragon || e.getTarget() instanceof EnderDragonPart)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMobInteract(EntityInteractEvent e) {
        // Prevent entity trampling
        if (e.getBlock().getType() == Material.FARMLAND)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent e) {
        if (e.getEntityType() == EntityType.ALLAY)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityHeal(EntityRegainHealthEvent e) {
        if (e.getAmount() <= 0) return;
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        if (living.getHealth() >= Objects.requireNonNull(living.getAttribute(Attribute.MAX_HEALTH)).getValue()) return;

        CustomEntityType.TEXT_INDICATOR.spawn(living.getLocation()).setText(Component.text("+" + dFormat.format(e.getAmount()), TextColor.color(0x33ff77)));

        if (plugin.getCustomEntityManager().hasHealthBar(living) || e.getEntity() instanceof Player) return;
        CustomEntityType.MOB_HEALTH_BAR.spawn(living.getEyeLocation(), (entity) -> entity.setTrackedMob(living));
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingDamage(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e) {
        doMoguItemEvents(e.getItemStack(), e);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getFinalDamage() <= 0) return;
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        Entity cause = e.getDamageSource().getCausingEntity();
        Entity direct = e.getDamageSource().getDirectEntity();

        if (living instanceof ArmorStand && !(cause instanceof Player)) {
            e.setCancelled(true);
            return;
        }

        if (e.getEntity() instanceof Player player && cause instanceof Player harmer && player != harmer) {
            e.setCancelled(true);
            return;
        }

        // Enderman immunity from direct dragon hits
        if (cause instanceof EnderDragon || cause instanceof EnderDragonPart) {
            if (living instanceof Enderman) {
                e.setCancelled(true);
                return;
            }
        }

        // 1 true damage from ender sourced entities...
        if (direct != null)
            switch (direct.getType()) {
                case SHULKER_BULLET, SHULKER, ENDERMITE, ENDERMAN -> living.setHealth(living.getHealth() - 1);
            }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageFinal(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;

        TextColor textColor = switch (e.getCause()) {
            case FIRE, FIRE_TICK, CAMPFIRE, LAVA, HOT_FLOOR, MELTING -> TextColor.color(0xcf4f1f);
            case DROWNING, FREEZE -> TextColor.color(0x3273ba);
            case WITHER, VOID, SUFFOCATION -> TextColor.color(0x3a3a3a);
            case POISON -> TextColor.color(0x2a4a13);
            case MAGIC, DRAGON_BREATH, THORNS -> TextColor.color(0x762276);
            case ENTITY_SWEEP_ATTACK -> TextColor.color(0xef5263);
            default -> TextColor.color(0xdf2233);
        };

        CustomEntityType.TEXT_INDICATOR.spawn(living.getLocation(), (text) -> text.setText(Component.text(dFormat.format(e.getFinalDamage()), textColor)));

        if (plugin.getCustomEntityManager().hasHealthBar(living) || e.getEntity() instanceof Player) return;
        CustomEntityType.MOB_HEALTH_BAR.spawn(living.getEyeLocation(), (entity) -> entity.setTrackedMob(living));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) return;

        if (!(e.getEntity() instanceof Monster monster)) return;

        boolean spawnerEntity = monster.fromMobSpawner() && monster.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER;
        boolean playerInvolved = e.getEntity().getKiller() != null;

        if (spawnerEntity)
            e.setDroppedExp(e.getDroppedExp() / 2);

        if (!playerInvolved) {
            e.setDroppedExp(0);
        } else {
            if (e.getDroppedExp() >= 0 && e.getEntity() instanceof Wither)
                e.setDroppedExp(1000);
        }

        if (spawnerEntity || !playerInvolved) {
            // 50% chance to negate all drops
            if (plugin.getRandom().nextBoolean())
                e.getDrops().clear();

            // Remove special drops
            e.getDrops().removeIf(itemStack -> plugin.getItemManager().getItemRarity(itemStack).isRarity(ItemRarity.UNCOMMON) || itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable);
            MoguTag.UNNATURAL_KILL.applyTag(e.getEntity(), true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityPlace(EntityPlaceEvent e) {
        if (e.getPlayer() == null) return;
        MoguTag.ENTITY_OWNER_ID.applyTag(e.getEntity(), MoguProfile.from(e.getPlayer()).getDatabaseId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillager(VillagerAcquireTradeEvent e) {
        ItemStack result = e.getRecipe().getResult();

        if (result.getType() == Material.ENCHANTED_BOOK) {
            MerchantRecipe recipe = e.getRecipe();
            List<ItemStack> ingredients = recipe.getIngredients();
            int emeraldCost = ingredients.getFirst().getAmount();

            ingredients.set(0, new ItemStack(Material.EMERALD_BLOCK, Math.max(2, emeraldCost)));
            ingredients.set(1, new ItemStack(Material.DIAMOND, Math.max(2, emeraldCost / 2)));

            recipe.setIngredients(ingredients);
            recipe.setIgnoreDiscounts(true);
            recipe.setMaxUses(1);
            e.setRecipe(recipe);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSillyManSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof WanderingTrader trader) {
            for (int x = -1; ++x < 3;) {
                Enchantment enchant = wanderingEnchants.get(random.nextInt(wanderingEnchants.size()));

                MerchantRecipe recipe = new MerchantRecipe(plugin.getItemManager().createEnchantBook(enchant, enchant.getMaxLevel() + 1), 1);
                recipe.setIgnoreDiscounts(true);
                recipe.setMaxUses(1);
                recipe.addIngredient(plugin.getItemManager().createEnchantBook(enchant, enchant.getMaxLevel()));
                recipe.addIngredient(plugin.getItemManager().createEnchantBook(enchant, enchant.getMaxLevel()));

                trader.setRecipe(random.nextInt(trader.getRecipeCount()), recipe);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerTrade(PlayerTradeEvent e) {
        if (MoguTag.WANDERING_TRADER_LOCK.hasTag(e.getVillager())) {
            e.setCancelled(true);
            return;
        }

        if (e.getVillager() instanceof WanderingTrader trader) {
            if (!e.getTrade().getResult().hasData(DataComponentTypes.STORED_ENCHANTMENTS)) return;
            MoguTag.WANDERING_TRADER_LOCK.applyTag(trader, true);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                World world = trader.getWorld();

                e.getPlayer().getOpenInventory().close();
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, trader.getLocation(), 1);
                world.spawnParticle(Particle.ENCHANT, trader.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5);
                trader.remove();
            }, 2L);
        }
    }

}
