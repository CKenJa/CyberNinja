package mod.ckenja.cyninja.mixin;

import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> p_316430_);

    public LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);

    }

    protected Vec3 maybeBackOffFromEdge(Vec3 p_20019_, MoverType p_20020_) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (NinjaActionUtils.getActionData(livingEntity).getNinjaAction().value() == NinjaActions.SLIDE.value()) {
            return p_20019_;
        }
        return super.maybeBackOffFromEdge(p_20019_, p_20020_);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(Vec3 p_21280_, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        double d0 = this.getGravity();
        boolean flag = this.getDeltaMovement().y <= 0.0;
        if (flag && this.hasEffect(MobEffects.SLOW_FALLING)) {
            d0 = Math.min(d0, 0.01);
        }
        if (NinjaActionUtils.getActionData(livingEntity).getNinjaAction().value() == NinjaActions.SLIDE.value()) {
            if (this.isControlledByLocalInstance()) {
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
                    this.setDeltaMovement(vec35.x * (double) f3, this instanceof FlyingAnimal ? d2 * (double) f3 : d2 * 0.98F, vec35.z * (double) f3);
                }
                this.move(MoverType.SELF, this.getDeltaMovement());
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
