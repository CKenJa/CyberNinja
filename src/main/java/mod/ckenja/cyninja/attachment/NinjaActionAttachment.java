package mod.ckenja.cyninja.attachment;

import com.google.common.collect.Maps;
import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.ninja_action.ModifierType;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.ninja_action.NinjaActionTickType;
import mod.ckenja.cyninja.ninja_action.TickState;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

public class NinjaActionAttachment implements INBTSerializable<CompoundTag> {
    private Map<NinjaAction, Integer> cooldown = Maps.newHashMap();

    private EnumSet<NinjaInput> previousInputs;
    private EnumSet<NinjaInput> inputs;

    private Holder<NinjaAction> ninjaAction = NinjaActions.NONE;
    private int actionTick;
    private int inFluidTick;
    private int airTick;

    private int airJumpCount;
    private int airSlideCount;

    private float actionXRot;
    private float actionYRot;

    public void checkKeyDown() {
        EnumSet<NinjaInput> inputs = EnumSet.noneOf(NinjaInput.class);
        Options options = Minecraft.getInstance().options;
        if (options.keyShift.isDown())
            inputs.add(NinjaInput.SNEAK);
        if (options.keyJump.isDown())
            inputs.add(NinjaInput.JUMP);
        if (options.keySprint.isDown())
            inputs.add(NinjaInput.SPRINT);
        if (options.keyUse.isDown())
            inputs.add(NinjaInput.LEFT_CLICK);
        previousInputs = inputs;
        this.inputs = inputs;
    }

    public int getActionTick() {
        return actionTick;
    }

    public Holder<NinjaAction> getCurrentAction() {
        return ninjaAction;
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

    public boolean wasInFluid() {
        return inFluidTick > 0;
    }

    public boolean isFullAir() {
        return airTick <= 0;
    }

    public void setAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        if (this.ninjaAction.value().getOriginAction() != null && this.ninjaAction.value().getModifierType() == ModifierType.INJECT) {
            this.ninjaAction.value().getOriginAction().value().stopAction(livingEntity);
        }
        this.ninjaAction.value().stopAction(livingEntity);
        if (this.ninjaAction.value().getCooldown() > 0) {
            if (this.ninjaAction.value().getOriginAction() != null) {
                this.setCooldown(this.ninjaAction.value().getOriginAction(), this.ninjaAction.value().getCooldown());
            }
            this.setCooldown(this.ninjaAction, this.ninjaAction.value().getCooldown());

        }
        this.ninjaAction = ninjaAction;
        this.setActionTick(0);
        if (this.ninjaAction.value().getOriginAction() != null && this.ninjaAction.value().getModifierType() == ModifierType.INJECT) {
            this.ninjaAction.value().getOriginAction().value().startAction(livingEntity);
        }
        this.ninjaAction.value().startAction(livingEntity);
        livingEntity.refreshDimensions();
    }

    public void syncAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        if (this.ninjaAction.value().getOriginAction() != null && this.ninjaAction.value().getModifierType() == ModifierType.INJECT) {
            this.ninjaAction.value().getOriginAction().value().stopAction(livingEntity);
        }
        this.ninjaAction.value().stopAction(livingEntity);

        if (this.ninjaAction.value().getCooldown() > 0) {
            if (this.ninjaAction.value().getOriginAction() != null) {
                this.setCooldown(this.ninjaAction.value().getOriginAction(), this.ninjaAction.value().getCooldown());
            }
            this.setCooldown(this.ninjaAction, this.ninjaAction.value().getCooldown());

        }
        this.ninjaAction = ninjaAction;
        this.setActionTick(0);
        if (!livingEntity.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SetActionToClientPacket(livingEntity, ninjaAction));
        }
        if (this.ninjaAction.value().getOriginAction() != null && this.ninjaAction.value().getModifierType() == ModifierType.INJECT) {
            this.ninjaAction.value().getOriginAction().value().startAction(livingEntity);
        }
        this.ninjaAction.value().startAction(livingEntity);

        livingEntity.refreshDimensions();
    }

    public boolean isActionStop() {
        return this.actionTick >= this.ninjaAction.value().getEndTick();
    }

    public boolean isActionDo() {
        return this.actionTick >= this.ninjaAction.value().getStartTick();
    }

    public void setActionTick(int actionTick) {
        this.actionTick = actionTick;
    }


    public float movementSpeed(LivingEntity user) {
        return getTickState(user) == TickState.START ? this.ninjaAction.value().getMoveSpeed() : 0.0F;
    }

    public void actionTick(LivingEntity user) {
        if (getTickState(user) == TickState.START) {
            this.ninjaAction.value().tickAction(user);
        }
    }

    public void actionHold(LivingEntity user) {
        if (getTickState(user) == TickState.START) {
            this.ninjaAction.value().holdAction(user);
        }
    }

    public void actionHold(LivingEntity target, LivingEntity user) {
        if (getTickState(user) == TickState.START) {
            this.ninjaAction.value().hitEffect(target, user);
        }
    }

    public TickState getTickState(LivingEntity user) {
        return ninjaAction.value().getNinjaActionTickType().getFunction().apply(user);
    }

    public void tick(LivingEntity user) {
        NinjaActionTickType tickType = getCurrentAction().value().getNinjaActionTickType();
        Holder<NinjaAction> origin = getCurrentAction().value().getOriginAction();
        ModifierType modifierType = getCurrentAction().value().getModifierType();
        TickState tickState = tickType.getFunction().apply(user);
        if (tickState == TickState.START) {
            if (origin != null && modifierType == ModifierType.INJECT) {
                origin.value().tickAction(user);
                origin.value().holdAction(user);
            }

            this.actionTick(user);
            this.actionHold(user);
        }
        for (Map.Entry<NinjaAction, Integer> cooldownMap : cooldown.entrySet()) {
            if (cooldownMap.getValue() > 0) {
                cooldown.replace(cooldownMap.getKey(), cooldownMap.getValue() - 1);
            }
        }
        cooldown.entrySet().removeIf(entry -> {
            return entry.getValue() <= 0;
        });

        if (tickState != TickState.STOP) {
            this.setActionTick(this.getActionTick() + 1);
        } else {
            setAction(user, this.getCurrentAction().value().getNextOfTimeout().apply(user));
        }

        if (tickState == TickState.START) {
            Holder<NinjaAction> ninjaAction = this.getCurrentAction().value().getNext().apply(user);
            if (ninjaAction != null) {
                setAction(user, ninjaAction);
            }
        }
        if (user.isInFluidType() || user.isInWater()) {
            this.inFluidTick = 5;
        } else {
            if (user.onGround()) {
                this.inFluidTick = 0;
            }

            if (this.inFluidTick > 0) {
                this.inFluidTick--;
            }
        }

        if (user.isInFluidType() || user.isInWater()) {
            this.airTick = 3;
        } else {
            if (user.onGround()) {
                this.airTick = 3;
            } else {
                if (this.airTick > 0) {
                    this.airTick--;
                }
            }
        }
        if(user.onGround()){
            resetAirJumpCount();
            resetAirSlideCount();
        }
    }

    public void pretick(LivingEntity user) {
        if (getTickState(user) == TickState.START) {
            if (!this.ninjaAction.value().isCanAction()) {
                user.setSprinting(false);
                user.setShiftKeyDown(false);
            }
            if (this.ninjaAction.value().getHitBox().isPresent()) {
                user.setPose(Pose.STANDING);
            }
        }


        if (user instanceof Player player && this.ninjaAction.value().isNoBob()) {
            player.bob = 0.0F;
            player.oBob = 0.0F;
        }
    }


    public Optional<EntityDimensions> hitBox(LivingEntity user) {
        return getTickState(user) == TickState.START ? this.ninjaAction.value().getHitBox() : Optional.empty();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        ResourceLocation resourceLocation = NinjaActions.getRegistry().getKey(this.ninjaAction.value());
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
                this.ninjaAction = action.get();
            }
        }

    }

    public void setCooldown(Holder<NinjaAction> ninjaAction, int cooldown) {
        this.cooldown.putIfAbsent(ninjaAction.value(), cooldown);
    }

    public boolean canAction(Holder<NinjaAction> ninjaAction) {
        return this.cooldown == null || !this.cooldown.containsKey(ninjaAction.value());
    }


    public boolean canAirJump(LivingEntity livingEntity) {
        return canAirJump(livingEntity, NinjaActions.NONE.value());
    }

    public boolean canAirJump(LivingEntity livingEntity, NinjaAction action) {
        return airJumpCount>0 && isFullAir() &&
                previousInputs.contains(NinjaInput.JUMP) &&//今tickからジャンプキーを押し始めたか?
                getCurrentAction().value() == action &&
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