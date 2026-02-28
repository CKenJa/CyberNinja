package mod.ckenja.cyninja.core.util;

import mod.ckenja.cyninja.core.action.Action;
import mod.ckenja.cyninja.core.action.ActionAttachment;
import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class JumpHelper {
    public static boolean canAirJump(LivingEntity livingEntity) {
        return canAirJump(livingEntity, NinjaActions.NONE);
    }

    public static boolean canAirJump(LivingEntity livingEntity, Holder<Action> action) {
        return livingEntity.getData(ModAttachments.STATES).canAirJump() && canJump(livingEntity, action);
    }

    public static boolean canJump(LivingEntity livingEntity, Holder<Action> needAction) {
        ActionAttachment data = livingEntity.getData(ModAttachments.ACTION);
        return NinjaActionUtils.isInAir(livingEntity) &&
                !livingEntity.getData(ModAttachments.INPUT).keyDown(NinjaInput.JUMP) &&//今tickからジャンプキーを押し始めたか?
                data.getCurrentAction().is(needAction) &&
                //currentAction.value() == needAction.value() &&
                (!(livingEntity instanceof Player player) || !player.getAbilities().flying);
    }
}
