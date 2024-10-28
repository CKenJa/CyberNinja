package mod.ckenja.cyninja.ninja_action;

import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaInput;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class NinjaAction {

    public static final List<Holder<NinjaAction>> NINJA_ACTIONS = new ArrayList<>();

    private final int startTick;
    private final int endTick;
    private final int cooldown;
    private final float moveSpeed;
    private final float reduceDamage;
    private final float reduceKnockback;
    private final NinjaActionTickType ninjaActionTickType;
  
    private final boolean canVanillaAction;

    private final ModifierType modifierType;
    private final Holder<NinjaAction> originAction;
  
    private final boolean noBob;

    // Next input acceptance period *ms
    //TODO: add timeout map
    private final int timeout;

    private final Function<LivingEntity, Holder<NinjaAction>> next;
    private final Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout;
    private final Predicate<LivingEntity> needCondition;

    private final Consumer<LivingEntity> tickAction;

    private final Consumer<LivingEntity> startAction;
    private final Consumer<LivingEntity> stopAction;
    private final int priority;

    private final Optional<EntityDimensions> hitBox;

    private final EnumSet<NinjaInput> startInputs;

    private NinjaAction(Builder builder) {
        this.startTick = builder.startTick;
        this.endTick = builder.endTick;
        this.cooldown = builder.cooldown;
        this.moveSpeed = builder.moveSpeed;
        this.reduceDamage = builder.reduceDamage;
        this.reduceKnockback = builder.reduceKnockback;
        this.timeout = builder.timeout;
        this.ninjaActionTickType = builder.ninjaActionTickType;
      
        this.canVanillaAction = builder.canVanillaAction;

        this.modifierType = builder.modifierType;
        this.originAction = builder.originAction;

        this.noBob = builder.noBob;

        this.next = builder.next;
        this.hitBox = builder.hitBox;
        this.nextOfTimeout = builder.nextOfTimeout;
        this.needCondition = builder.needCondition;

        this.startInputs = builder.startInputs;
        if (this.startInputs == null || !this.startInputs.isEmpty()) {
            NINJA_ACTIONS.add(Holder.direct(this));
        }
        this.tickAction = builder.tickAction;
        this.startAction = builder.startAction;
        this.stopAction = builder.stopAction;

        this.priority = builder.priority;
    }

    NinjaActionTickType getNinjaActionTickType() {
        return ninjaActionTickType;
    }

    public boolean isModifierOf(NinjaAction action) {
        if (!isModifier())
            return false;
        ResourceLocation resourceLocation = NinjaActions.getRegistry().getKey(action);
        if (resourceLocation == null)
            return false;
        return originAction.is(resourceLocation);
    }

    int getStartTick() {
        return startTick;
    }

    int getEndTick() {
        return endTick;
    }

    int getCooldown() {
        return cooldown;
    }

    int getTimeout() {
        return timeout;
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

    public int getPriority() {
        return priority;
    }

    public boolean isCanVanillaAction() {
        return canVanillaAction;
    }

    public boolean isNoBob() {
        return noBob;
    }

    public Optional<EntityDimensions> getHitBox() {
        return hitBox;
    }

    Holder<NinjaAction> getNext(LivingEntity entity) {
        return next.apply(entity);
    }

    Holder<NinjaAction> getNextOfTimeout(LivingEntity entity) {
        return nextOfTimeout.apply(entity);
    }

    boolean needCondition(LivingEntity entity) {
        return needCondition.test(entity);
    }

    void tickAction(LivingEntity user) {
        tickAction.accept(user);
    }

    void startAction(LivingEntity user) {
        startAction.accept(user);
    }

    void stopAction(LivingEntity user) {
        stopAction.accept(user);
    }

    EnumSet<NinjaInput> getStartInputs() {
        return startInputs;
    }

    public boolean isModifier() {
        return isInject() || isOverride();
    }

    public boolean isInject() {
        return modifierType == ModifierType.INJECT;
    }

    public boolean isOverride() {
        return modifierType == ModifierType.OVERRIDE;
    }

    NinjaAction getOriginAction() {
        return originAction.value();
    }

    public static class Builder {
        private EnumSet<NinjaInput> startInputs;
        private int priority;
        private boolean canVanillaAction;
        private boolean noBob;
        private int startTick;
        private int endTick;
        private int cooldown;
        private float moveSpeed;
        private NinjaActionTickType ninjaActionTickType;
        private int timeout;
        private float reduceDamage;
        private float reduceKnockback;
        private Function<LivingEntity, Holder<NinjaAction>> next;
        private Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout;
        private Predicate<LivingEntity> needCondition;

        private Consumer<LivingEntity> startAction;
        private Consumer<LivingEntity> stopAction;
        private Consumer<LivingEntity> tickAction;
        private Optional<ResourceLocation> animationID;
        private Optional<EntityDimensions> hitBox = Optional.empty();

        private ModifierType modifierType = ModifierType.NONE;
        private Holder<NinjaAction> originAction;

        private Builder() {
            this.priority = 1000;
            this.timeout = 0;
            this.moveSpeed = 0F;
            this.reduceDamage = 0.0F;
            this.ninjaActionTickType = NinjaActionTickType.START_TO_END;
            this.next = entity -> null;
            this.nextOfTimeout = entity -> NinjaActions.NONE;
            this.needCondition = entity -> true;
            this.tickAction = (livingEntity -> {

            });
            this.startAction = (livingEntity -> {

            });
            this.stopAction = (livingEntity -> {

            });
            this.animationID = Optional.empty();
            this.canVanillaAction = true;
            this.noBob = false;
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
            this.ninjaActionTickType = NinjaActionTickType.START_TO_END;
            return this;
        }

        public Builder instant() {
            this.ninjaActionTickType = NinjaActionTickType.INSTANT;
            return this;
        }

        //Set Cooldown
        public Builder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }


        //The smaller the number, the higher the priority.
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
            this.ninjaActionTickType = NinjaActionTickType.LOOP;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * 途中で終了して次のアクションに変えるか決定する関数を設定する。
         * @param next 発動中毎tick実行され、null以外を返すとエンティティのアクションを返り値に変更し、nullを返すとアクションを変更しない。
         * @return このビルダー自身
         */
        public Builder next(Function<LivingEntity, Holder<NinjaAction>> next) {
            this.next = next;
            return this;
        }

        public Builder nextOfTimeout(Function<LivingEntity, Holder<NinjaAction>> nextOfTimeout) {
            this.nextOfTimeout = nextOfTimeout;
            return this;
        }

        public Builder addTickAction(Consumer<LivingEntity> tickAction) {
            this.tickAction = this.tickAction.andThen(tickAction);
            return this;
        }

        public Builder addStartAction(Consumer<LivingEntity> startAction) {
            this.startAction = this.startAction.andThen(startAction);
            return this;
        }

        public Builder addStopAction(Consumer<LivingEntity> stopAction) {
            this.stopAction = this.stopAction.andThen(stopAction);
            return this;
        }

        public Builder setHitBox(EntityDimensions hitBox) {
            this.hitBox = Optional.of(hitBox);
            return this;
        }

        public Builder setCanVanillaAction(boolean canVanillaAction) {
            this.canVanillaAction = canVanillaAction;
            return this;
        }

        public Builder addNeedCondition(Predicate<LivingEntity> needCondition) {
            this.needCondition = this.needCondition.and(needCondition);
            return this;
        }

        public Builder setStartInput(NinjaInput... ninjaInputs) {
            startInputs = EnumSet.copyOf(Arrays.asList(ninjaInputs));
            return this;
        }

        public Builder setNoBob(boolean noBob) {
            this.noBob = noBob;
            return this;
        }

        public Builder inject(Holder<NinjaAction> holder) {
            this.modifierType = ModifierType.INJECT;
            this.originAction = holder;
            return this;
        }

        public Builder override(Holder<NinjaAction> holder) {
            this.modifierType = ModifierType.OVERRIDE;
            this.originAction = holder;
            return this;
        }
    }
}
