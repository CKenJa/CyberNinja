package mod.ckenja.cyninja.infrastructure.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public abstract class CraftingDataHelper extends RecipeProvider {
    public CraftingDataHelper(PackOutput generator, CompletableFuture<HolderLookup.Provider> p_323846_) {
        super(generator, p_323846_);
    }
}
