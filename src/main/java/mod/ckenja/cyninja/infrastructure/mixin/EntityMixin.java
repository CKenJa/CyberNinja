package mod.ckenja.cyninja.infrastructure.mixin;

import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import mod.ckenja.cyninja.core.util.NinjaActionUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract void setDeltaMovement(Vec3 p_20257_);

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(double p_20335_, double p_20336_, double p_20337_);

    @Inject(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collideWithShapes(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;", shift = At.Shift.BEFORE))
    private void collide(Vec3 p_20273_, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.getData(ModAttachments.ACTION).getCurrentAction().value() == NinjaActions.SLIDE.value()) {

                Vec3 vec3 = getDeltaMovement();
                //段差に当たったとき減速する
                setDeltaMovement(vec3.x * 0.85F, vec3.y, vec3.z * 0.85F);
            }
        }
    }
}
