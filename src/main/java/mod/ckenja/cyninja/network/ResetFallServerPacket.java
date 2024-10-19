package mod.ckenja.cyninja.network;

import mod.ckenja.cyninja.Cyninja;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class ResetFallServerPacket implements CustomPacketPayload, IPayloadHandler<ResetFallServerPacket> {

    public static final StreamCodec<FriendlyByteBuf, ResetFallServerPacket> STREAM_CODEC = CustomPacketPayload.codec(
            ResetFallServerPacket::write, ResetFallServerPacket::new
    );
    public static final Type<ResetFallServerPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "reset_fall"));


    public ResetFallServerPacket() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public ResetFallServerPacket(FriendlyByteBuf buffer) {
        this();
    }

    public void handle(ResetFallServerPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                player.resetFallDistance();
            }
        });
    }
}