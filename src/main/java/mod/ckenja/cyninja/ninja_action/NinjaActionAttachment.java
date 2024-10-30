package mod.ckenja.cyninja.ninja_action;

import com.google.common.collect.Maps;
import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static mod.ckenja.cyninja.ninja_action.NinjaAction.NINJA_ACTIONS;

public class NinjaActionAttachment implements INBTSerializable<CompoundTag> {
    private final Map<NinjaAction, Integer> cooldownMap = Maps.newHashMap();

    private EnumSet<NinjaInput> previousInputs = EnumSet.noneOf(NinjaInput.class);
    private EnumSet<NinjaInput> currentInputs = EnumSet.noneOf(NinjaInput.class);

    private Holder<NinjaAction> currentAction = NinjaActions.NONE;
    private int actionTick;
    private int airTick;

    private int airJumpCount;
    private int airSlideCount;

    private float actionXRot;
    private float actionYRot;

    public void checkKeyDown(ClientTickEvent.Pre event) {
        previousInputs = currentInputs;
        currentInputs = EnumSet.noneOf(NinjaInput.class);
        Options options = Minecraft.getInstance().options;
        if (options.keyShift.isDown())
            currentInputs.add(NinjaInput.SNEAK);
        if (options.keyJump.isDown())
            currentInputs.add(NinjaInput.JUMP);
        if (options.keySprint.isDown())
            currentInputs.add(NinjaInput.SPRINT);
        if (options.keyUse.isDown())
            currentInputs.add(NinjaInput.LEFT_CLICK);
    }

    public int getActionTick() {
        return actionTick;
    }

    public Holder<NinjaAction> getCurrentAction() {
        return currentAction;
    }

    public void setActionYRot(float actionYRot) {
        this.actionYRot = actionYRot;
    }

    public float getActionYRot() {
        return actionYRot;
    }

    public void setActionXRot(float actionXRot) {
        this.actionXRot = actionXRot;
    }

    public float getActionXRot() {
        return actionXRot;
    }

    public void setAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        if (ninjaAction.value().getNinjaActionTickType() != NinjaActionTickType.INSTANT){
            executeActionWithModifier(ninjaAction.value(), livingEntity, action -> action.stopAction(livingEntity));

            if (currentAction.value().getCooldown() > 0) {
                executeActionWithModifier(currentAction.value(), livingEntity, this::setCooldown);
            }

            currentAction = ninjaAction;
            setActionTick(0);
        }
        executeActionWithModifier(ninjaAction.value(), livingEntity, action -> action.startAction(livingEntity));
        livingEntity.refreshDimensions();
    }

    public void syncAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        setAction(livingEntity, ninjaAction);
        if (!livingEntity.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SetActionToClientPacket(livingEntity, ninjaAction));
        }
    }

    public void setActionTick(int actionTick) {
        this.actionTick = actionTick;
    }

    public TickState getTickState(LivingEntity user) {
        return currentAction.value().getNinjaActionTickType().apply(user);
    }

    public void tick(LivingEntity user) {
        TickState tickState = getTickState(user);

        if (tickState == TickState.STARTED) {
            executeActionWithModifier(currentAction.value(), user, action -> action.tickAction(user));
        }

        cooldownMap.replaceAll((key,value) -> value - 1);
        cooldownMap.entrySet().removeIf(entry -> entry.getValue() <= 0);

        if (tickState != TickState.STOPPED) {
            setActionTick(getActionTick() + 1);
        } else {
            setAction(user, currentAction.value().getNextOfTimeout(user));
        }

        if (tickState == TickState.STARTED) {
            Holder<NinjaAction> ninjaAction = currentAction.value().getNext(user);
            if (ninjaAction != null) {
                setAction(user, ninjaAction);
            }
        }

        if (user.isInFluidType() || user.isInWater()) {
            airTick = 3;
        } else if (user.onGround()) {
            airTick = 3;
        } else if (airTick > 0) {
            airTick--;
        }
        if(user.onGround()){
            resetAirJumpCount();
            resetAirSlideCount();
        }
    }

    public void pretick(LivingEntity user) {
        if (getTickState(user) == TickState.STARTED) {
            if (!currentAction.value().isCanVanillaAction()) {
                user.setSprinting(false);
                user.setShiftKeyDown(false);
            }
            if (currentAction.value().getHitBox().isPresent()) {
                user.setPose(Pose.STANDING);
            }
        }


        if (user instanceof Player player && currentAction.value().isNoBob()) {
            player.bob = 0.0F;
            player.oBob = 0.0F;
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        ResourceLocation resourceLocation = NinjaActions.getRegistry().getKey(currentAction.value());
        if (resourceLocation != null) {
            nbt.putString("NinjaAction", resourceLocation.toString());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

        if (nbt.contains("NinjaAction")) {
            String s = nbt.getString("NinjaAction");
            Optional<Holder.Reference<NinjaAction>> action = NinjaActions.getRegistry().getHolder(ResourceLocation.parse(s));
            action.ifPresent(ninjaActionReference -> currentAction = ninjaActionReference);
        }

    }

    public void setCooldown(NinjaAction ninjaAction) {
        cooldownMap.putIfAbsent(ninjaAction, ninjaAction.getCooldown());
    }

    public boolean isCooldownFinished(NinjaAction action){
        return !cooldownMap.containsKey(action);
    }

    public boolean canAction(NinjaAction action, Player player) {
        return action != NinjaActions.NONE.get() &&
                //入力が必要ないもの or 必要で、一致するもの
                (action.getStartInputs() == null ||
                        action.getStartInputs() != null && currentInputs.containsAll(action.getStartInputs())) &&
                action != currentAction &&
                action.needCondition(player) &&
                isCooldownFinished(action);
    }


    public boolean canAirJump(LivingEntity livingEntity) {
        return canAirJump(livingEntity, NinjaActions.NONE);
    }

    public boolean canAirJump(LivingEntity livingEntity, Holder<NinjaAction> action) {
        return airJumpCount>0 && canJump(livingEntity, action);
    }

    public boolean canJump(LivingEntity livingEntity, Holder<NinjaAction> needAction) {
        ResourceLocation resourceLocation = NinjaActions.getRegistry().getKey(needAction.value());
        if (resourceLocation == null)
            return false;
        return airTick <= 0 &&
                !previousInputs.contains(NinjaInput.JUMP) &&//今tickからジャンプキーを押し始めたか?
                currentAction.is(resourceLocation) &&
                //currentAction.value() == needAction.value() &&
                (!(livingEntity instanceof Player player) || !player.getAbilities().flying);
    }

    public EnumSet<NinjaInput> getCurrentInputs() {
        return currentInputs;
    }

    public EnumSet<NinjaInput> getPreviousInputs() {
        return previousInputs;
    }

    public void resetAirJumpCount() {
        airJumpCount = 1;
    }

    public void decreaseAirJumpCount() {
        airJumpCount--;
    }

    public void resetAirSlideCount() {
        airSlideCount = 1;
    }

    public void decreaseAirSlideCount() {
        airSlideCount--;
    }

    public boolean canAirSlideCount() {
        return airSlideCount > 0;
    }

    public static Stream<NinjaAction> getModifiers(NinjaAction ninjaAction, LivingEntity livingEntity) {
        return NINJA_ACTIONS.stream()
                .map(Holder::value)
                .filter(action -> action.isModifier() &&
                        action.needCondition(livingEntity) &&
                        action.isModifierOf(ninjaAction.isOverride() ? ninjaAction.getOriginAction() : ninjaAction));
    }

    public static void executeActionWithModifier(NinjaAction ninjaAction, LivingEntity livingEntity, Consumer<NinjaAction> consumer) {
        NinjaAction doAction = getModifiers(ninjaAction, livingEntity)
                .filter(NinjaAction::isOverride)
                .min(Comparator.comparingInt(NinjaAction::getPriority))
                .orElse(ninjaAction);
        consumer.accept(doAction);
        getModifiers(ninjaAction, livingEntity)
                .filter(NinjaAction::isInject)
                .forEach(consumer);
    }

    public static NinjaAction getActionOrOveride(NinjaAction ninjaAction, LivingEntity livingEntity) {
        return getModifiers(ninjaAction, livingEntity)
                .filter(NinjaAction::isOverride)
                .min(Comparator.comparingInt(NinjaAction::getPriority))
                .orElse(ninjaAction);
    }
}