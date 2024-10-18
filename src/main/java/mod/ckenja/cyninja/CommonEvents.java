package mod.ckenja.cyninja;

import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.registry.ModAttachments;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Cyninja.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void scaleEvent(EntityEvent.Size event) {
        //If Player's inventory is null. don't check
        if (event.getEntity() instanceof Player player && player.getInventory() != null) {
            NinjaActionAttachment ninjaAction = NinjaActionUtils.getActionData(player);
            if (ninjaAction != null && ninjaAction.getNinjaAction().value() != NinjaActions.NONE.value() && ninjaAction.getNinjaAction().value().getHitBox().isPresent()) {
                event.setNewSize(ninjaAction.getNinjaAction().value().getHitBox().get());
            }
        }
    }


    @SubscribeEvent
    public static void fallEvent(LivingFallEvent event) {
        if (NinjaActionUtils.isWearingFullNinjaSuit(event.getEntity())) {
            event.setDistance(event.getDistance() - 4);
        }
    }

    @SubscribeEvent
    public static void tickEvent(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            //basic action handle
            NinjaActionAttachment actionData = NinjaActionUtils.getActionData(livingEntity);
            actionData.pretick(livingEntity);

        }
    }


    @SubscribeEvent
    public static void tickEvent(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            //basic action handle
            NinjaActionAttachment actionData = NinjaActionUtils.getActionData(livingEntity);
            if (actionData != null) {
                if (!NinjaActionUtils.isWearingFullNinjaSuit(livingEntity)) {
                    NinjaActionUtils.setAction(livingEntity, NinjaActions.NONE);
                } else {
                    actionData.tick(livingEntity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        NinjaActionAttachment ninjaActionAttachment = event.getEntity().getData(ModAttachments.NINJA_ACTION);
        if (ninjaActionAttachment != null) {
            event.setStrength(event.getStrength() * (1.0F - ninjaActionAttachment.getNinjaAction().value().getReduceKnockback()));
            if (ninjaActionAttachment.getNinjaAction().value().getReduceKnockback() >= 1.0F) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        NinjaActionAttachment ninjaActionAttachment = event.getEntity().getData(ModAttachments.NINJA_ACTION);
        if (ninjaActionAttachment != null && event.getSource().isDirect() && event.getSource().getDirectEntity() != null && !event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setAmount(event.getAmount() * (1.0F - ninjaActionAttachment.getNinjaAction().value().getReduceDamage()));
            if (ninjaActionAttachment.getNinjaAction().value().getReduceDamage() >= 1.0F) {
                event.setCanceled(true);
            }
        }
    }
}
