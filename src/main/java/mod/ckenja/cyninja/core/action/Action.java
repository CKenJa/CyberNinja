package mod.ckenja.cyninja.core.action;

import mod.ckenja.cyninja.core.util.NinjaInput;
import mod.ckenja.cyninja.infrastructure.registry.ActionRegistry;
import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.HitResult;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Action {
    private final EquipmentCondition equipmentCondition;
    private final StartCondition startCondition;
    private final List<ActionBehaviour> actionBehaviours;

    private final ActionTickType actionTickType;
    private final int startTick;
    private final int endTick;
    private final Function<LivingEntity, Optional<Holder<Action>>> next;
    private final Function<LivingEntity, Holder<Action>> nextOfTimeout;

    private final Consumer<LivingEntity> tickAction;

    private final Consumer<LivingEntity> startAction;
    private final Consumer<LivingEntity> stopAction;

    private final float moveSpeed;
    private final float reduceDamage;
    private final float reduceKnockback;
    private final boolean noBob;
    private final EntityDimensions hitBox;

    private final boolean canVanillaAction;

    private final ModifierType modifierType;
    private final Holder<Action> originAction;

    private final BiConsumer<Projectile, HitResult> hitAction;

    public static final List<Holder<Action>> NINJA_ACTIONS = new ArrayList<>();

    private Action(Builder builder) {
        this.equipmentCondition = builder.equipmentCondition;
        this.startCondition = builder.startCondition.build(this);
        this.actionBehaviours = builder.actionBehaviours;

        this.startTick = builder.startTick;
        this.endTick = builder.endTick;
        this.moveSpeed = builder.moveSpeed;
        this.reduceDamage = builder.reduceDamage;
        this.reduceKnockback = builder.reduceKnockback;
        this.actionTickType = builder.actionTickType;

        this.canVanillaAction = builder.canVanillaAction;

        this.modifierType = builder.modifierType;
        this.originAction = builder.originAction;

        this.noBob = builder.noBob;

        this.next = builder.next;
        this.hitBox = builder.hitBox;
        this.nextOfTimeout = builder.nextOfTimeout;
        this.tickAction = builder.tickAction;
        this.hitAction = builder.hitAction;
        this.startAction = builder.startAction;
        this.stopAction = builder.stopAction;
    }

    ActionTickType getActionTickType() {
        return actionTickType;
    }

    public boolean isModifierOf(Action action) {
        if (!isModifier())
            return false;
        ResourceLocation resourceLocation = ActionRegistry.getRegistry().getKey(action);
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

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getReduceDamage() {
        return reduceDamage;
    }

    public float getReduceKnockback() {
        return reduceKnockback;
    }

    public boolean isCanVanillaAction() {
        return canVanillaAction;
    }

    public boolean isNoBob() {
        return noBob;
    }

    public Optional<EntityDimensions> getHitBox() {
        return Optional.ofNullable(hitBox);
    }

    Optional<Holder<Action>> getNext(LivingEntity entity) {
        return next.apply(entity);
    }

    Holder<Action> getNextOfTimeout(LivingEntity entity) {
        return nextOfTimeout.apply(entity);
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

    void hitAction(Projectile projectile, HitResult hitResult) {
        hitAction.accept(projectile, hitResult);
    }

    boolean isModifier() {
        return isInject() || isOverride();
    }

    boolean isInject() {
        return modifierType == ModifierType.INJECT;
    }

    boolean isOverride() {
        return modifierType == ModifierType.OVERRIDE;
    }

    Action getOriginAction() {
        return originAction.value();
    }

    StartCondition getStartCondition() {
        return startCondition;
    }

    TickState getTickState(int actionTick) {
        return getActionTickType().apply(this, actionTick);
    }

    EquipmentCondition getEquipmentCondition() {
        return equipmentCondition;
    }

    public static class Builder {
        private List<ActionBehaviour> actionBehaviours = new ArrayList<>();
        private EquipmentCondition equipmentCondition;
        private StartCondition.Builder startCondition;
        private boolean canVanillaAction;
        private boolean noBob;
        private int startTick;
        private int endTick;
        private float moveSpeed;
        private ActionTickType actionTickType;
        private int timeout;
        private float reduceDamage;
        private float reduceKnockback;
        private Function<LivingEntity, Optional<Holder<Action>>> next;
        private Function<LivingEntity, Holder<Action>> nextOfTimeout;

        private Consumer<LivingEntity> startAction;
        private Consumer<LivingEntity> stopAction;
        private BiConsumer<Projectile, HitResult> hitAction;
        private Consumer<LivingEntity> tickAction;
        private Optional<ResourceLocation> animationID;
        private EntityDimensions hitBox = null;

        private ModifierType modifierType = ModifierType.NONE;
        private Holder<Action> originAction;

        private Builder() {
            this.timeout = 0;
            this.moveSpeed = 0F;
            this.reduceDamage = 0.0F;
            this.actionTickType = ActionTickType.START_TO_END;
            this.next = entity -> null;
            this.nextOfTimeout = entity -> NinjaActions.NONE;
            this.tickAction = (livingEntity -> {

            });
            this.startAction = (livingEntity -> {

            });
            this.hitAction = ((projectile, hitResult) -> {

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

        public Action build() {
            return new Action(this);
        }

        public Builder transform(Function<Builder, Builder> transformer){
            return transformer.apply(this);
        }

        //This is set start action and stop action
        public Builder startAndEnd(int start, int end) {
            this.startTick = start;
            this.endTick = end;
            this.actionTickType = ActionTickType.START_TO_END;
            return this;
        }

        public Builder instant() {
            this.actionTickType = ActionTickType.INSTANT;
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
            this.actionTickType = ActionTickType.LOOP;
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
        public Builder next(Function<LivingEntity, Optional<Holder<Action>>> next) {
            this.next = next;
            return this;
        }

        public Builder nextOfTimeout(Function<LivingEntity, Holder<Action>> nextOfTimeout) {
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

        //※instantアクションで動作しない
        public Builder addHitAction(BiConsumer<Projectile, HitResult> hitAction) {
            this.hitAction = this.hitAction.andThen(hitAction);
            return this;
        }

        public Builder addStopAction(Consumer<LivingEntity> stopAction) {
            this.stopAction = this.stopAction.andThen(stopAction);
            return this;
        }

        public Builder setHitBox(EntityDimensions hitBox) {
            this.hitBox = hitBox;
            return this;
        }

        public Builder setCanVanillaAction(boolean canVanillaAction) {
            this.canVanillaAction = canVanillaAction;
            return this;
        }

        public Builder setNoBob(boolean noBob) {
            this.noBob = noBob;
            return this;
        }

        public Builder inject(Holder<Action> holder) {
            this.modifierType = ModifierType.INJECT;
            this.originAction = holder;
            return this;
        }

        /**
         * 現在のアクションを引数の指定したアクションの「オーバーライドアクション」にする。
         * 「オーバーライドアクション」は、「オリジナルアクション」を発動しようとしたとき、addNeedActionで指定した条件を満たしていれば発動する。
         * 条件以外は通常のアクション同様の設定が必要。
         *
         * @param holder 上書き先に指定する{@link Action} のホルダー
         * @return 現在の {@link Builder} インスタンス
         */
        public Builder override(Holder<Action> holder) {
            this.modifierType = ModifierType.OVERRIDE;
            this.originAction = holder;
            return this;
        }

        public Builder equipmentCondition(EquipmentCondition condition) {
            this.equipmentCondition = condition;
            return this;
        }

        public Builder equipmentCondition(EquipmentSlotGroup equipmentSlot, Item item) {
            this.equipmentCondition = new EquipmentCondition(equipmentSlot, item);
            return this;
        }

        public Builder startPredicate(Predicate<LivingEntity> predicate) {
            this.startCondition.predicate = Optional.ofNullable(this.startCondition.predicate).orElse(livingEntity -> true).and(predicate);
            return this;
        }

        public Builder startPredicateWithData(Predicate<ActionAttachment> predicate) {
            return startPredicate(livingEntity -> predicate.test(livingEntity.getData(ModAttachments.ACTION)));
        }

        public Builder setStartInput(NinjaInput... ninjaInputs) {
            this.startCondition.inputs = EnumSet.copyOf(Arrays.asList(ninjaInputs));
            return this;
        }

        //Set Cooldown
        public Builder cooldown(int cooldown) {
            this.startCondition.cooldown = cooldown;
            return this;
        }

        //The smaller the number, the higher the priority.
        public Builder priority(int priority) {
            this.startCondition.priority = priority;
            return this;
        }

        //The smaller the number, the higher the priority.
        public Builder addBehaviour(ActionBehaviour behaviour) {
            this.actionBehaviours.add(behaviour);
            return this;
        }
    }
}
