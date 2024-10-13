package mod.ckenja.cyninja.client;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.client.render.ThrownItemEntityRenderer;
import mod.ckenja.cyninja.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientRegistrar {

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THROWN_ITEM.get(), ThrownItemEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.AddLayers event) {
    }
}