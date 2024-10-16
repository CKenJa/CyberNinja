package mod.ckenja.cyninja.entity.goal;

import bagu_chan.bagus_lib.entity.goal.TimeConditionGoal;
import mod.ckenja.cyninja.registry.ModItems;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.EnumSet;

public class JumpGoal extends TimeConditionGoal {
    private int cooldown;
    private int maxCooldown;
    private final UniformInt timeBetweenCooldown;
    public JumpGoal(Mob mob, UniformInt cooldown) {
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
        if (livingEntity == null || !this.mob.isAlive()) {
            return false;
        }

        return livingEntity.isAlive() && this.mob.hasLineOfSight(livingEntity) && (this.mob.distanceTo(livingEntity) < 3F) && (this.mob.onGround() || this.mob.fallDistance > 3.0F);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.onGround() || this.tick < 2;
    }

    @Override
    public void start() {
        super.start();

        this.mob.jumpFromGround();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tick == 3) {
            NinjaActionUtils.syncAction(this.mob, NinjaActions.AIR_JUMP);
        }
        if (this.tick == 7 && this.mob.isHolding(ModItems.KATANA.asItem())) {
            NinjaActionUtils.syncAction(this.mob, NinjaActions.SPIN);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
