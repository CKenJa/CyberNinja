package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.item.NinjaArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cyninja.MODID);

    public static final DeferredItem<Item> NINJA_CHESTPLATE = ITEMS.register("ninja_chestplate", () -> new NinjaArmorItem(ModArmorMaterials.NINJA, ArmorItem.Type.CHESTPLATE, (new Item.Properties()).durability(ArmorItem.Type.CHESTPLATE.getDurability(18))));
}
