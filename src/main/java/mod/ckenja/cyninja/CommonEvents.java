package mod.ckenja.cyninja;

import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.registry.ModAttachments;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
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
            if (ninjaAction != null && ninjaAction.getCurrentAction().value() != NinjaActions.NONE.value() && ninjaAction.getCurrentAction().value().getHitBox().isPresent()) {
                event.setNewSize(ninjaAction.getCurrentAction().value().getHitBox().get());
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
    public static void impact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity living) {
            if (NinjaActionUtils.getActionData(living).getCurrentAction().value().getReduceDamage() >= 1.0F) {
                living.playSound(SoundEvents.BREEZE_DEFLECT);
                event.getProjectile().deflect(ProjectileDeflection.MOMENTUM_DEFLECT, event.getProjectile().getOwner(), event.getProjectile().getOwner(), true);
                event.setCanceled(true);
            }
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
            event.setStrength(event.getStrength() * (1.0F - ninjaActionAttachment.getCurrentAction().value().getReduceKnockback()));
            if (ninjaActionAttachment.getCurrentAction().value().getReduceKnockback() >= 1.0F) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        NinjaActionAttachment ninjaActionAttachment = event.getEntity().getData(ModAttachments.NINJA_ACTION);
        if (ninjaActionAttachment != null && event.getSource().isDirect() && event.getSource().getDirectEntity() != null && !event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setAmount(event.getAmount() * (1.0F - ninjaActionAttachment.getCurrentAction().value().getReduceDamage()));
            if (ninjaActionAttachment.getCurrentAction().value().getReduceDamage() >= 1.0F) {
                event.setCanceled(true);
            }
        }
    }
}
