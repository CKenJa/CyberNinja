package mod.ckenja.cyninja.entity;

import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.ninja_action.NinjaActionAttachment;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class NinjaFaker extends PathfinderMob {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_UUID = SynchedEntityData.defineId(NinjaFaker.class, EntityDataSerializers.OPTIONAL_UUID);
    private int duration = 300;

    public NinjaFaker(EntityType<? extends NinjaFaker> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
    }

    public static AttributeSupplier.Builder createMonsterAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity owner = this.getOwner();
        if (owner != null && !this.level().isClientSide() && NinjaActionUtils.isWearingNinjaTrim(owner, Items.AMETHYST_SHARD)) {
            NinjaActionAttachment data = NinjaActionUtils.getActionData(this);
            Holder<NinjaAction> ownerAction = NinjaActionUtils.getActionData(owner).getCurrentAction();
            if (data.getCurrentAction().value() != ownerAction.value()) {
                data.syncAction(this, ownerAction);
            }
        }
        if (this.tickCount > duration) {
            if (!this.level().isClientSide()) {
                this.discard();
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_326499_) {
        super.defineSynchedData(p_326499_);
        p_326499_.define(DATA_UUID, Optional.empty());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21450_) {
        super.readAdditionalSaveData(p_21450_);
        duration = p_21450_.getInt("Duration");

        UUID uuid;
        if (p_21450_.hasUUID("Owner")) {
            uuid = p_21450_.getUUID("Owner");
        } else {
            String s = p_21450_.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setDataUuid(uuid);
            } catch (Throwable throwable) {
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21484_) {
        super.addAdditionalSaveData(p_21484_);
        p_21484_.putInt("Duration", duration);
        if (this.getDataUuid().isPresent()) {
            p_21484_.putUUID("Owner", this.getDataUuid().get());
        }
    }

    public Optional<UUID> getDataUuid() {
        return this.entityData.get(DATA_UUID);
    }

    public void setDataUuid(UUID p_36363_) {
        this.entityData.set(DATA_UUID, Optional.of(p_36363_));
    }

    @Override
    public boolean isInvisibleTo(Player p_20178_) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity p_21822_) {
        return this.isOwnedBy(p_21822_) ? false : super.canAttack(p_21822_);
    }

    public boolean isOwnedBy(LivingEntity p_21831_) {
        return p_21831_ == this.getOwner();
    }


    @Override
    public PlayerTeam getTeam() {
        LivingEntity livingentity = this.getOwner();
        if (livingentity != null) {
            return livingentity.getTeam();
        }
        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity p_21833_) {
        LivingEntity livingentity = this.getOwner();
        if (p_21833_ == livingentity) {
            return true;
        }

        if (livingentity != null) {
            return livingentity.isAlliedTo(p_21833_);
        }


        return super.isAlliedTo(p_21833_);
    }

    @Nullable
    public LivingEntity getOwner() {
        Optional<UUID> uuid = this.getDataUuid();
        return uuid.isEmpty() ? null : this.level().getPlayerByUUID(uuid.orElse(null));
    }

    public void setDuration(int d){
        if(d>0){
            duration = d;
        }
    }
}
