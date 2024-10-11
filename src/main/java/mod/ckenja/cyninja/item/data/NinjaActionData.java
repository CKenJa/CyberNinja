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

import java.util.Optional;

public record NinjaActionData(int actionTick, Holder<NinjaAction> ninjaActionHolder) {
    public static final Codec<NinjaActionData> CODEC = RecordCodecBuilder.create(
            p_337892_ -> p_337892_.group(
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("action_tick").forGetter(NinjaActionData::actionTick),
                            NinjaActions.getRegistry().holderByNameCodec().fieldOf("action").orElse(NinjaActions.NONE).forGetter(NinjaActionData::ninjaActionHolder))

                    .apply(p_337892_, NinjaActionData::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NinjaActionData> DIRECT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            NinjaActionData::actionTick,
            ByteBufCodecs.holderRegistry(NinjaActions.NINJA_ACTIONS_REGISTRY),
            NinjaActionData::ninjaActionHolder,
            NinjaActionData::new
    );

    public NinjaActionData setActionTick(int actionTick) {
        return new NinjaActionData(actionTick, this.ninjaActionHolder);
    }

    public NinjaActionData setAction(Holder<NinjaAction> action) {
        return new NinjaActionData(this.actionTick, action);
    }

    public boolean isActionStop() {
        return this.actionTick >= this.ninjaActionHolder.value().getEndTick();
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
        if (!this.ninjaActionHolder.value().isLoop()) {
            if (!this.isActionStop()) {
                this.setActionTick(this.actionTick + 1);
            } else {
                this.setAction(this.ninjaActionHolder.value().getNextOfTimeout().apply(user));
            }
        }
    }


    public Optional<EntityDimensions> hitBox() {
        return this.isActionDo() && !this.isActionStop() ? this.ninjaActionHolder.value().getHitBox() : Optional.empty();
    }
}