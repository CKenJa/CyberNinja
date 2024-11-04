package mod.ckenja.cyninja.client;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.client.model.CyberIllagerModel;
import mod.ckenja.cyninja.client.render.CyberIllagerRender;
import mod.ckenja.cyninja.client.render.NinjaFakerRenderer;
import mod.ckenja.cyninja.client.render.SickleEntityRenderer;
import mod.ckenja.cyninja.client.render.ThrownItemEntityRenderer;
import mod.ckenja.cyninja.registry.ModDataComponents;
import mod.ckenja.cyninja.registry.ModEntities;
import mod.ckenja.cyninja.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientRegistrar {

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CYBER_ILLAGER.get(), CyberIllagerRender::new);
        event.registerEntityRenderer(ModEntities.NINJA_FAKER.get(), NinjaFakerRenderer::new);

        event.registerEntityRenderer(ModEntities.SMOKE_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_ITEM.get(), ThrownItemEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.SICKLE.get(), SickleEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CYBER_NINJA, CyberIllagerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void modelBake(ModelEvent.ModifyBakingResult event) {
        ItemProperties.register(ModItems.CHAIN_SICKLE.get(), ResourceLocation.parse("chain_only"), (stack, p_174631_, p_174632_, p_174633_) -> {
            return stack.get(ModDataComponents.CHAIN_ONLY) != null ? 1.0F : 0.0F;
        });
    }

}