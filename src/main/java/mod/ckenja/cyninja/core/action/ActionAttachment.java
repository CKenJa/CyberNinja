package mod.ckenja.cyninja.core.action;

import mod.ckenja.cyninja.infrastructure.network.SetActionToClientPacket;
import mod.ckenja.cyninja.infrastructure.network.SetActionToServerPacket;
import mod.ckenja.cyninja.infrastructure.registry.ActionRegistry;
import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static mod.ckenja.cyninja.core.action.Action.NINJA_ACTIONS;

public class ActionAttachment implements INBTSerializable<CompoundTag> {
    private final Map<Action, Integer> cooldownMap = new HashMap<>();

    private Holder<Action> currentAction = NinjaActions.NONE;
    private int actionTick;

    public void setAction(LivingEntity livingEntity, Holder<Action> newAction) {
        //stop
        executeActionWithModifier(currentAction.value(), livingEntity, action -> {
            action.stopAction(livingEntity);
            livingEntity.getData(ModAttachments.COOLDOWN).setCooldown(action);
        });

        //start
        if (newAction.value().getActionTickType() != ActionTickType.INSTANT) {
            currentAction = newAction;
            actionTick = 0;
        }
        executeActionWithModifier(newAction.value(), livingEntity, action -> action.startAction(livingEntity));

        //EntityEvent.sizeを叩いてCommonEvents.scaleEventを実行させる
        livingEntity.refreshDimensions();
    }

    public void syncAction(LivingEntity livingEntity, Holder<Action> action) {
        setAction(livingEntity, action);
        if (!livingEntity.level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SetActionToClientPacket(livingEntity, action));
        }
    }

    public void selectAndSendAction(Player player) {
        NINJA_ACTIONS.stream()
                .map(Holder::value)
                .filter(action -> !action.isModifier() &&
                        currentAction.value().getStartCondition().canAction(player) &&
                        currentAction.value().getEquipmentCondition().test(player))
                .filter(action -> {
                    if (action.getActionTickType() == ActionTickType.INSTANT) {
                        sendAction(action, player);
                        return false;
                    }
                    return true;
                })
                .min(Comparator.comparingInt(action -> action.getStartCondition().priority))
                .ifPresent(action -> sendAction(action, player));
    }

    private void sendAction(Action action, Player player) {
        ResourceLocation sendAction = ActionRegistry.getRegistry().getKey(ActionAttachment.getActionOrOverride(action, player));
        PacketDistributor.sendToServer(new SetActionToServerPacket(sendAction));
    }

    public void tick(LivingEntity user) {
        actionTick++;

        switch (getTickState()){
            case STARTED:
                executeActionWithModifier(currentAction.value(), user, action -> action.tickAction(user));
                currentAction.value().getNext(user).ifPresent(action -> syncAction(user, action));
                break;
            case STOPPED:
                executeActionWithModifier(currentAction.value(), user, action -> action.getNextOfTimeout(user));
                break;
        }
    }

    public void pretick(LivingEntity user) {
        if (getTickState() == TickState.STARTED) {
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

    private TickState getTickState() {
        return currentAction.value().getTickState(actionTick);
    }

    public static Stream<Action> getModifiers(Action Action, LivingEntity livingEntity) {
        return NINJA_ACTIONS.stream()
                .map(Holder::value)
                .filter(action -> action.isModifier() &&
                        action.getStartCondition().predicate.test(livingEntity) &&
                        action.isModifierOf(Action.isOverride() ? Action.getOriginAction() : Action));
    }

    public static void executeActionWithModifier(Action action, LivingEntity livingEntity, Consumer<Action> consumer) {
        consumer.accept(getActionOrOverride(action,livingEntity));
        getModifiers(action, livingEntity)
                .filter(Action::isInject)
                .forEach(consumer);
    }

    public static Action getActionOrOverride(Action origin, LivingEntity livingEntity) {
        return getModifiers(origin, livingEntity)
                .filter(Action::isOverride)
                .min(Comparator.comparingInt(action -> action.getStartCondition().priority))
                .orElse(origin);
    }

    public Holder<Action> getCurrentAction() {
        return currentAction;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();

        ResourceLocation resourceLocation = ActionRegistry.getRegistry().getKey(currentAction.value());
        if (resourceLocation != null) {
            nbt.putString("Action", resourceLocation.toString());
        }
        nbt.putInt("ActionTick", actionTick);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("Action")) {
            String s = nbt.getString("Action");
            Optional<Holder.Reference<Action>> action = ActionRegistry.getRegistry().getHolder(ResourceLocation.parse(s));
            action.ifPresent(ActionReference -> currentAction = ActionReference);
        }
        if (nbt.contains("ActionTick")) {
            actionTick = nbt.getInt("ActionTick");
        }
    }
}