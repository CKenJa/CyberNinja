package mod.ckenja.cyninja.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.ckenja.cyninja.ninja_skill.NinjaAction;
import mod.ckenja.cyninja.registry.NinjaActions;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.Optional;

public record NinjaActionData(int actionTick, boolean stop, Holder<NinjaAction> ninjaActionHolder) {
    public static final Codec<NinjaActionData> CODEC = RecordCodecBuilder.create(
            p_337892_ -> p_337892_.group(
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("action_tick").forGetter(NinjaActionData::actionTick),
                            Codec.BOOL.fieldOf("stop").forGetter(NinjaActionData::stop),
                            NinjaActions.getRegistry().holderByNameCodec().fieldOf("action").forGetter(NinjaActionData::ninjaActionHolder))

                    .apply(p_337892_, NinjaActionData::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NinjaActionData> DIRECT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            NinjaActionData::actionTick,
            ByteBufCodecs.BOOL,
            NinjaActionData::stop,
            ByteBufCodecs.holderRegistry(NinjaActions.NINJA_ACTIONS_REGISTRY),
            NinjaActionData::ninjaActionHolder,
            NinjaActionData::new
    );

    public NinjaActionData setActionTick(int actionTick) {
        return new NinjaActionData(actionTick, this.stop, this.ninjaActionHolder);
    }

    public NinjaActionData setAction(Holder<NinjaAction> action) {
        return new NinjaActionData(this.actionTick, this.stop, action);
    }

    public NinjaActionData setActionStop(boolean stop) {
        return new NinjaActionData(this.actionTick, stop, this.ninjaActionHolder);
    }

    public boolean isActionStop() {
        return this.actionTick >= this.ninjaActionHolder.value().getEndTick() || stop();
    }

    public boolean isActionDo() {
        return this.actionTick >= this.ninjaActionHolder.value().getStartTick();
    }

    public float movementSpeed() {
        return this.isActionDo() && !this.isActionStop() ? this.ninjaActionHolder.value().getMoveSpeed() : 0.0F;
    }

    public void actionTick(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            this.ninjaActionHolder.value().tickAction(user);
        }
    }

    public void actionHold(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            this.ninjaActionHolder.value().holdAction(user);
        }
    }

    public void actionHold(LivingEntity target, LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            this.ninjaActionHolder.value().hitEffect(target, user);
        }
    }

    public void tick(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            this.actionTick(user);
            this.actionHold(user);
        }

    }

    public void pretick(LivingEntity user) {
        if (this.isActionDo() && !this.isActionStop()) {
            user.setSprinting(false);
            user.setShiftKeyDown(false);
            user.setPose(Pose.STANDING);
        }

    }


    public Optional<EntityDimensions> hitBox() {
        return this.isActionDo() && !this.isActionStop() ? this.ninjaActionHolder.value().getHitBox() : Optional.empty();
    }
}