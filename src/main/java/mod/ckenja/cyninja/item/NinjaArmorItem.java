package mod.ckenja.cyninja.item;

import mod.ckenja.cyninja.item.data.NinjaActionData;
import mod.ckenja.cyninja.registry.ModDataComponents;
import mod.ckenja.cyninja.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NinjaArmorItem extends ArmorItem {
    public NinjaArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void onAnimalArmorTick(ItemStack stack, Level level, Mob mob) {
        super.onAnimalArmorTick(stack, level, mob);
        NinjaActionData actionData = stack.getOrDefault(ModDataComponents.NINJA_ACTION_DATA, new NinjaActionData(0, NinjaActions.NONE));
        actionData.tick(mob);
        stack.set(ModDataComponents.NINJA_ACTION_DATA, actionData);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof LivingEntity livingEntity) {
            NinjaActionData actionData = stack.getOrDefault(ModDataComponents.NINJA_ACTION_DATA, new NinjaActionData(0, NinjaActions.NONE));
            actionData.tick(livingEntity);
            stack.set(ModDataComponents.NINJA_ACTION_DATA, actionData);
        }
    }
}
