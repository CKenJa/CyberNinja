package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.item.NinjaArmorItem;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.ModAttachments;
import mod.ckenja.cyninja.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NinjaActionUtils {

    public static void checkSlideAttack(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            List<Entity> list = livingEntity.level().getEntities(livingEntity, livingEntity.getBoundingBox());
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable()) {
                        entity.hurt(livingEntity.damageSources().source(DamageTypes.MOB_ATTACK), 6F);
                        if (entity instanceof LivingEntity target) {
                            double d0 = livingEntity.getX() - target.getX();
                            double d1 = livingEntity.getZ() - target.getZ();
                            target.knockback(0.8F, d0, d1);
                        }
                        livingEntity.hasImpulse = true;
                        break;
                    }
                }
            }
        }

        //slide to looking way
        livingEntity.moveRelative(0.2F, new Vec3(0, 0, NinjaActions.SLIDE.get().getMoveSpeed()));
        livingEntity.hasImpulse = true;
    }


    public static void setActionData(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        livingEntity.getData(ModAttachments.NINJA_ACTION.get()).setNinjaAction(ninjaAction);
    }

    public static NinjaActionAttachment getAction(LivingEntity livingEntity) {
        return livingEntity.getData(ModAttachments.NINJA_ACTION.get());
    }

    public static boolean isWearingNinja(LivingEntity livingEntity) {
        int i = 0;
        for (ItemStack itemstack : livingEntity.getArmorAndBodyArmorSlots()) {
            if ((itemstack.getItem() instanceof NinjaArmorItem ninjaArmorItem)) {
                i++;
            }
        }

        return i >= 4;
    }

    public static boolean isWearingNinjaForWolf(Mob livingEntity) {
        if (!(livingEntity.getBodyArmorItem().getItem() instanceof NinjaArmorItem ninjaArmorItem)) {
            return false;
        }


        return true;
    }
}
