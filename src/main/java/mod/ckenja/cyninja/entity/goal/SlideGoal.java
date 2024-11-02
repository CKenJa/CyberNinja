package mod.ckenja.cyninja.entity.goal;

import bagu_chan.bagus_lib.entity.goal.TimeConditionGoal;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.EnumSet;

public class SlideGoal extends TimeConditionGoal {
    private int cooldown;
    private int maxCooldown;
    private final UniformInt timeBetweenCooldown;

    public SlideGoal(Mob mob, UniformInt cooldown) {
        super(mob, cooldown);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        this.timeBetweenCooldown = cooldown;
    }

    @Override
    public boolean canUse() {
        if (this.maxCooldown <= 0) {
            this.maxCooldown = this.timeBetweenCooldown.sample(this.mob.getRandom());
            return false;
        } else if (this.cooldown > this.maxCooldown && this.isMatchCondition()) {
            this.cooldown = 0;
            this.maxCooldown = this.timeBetweenCooldown.sample(this.mob.getRandom());
            return true;
        } else {
            ++this.cooldown;
            return false;
        }
    }


    @Override
    public boolean isMatchCondition() {
        LivingEntity livingEntity = this.mob.getTarget();
        return this.mob.onGround() && this.mob.isAlive() && livingEntity != null && livingEntity.isAlive() && this.mob.hasLineOfSight(livingEntity) && (this.mob.distanceTo(livingEntity) < 6F);
    }

    @Override
    public void stop() {
        super.stop();
        this.cooldown = this.maxCooldown;
        this.maxCooldown = this.timeBetweenCooldown.sample(this.mob.getRandom());

    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        super.start();

        NinjaActionUtils.syncAction(this.mob, NinjaActions.SLIDE);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
