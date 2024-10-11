package mod.ckenja.cyninja.attachment;

import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class NinjaActionAttachment implements INBTSerializable<CompoundTag> {
    private Holder<NinjaAction> ninjaAction = NinjaActions.NONE;
    private int actionTick;
    private int climbableTick;

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
        this.ninjaAction = ninjaAction;
        this.setActionTick(0);
        livingEntity.refreshDimensions();
    }

    public void sync(LivingEntity livingEntity, Holder<NinjaAction> ninjaAction) {

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

    public float movementSpeed() {
        return this.isActionDo() && !this.isActionStop() ? this.ninjaAction.value().getMoveSpeed() : 0.0F;
    }

    public void actionTick(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            this.ninjaAction.value().tickAction(user);
        }
    }

    public void actionHold(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            this.ninjaAction.value().holdAction(user);
        }
    }

    public void actionHold(LivingEntity target, LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            this.ninjaAction.value().hitEffect(target, user);
        }
    }

    public void tick(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop() || this.getNinjaAction().value().isLoop()) {
            this.actionTick(user);
            this.actionHold(user);
        }
        if (!this.getNinjaAction().value().isLoop()) {
            if (!this.isActionStop()) {
                this.setActionTick(this.getActionTick() + 1);
            } else {
                NinjaActionUtils.setAction(user, this.getNinjaAction().value().getNextOfTimeout().apply(user));
            }
        }
        if (this.isActionDo() && !this.isActionStop() && !this.getNinjaAction().value().isLoop() || this.getNinjaAction().value().isLoop()) {
            Holder<NinjaAction> ninjaAction = this.getNinjaAction().value().getNext().apply(user);
            if (ninjaAction != null) {
                NinjaActionUtils.setAction(user, ninjaAction);
            }
        }
    }

    public void pretick(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop() || this.getNinjaAction().value().isLoop()) {
            if (!this.ninjaAction.value().isCanJump()) {
                user.setSprinting(false);
                user.setShiftKeyDown(false);
            }
            user.setPose(Pose.STANDING);
        }
    }


    public Optional<EntityDimensions> hitBox() {
        return this.isActionDo() && !this.isActionStop() ? this.ninjaAction.value().getHitBox() : Optional.empty();
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