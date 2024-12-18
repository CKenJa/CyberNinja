package mod.ckenja.cyninja.data;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.item.NinjaArmorItem;
import mod.ckenja.cyninja.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.ItemLayerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mod.ckenja.cyninja.Cyninja.prefix;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, Cyninja.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.singleTexTool(ModItems.SHURIKEN);
        this.singleTex(ModItems.SMOKE_BOMB);
        this.singleTex(ModItems.CYBER_TRIM_SMITHING_TEMPLATE);
        this.singleTexTool(ModItems.KATANA);
        this.sickleItem(ModItems.CHAIN_SICKLE);
        this.trimmedArmor(ModItems.NINJA_BOOTS);
        this.trimmedArmor(ModItems.NINJA_LEGGINGS);
        this.trimmedArmor(ModItems.NINJA_CHESTPLATE);
        this.trimmedArmor(ModItems.NINJA_HELMET);
        this.egg(ModItems.CYBER_NINJA_SPAWN_EGG);
    }

    public ItemModelBuilder egg(Supplier<Item> item) {
        return withExistingParent(BuiltInRegistries.ITEM.getKey(item.get()).getPath(), mcLoc("item/template_spawn_egg"));
    }

    public ItemModelBuilder sickleItem(Supplier<? extends Item> item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.get());
        buildItem(id.getPath() + "_chain_only", mcLoc("item/handheld").toString(), 0, modLoc("item/" + id.getPath() + "_chain_only"));
        return withExistingParent(id.getPath(), mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + id.getPath()))
                .override().predicate(ResourceLocation.parse("chain_only"), 1).model(getExistingFile(modLoc("item/" + id.getPath() + "_chain_only"))).end();
    }



    private ItemModelBuilder tool(String name, ResourceLocation... layers) {
        return buildItem(name, "item/handheld", 0, layers);
    }

    private ItemModelBuilder singleTexTool(Supplier<? extends Item> item) {
        return tool(itemPath(item).getPath(), prefix("item/" + itemPath(item).getPath()));
    }

    private ItemModelBuilder singleTexRodTool(Supplier<? extends Item> item) {
        return toolRod(itemPath(item).getPath(), prefix("item/" + itemPath(item).getPath()));
    }

    private ItemModelBuilder toolRod(String name, ResourceLocation... layers) {
        return buildItem(name, "item/handheld_rod", 0, layers);
    }


    private ItemModelBuilder singleTex(Supplier<? extends ItemLike> item) {
        return generated(itemPath(item).getPath(), prefix("item/" + itemPath(item).getPath()));
    }

    private ItemModelBuilder generated(String name, ResourceLocation... layers) {
        return buildItem(name, "item/generated", 0, layers);
    }


    private ItemModelBuilder buildItem(String name, String parent, int emissivity, ResourceLocation... layers) {
        ItemModelBuilder builder = withExistingParent(name, parent);
        for (int i = 0; i < layers.length; i++) {
            builder = builder.texture("layer" + i, layers[i]);
        }
        if (emissivity > 0)
            builder = builder.customLoader(ItemLayerModelBuilder::begin).emissive(emissivity, emissivity, 0).renderType("minecraft:translucent", 0).end();
        return builder;
    }


    public ResourceLocation itemPath(Supplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem());
    }

    private void trimmedArmor(DeferredItem<NinjaArmorItem> armor) {
        ItemModelBuilder base = this.singleTex(armor);
        for (ItemModelGenerators.TrimModelData trim : ItemModelGenerators.GENERATED_TRIM_MODELS) {
            String material = trim.name();
            String name = itemPath(armor).getPath() + "_" + material + "_trim";
            ModelFile trimModel = this.withExistingParent(name, this.mcLoc("item/generated"))
                    .texture("layer0", prefix("item/" + itemPath(armor).getPath()))
                    .texture("layer1", this.mcLoc("trims/items/" + armor.get().getType().getName() + "_trim_" + material));
            base.override().predicate(ResourceLocation.parse("trim_type"), trim.itemModelIndex()).model(trimModel).end();
        }
    }
}
