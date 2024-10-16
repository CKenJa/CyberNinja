package mod.ckenja.cyninja.network;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.ModAttachments;
import mod.ckenja.cyninja.registry.NinjaActions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

public class SetActionToClientPacket implements CustomPacketPayload, IPayloadHandler<SetActionToClientPacket> {

    public static final StreamCodec<FriendlyByteBuf, SetActionToClientPacket> STREAM_CODEC = CustomPacketPayload.codec(
            SetActionToClientPacket::write, SetActionToClientPacket::new
    );
    public static final CustomPacketPayload.Type<SetActionToClientPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "action_client"));


    private int entityId;
    private ResourceLocation resourceLocation;


    public SetActionToClientPacket(int id, ResourceLocation resourceLocation) {
        this.entityId = id;
        this.resourceLocation = resourceLocation;
    }

    public SetActionToClientPacket(Entity entity, Holder<NinjaAction> holder) {
        this.entityId = entity.getId();
        this.resourceLocation = holder.getKey().location();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeResourceLocation(this.resourceLocation);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public SetActionToClientPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readResourceLocation());
    }

    @Override
    public void handle(SetActionToClientPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().player.level().getEntity(message.entityId);
            if (entity != null && entity instanceof Player player) {
                NinjaActionAttachment attachment = player.getData(ModAttachments.NINJA_ACTION);
                Optional<Holder.Reference<NinjaAction>> holder = NinjaActions.getRegistry().getHolder(resourceLocation);
                if (holder.isPresent()) {
                    attachment.setAction(player, holder.get());
                }
            }
        });
    }
}