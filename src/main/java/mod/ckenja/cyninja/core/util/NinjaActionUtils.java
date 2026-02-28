package mod.ckenja.cyninja.core.util;

import mod.ckenja.cyninja.content.entity.NinjaFaker;
import mod.ckenja.cyninja.core.action.Action;
import mod.ckenja.cyninja.infrastructure.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NinjaActionUtils {
    public static void resetEntitiesTarget(Level level, Vec3 position){
        List<Entity> entities = NinjaActionUtils.getEnemiesInSphere(level, position, 6);
        entities.forEach(entity -> {
            if (entity instanceof Mob mob)
                mob.setTarget(null);
        });
    }

    public static void attackEntities(LivingEntity attacker, List<Entity> victims, float damage, float knockback, ResourceKey<DamageType> damageType) {
        for(Entity victim: victims){
            if (victim.isAttackable() && !victim.isAlliedTo(attacker)) {
                victim.hurt(attacker.damageSources().source(damageType, attacker), damage);
                if (victim instanceof LivingEntity livingVictim) {
                    double d0 = attacker.getX() - livingVictim.getX();
                    double d1 = attacker.getZ() - livingVictim.getZ();
                    livingVictim.knockback(knockback, d0, d1);
                }
                //TODO: これはノックバックしたときのみ必要なのか、それともダメージを受けた時も必要なのか確認
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
                double d0 = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * livingEntity.getDimensions(livingEntity.getPose()).width();
                double d1 = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * livingEntity.getDimensions(livingEntity.getPose()).width();
                if (blockpos1.getX() != blockpos.getX()) {
                    d0 = Mth.clamp(d0, blockpos.getX(), blockpos.getX() + 1.0);
                }

                if (blockpos1.getZ() != blockpos.getZ()) {
                    d1 = Mth.clamp(d1, blockpos.getZ(), blockpos.getZ() + 1.0);
                }

                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), d0, livingEntity.getY() + 0.1, d1, vec3.x * -4.0, 1.5, vec3.z * -4.0);
            }
    }

    public static List<Entity> getEnemiesInSphere(Level level, Vec3 position, double r) {
        AABB aabb = new AABB(position.x-r,position.y-r,position.z-r,position.x+r,position.y+r,position.z+r);
        return level.getEntitiesOfClass(Entity.class,aabb).stream()
                .filter(entity -> entity.position().distanceTo(position) <= r)
                .toList();
    }

    public static boolean setEntityWithSummonShadow(LivingEntity living, Vec3 pos, Vec3 offset, float yRot, Holder<Action> actionHolder) {
        return setEntityWithSummonShadow(living, pos, offset, yRot, actionHolder, 300);
    }

    public static boolean setEntityWithSummonShadow(LivingEntity living, Vec3 pos, Vec3 offset, float yRot, Holder<Action> actionHolder, int duration) {
        if (!(living instanceof NinjaFaker)) {
            CompoundTag compoundtag = new CompoundTag();
            living.saveWithoutId(compoundtag);
            if (!living.level().isClientSide()) {
                NinjaFaker faker = ModEntities.NINJA_FAKER.get().create(living.level());
                for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                    ItemStack itemstack = living.getItemBySlot(equipmentslot);
                    if (!itemstack.isEmpty()) {
                        faker.setItemSlot(equipmentslot, itemstack.copy());
                        faker.setDropChance(equipmentslot, 0.0F);
                    }
                }
                faker.readAdditionalSaveData(compoundtag);
                faker.setDataUuid(living.getUUID());
                faker.setXRot(living.getXRot());
                faker.setYRot(living.getYRot() + yRot);
                faker.setPos(pos.add(VectorUtil.getInputVector(offset, 0, faker.getYRot())));
                VectorUtil.moveRelativeActionY(faker, 0.5F, offset);
                faker.setOnGround(false);
                faker.setDuration(duration);

                if (living.level().addFreshEntity(faker)) {
                    faker.getData(ModAttachments.ACTION).syncAction(faker, actionHolder);

                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isInAir(LivingEntity livingEntity) {
        return !livingEntity.onGround() && !isInFluid(livingEntity);
    }

    public static boolean isInFluid(LivingEntity livingEntity) {
        return livingEntity.isInFluidType() || livingEntity.isInWater();
    }
}
