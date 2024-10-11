package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, Cyninja.MODID);

    public static final Holder<ArmorMaterial> NINJA = register("ninja", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323378_ -> {
        p_323378_.put(ArmorItem.Type.BOOTS, 3);
        p_323378_.put(ArmorItem.Type.LEGGINGS, 5);
        p_323378_.put(ArmorItem.Type.CHESTPLATE, 7);
        p_323378_.put(ArmorItem.Type.HELMET, 3);
        p_323378_.put(ArmorItem.Type.BODY, 6);
    }), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.01F, () -> Ingredient.of(Items.PHANTOM_MEMBRANE));

    private static Holder<ArmorMaterial> register(
            String name,
            EnumMap<ArmorItem.Type, Integer> p_324599_,
            int enchantabilty,
            Holder<SoundEvent> sound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient
    ) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, name)));
        return register(name, p_324599_, enchantabilty, sound, toughness, knockbackResistance, repairIngredient, list);
    }

    private static Holder<ArmorMaterial> register(
            String name,
            EnumMap<ArmorItem.Type, Integer> p_324599_,
            int enchantabilty,
            Holder<SoundEvent> sound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient,
            List<ArmorMaterial.Layer> p_323990_
    ) {
        EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);

        for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
            enummap.put(armoritem$type, p_324599_.get(armoritem$type));
        }

        return ARMOR_MATERIALS.register(name,
                () -> new ArmorMaterial(enummap, enchantabilty, sound, repairIngredient, p_323990_, toughness, knockbackResistance)
        );
    }
}