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
import mod.ckenja.cyninja.registry.ModAnimations;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import mod.ckenja.cyninja.util.NinjaInput;
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
import java.util.EnumSet;

import static mod.ckenja.cyninja.registry.ModAttachments.NINJA_ACTION;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void checkKeyDown(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        EnumSet<NinjaInput> inputs = EnumSet.noneOf(NinjaInput.class);
        if (Minecraft.getInstance().options.keyShift.isDown())
            inputs.add(NinjaInput.SNEAK);
        if (Minecraft.getInstance().options.keyJump.isDown())
            inputs.add(NinjaInput.JUMP);
        if (Minecraft.getInstance().options.keySprint.isDown())
            inputs.add(NinjaInput.SPRINT);
        if (Minecraft.getInstance().options.keyUse.isDown())
            inputs.add(NinjaInput.LEFT_CLICK);

        final boolean[] flag = {false};
        Cyninja.NINJA_ACTION_MAP.stream()
                .sorted(Comparator.comparingInt(ninjaActionHolder -> ninjaActionHolder.value().getPriority()))
                .filter(ninjaActionEntry -> ninjaActionEntry.value().getInputs() == null || inputs.containsAll(ninjaActionEntry.value().getInputs()))
                .forEach(holderNinjaInputEntry -> {
                    if (holderNinjaInputEntry.value().getNeedCondition().test(player) && !flag[0]) {
                        ResourceLocation ninjaAction = NinjaActions.getRegistry().getKey(holderNinjaInputEntry.value());
                        PacketDistributor.sendToServer(new SetActionToServerPacket(ninjaAction));
                        flag[0] = true;
                    }
                });
        player.getData(NINJA_ACTION).inputs = inputs;
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
        ActionAnimationUtil.animationWalkWithHandHeadRotation(bagusModelEvent, PlayerAnimations.slide, NinjaActions.SLIDE, 1.0F, 2.5F);

        if (entity instanceof LivingEntity livingEntity) {
            if (bagusModelEvent.isSupportedAnimateModel()) {
                NinjaActionAttachment actionHolder = NinjaActionUtils.getActionData(livingEntity);
                BaguAnimationController animationController = AnimationUtil.getAnimationController(bagusModelEvent.getEntity());

                if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.AIR_ROCKET.value()) {
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
            if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.AIR_ROCKET.value()) {
                bagusModelEvent.getPoseStack().mulPose(Axis.XP.rotationDegrees(livingEntity.getXRot()));
            }

            if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.SPIN.value()) {
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees((bagusModelEvent.getPartialTick() + entity.tickCount) * 60F));

            }

            if (actionHolder != null && actionHolder.getNinjaAction().value() == NinjaActions.WALL_SLIDE.value()) {
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