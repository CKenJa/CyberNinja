package mod.ckenja.cyninja.attachment;

import com.google.common.collect.Maps;
import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.ninja_action.ModifierType;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.ninja_action.TickState;
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

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

public class NinjaActionAttachment implements INBTSerializable<CompoundTag> {
    private final Map<NinjaAction, Integer> cooldownMap = Maps.newHashMap();

    private EnumSet<NinjaInput> previousInputs;
    private EnumSet<NinjaInput> inputs;

    private Holder<NinjaAction> currentAction = NinjaActions.NONE;
    private int actionTick;
    private int airTick;

    private int airJumpCount;
    private int airSlideCount;

    private float actionXRot;
    private float actionYRot;

    public void checkKeyDown(ClientTickEvent.Pre event) {
        previousInputs = inputs;
        inputs = EnumSet.noneOf(NinjaInput.class);
        Options options = Minecraft.getInstance().options;
        if (options.keyShift.isDown())
            inputs.add(NinjaInput.SNEAK);
        if (options.keyJump.isDown())
            inputs.add(NinjaInput.JUMP);
        if (options.keySprint.isDown())
            inputs.add(NinjaInput.SPRINT);
        if (options.keyUse.isDown())
            inputs.add(NinjaInput.LEFT_CLICK);
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
        if (this.currentAction.value().getOriginAction() != null && this.currentAction.value().getModifierType() == ModifierType.INJECT) {
            this.currentAction.value().getOriginAction().value().stopAction(livingEntity);
        }
        this.currentAction.value().stopAction(livingEntity);
        if (this.currentAction.value().getCooldown() > 0) {
            if (this.currentAction.value().getOriginAction() != null) {
                this.setCooldown(this.currentAction.value().getOriginAction(), this.currentAction.value().getCooldown());
            }
            this.setCooldown(this.currentAction, this.currentAction.value().getCooldown());

        }
        this.currentAction = ninjaAction;
        this.setActionTick(0);
        if (this.currentAction.value().getOriginAction() != null && this.currentAction.value().getModifierType() == ModifierType.INJECT) {
            this.currentAction.value().getOriginAction().value().startAction(livingEntity);
        }
        this.currentAction.value().startAction(livingEntity);
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

    public void actionTick(LivingEntity user) {
        if (getTickState(user) == TickState.START) {
            this.currentAction.value().tickAction(user);
        }
    }

    public TickState getTickState(LivingEntity user) {
        return currentAction.value().getNinjaActionTickType().apply(user);
    }

    public void tick(LivingEntity user) {
        Holder<NinjaAction> origin = getCurrentAction().value().getOriginAction();
        ModifierType modifierType = getCurrentAction().value().getModifierType();
        TickState tickState = getTickState(user);
        if (tickState == TickState.START) {
            if (origin != null && modifierType == ModifierType.INJECT) {
                origin.value().tickAction(user);
            }

            this.actionTick(user);
        }
        cooldownMap.replaceAll((key,value) -> value - 1);
        cooldownMap.entrySet().removeIf(entry -> entry.getValue() <= 0);

        if (tickState != TickState.STOP) {
            this.setActionTick(this.getActionTick() + 1);
        } else {
            setAction(user, this.getCurrentAction().value().getNextOfTimeout(user));
        }

        if (tickState == TickState.START) {
            Holder<NinjaAction> ninjaAction = this.getCurrentAction().value().getNext(user);
            if (ninjaAction != null) {
                setAction(user, ninjaAction);
            }
        }
        if (user.isInFluidType() || user.isInWater()) {
            this.airTick = 3;
        } else if (user.onGround()) {
            this.airTick = 3;
        } else if (this.airTick > 0) {
            this.airTick--;
        }
        if(user.onGround()){
            resetAirJumpCount();
            resetAirSlideCount();
        }
    }

    public void pretick(LivingEntity user) {
        if (getTickState(user) == TickState.START) {
            if (!this.currentAction.value().isCanVanillaAction()) {
                user.setSprinting(false);
                user.setShiftKeyDown(false);
            }
            if (this.currentAction.value().getHitBox().isPresent()) {
                user.setPose(Pose.STANDING);
            }
        }


        if (user instanceof Player player && this.currentAction.value().isNoBob()) {
            player.bob = 0.0F;
            player.oBob = 0.0F;
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        ResourceLocation resourceLocation = NinjaActions.getRegistry().getKey(this.currentAction.value());
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
            if (action.isPresent()) {
                this.currentAction = action.get();
            }
        }

    }

    public void setCooldown(Holder<NinjaAction> ninjaAction, int cooldown) {
        this.cooldownMap.putIfAbsent(ninjaAction.value(), cooldown);
    }

    public boolean canAction(Holder<NinjaAction> ninjaAction) {
        return !this.cooldownMap.containsKey(ninjaAction.value());
    }


    public boolean canAirJump(LivingEntity livingEntity) {
        return canAirJump(livingEntity, NinjaActions.NONE);
    }

    public boolean canAirJump(LivingEntity livingEntity, Holder<NinjaAction> action) {
        return airJumpCount>0 && canJump(livingEntity, action);
    }

    public boolean canJump(LivingEntity livingEntity, Holder<NinjaAction> action) {
        return airTick <= 0 &&
                !previousInputs.contains(NinjaInput.JUMP) &&//今tickからジャンプキーを押し始めたか?
                getCurrentAction().value() == action.value() &&
                (!(livingEntity instanceof Player player) || !player.getAbilities().flying);
    }

    public EnumSet<NinjaInput> getInputs() {
        return inputs;
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
}