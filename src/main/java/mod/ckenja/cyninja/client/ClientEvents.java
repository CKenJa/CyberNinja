package mod.ckenja.cyninja.client;

import bagu_chan.bagus_lib.animation.BaguAnimationController;
import bagu_chan.bagus_lib.api.client.IRootModel;
import bagu_chan.bagus_lib.client.event.BagusModelEvent;
import bagu_chan.bagus_lib.util.client.AnimationUtil;
import com.mojang.math.Axis;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.client.animation.PlayerAnimations;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.ninja_action.ModifierType;
import mod.ckenja.cyninja.registry.ModAnimations;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import mod.ckenja.cyninja.util.client.ActionAnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;

import static mod.ckenja.cyninja.ninja_action.NinjaAction.NINJA_ACTIONS;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void triggerNinjaAction(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator())
            return;

        NinjaActionAttachment data = NinjaActionUtils.getActionData(player);
        data.checkKeyDown();
        final ResourceLocation[] doAction = {null};
        NINJA_ACTIONS.stream()
                //入力が必要ないもの or 必要で、一致するもの

                .filter(action -> action.value().getModifierType() == ModifierType.NONE)
                .filter(action -> action.value() != NinjaActions.NONE.value() && action.value().getStartInputs() == null ||
                        action.value().getStartInputs() != null && data.getInputs().containsAll(action.value().getStartInputs()))
                .filter(action -> action.value() != data.getCurrentAction().value())
                .filter(action -> action.value().getNeedCondition().test(player))
                .filter(data::canAction)
                .min(Comparator.comparingInt(holder -> holder.value().getPriority()))
                .ifPresent(holder ->
                {
                    doAction[0] = NinjaActions.getRegistry().getKey(holder.value());
                });

        NINJA_ACTIONS.stream().filter(action -> {
                    if (action.value().getOriginAction() != null) {
                        return NinjaActions.getRegistry().getKey(action.value().getOriginAction().value()) == doAction[0];
                    }
                    return false;
                })
                .filter(action -> action.value().getNeedCondition().test(player))
                .findFirst()
                .ifPresent(holder ->
                {
                    doAction[0] = NinjaActions.getRegistry().getKey(holder.value());
                });

        if (doAction[0] != null) {
            PacketDistributor.sendToServer(new SetActionToServerPacket(doAction[0]));
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
        ActionAnimationUtil.animationWalkWithHeadRotation(bagusModelEvent, PlayerAnimations.wall_run, NinjaActions.WALL_SLIDE, 1.0F, 2.5F);
        ActionAnimationUtil.animationWalkWithHeadRotation(bagusModelEvent, PlayerAnimations.slide, NinjaActions.SLIDE, 1.0F, 2.5F);

        if (entity instanceof LivingEntity livingEntity) {
            if (bagusModelEvent.isSupportedAnimateModel()) {
                NinjaActionAttachment actionHolder = NinjaActionUtils.getActionData(livingEntity);
                BaguAnimationController animationController = AnimationUtil.getAnimationController(bagusModelEvent.getEntity());

                if (actionHolder != null && actionHolder.getCurrentAction().value() == NinjaActions.AIR_ROCKET.value()) {
                    if (animationController.getAnimationState(ModAnimations.AIR_ROCKET).isStarted()) {
                        rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);

                        rootModel.animateBagu(animationController.getAnimationState(ModAnimations.AIR_ROCKET), PlayerAnimations.air_rocket, bagusModelEvent.getAgeInTick());
                    }
                }
            }
        }
    }

    public static float tickBaseParticalTick(int tick, float partialTick) {
        return tick - (1.0F - partialTick);
    }

    @SubscribeEvent
    public static void rotation(BagusModelEvent.Scale bagusModelEvent) {
        Entity entity = bagusModelEvent.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            NinjaActionAttachment actionHolder = NinjaActionUtils.getActionData(livingEntity);
            if (actionHolder != null && actionHolder.getCurrentAction().value() == NinjaActions.AIR_ROCKET.value()) {
                bagusModelEvent.getPoseStack().mulPose(Axis.XP.rotationDegrees(livingEntity.getXRot()));
            }

            if (actionHolder != null && actionHolder.getCurrentAction().value() == NinjaActions.SPIN.value()) {
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees((bagusModelEvent.getPartialTick() + entity.tickCount) * 60F));

            }

            if (actionHolder != null && actionHolder.getCurrentAction().value() == NinjaActions.AIR_ROCKET.value()) {
                float f = Mth.rotLerp(bagusModelEvent.getPartialTick(), livingEntity.yBodyRotO, livingEntity.yBodyRot);
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(-f));
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(actionHolder.getActionYRot()));
                bagusModelEvent.getPoseStack().mulPose(Axis.XP.rotationDegrees(-livingEntity.getXRot()));
                bagusModelEvent.getPoseStack().mulPose(Axis.XP.rotationDegrees(actionHolder.getActionXRot()));
            }


            if (actionHolder != null && actionHolder.getCurrentAction().value() == NinjaActions.SLIDE.value()) {
                float f = Mth.rotLerp(bagusModelEvent.getPartialTick(), livingEntity.yBodyRotO, livingEntity.yBodyRot);
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(-f));
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(actionHolder.getActionYRot()));
            }

            if (actionHolder != null && actionHolder.getCurrentAction().value() == NinjaActions.WALL_SLIDE.value()) {
                float f = Mth.rotLerp(bagusModelEvent.getPartialTick(), livingEntity.yBodyRotO, livingEntity.yBodyRot);
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(-f));
                Direction direction = livingEntity.getMotionDirection();
                for (int i = 0; i < Direction.Plane.HORIZONTAL.length(); i++) {
                    direction = direction.getClockWise();
                    if (!livingEntity.level().noBlockCollision(livingEntity, livingEntity.getBoundingBox().move(direction.step().mul(0.01F)))) {
                        break;
                    }
                }

                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(direction.toYRot()));
            }
        }
    }
}