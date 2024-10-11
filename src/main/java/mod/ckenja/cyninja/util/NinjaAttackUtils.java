package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.item.NinjaArmorItem;
import mod.ckenja.cyninja.item.data.NinjaActionData;
import mod.ckenja.cyninja.ninja_skill.NinjaAction;
import mod.ckenja.cyninja.registry.ModDataComponents;
import mod.ckenja.cyninja.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class NinjaAttackUtils {

    public static void checkSlideAttack(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            List<Entity> list = livingEntity.level().getEntities(livingEntity, livingEntity.getBoundingBox());
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable()) {
                        entity.hurt(livingEntity.damageSources().source(DamageTypes.MOB_ATTACK), 4F);
                        if (entity instanceof LivingEntity target) {
                            double d0 = livingEntity.getX() - target.getX();
                            double d1 = livingEntity.getZ() - target.getZ();
                            target.knockback(0.6F, d0, d1);
                        }
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().scale(-0.2));
                        livingEntity.hasImpulse = true;
                        break;
                    }
                }
            } else if (livingEntity.horizontalCollision) {
                setAction(livingEntity, NinjaActions.NONE);
            }
        }
        livingEntity.zza = NinjaActions.SLIDE.get().getMoveSpeed();
    }

    public static void setAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack stack = livingEntity.getItemBySlot(equipmentSlot);
            if (stack.getItem() instanceof NinjaArmorItem armorItem) {
                stack.set(ModDataComponents.NINJA_ACTION_DATA, new NinjaActionData(0, ninjaAction));
            }
        }
    }

    @Nullable
    public static Holder<NinjaAction> getAction(LivingEntity livingEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack stack = livingEntity.getItemBySlot(equipmentSlot);
            if (stack.getItem() instanceof NinjaArmorItem armorItem) {
                return stack.getOrDefault(ModDataComponents.NINJA_ACTION_DATA, new NinjaActionData(0, NinjaActions.NONE)).ninjaActionHolder();
            }
        }
        return null;
    }
}
