package mod.ckenja.cyninja.entity.goal;

import bagu_chan.bagus_lib.entity.goal.TimeConditionGoal;
import mod.ckenja.cyninja.registry.ModItems;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;

import java.util.EnumSet;

public class JumpGoal extends TimeConditionGoal {
    public JumpGoal(Mob mob, UniformInt cooldown) {
        super(mob, cooldown);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    public JumpGoal(Mob mob, UniformInt cooldown, UniformInt time) {
        super(mob, cooldown, time);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean isMatchCondition() {
        return this.mob.onGround() || this.mob.fallDistance > 3.0F;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.onGround() || NinjaActionUtils.getActionData(this.mob).getNinjaAction().value() == NinjaActions.AIR_JUMP.value();
    }

    @Override
    public void start() {
        super.start();

        NinjaActionUtils.syncAction(this.mob, NinjaActions.AIR_JUMP);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tick == 5 && this.mob.isHolding(ModItems.KATANA.asItem())) {
            NinjaActionUtils.syncAction(this.mob, NinjaActions.SPIN);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
