package mod.ckenja.cyninja.infrastructure.mixin;

import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import mod.ckenja.cyninja.core.util.NinjaActionUtils;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    protected abstract float getWaterSlowDown();

    @Shadow
    public abstract float getSpeed();

    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> p_251296_);

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract boolean canStandOnFluid(FluidState p_204042_);

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Shadow
    public abstract Vec3 getFluidFallingAdjustedMovement(double p_20995_, boolean p_20996_, Vec3 p_20997_);

    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> p_316430_);

    public LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);

    }
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(Vec3 p_21280_, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        double d0 = this.getGravity();
        boolean flag = this.getDeltaMovement().y <= 0.0;
        if (flag && this.hasEffect(MobEffects.SLOW_FALLING)) {
            d0 = Math.min(d0, 0.01);
        }
        if (livingEntity.getData(ModAttachments.ACTION).getCurrentAction().value() == NinjaActions.SLIDE.value()) {
            if (this.isControlledByLocalInstance()) {
                FluidState fluidstate = this.level().getFluidState(this.blockPosition());
                if ((this.isInWater() || (this.isInFluidType(fluidstate) && fluidstate.getFluidType() != net.neoforged.neoforge.common.NeoForgeMod.LAVA_TYPE.value())) && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate)) {
                    if (this.isInWater() || (this.isInFluidType(fluidstate) && !livingEntity.moveInFluid(fluidstate, p_21280_, d0))) {
                        double d9 = this.getY();
                        float f4 = 0.92F;
                        float f5 = 0.02F;
                        float f6 = (float) this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
                        if (!this.onGround()) {
                            f6 *= 0.5F;
                        }

                        if (f6 > 0.0F) {
                            f4 += (0.54600006F - f4) * f6;
                            f5 += (this.getSpeed() - f5) * f6;
                        }

                        if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                            f4 = 0.96F;
                        }

                        f5 *= (float) this.getAttributeValue(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
                        this.moveRelative(f5, p_21280_);
                        this.move(MoverType.SELF, this.getDeltaMovement());
                        Vec3 vec36 = this.getDeltaMovement();
                        if (this.horizontalCollision && this.onClimbable()) {
                            vec36 = new Vec3(vec36.x, 0.2, vec36.z);
                        }

                        this.setDeltaMovement(vec36.multiply((double) f4, 0.8F, (double) f4));
                        Vec3 vec32 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
                        this.setDeltaMovement(vec32);
                        if (this.horizontalCollision && this.isFree(vec32.x, vec32.y + 0.6F - this.getY() + d9, vec32.z)) {
                            this.setDeltaMovement(vec32.x, 0.3F, vec32.z);
                        }
                    }
                } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate)) {
                    double d8 = this.getY();
                    this.moveRelative(0.02F, p_21280_);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8F, 0.5));
                        Vec3 vec33 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
                        this.setDeltaMovement(vec33);
                    } else {
                        this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
                    }

                    if (d0 != 0.0) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d0 / 4.0, 0.0));
                    }

                    Vec3 vec34 = this.getDeltaMovement();
                    if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + 0.6F - this.getY() + d8, vec34.z)) {
                        this.setDeltaMovement(vec34.x, 0.3F, vec34.z);
                    }
                } else {
                    if (d0 != 0.0) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d0, 0.0));
                    }

                    float f2 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getFriction(level(), this.getBlockPosBelowThatAffectsMyMovement(), this);

                    float f3 = 0.98F - Mth.clamp(0.6F - f2, 0.0F, 1.0F);
                    Vec3 vec35 = this.getDeltaMovement();
                    double d2 = vec35.y;
                    if (this.shouldDiscardFriction()) {
                        this.setDeltaMovement(vec35.x, d2, vec35.z);
                    } else {
                        this.setDeltaMovement(vec35.x * (double) f3 + vec35.x * d0 * 0.01F, this instanceof FlyingAnimal ? d2 * (double) f3 : d2 * 0.98F, vec35.z * (double) f3 + vec35.z * d0 * 0.01F);
                    }
                    this.move(MoverType.SELF, this.getDeltaMovement());
                }
            }

            this.calculateEntityAnimation(this instanceof FlyingAnimal);
            ci.cancel();
        }

    }

    @Shadow
    public void calculateEntityAnimation(boolean b) {
    }

    @Shadow
    public boolean shouldDiscardFriction() {
        return false;
    }
}
