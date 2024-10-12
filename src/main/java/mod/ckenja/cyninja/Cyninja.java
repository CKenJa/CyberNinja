package mod.ckenja.cyninja;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import mod.ckenja.cyninja.network.SetActionToClientPacket;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.*;
import mod.ckenja.cyninja.util.NinjaInput;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.Map;

@Mod(Cyninja.MODID)
public class Cyninja
{
    public static final String MODID = "cyninja";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Holder<NinjaAction>, NinjaInput> NINJA_ACTION_MAP = Maps.newHashMap();

    public Cyninja(IEventBus modEventBus)
    {
        NinjaActions.NINJA_ACTIONS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::setupPackets);
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NINJA_ACTION_MAP.put(NinjaActions.SLIDE, NinjaInput.SNEAK);
        });
    }

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0.0").optional();
        registrar.playBidirectional(SetActionToServerPacket.TYPE, SetActionToServerPacket.STREAM_CODEC, (handler, payload) -> handler.handle(handler, payload));
        registrar.playBidirectional(SetActionToClientPacket.TYPE, SetActionToClientPacket.STREAM_CODEC, (handler, payload) -> handler.handle(handler, payload));
    }
}
