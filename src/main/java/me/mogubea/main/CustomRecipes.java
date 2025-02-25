package me.mogubea.main;

import org.bukkit.*;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class CustomRecipes {

    private final Main plugin;
    private final Map<NamespacedKey, Recipe> customRecipes;
    private final Map<Material, ItemStack> cookedTypes;

    public CustomRecipes(Main plugin) {
        this.plugin = plugin;
        Map<Material, ItemStack> cookTypes = new HashMap<>();
        customRecipes = new HashMap<>();

        // Remove the existing recipe
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe != null && (recipe.getResult().getType() == Material.SHULKER_BOX || recipe.getResult().getType() == Material.BEACON || recipe.getResult().getType() == Material.BUNDLE))
                recipeIterator.remove();

            if (recipe instanceof CookingRecipe<?> furnaceRecipe && furnaceRecipe.getInputChoice() instanceof RecipeChoice.MaterialChoice choice) {
                for (int x = -1; ++x < choice.getChoices().size();) {
                    Material toSmelt = choice.getChoices().get(x);
                    if (toSmelt.getCreativeCategory() == CreativeCategory.COMBAT || toSmelt.getCreativeCategory() == CreativeCategory.TOOLS) continue;
                    cookTypes.put(toSmelt, furnaceRecipe.getResult());
                }
            }
        }

        cookedTypes = Map.copyOf(cookTypes);

        NamespacedKey key = Main.key("recipe_shulker_box");
        ShapedRecipe newRecipe = new ShapedRecipe(key, new ItemStack(Material.SHULKER_BOX, 1));
        newRecipe.shape("SSS", "SCS", "SSS");
        newRecipe.setIngredient('S', Material.SHULKER_SHELL);
        newRecipe.setIngredient('C', Material.ENDER_CHEST);
        Bukkit.addRecipe(newRecipe, false);
        customRecipes.put(newRecipe.getKey(), newRecipe);

        key = Main.key("recipe_beacon");
        newRecipe = new ShapedRecipe(key, new ItemStack(Material.BEACON, 1));
        newRecipe.shape("GGG", "GSG", "OEO");
        newRecipe.setIngredient('G', Material.TINTED_GLASS);
        newRecipe.setIngredient('S', Material.NETHER_STAR);
        newRecipe.setIngredient('O', new RecipeChoice.MaterialChoice(Material.OBSIDIAN, Material.CRYING_OBSIDIAN));
        newRecipe.setIngredient('E', Material.DRAGON_EGG);
        Bukkit.addRecipe(newRecipe, false);
        customRecipes.put(newRecipe.getKey(), newRecipe);

        key = Main.key("recipe_bundle");
        newRecipe = new ShapedRecipe(key, new ItemStack(Material.BUNDLE, 1));
        newRecipe.shape("LSL", "L L", "LLL");
        newRecipe.setIngredient('S', Material.STRING);
        newRecipe.setIngredient('L', Material.LEATHER);
        Bukkit.addRecipe(newRecipe, true);
        customRecipes.put(newRecipe.getKey(), newRecipe);
    }

    public Set<NamespacedKey> keys() {
        return customRecipes.keySet();
    }

    public @Unmodifiable @NotNull Map<Material, ItemStack> getFurnaceRecipes() {
        return cookedTypes;
    }

    public boolean hasSmeltedVersion(@NotNull ItemStack item) {
        return getFurnaceRecipes().containsKey(item.getType());
    }

    public @NotNull ItemStack getSmeltedVersion(@NotNull ItemStack item) {
        if (!hasSmeltedVersion(item)) return item;
        int amt = item.getAmount();
        ItemStack result = getFurnaceRecipes().get(item.getType()).clone();
        result.setAmount(amt * result.getAmount());

        return result;
    }

}
