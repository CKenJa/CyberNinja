package mod.ckenja.cyninja.data;

import mod.ckenja.cyninja.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SmithingTrimRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

import static mod.ckenja.cyninja.Cyninja.prefix;

public class CraftingGenerator extends CraftingDataHelper {
    public CraftingGenerator(PackOutput generator, CompletableFuture<HolderLookup.Provider> p_323846_) {
        super(generator, p_323846_);
    }

    @Override
    protected void buildRecipes(RecipeOutput consumer) {

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CYBER_TRIM_SMITHING_TEMPLATE, 2)
                .pattern("DMD")
                .pattern("RTR")
                .pattern("DMD")
                .define('D', Items.DIAMOND)
                .define('R', Items.REDSTONE)
                .define('M', Items.COPPER_INGOT)
                .define('T', ModItems.CYBER_TRIM_SMITHING_TEMPLATE)
                .unlockedBy("has_item", has(ModItems.CYBER_TRIM_SMITHING_TEMPLATE))
                .save(consumer, prefix("cyber_trim_dupe"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CHAIN_SICKLE, 1)
                .pattern("F")
                .pattern("C")
                .pattern("C")
                .define('F', Items.IRON_HOE)
                .define('C', Items.CHAIN)
                .unlockedBy("has_item", has(Items.CHAIN))
                .save(consumer);
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(ModItems.CYBER_TRIM_SMITHING_TEMPLATE), Ingredient.of(ModItems.NINJA_HELMET), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.COMBAT).unlocks("has_item", has(ModItems.CYBER_TRIM_SMITHING_TEMPLATE)).save(consumer, prefix("cyber_helmet"));
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(ModItems.CYBER_TRIM_SMITHING_TEMPLATE), Ingredient.of(ModItems.NINJA_CHESTPLATE), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.COMBAT).unlocks("has_item", has(ModItems.CYBER_TRIM_SMITHING_TEMPLATE)).save(consumer, prefix("cyber_chestplate"));
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(ModItems.CYBER_TRIM_SMITHING_TEMPLATE), Ingredient.of(ModItems.NINJA_LEGGINGS), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.COMBAT).unlocks("has_item", has(ModItems.CYBER_TRIM_SMITHING_TEMPLATE)).save(consumer, prefix("cyber_leggings"));
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(ModItems.CYBER_TRIM_SMITHING_TEMPLATE), Ingredient.of(ModItems.NINJA_BOOTS), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.COMBAT).unlocks("has_item", has(ModItems.CYBER_TRIM_SMITHING_TEMPLATE)).save(consumer, prefix("cyber_boots"));
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(ModItems.CYBER_TRIM_SMITHING_TEMPLATE), Ingredient.of(ModItems.CHAIN_SICKLE), Ingredient.of(Items.COPPER_INGOT), RecipeCategory.COMBAT).unlocks("has_item", has(ModItems.CYBER_TRIM_SMITHING_TEMPLATE)).save(consumer, prefix("cyber_sickle_copper"));

    }
}
