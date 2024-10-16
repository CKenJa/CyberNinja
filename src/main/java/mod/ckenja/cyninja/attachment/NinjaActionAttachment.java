package mod.ckenja.cyninja.attachment;

import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.ModAttachments;
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
import java.util.Optional;

public class NinjaActionAttachment implements INBTSerializable<CompoundTag> {
    private EnumSet<NinjaInput> previousInputs;
    private EnumSet<NinjaInput> inputs;

    private Holder<NinjaAction> ninjaAction = NinjaActions.NONE;
    private int actionTick;
    private int inFluidTick;
    private int airTick;

    private int airJumpCount;
    private int airSlideCount;
    private int slideTick;

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


    public Holder<NinjaAction> getNinjaAction() {
        return ninjaAction;
    }

    public boolean wasInFluid() {
        return inFluidTick > 0;
    }

    public boolean isFullAir() {
        return airTick <= 0;
    }

    public void setAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        this.ninjaAction.value().stopAction(livingEntity);
        this.ninjaAction = ninjaAction;
        this.setActionTick(0);
        this.ninjaAction.value().startAction(livingEntity);
        livingEntity.refreshDimensions();
    }

    public void syncAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        this.ninjaAction.value().stopAction(livingEntity);
        this.ninjaAction = ninjaAction;
        this.setActionTick(0);
        if (!livingEntity.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SetActionToClientPacket(livingEntity, ninjaAction));
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

    public boolean isActionActive(){
        return isActionDo() && !isActionStop();
    }

    public boolean isActionLoop(){
        return this.getNinjaAction().value().isLoop();
    }
    
    public float movementSpeed() {
        return isActionActive() ? this.ninjaAction.value().getMoveSpeed() : 0.0F;
    }

    public void actionTick(LivingEntity user) {
        if (isActionActive()) {
            this.ninjaAction.value().tickAction(user);
        }
    }

    public void actionHold(LivingEntity user) {
        if (isActionActive()) {
            this.ninjaAction.value().holdAction(user);
        }
    }

    public void actionHold(LivingEntity target, LivingEntity user) {
        if (isActionActive()) {
            this.ninjaAction.value().hitEffect(target, user);
        }
    }

    public void tick(LivingEntity user) {
        if (isActionActive() || isActionLoop()) {
            this.actionTick(user);
            this.actionHold(user);
        }
        if (!isActionLoop()) {
            if (!this.isActionStop()) {
                this.setActionTick(this.getActionTick() + 1);
            } else {
                setAction(user, this.getNinjaAction().value().getNextOfTimeout().apply(user));
            }
        }
        if (isActionActive() && !isActionLoop() || isActionLoop()) {
            Holder<NinjaAction> ninjaAction = this.getNinjaAction().value().getNext().apply(user);
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
        if (getNinjaAction().value() == NinjaActions.SLIDE.value()) {
            this.setSlideTick(this.getSlideTick() + 1);
        } else {
            this.setSlideTick(0);
        }
        if(user.onGround()){
            user.getData(ModAttachments.NINJA_ACTION).airJumpCount = 1;
            if (getNinjaAction().value() != NinjaActions.SLIDE.value()) {
                user.getData(ModAttachments.NINJA_ACTION).airSlideCount = 1;
            }
        }
    }

    public void pretick(LivingEntity user) {
        if (isActionActive() || isActionLoop()) {
            if (!this.ninjaAction.value().isCanJump()) {
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


    public Optional<EntityDimensions> hitBox() {
        return isActionActive() ? this.ninjaAction.value().getHitBox() : Optional.empty();
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

    public boolean canAirJump(LivingEntity livingEntity) {
        return canAirJump(livingEntity, NinjaActions.NONE.value());
    }

    public boolean canAirJump(LivingEntity livingEntity, NinjaAction action) {
        return airJumpCount>0 && isFullAir() &&
                previousInputs.contains(NinjaInput.JUMP) &&//今tickからジャンプキーを押し始めたか?
                getNinjaAction().value() == action &&
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

    public int getAirSlideCount() {
        return airSlideCount;
    }

    public void setSlideTick(int slideTick) {
        this.slideTick = slideTick;
    }

    public int getSlideTick() {
        return slideTick;
    }
}