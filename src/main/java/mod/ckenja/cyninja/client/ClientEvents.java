package mod.ckenja.cyninja.client;

import bagu_chan.bagus_lib.animation.BaguAnimationController;
import bagu_chan.bagus_lib.api.client.IRootModel;
import bagu_chan.bagus_lib.client.event.BagusModelEvent;
import bagu_chan.bagus_lib.util.client.AnimationUtil;
import com.mojang.math.Axis;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.client.animation.PlayerAnimations;
import mod.ckenja.cyninja.content.item.NinjaArmorItem;
import mod.ckenja.cyninja.core.action.ActionAttachment;
import mod.ckenja.cyninja.infrastructure.attachment.ActionStatesAttachment;
import mod.ckenja.cyninja.infrastructure.registry.ModAnimations;
import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import mod.ckenja.cyninja.core.util.client.ActionAnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Cyninja.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void toolTipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ArmorTrim armorTrim = stack.get(DataComponents.TRIM);
        if (armorTrim != null) {
            String rawStr = armorTrim.material().value().description().getString();
            boolean flag = stack.getItem() instanceof NinjaArmorItem;
            String itemNameStr = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
            String itemPathStr = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

            if (flag) {
                itemNameStr = Cyninja.MODID;
                itemPathStr = "ninja_armor";
            }

            String totalName = rawStr + ".ninja." + itemNameStr + "." + itemPathStr;
            if (I18n.exists(totalName)) {
                event.getToolTip().add(Component.translatable(totalName));
            }
        }

    }

    @SubscribeEvent
    public static void triggerNinjaAction(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator())
            return;

        ActionAttachment data = player.getData(ModAttachments.ACTION);
        player.getData(ModAttachments.INPUT).update(event);
        data.selectAndSendAction(player);
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
                ActionAttachment actionHolder = livingEntity.getData(ModAttachments.ACTION);
                BaguAnimationController animationController = AnimationUtil.getAnimationController(bagusModelEvent.getEntity());

                if (actionHolder.getCurrentAction().value() == NinjaActions.AIR_ROCKET.value()) {
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
            ActionAttachment actionHolder = livingEntity.getData(ModAttachments.ACTION);
            if (actionHolder.getCurrentAction().value() == NinjaActions.SPIN.value()) {
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees((bagusModelEvent.getPartialTick() + entity.tickCount) * 60F));
            }
            ActionStatesAttachment state = livingEntity.getData(ModAttachments.STATES);
            if (actionHolder.getCurrentAction().value() == NinjaActions.AIR_ROCKET.value()) {
                bagusModelEvent.getPoseStack().mulPose(Axis.XP.rotationDegrees(livingEntity.getXRot()));

                float f = Mth.rotLerp(bagusModelEvent.getPartialTick(), livingEntity.yBodyRotO, livingEntity.yBodyRot);
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(-f));
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(state.getActionYRot()));
                bagusModelEvent.getPoseStack().mulPose(Axis.XP.rotationDegrees(-livingEntity.getXRot()));
                bagusModelEvent.getPoseStack().mulPose(Axis.XP.rotationDegrees(state.getActionXRot()));
            }


            if (actionHolder.getCurrentAction().value() == NinjaActions.SLIDE.value()) {
                float f = Mth.rotLerp(bagusModelEvent.getPartialTick(), livingEntity.yBodyRotO, livingEntity.yBodyRot);
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(-f));
                bagusModelEvent.getPoseStack().mulPose(Axis.YP.rotationDegrees(state.getActionYRot()));
            }

            if (actionHolder.getCurrentAction().value() == NinjaActions.WALL_SLIDE.value()) {
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