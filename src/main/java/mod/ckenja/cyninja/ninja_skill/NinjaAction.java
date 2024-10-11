package mod.ckenja.cyninja.ninja_skill;

import mod.ckenja.cyninja.registry.NinjaActions;
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
    private boolean loop;

    // Next input acceptance period *ms
    public int timeout;

    private Function<LivingEntity, Holder<NinjaAction>> next;
    private Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout;

    private Consumer<LivingEntity> holdAction;

    private Consumer<LivingEntity> tickAction;

    private BiConsumer<LivingEntity, LivingEntity> hitEffect;

    private int priority;

    private Optional<EntityDimensions> hitBox = Optional.empty();

    public NinjaAction(Builder builder) {
        this.startTick = builder.startTick;
        this.endTick = builder.endTick;
        this.moveSpeed = builder.moveSpeed;
        this.timeout = builder.timeout;
        this.loop = builder.loop;
        this.next = builder.next;
        this.hitBox = builder.hitBox;
        this.nextOfTimeout = builder.nextOfTimeout;

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

    public int getTimeout() {
        return timeout;
    }

    public int getPriority() {
        return priority;
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
        private int startTick;
        private int endTick;
        private float moveSpeed;
        private boolean loop;
        private int timeout;
        private Function<LivingEntity, Holder<NinjaAction>> next;
        private Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout;


        private Consumer<LivingEntity> holdAction;
        private Consumer<LivingEntity> tickAction;
        private BiConsumer<LivingEntity, LivingEntity> hitEffect;
        private Optional<EntityDimensions> hitBox = Optional.empty();

        private Builder() {
            this.priority = 1000;
            this.timeout = 0;
            this.moveSpeed = 0F;
            this.loop = false;
            this.next = entity -> NinjaActions.NONE;
            this.nextOfTimeout = entity -> NinjaActions.NONE;
            this.tickAction = (livingEntity -> {

            });
            this.holdAction = (a) -> {
            };
            this.hitEffect = (a, b) -> {
            };
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public NinjaAction build() {
            return new NinjaAction(this);
        }

        public Builder startAndEnd(int start, int end) {
            this.startTick = start;
            this.endTick = end;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder speed(float speed) {
            this.moveSpeed = speed;
            return this;
        }

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
    }
}
