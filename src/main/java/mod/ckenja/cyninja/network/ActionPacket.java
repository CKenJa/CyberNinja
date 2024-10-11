package mod.ckenja.cyninja.network;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.ninja_skill.NinjaAction;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

public class ActionPacket implements CustomPacketPayload, IPayloadHandler<ActionPacket> {

    public static final StreamCodec<FriendlyByteBuf, ActionPacket> STREAM_CODEC = CustomPacketPayload.codec(
            ActionPacket::write, ActionPacket::new
    );
    public static final Type<ActionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "action"));

    public final ResourceLocation actionHolder;

    public ActionPacket(Holder<NinjaAction> actionHolder) {
        this.actionHolder = actionHolder.getKey().location();
    }

    public ActionPacket(ResourceLocation actionHolder) {
        this.actionHolder = actionHolder;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.actionHolder);
    }

    public ActionPacket(FriendlyByteBuf buffer) {
        this(buffer.readResourceLocation());
    }

    public void handle(ActionPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Optional<Holder.Reference<NinjaAction>> ninjaAction = NinjaActions.getRegistry().getHolder(message.actionHolder);
            if (player instanceof ServerPlayer serverPlayer) {
                ninjaAction.ifPresent(ninjaActionReference -> NinjaActionUtils.setAction(serverPlayer, ninjaActionReference));
            }
        });
    }
}