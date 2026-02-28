package mod.ckenja.cyninja.infrastructure.event;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.content.item.NinjaArmorItemExtensions;
import mod.ckenja.cyninja.infrastructure.registry.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Cyninja.MODID)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event){
        event.registerItem(
                new NinjaArmorItemExtensions(),
                ModItems.NINJA_BOOTS, ModItems.NINJA_LEGGINGS, ModItems.NINJA_HELMET, ModItems.NINJA_CHESTPLATE
        );
    }
}
