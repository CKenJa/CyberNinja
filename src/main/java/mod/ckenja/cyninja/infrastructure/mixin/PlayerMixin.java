package mod.ckenja.cyninja.infrastructure.mixin;

import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import mod.ckenja.cyninja.core.util.NinjaActionUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity {

    public PlayerMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);

    }

    @Inject(method = "maybeBackOffFromEdge", at = @At("HEAD"), cancellable = true)
    protected void maybeBackOffFromEdge(Vec3 p_36201_, MoverType p_36202_, CallbackInfoReturnable<Vec3> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity.getData(ModAttachments.ACTION).getCurrentAction().value() == NinjaActions.SLIDE.value()) {
            cir.setReturnValue(p_36201_);
        }
    }
}
