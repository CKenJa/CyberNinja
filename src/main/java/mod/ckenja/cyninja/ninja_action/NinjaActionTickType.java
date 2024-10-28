package mod.ckenja.cyninja.ninja_action;

import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public enum NinjaActionTickType {
    START_TO_END(livingEntity -> {
        NinjaActionAttachment ninjaActionAttachment = NinjaActionUtils.getActionData(livingEntity);
        NinjaAction ninjaAction = ninjaActionAttachment.getCurrentAction().value();
        if (ninjaActionAttachment.getActionTick() >= ninjaAction.getStartTick() && ninjaActionAttachment.getActionTick() < ninjaAction.getEndTick()) {
            return TickState.STARTED;
        }
        if (ninjaActionAttachment.getActionTick() < ninjaAction.getStartTick()) {
            return TickState.NOT_START;
        }
        return TickState.STOPPED;
    }),
    LOOP(livingEntity -> TickState.STARTED),
    INSTANT(livingEntity -> TickState.STOPPED);

    private final Function<LivingEntity, TickState> function;

    NinjaActionTickType(Function<LivingEntity, TickState> function) {
        this.function = function;
    }

    TickState apply(LivingEntity user) {
        return function.apply(user);
    }
}
