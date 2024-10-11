package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.item.NinjaArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cyninja.MODID);

    public static final DeferredItem<Item> NINJA_HELMET = ITEMS.register("ninja_helmet", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.HELMET, (new Item.Properties()).durability(ArmorItem.Type.HELMET.getDurability(18))));
    public static final DeferredItem<Item> NINJA_CHESTPLATE = ITEMS.register("ninja_chestplate", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.CHESTPLATE, (new Item.Properties()).durability(ArmorItem.Type.CHESTPLATE.getDurability(18))));
    public static final DeferredItem<Item> NINJA_LEGGINGS = ITEMS.register("ninja_leggings", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.LEGGINGS, (new Item.Properties()).durability(ArmorItem.Type.LEGGINGS.getDurability(18))));
    public static final DeferredItem<Item> NINJA_BOOTS = ITEMS.register("ninja_boots", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.BOOTS, (new Item.Properties()).durability(ArmorItem.Type.BOOTS.getDurability(18))));
}
