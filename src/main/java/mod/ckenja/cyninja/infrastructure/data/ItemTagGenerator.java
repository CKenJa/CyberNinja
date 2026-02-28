package mod.ckenja.cyninja.infrastructure.data;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.infrastructure.registry.ModItemTags;
import mod.ckenja.cyninja.infrastructure.registry.ModItems;
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
        tag(ItemTags.FOOT_ARMOR).add(ModItems.NINJA_BOOTS.asItem());
        tag(ItemTags.LEG_ARMOR).add(ModItems.NINJA_LEGGINGS.asItem());
        tag(ItemTags.CHEST_ARMOR).add(ModItems.NINJA_CHESTPLATE.asItem());
        tag(ItemTags.HEAD_ARMOR).add(ModItems.NINJA_HELMET.asItem());
        tag(ItemTags.DYEABLE).add(ModItems.NINJA_BOOTS.asItem()).add(ModItems.NINJA_LEGGINGS.asItem())
                .add(ModItems.NINJA_CHESTPLATE.asItem()).add(ModItems.NINJA_HELMET.asItem());

        tag(ModItemTags.KATANA).add(ModItems.KATANA.asItem());
        tag(ModItemTags.SICKLE).add(ModItems.CHAIN_SICKLE.asItem());
        tag(ModItemTags.SHURIKEN).add(ModItems.SHURIKEN.asItem());
        tag(ModItemTags.NINJA_ARMOR).add(ModItems.NINJA_BOOTS.asItem()).add(ModItems.NINJA_LEGGINGS.asItem())
                .add(ModItems.NINJA_CHESTPLATE.asItem()).add(ModItems.NINJA_HELMET.asItem());
        tag(ModItemTags.SMOKE_BOMB).add(ModItems.SMOKE_BOMB.asItem());
    }
}
