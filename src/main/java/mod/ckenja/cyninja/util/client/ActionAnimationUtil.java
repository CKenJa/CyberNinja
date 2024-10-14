package mod.ckenja.cyninja.util.client;

import bagu_chan.bagus_lib.animation.BaguAnimationController;
import bagu_chan.bagus_lib.api.client.IRootModel;
import bagu_chan.bagus_lib.client.event.BagusModelEvent;
import bagu_chan.bagus_lib.util.client.AnimationUtil;
import bagu_chan.bagus_lib.util.client.VectorUtil;
import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.Optional;

public class ActionAnimationUtil {

    public static void animationWalkWithHeadRotation(BagusModelEvent.PostAnimate event, AnimationDefinition animationDefinition, Holder<NinjaAction> ninjaActions, float scale, float speed) {
        Entity entity = event.getEntity();
        IRootModel rootModel = event.getRootModel();
        if (entity instanceof LivingEntity livingEntity) {
            if (event.isSupportedAnimateModel()) {
                BaguAnimationController animationController = AnimationUtil.getAnimationController(event.getEntity());

                NinjaActionAttachment actionHolder = NinjaActionUtils.getActionData(livingEntity);
                if (actionHolder != null && actionHolder.getNinjaAction().value() == ninjaActions.value()) {

                    Optional<ModelPart> headPart = rootModel.getBetterAnyDescendantWithName("head");
                    Optional<ModelPart> hatPart = rootModel.getBetterAnyDescendantWithName("hat");

                    Vector3f headVec = new Vector3f();
                    Vector3f rightVec = new Vector3f();
                    Vector3f leftVec = new Vector3f();


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
                    float f5 = livingEntity.walkAnimation.position(event.getPartialTick());
                    rootModel.animateWalkBagu(animationDefinition, f5, scale, speed, 2.5F);
                }
            }
        }
    }


    public static void animationWalkWithHandHeadRotation(BagusModelEvent.PostAnimate event, AnimationDefinition animationDefinition, Holder<NinjaAction> ninjaActions, float scale, float speed) {
        Entity entity = event.getEntity();
        IRootModel rootModel = event.getRootModel();
        if (entity instanceof LivingEntity livingEntity) {
            if (event.isSupportedAnimateModel()) {
                BaguAnimationController animationController = AnimationUtil.getAnimationController(event.getEntity());

                NinjaActionAttachment actionHolder = NinjaActionUtils.getActionData(livingEntity);
                if (actionHolder != null && actionHolder.getNinjaAction().value() == ninjaActions.value()) {

                        Optional<ModelPart> headPart = rootModel.getBetterAnyDescendantWithName("head");
                        Optional<ModelPart> hatPart = rootModel.getBetterAnyDescendantWithName("hat");
                        Optional<ModelPart> right_arm = rootModel.getBetterAnyDescendantWithName("right_arm");
                        Optional<ModelPart> left_arm = rootModel.getBetterAnyDescendantWithName("left_arm");
                        Optional<ModelPart> right_sleeve = rootModel.getBetterAnyDescendantWithName("right_sleeve");
                        Optional<ModelPart> left_sleeve = rootModel.getBetterAnyDescendantWithName("left_sleeve");

                        Vector3f headVec = new Vector3f();
                        Vector3f rightVec = new Vector3f();
                        Vector3f leftVec = new Vector3f();


                        if (headPart.isPresent()) {
                            headVec = VectorUtil.movePartToVec(headPart.get());
                        }
                        if (right_arm.isPresent()) {
                            rightVec = VectorUtil.movePartToVec(right_arm.get());
                        }
                        if (left_arm.isPresent()) {
                            leftVec = VectorUtil.movePartToVec(left_arm.get());
                        }

                        rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);
                        if (headPart.isPresent()) {
                            VectorUtil.moveVecToPart(headVec, headPart.get());
                        }
                        if (right_arm.isPresent()) {
                            VectorUtil.moveVecToPart(rightVec, right_arm.get());
                        }
                        if (left_arm.isPresent()) {
                            VectorUtil.moveVecToPart(leftVec, left_arm.get());
                        }
                        if (hatPart.isPresent()) {
                            VectorUtil.moveVecToPart(headVec, hatPart.get());
                        }
                        if (right_sleeve.isPresent()) {
                            VectorUtil.moveVecToPart(rightVec, right_sleeve.get());
                        }
                        if (left_sleeve.isPresent()) {
                            VectorUtil.moveVecToPart(leftVec, left_sleeve.get());
                        }
                        float f5 = livingEntity.walkAnimation.position(event.getPartialTick());
                    rootModel.animateWalkBagu(animationDefinition, f5, scale, speed, 2.5F);
                }
            }
        }
    }

    public static void animationWithHandHeadRotation(BagusModelEvent.PostAnimate event, AnimationDefinition animationDefinition, ResourceLocation resourceLocation) {
        Entity entity = event.getEntity();
        IRootModel rootModel = event.getRootModel();
        if (entity instanceof LivingEntity livingEntity) {
            if (event.isSupportedAnimateModel()) {
                BaguAnimationController animationController = AnimationUtil.getAnimationController(event.getEntity());

                if (animationController != null) {
                    if (animationController.getAnimationState(resourceLocation).isStarted()) {
                        Optional<ModelPart> headPart = rootModel.getBetterAnyDescendantWithName("head");
                        Optional<ModelPart> hatPart = rootModel.getBetterAnyDescendantWithName("hat");
                        Optional<ModelPart> right_arm = rootModel.getBetterAnyDescendantWithName("right_arm");
                        Optional<ModelPart> left_arm = rootModel.getBetterAnyDescendantWithName("left_arm");
                        Optional<ModelPart> right_sleeve = rootModel.getBetterAnyDescendantWithName("right_sleeve");
                        Optional<ModelPart> left_sleeve = rootModel.getBetterAnyDescendantWithName("left_sleeve");

                        Vector3f headVec = new Vector3f();
                        Vector3f rightVec = new Vector3f();
                        Vector3f leftVec = new Vector3f();


                        if (headPart.isPresent()) {
                            headVec = VectorUtil.movePartToVec(headPart.get());
                        }
                        if (right_arm.isPresent()) {
                            rightVec = VectorUtil.movePartToVec(right_arm.get());
                        }
                        if (left_arm.isPresent()) {
                            leftVec = VectorUtil.movePartToVec(left_arm.get());
                        }

                        rootModel.getBagusRoot().getAllParts().forEach(ModelPart::resetPose);
                        if (headPart.isPresent()) {
                            VectorUtil.moveVecToPart(headVec, headPart.get());
                        }
                        if (right_arm.isPresent()) {
                            VectorUtil.moveVecToPart(rightVec, right_arm.get());
                        }
                        if (left_arm.isPresent()) {
                            VectorUtil.moveVecToPart(leftVec, left_arm.get());
                        }
                        if (hatPart.isPresent()) {
                            VectorUtil.moveVecToPart(headVec, hatPart.get());
                        }
                        if (right_sleeve.isPresent()) {
                            VectorUtil.moveVecToPart(rightVec, right_sleeve.get());
                        }
                        if (left_sleeve.isPresent()) {
                            VectorUtil.moveVecToPart(leftVec, left_sleeve.get());
                        }
                        rootModel.animateBagu(animationController.getAnimationState(resourceLocation), animationDefinition, event.getAgeInTick());
                    }
                }
            }
        }
    }

    public static void animationWithHeadRotation(BagusModelEvent.PostAnimate event, AnimationDefinition animationDefinition, ResourceLocation resourceLocation) {
        Entity entity = event.getEntity();
        IRootModel rootModel = event.getRootModel();
        if (entity instanceof LivingEntity livingEntity) {
            if (event.isSupportedAnimateModel()) {
                BaguAnimationController animationController = AnimationUtil.getAnimationController(event.getEntity());

                if (animationController != null) {
                    if (animationController.getAnimationState(resourceLocation).isStarted()) {
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
                        rootModel.animateBagu(animationController.getAnimationState(resourceLocation), animationDefinition, event.getAgeInTick());
                    }
                }
            }
        }
    }
}
