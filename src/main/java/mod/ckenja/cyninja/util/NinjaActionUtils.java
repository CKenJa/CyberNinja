package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.item.NinjaArmorItem;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NinjaActionUtils {

    public static void doAirJump(LivingEntity livingEntity) {
        Vec3 vec3 = livingEntity.getDeltaMovement();
        livingEntity.setDeltaMovement(vec3.x, 0.6F, vec3.z);
        livingEntity.resetFallDistance();
        livingEntity.hasImpulse = true;
        NinjaActionUtils.getActionData(livingEntity).decreaseAirJumpCount();
    }

    public static void tickHeavyAirJump(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        Vec3 delta = livingEntity.getDeltaMovement();
        Vec3 look = livingEntity.getLookAngle();
        livingEntity.setDeltaMovement(delta.x, 0.6F, delta.z);
        livingEntity.moveRelative(0.6F, new Vec3(livingEntity.xxa, 0, livingEntity.zza));
        livingEntity.hasImpulse = true;

        NinjaActionUtils.getActionData(livingEntity).decreaseAirJumpCount();

        livingEntity.resetFallDistance();
        livingEntity.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value());

        if (!level.isClientSide()) {
            List<Entity> list = level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(1.0F).move(look.reverse().scale(2.0F)));
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable()) {
                        entity.hurt(livingEntity.damageSources().source(DamageTypes.MOB_ATTACK, livingEntity), 8F);
                    }
                }
            }
        } else {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), delta.x * -2, delta.y * -2, delta.z * -2);
        }
    }

    public static void tickAirRocket(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        Vec3 delta = livingEntity.getDeltaMovement();
        Vec3 look = livingEntity.getLookAngle();

        livingEntity.setDeltaMovement(delta.x + look.x * 0.08F, delta.y + look.y * 0.08F + livingEntity.getGravity() * 1.01F, delta.z + look.z * 0.08F);
        livingEntity.hasImpulse = true;

        livingEntity.resetFallDistance();
        livingEntity.playSound(SoundEvents.WIND_CHARGE_BURST.value());

        if (level.isClientSide()) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), delta.x * -2, delta.y * -2, delta.z * -2);
        }
    }

    public static void checkWallSlide(LivingEntity livingEntity) {

        Vec3 vec3 = livingEntity.getDeltaMovement();
        //slide to looking way
        if (vec3.y < 0.0F) {
            livingEntity.setDeltaMovement(vec3.x, vec3.y * 0.6F, vec3.z);
            livingEntity.resetFallDistance();
            livingEntity.hasImpulse = true;
        }
    }

    public static void attackEntities(LivingEntity attacker, List<Entity> victims, float damage, float knockback, ResourceKey<DamageType> damageType) {
        for(Entity victim: victims){
            if (victim.isAttackable()) {
                victim.hurt(attacker.damageSources().source(damageType, attacker), damage);
                if (victim instanceof LivingEntity livingVictim) {
                    double d0 = attacker.getX() - livingVictim.getX();
                    double d1 = attacker.getZ() - livingVictim.getZ();
                    livingVictim.knockback(knockback, d0, d1);
                }
                //これはノックバックしたときのみ必要なのか、それともダメージを受けた時も必要なのか
                attacker.hasImpulse = true;
            }
        }
    }


    public static void spawnSprintParticle(LivingEntity livingEntity) {
        BlockPos blockpos = livingEntity.getOnPosLegacy();
        Level level = livingEntity.level();

        BlockState blockstate = level.getBlockState(blockpos);
        if (!blockstate.addRunningEffects(level, blockpos, livingEntity))
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

                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), d0, livingEntity.getY() + 0.1, d1, vec3.x * -4.0, 1.5, vec3.z * -4.0);
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

    public static boolean isWearingNinjaTrim(LivingEntity livingEntity, Item item) {
        for (ItemStack itemstack : livingEntity.getArmorAndBodyArmorSlots()) {
            if ((itemstack.getItem() instanceof NinjaArmorItem ninjaArmorItem)) {
                ArmorTrim armorTrim = itemstack.get(DataComponents.TRIM);
                if (armorTrim != null && armorTrim.material().value().ingredient().value() == item) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWearingNinjaForWolf(Mob livingEntity) {
        if (!(livingEntity.getBodyArmorItem().getItem() instanceof NinjaArmorItem ninjaArmorItem)) {
            return false;
        }


        return true;
    }

    public static List<Entity> getEnemiesInSphere(Level level, Vec3 position, double r) {
        AABB aabb = new AABB(position.x-r,position.y-r,position.z-r,position.x+r,position.y+r,position.z+r);
        return level.getEntitiesOfClass(Entity.class,aabb).stream()
                .filter(entity -> entity.position().distanceTo(position) <= r).toList();
    }

    public static boolean canDoubleJump(LivingEntity livingEntity) {
        return getActionData(livingEntity).canDoubleJump(livingEntity);
    }
}
