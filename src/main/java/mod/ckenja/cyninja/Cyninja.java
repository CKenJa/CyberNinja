package mod.ckenja.cyninja;

import com.mojang.logging.LogUtils;
import mod.ckenja.cyninja.network.ResetFallServerPacket;
import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.Locale;

@Mod(Cyninja.MODID)
public class Cyninja
{
    public static final String MODID = "cyninja";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Cyninja(IEventBus modEventBus)
    {
        NinjaActions.NINJA_ACTIONS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES_REGISTRY.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModEntityDataSerializer.DATA_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(this::setupPackets);
    }

    public static ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, name.toLowerCase(Locale.ROOT));
    }

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0.0").optional();
        registrar.playBidirectional(SetActionToServerPacket.TYPE, SetActionToServerPacket.STREAM_CODEC, (handler, payload) -> handler.handle(handler, payload));
        registrar.playBidirectional(SetActionToClientPacket.TYPE, SetActionToClientPacket.STREAM_CODEC, (handler, payload) -> handler.handle(handler, payload));
        registrar.playBidirectional(ResetFallServerPacket.TYPE, ResetFallServerPacket.STREAM_CODEC, (handler, payload) -> handler.handle(handler, payload));
    }
}
