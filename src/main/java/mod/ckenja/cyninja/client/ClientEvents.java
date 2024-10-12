package mod.ckenja.cyninja.client;

import bagu_chan.bagus_lib.animation.BaguAnimationController;
import bagu_chan.bagus_lib.api.client.IRootModel;
import bagu_chan.bagus_lib.client.event.BagusModelEvent;
import bagu_chan.bagus_lib.util.client.AnimationUtil;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.client.animation.PlayerAnimations;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.registry.ModAnimations;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import mod.ckenja.cyninja.util.NinjaInput;
import mod.ckenja.cyninja.util.VectorUtil;
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
import org.joml.Vector3f;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static NinjaInput ninjaInput = NinjaInput.NONE;

    @SubscribeEvent
    public static void onKeyPush(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (event.getKey() == Minecraft.getInstance().options.keyShift.getKey().getValue()) {
            ninjaInput = NinjaInput.SNEAK;
        }

        if (event.getKey() == Minecraft.getInstance().options.keyJump.getKey().getValue()) {
            ninjaInput = NinjaInput.JUMP;
        }


        if (event.getKey() == Minecraft.getInstance().options.keySprint.getKey().getValue()) {
            ninjaInput = NinjaInput.SPRINT;
        }

        Cyninja.NINJA_ACTION_MAP.entrySet().stream().filter(ninjaActionEntry -> {
            return ninjaActionEntry.getValue().name().equals(ninjaInput.name());
        }).forEach(holderNinjaInputEntry -> {
            if (holderNinjaInputEntry.getKey().value().getNeedCondition().apply(player)) {
                PacketDistributor.sendToServer(new SetActionToServerPacket(holderNinjaInputEntry.getKey()));
                NinjaActionUtils.setAction(player, holderNinjaInputEntry.getKey());
                return;
            }
        });


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
                NinjaActionAttachment actionHolder = NinjaActionUtils.getActionData(livingEntity);
                if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.SLIDE.value()) {
                    Optional<ModelPart> headPart = rootModel.getBetterAnyDescendantWithName("head");
                    Optional<ModelPart> hatPart = rootModel.getBetterAnyDescendantWithName("hat");
                    Optional<ModelPart> right_leg = rootModel.getBetterAnyDescendantWithName("right_leg");
                    Optional<ModelPart> left_leg = rootModel.getBetterAnyDescendantWithName("left_leg");
                    Optional<ModelPart> right_pants = rootModel.getBetterAnyDescendantWithName("right_pants");
                    Optional<ModelPart> left_pants = rootModel.getBetterAnyDescendantWithName("left_pants");

                    Vector3f headVec = new Vector3f();
                    Vector3f rightVec = new Vector3f();
                    Vector3f leftVec = new Vector3f();


                    if (headPart.isPresent()) {
                        headVec = VectorUtil.movePartToVec(headPart.get());
                    }
                    if (right_leg.isPresent()) {
                        rightVec = VectorUtil.movePartToVec(right_leg.get());
                    }
                    if (left_leg.isPresent()) {
                        leftVec = VectorUtil.movePartToVec(left_leg.get());
                    }

                    rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);
                    if (headPart.isPresent()) {
                        VectorUtil.moveVecToPart(headVec, headPart.get());
                    }
                    if (right_leg.isPresent()) {
                        VectorUtil.moveVecToPart(rightVec, right_leg.get());
                    }
                    if (left_leg.isPresent()) {
                        VectorUtil.moveVecToPart(leftVec, left_leg.get());
                    }
                    if (hatPart.isPresent()) {
                        VectorUtil.moveVecToPart(headVec, hatPart.get());
                    }
                    if (right_pants.isPresent()) {
                        VectorUtil.moveVecToPart(rightVec, right_pants.get());
                    }
                    if (left_pants.isPresent()) {
                        VectorUtil.moveVecToPart(leftVec, left_pants.get());
                    }
                    float f4 = livingEntity.walkAnimation.speed(bagusModelEvent.getPartialTick());
                    float f5 = livingEntity.walkAnimation.position(bagusModelEvent.getPartialTick());
                    rootModel.animateWalkBagu(PlayerAnimations.slide, f5, 1.0F, 2.0F, 2.5F);

                }
                BaguAnimationController animationController = AnimationUtil.getAnimationController(bagusModelEvent.getEntity());

                if (animationController != null) {
                    if (animationController.getAnimationState(ModAnimations.AIR_JUMP).isStarted()) {
                        Optional<ModelPart> headPart = rootModel.getBetterAnyDescendantWithName("head");
                        Optional<ModelPart> hatPart = rootModel.getBetterAnyDescendantWithName("hat");

                        Vector3f headVec = new Vector3f();
                        if (headPart.isPresent()) {
                            headVec = VectorUtil.movePartToVec(headPart.get());
                        }

                        rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);
                        if (headPart.isPresent()) {
                            VectorUtil.moveVecToPart(headVec, headPart.get());
                        }
                        if (hatPart.isPresent()) {
                            VectorUtil.moveVecToPart(headVec, hatPart.get());
                        }
                        rootModel.animateBagu(animationController.getAnimationState(ModAnimations.AIR_JUMP), PlayerAnimations.jump, bagusModelEvent.getAgeInTick());
                    }
                }

                /*if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.WALL_RUN.value()) {
                    rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);
                    float f4 = livingEntity.walkAnimation.speed(bagusModelEvent.getPartialTick());
                    float f5 = livingEntity.walkAnimation.position(bagusModelEvent.getPartialTick());
                    rootModel.animateWalkBagu(PlayerAnimations.wall_run, f5, 1.0F, 2.0F, 2.5F);
                }*/
            }
        }
    }

    public static float tickBaseParticalTick(int tick, float partialTick) {
        return tick - (1.0F - partialTick);
    }

    @SubscribeEvent
    public static void rotation(BagusModelEvent.Scale bagusModelEvent) {
        Entity entity = bagusModelEvent.getEntity();
        /*if (entity instanceof LivingEntity livingEntity) {
            NinjaActionAttachment actionHolder = NinjaActionUtils.getActionData(livingEntity);
            if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.WALL_RUN.value()) {
                float f = Mth.rotLerp(bagusModelEvent.getPartialTick(), livingEntity.yBodyRotO, livingEntity.yBodyRot);
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(-f));
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(livingEntity.getDirection().toYRot()));
            }
        }*/
    }
}