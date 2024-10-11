package mod.ckenja.cyninja.client;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaAttackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static int pressSummonTick;


    @SubscribeEvent
    public static void onMouseScroll(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            if (event.getKeyMapping() == Minecraft.getInstance().options.keyShift && player.isSprinting()) {
                if (player.onGround() && NinjaAttackUtils.getAction(player) == NinjaActions.NONE) {
                    NinjaAttackUtils.setAction(player, NinjaActions.SLIDE);
                }
            }
        }
    }


}