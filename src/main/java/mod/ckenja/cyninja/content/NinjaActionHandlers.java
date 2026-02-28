package mod.ckenja.cyninja.content;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.core.action.ActionAttachment;
import mod.ckenja.cyninja.core.action.EquipmentCondition;
import mod.ckenja.cyninja.core.action.Action;
import mod.ckenja.cyninja.core.util.NinjaActionUtils;
import mod.ckenja.cyninja.core.util.NinjaInput;
import mod.ckenja.cyninja.infrastructure.attachment.ActionStatesAttachment;
import mod.ckenja.cyninja.infrastructure.network.ResetFallServerPacket;
import mod.ckenja.cyninja.infrastructure.network.SetActionToServerPacket;
import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static mod.ckenja.cyninja.core.util.VectorUtil.moveToLookingWay;
import static mod.ckenja.cyninja.infrastructure.registry.NinjaActions.NONE;
import static net.minecraft.world.entity.EquipmentSlotGroup.ARMOR;

public class NinjaActionHandlers {
    private static final ResourceLocation SLIDE_STEP_ID = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "slide_step");
    private static final ResourceLocation HEAVY_GRAVITY_ID = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "heavy_gravity");

    public static void doAirJump(LivingEntity livingEntity) {
        Vec3 delta = livingEntity.getDeltaMovement();
        //架空の床を踏んで飛ぶイメージなので、上向きの運動量に定数を設定する
        livingEntity.setDeltaMovement(delta.x, 0.6F, delta.z);
        livingEntity.hasImpulse = true;

        livingEntity.resetFallDistance();
        livingEntity.getData(ModAttachments.STATES).decreaseAirJumpCount();
    }

    public static void mirrorImageDo(LivingEntity living) {
        NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, -180F, NinjaActions.SLIDE,20);
        if(new EquipmentCondition(ARMOR, Items.COPPER_INGOT).test(living)){ //TODO シャドウラークが発動しているかどうかにする
            NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, 120F, NinjaActions.SLIDE, 20);
            NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, -120F, NinjaActions.SLIDE, 20);
        }

    }

    private static void knockback(Level p_335716_, LivingEntity attacker) {
        p_335716_.levelEvent(2013, attacker.getOnPos(), 750);
        p_335716_.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(3.5), knockbackPredicate(attacker))
                .forEach(p_347296_ -> {
                    Vec3 vec3 = p_347296_.position().subtract(attacker.position());
                    double d0 = getKnockbackPower(attacker, p_347296_, vec3);
                    Vec3 vec31 = vec3.normalize().scale(d0);
                    if (d0 > 0.0) {
                        p_347296_.push(vec31.x, 0.7F, vec31.z);
                        if (p_347296_ instanceof ServerPlayer serverplayer) {
                            serverplayer.connection.send(new ClientboundSetEntityMotionPacket(serverplayer));
                        }
                    }
                });
    }

    private static Predicate<LivingEntity> knockbackPredicate(LivingEntity owner) {
        return p_344407_ -> {
            boolean flag;
            boolean flag1;
            boolean flag2;
            boolean flag6;
            label62:
            {
                flag = !p_344407_.isSpectator();
                flag1 = p_344407_ != owner;
                flag2 = !owner.isAlliedTo(p_344407_);
                if (p_344407_ instanceof TamableAnimal tamableanimal && tamableanimal.isTame() && owner.getUUID().equals(tamableanimal.getOwnerUUID())) {
                    flag6 = true;
                    break label62;
                }

                flag6 = false;
            }

            boolean flag3;
            label55:
            {
                flag3 = !flag6;
                if (p_344407_ instanceof ArmorStand armorstand && armorstand.isMarker()) {
                    flag6 = false;
                    break label55;
                }

                flag6 = true;
            }

            boolean flag4 = flag6;
            boolean flag5 = owner.distanceToSqr(p_344407_) <= Math.pow(3.5, 2.0);
            return flag && flag1 && flag2 && flag3 && flag4 && flag5;
        };
    }

    private static double getKnockbackPower(LivingEntity owner, LivingEntity p_338630_, Vec3 p_338866_) {
        return (3.5 - p_338866_.length())
                * 0.7F
                * (double) (owner.fallDistance > 5.0F ? 2 : 1)
                * (1.0 - p_338630_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static void startHeavyFall(LivingEntity livingEntity) {
        livingEntity.getData(ModAttachments.STATES).decreaseAirJumpCount();

        livingEntity.playSound(SoundEvents.SMITHING_TABLE_USE, 1.0F, 1.0F);
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.GRAVITY);
        if (attributeinstance != null && !attributeinstance.hasModifier(HEAVY_GRAVITY_ID)) {
            attributeinstance.addTransientModifier(new AttributeModifier(HEAVY_GRAVITY_ID, 0.08F, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void stopHeavyFall(LivingEntity livingEntity) {
        Level level = livingEntity.level();

        if (!level.isClientSide()) {
            knockback(level, livingEntity);
        }
        livingEntity.resetFallDistance();

        livingEntity.playSound(SoundEvents.ANVIL_PLACE, 1.0F, 1.5F);
        livingEntity.playSound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 1.25F);
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.GRAVITY);
        if (attributeinstance != null && !attributeinstance.hasModifier(HEAVY_GRAVITY_ID)) {
            attributeinstance.removeModifier(HEAVY_GRAVITY_ID);
        }
    }

    public static void tickHeavyFall(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        Vec3 look = livingEntity.getLookAngle();

        if (!level.isClientSide()) {
            List<Entity> list = level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(1.0F).move(look.reverse().scale(2.0F)));
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable() && !entity.isAlliedTo(livingEntity)) {
                        entity.hurt(livingEntity.damageSources().source(DamageTypes.MOB_ATTACK, livingEntity), 8F);
                    }
                }
            }
        }
    }

    public static void tickHeavyAirJump(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        Vec3 delta = livingEntity.getDeltaMovement();
        Vec3 look = livingEntity.getLookAngle();
        livingEntity.setDeltaMovement(delta.x, 0.6F, delta.z);
        livingEntity.moveRelative(0.6F, new Vec3(livingEntity.xxa, 0, livingEntity.zza));
        livingEntity.hasImpulse = true;

        livingEntity.getData(ModAttachments.STATES).decreaseAirJumpCount();

        livingEntity.resetFallDistance();
        livingEntity.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value());

        if (!level.isClientSide) {
            List<Entity> list = level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(1.0F).move(look.reverse().scale(2.0F)));
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable() && !entity.isAlliedTo(livingEntity)) {
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
        ActionStatesAttachment attachment = livingEntity.getData(ModAttachments.STATES);
        Vec3 look = livingEntity.calculateViewVector(attachment.getActionXRot(), attachment.getActionYRot());

        livingEntity.setDeltaMovement(look.x * 0.8F, look.y * 0.8F, look.z * 0.8F);
        //livingEntity.push(look.scale(0.08F));
        livingEntity.hasImpulse = true;

        livingEntity.resetFallDistance();
        livingEntity.playSound(SoundEvents.WIND_CHARGE_BURST.value());

        if (level.isClientSide) {
            Vec3 delta = livingEntity.getDeltaMovement();

            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), delta.x * -2, delta.y * -2, delta.z * -2);
        }
    }

    public static void checkWallSlide(LivingEntity livingEntity) {
        Vec3 vec3 = livingEntity.getDeltaMovement();
        //slide to looking way
        if (vec3.y < 0.0F) {
            livingEntity.setDeltaMovement(vec3.x, vec3.y * 0.6F, vec3.z);
            if (livingEntity.level().isClientSide()) {
                PacketDistributor.sendToServer(new ResetFallServerPacket());
            }
            livingEntity.resetFallDistance();
            livingEntity.hasImpulse = true;
        }
    }

    public static void startSlide(LivingEntity livingEntity) {
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.STEP_HEIGHT);
        if (attributeinstance != null && !attributeinstance.hasModifier(SLIDE_STEP_ID)) {
            livingEntity.getAttribute(Attributes.STEP_HEIGHT).addTransientModifier(new AttributeModifier(SLIDE_STEP_ID, 0.5F, AttributeModifier.Operation.ADD_VALUE));
        }
        ActionStatesAttachment state = livingEntity.getData(ModAttachments.STATES);
        state.setActionYRot(livingEntity.yHeadRot);
        state.decreaseAirSlideCount();

        Vec3 vec3 = livingEntity.getDeltaMovement();
        livingEntity.setDeltaMovement(vec3.x, 0, vec3.z);
        livingEntity.resetFallDistance();
        moveToLookingWay(livingEntity, 1F, NinjaActions.SLIDE);
    }

    public static void stopSlide(LivingEntity livingEntity) {
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.STEP_HEIGHT);
        if (attributeinstance != null) {
            attributeinstance.removeModifier(SLIDE_STEP_ID);
        }
    }

    public static Optional<Holder<Action>> nextSlide(LivingEntity livingEntity) {
        //壁にぶつかったら止まる。そして減速
        if (livingEntity.horizontalCollision) {
            Vec3 delta = livingEntity.getDeltaMovement();
            livingEntity.setDeltaMovement(delta.x * 0.45F, delta.y, delta.z * 0.45F);
            return Optional.of(NinjaActions.NONE);
        }
        // jumpで止まる
        EnumSet<NinjaInput> currentInputs = livingEntity.getData(ModAttachments.INPUT).getCurrentInputs();
        if (livingEntity.level().isClientSide && currentInputs != null) {
            //sneakを押してなければnone
            if (!currentInputs.contains(NinjaInput.SNEAK)) {
                if (livingEntity.level().isClientSide()) {
                    PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
                }
                return Optional.of(NinjaActions.NONE);
            }

            if (currentInputs.contains(NinjaInput.JUMP) && livingEntity.onGround()) {
                if (livingEntity.level().isClientSide()) {
                    PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
                }
                return Optional.of(NinjaActions.NONE);
            }
        }
        // 一定時間経過かつ減速で止まる
        if (livingEntity.getDeltaMovement().horizontalDistance() < 0.2F) {
            return Optional.of(NinjaActions.NONE);
        }
        return Optional.empty();
    }
}
