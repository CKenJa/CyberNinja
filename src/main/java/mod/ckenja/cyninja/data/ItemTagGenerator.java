package mod.ckenja.cyninja.data;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> provider, ExistingFileHelper exFileHelper) {
        super(packOutput, lookupProvider, provider, Cyninja.MODID, exFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
        this.tag(ItemTags.FOOT_ARMOR).add(ModItems.NINJA_BOOTS.asItem());
        this.tag(ItemTags.LEG_ARMOR).add(ModItems.NINJA_LEGGINGS.asItem());
        this.tag(ItemTags.CHEST_ARMOR).add(ModItems.NINJA_CHESTPLATE.asItem());
        this.tag(ItemTags.HEAD_ARMOR).add(ModItems.NINJA_HELMET.asItem());
        this.tag(ItemTags.DYEABLE).add(ModItems.NINJA_BOOTS.asItem()).add(ModItems.NINJA_LEGGINGS.asItem())
                .add(ModItems.NINJA_CHESTPLATE.asItem()).add(ModItems.NINJA_HELMET.asItem());

    }
}
