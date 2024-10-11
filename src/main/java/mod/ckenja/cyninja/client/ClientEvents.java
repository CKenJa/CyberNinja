package mod.ckenja.cyninja.client;

import bagu_chan.bagus_lib.api.client.IRootModel;
import bagu_chan.bagus_lib.client.event.BagusModelEvent;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.client.animation.PlayerAnimations;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyPush(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            if (event.getKey() == Minecraft.getInstance().options.keyShift.getKey().getValue() && player.isSprinting()) {
                if (player.onGround() && NinjaActionUtils.getAction(player).getNinjaAction().value() == NinjaActions.NONE.value()) {
                    PacketDistributor.sendToServer(new SetActionToServerPacket(NinjaActions.SLIDE));
                }
            }
        }
    }

    @SubscribeEvent
    public static void animationInitEvent(BagusModelEvent.Init bagusModelEvent) {
        IRootModel rootModel = bagusModelEvent.getRootModel();
        if (bagusModelEvent.isSupportedAnimateModel()) {
            rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);
        }
    }

    @SubscribeEvent
    public static void animationPostEvent(BagusModelEvent.PostAnimate bagusModelEvent) {
        Entity entity = bagusModelEvent.getEntity();
        IRootModel rootModel = bagusModelEvent.getRootModel();
        if (entity instanceof LivingEntity livingEntity) {
            if (bagusModelEvent.isSupportedAnimateModel()) {
                NinjaActionAttachment actionHolder = NinjaActionUtils.getAction(livingEntity);
                if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.SLIDE.value()) {
                    rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);
                    float f4 = livingEntity.walkAnimation.speed(bagusModelEvent.getPartialTick());
                    float f5 = livingEntity.walkAnimation.position(bagusModelEvent.getPartialTick());
                    rootModel.animateWalkBagu(PlayerAnimations.slide, f5, 1.0F, 2.0F, 2.5F);
                }
            }
        }
    }
}