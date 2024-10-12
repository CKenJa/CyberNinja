package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.item.NinjaArmorItem;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.ModAttachments;
import mod.ckenja.cyninja.registry.NinjaActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NinjaActionUtils {

    public static void tickAirJump(LivingEntity livingEntity) {
        Vec3 vec3 = livingEntity.getDeltaMovement();
        livingEntity.setDeltaMovement(vec3.x, 0.6F, vec3.z);
        livingEntity.resetFallDistance();
        livingEntity.hasImpulse = true;
    }

    public static void checkSlideAttack(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            List<Entity> list = livingEntity.level().getEntities(livingEntity, livingEntity.getBoundingBox());
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable()) {
                        entity.hurt(livingEntity.damageSources().source(DamageTypes.MOB_ATTACK, livingEntity), 6F);
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
        } else {
            spawnSprintParticle(livingEntity);
        }

        //slide to looking way
        livingEntity.moveRelative(0.2F, new Vec3(0, 0, NinjaActions.SLIDE.get().getMoveSpeed()));
        livingEntity.hasImpulse = true;
    }

    protected static void spawnSprintParticle(LivingEntity livingEntity) {
        BlockPos blockpos = livingEntity.getOnPosLegacy();
        BlockState blockstate = livingEntity.level().getBlockState(blockpos);
        if (!blockstate.addRunningEffects(livingEntity.level(), blockpos, livingEntity))
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                Vec3 vec3 = livingEntity.getDeltaMovement();
                BlockPos blockpos1 = livingEntity.blockPosition();
                double d0 = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * (double) livingEntity.getDimensions(livingEntity.getPose()).width();
                double d1 = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * (double) livingEntity.getDimensions(livingEntity.getPose()).width();
                if (blockpos1.getX() != blockpos.getX()) {
                    d0 = Mth.clamp(d0, (double) blockpos.getX(), (double) blockpos.getX() + 1.0);
                }

                if (blockpos1.getZ() != blockpos.getZ()) {
                    d1 = Mth.clamp(d1, (double) blockpos.getZ(), (double) blockpos.getZ() + 1.0);
                }

                livingEntity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), d0, livingEntity.getY() + 0.1, d1, vec3.x * -4.0, 1.5, vec3.z * -4.0);
            }
    }


    public static void setAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        livingEntity.getData(ModAttachments.NINJA_ACTION.get()).setNinjaAction(livingEntity, ninjaAction);
    }

    public static void syncAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        livingEntity.getData(ModAttachments.NINJA_ACTION.get()).sync(livingEntity, ninjaAction);
    }

    public static NinjaActionAttachment getActionData(LivingEntity livingEntity) {
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
