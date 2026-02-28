package mod.ckenja.cyninja.infrastructure.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> KATANA = createTag("katana");
    public static final TagKey<Item> SICKLE = createTag("sickle");
    public static final TagKey<Item> SHURIKEN = createTag("shuriken");
    public static final TagKey<Item> NINJA_ARMOR = createTag("ninja_armor");
    public static final TagKey<Item> SMOKE_BOMB = createTag("smoke_bomb");

    private static TagKey<Item> createTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("cyninja", name));
    }
}