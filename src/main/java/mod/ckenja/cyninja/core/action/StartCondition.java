package mod.ckenja.cyninja.core.action;

import mod.ckenja.cyninja.core.util.NinjaInput;
import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;
import java.util.function.Predicate;

//物理クライアントでプレイヤーがアクションを行えるか判断するクラス
public class StartCondition {
    final Action action;
    final Predicate<LivingEntity> predicate;
    final int priority;
    final EnumSet<NinjaInput> inputs;
    final int cooldown;

    public StartCondition(Action action, Predicate<LivingEntity> predicate, int priority, EnumSet<NinjaInput> inputs, int cooldown) {
        this.action = action;
        this.predicate = predicate;
        this.priority = priority;
        this.inputs = inputs;
        this.cooldown = cooldown;
    }

    public boolean canAction(LivingEntity player){
        ActionAttachment data = player.getData(ModAttachments.ACTION);
        EnumSet<NinjaInput> currentInputs = player.getData(ModAttachments.INPUT).getCurrentInputs();
        return action != NinjaActions.NONE.get() &&
                action != data.getCurrentAction() &&
                //入力が必要ないもの or 必要で、一致するもの
                (inputs == null || inputs.containsAll(currentInputs)) &&
                predicate.test(player) &&
                player.getData(ModAttachments.COOLDOWN).isCooldownFinished(action);
    }

    public int getCooldown() {
        return cooldown;
    }

    public static class Builder {
        Predicate<LivingEntity> predicate;
        int priority;
        EnumSet<NinjaInput> inputs;
        int cooldown;

        public Builder() {
            this.predicate = livingEntity -> true;
            this.priority = 0;
            this.inputs = EnumSet.noneOf(NinjaInput.class);
            this.cooldown = 0;
        }

        public StartCondition build(Action action) {
            return new StartCondition(action, this.predicate, this.priority, this.inputs, this.cooldown);
        }
    }
}