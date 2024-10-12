package mod.ckenja.cyninja.action;

import mod.ckenja.cyninja.registry.ModActions;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class NinjaAction {

    private int startTick;
    private int endTick;
    private float moveSpeed;
    private float reduceDamage;
    private float reduceKnockback;
    private boolean loop;
    private boolean canJump;

    // Next input acceptance period *ms
    public int timeout;

    private Function<LivingEntity, Holder<NinjaAction>> next;
    private Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout;
    private Function<LivingEntity, Boolean> needCondition;

    private Consumer<LivingEntity> holdAction;

    private Consumer<LivingEntity> tickAction;

    private BiConsumer<LivingEntity, LivingEntity> hitEffect;

    private int priority;

    private Optional<EntityDimensions> hitBox = Optional.empty();

    public NinjaAction(Builder builder) {
        this.startTick = builder.startTick;
        this.endTick = builder.endTick;
        this.moveSpeed = builder.moveSpeed;
        this.reduceDamage = builder.reduceDamage;
        this.reduceKnockback = builder.reduceKnockback;
        this.timeout = builder.timeout;
        this.loop = builder.loop;
        this.canJump = builder.canJump;

        this.next = builder.next;
        this.hitBox = builder.hitBox;
        this.nextOfTimeout = builder.nextOfTimeout;
        this.needCondition = builder.needCondition;
        this.holdAction = builder.holdAction;

        this.tickAction = builder.tickAction;

        this.hitEffect = builder.hitEffect;

        this.priority = builder.priority;
    }

    public boolean isLoop() {
        return loop;
    }

    public int getStartTick() {
        return startTick;
    }

    public int getEndTick() {
        return endTick;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getReduceDamage() {
        return reduceDamage;
    }

    public float getReduceKnockback() {
        return reduceKnockback;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCanJump() {
        return canJump;
    }

    public Optional<EntityDimensions> getHitBox() {
        return hitBox;
    }


    public Function<LivingEntity, Holder<NinjaAction>> getNext() {
        return next;
    }

    public Function<LivingEntity, Holder<NinjaAction>> getNextOfTimeout() {
        return nextOfTimeout;
    }

    public Function<LivingEntity, Boolean> getNeedCondition() {
        return needCondition;
    }

    public void holdAction(LivingEntity user) {
        holdAction.accept(user);
    }

    public void tickAction(LivingEntity user) {
        tickAction.accept(user);
    }

    public void hitEffect(LivingEntity target, LivingEntity attacker) {
        hitEffect.accept(target, attacker);
    }

    public static class Builder {
        private int priority;
        private boolean canJump;
        private int startTick;
        private int endTick;
        private float moveSpeed;
        private boolean loop;
        private int timeout;
        private float reduceDamage;
        private float reduceKnockback;
        private Function<LivingEntity, Holder<NinjaAction>> next;
        private Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout;
        private Function<LivingEntity, Boolean> needCondition;

        private Consumer<LivingEntity> holdAction;
        private Consumer<LivingEntity> tickAction;
        private BiConsumer<LivingEntity, LivingEntity> hitEffect;
        private Optional<EntityDimensions> hitBox = Optional.empty();

        private Builder() {
            this.priority = 1000;
            this.timeout = 0;
            this.moveSpeed = 0F;
            this.reduceDamage = 0.0F;
            this.loop = false;
            this.next = entity -> null;
            this.nextOfTimeout = entity -> ModActions.NONE;
            this.needCondition = entity -> true;
            this.tickAction = (livingEntity -> {

            });
            this.holdAction = (a) -> {
            };
            this.hitEffect = (a, b) -> {
            };
            this.canJump = true;
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public NinjaAction build() {
            return new NinjaAction(this);
        }

        //This is set start action and stop action
        public Builder startAndEnd(int start, int end) {
            this.startTick = start;
            this.endTick = end;
            return this;
        }

        //set priority that Which one is used first(higher valve is make more first usable)
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        //this method set Straight-line speed
        public Builder speed(float speed) {
            this.moveSpeed = speed;
            return this;
        }

        //set reduce damage percent when action
        public Builder setReduceDamage(float reduceDamage) {
            this.reduceDamage = reduceDamage;
            return this;
        }

        public Builder setReduceKnockback(float reduceKnockback) {
            this.reduceKnockback = reduceKnockback;
            return this;
        }

        //loop action
        public Builder loop() {
            this.loop = true;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder next(Function<LivingEntity, Holder<NinjaAction>> next) {
            this.next = next;
            return this;
        }

        public Builder nextOfTimeout(Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout) {
            this.nextOfTimeout = nextOfTimeout;
            return this;
        }

        public Builder addHoldAction(Consumer<LivingEntity> holdAction) {
            this.holdAction = this.holdAction.andThen(holdAction);
            return this;
        }

        public Builder addTickAction(Consumer<LivingEntity> tickAction) {
            this.tickAction = this.tickAction.andThen(tickAction);
            return this;
        }

        public Builder addHitEffect(BiConsumer<LivingEntity, LivingEntity> hitEffect) {
            this.hitEffect = this.hitEffect.andThen(hitEffect);
            return this;
        }

        public Builder setHitBox(EntityDimensions hitBox) {
            this.hitBox = Optional.of(hitBox);
            return this;
        }

        public Builder setCanJump(boolean canJump) {
            this.canJump = canJump;
            return this;
        }

        public Builder setNeedCondition(Function<LivingEntity, Boolean> needCondition) {
            this.needCondition = needCondition;
            return this;
        }

    }
}
