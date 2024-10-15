package mod.ckenja.cyninja.registry;

import bagu_chan.bagus_lib.event.RegisterBagusAnimationEvents;
import mod.ckenja.cyninja.Cyninja;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Cyninja.MODID)
public class ModAnimations {
    public static final ResourceLocation AIR_ROCKET = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "air_rocket");

    @SubscribeEvent
    public static void entityAnimationRegister(RegisterBagusAnimationEvents events) {
        events.addAnimationState(AIR_ROCKET);
    }
}
