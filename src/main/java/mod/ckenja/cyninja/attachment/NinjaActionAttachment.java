package mod.ckenja.cyninja.attachment;

import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import mod.ckenja.cyninja.util.NinjaInput;
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
    private Holder<NinjaAction> ninjaAction = NinjaActions.NONE;
    private int actionTick;
    private int climbableTick;
    public EnumSet<NinjaInput> inputs;

    public int getActionTick() {
        return actionTick;
    }


    public Holder<NinjaAction> getNinjaAction() {
        return ninjaAction;
    }

    public void setClimbableTick(int climbableTick) {
        this.climbableTick = climbableTick;
    }

    public int getClimbableTick() {
        return climbableTick;
    }


    public boolean isClimbable() {
        return climbableTick <= 10;
    }

    public void setNinjaAction(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        this.ninjaAction.value().stopAction(livingEntity);
        this.ninjaAction = ninjaAction;
        this.setActionTick(0);
        livingEntity.refreshDimensions();
    }

    public void sync(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {
        this.ninjaAction.value().stopAction(livingEntity);
        this.ninjaAction = ninjaAction;
        this.setActionTick(0);
        if (!livingEntity.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SetActionToClientPacket(livingEntity, ninjaAction));
        }
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

            if (this.getActionTick() == 0) {
                this.ninjaAction.value().startAction(user);
            }
            if (!this.isActionStop()) {
                this.setActionTick(this.getActionTick() + 1);
            } else {
                NinjaActionUtils.setAction(user, this.getNinjaAction().value().getNextOfTimeout().apply(user));
            }
        }
        if (isActionActive() && !isActionLoop() || isActionLoop()) {
            Holder<NinjaAction> ninjaAction = this.getNinjaAction().value().getNext().apply(user);
            if (ninjaAction != null) {
                NinjaActionUtils.setAction(user, ninjaAction);
            }
        }
    }

    public void pretick(LivingEntity user) {
        if (isActionActive() || isActionLoop()) {
            if (!this.ninjaAction.value().isCanJump()) {
                user.setSprinting(false);
                user.setShiftKeyDown(false);
            }
            user.setPose(Pose.STANDING);
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
}