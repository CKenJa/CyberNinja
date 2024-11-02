package mod.ckenja.cyninja;

import mod.ckenja.cyninja.item.NinjaArmorItem;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.ninja_action.NinjaActionAttachment;
import mod.ckenja.cyninja.registry.ModAttachments;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Cyninja.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void toolTipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ArmorTrim armorTrim = stack.get(DataComponents.TRIM);
        if (armorTrim != null) {
            String rawStr = Util.makeDescriptionId("trim_material", armorTrim.material().unwrapKey().get().location());
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
    public static void scaleEvent(EntityEvent.Size event) {
        //If Player's inventory is null. don't check
        if (event.getEntity() instanceof Player player && player.getInventory() != null) {
            NinjaActionAttachment data = NinjaActionUtils.getActionData(player);
            Holder<NinjaAction> currentAction = data.getCurrentAction();
            if (currentAction != NinjaActions.NONE.value() && event.getEntity() instanceof LivingEntity) {
                data.getCurrentAction().value().getHitBox().ifPresent(event::setNewSize);
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
            if (!NinjaActionUtils.isWearingFullNinjaSuit(livingEntity)) {
                NinjaActionUtils.setAction(livingEntity, NinjaActions.NONE);
            } else {
                actionData.tick(livingEntity);
            }
        }
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        float reduceKnockback = event.getEntity().getData(ModAttachments.NINJA_ACTION).getCurrentAction().value().getReduceKnockback();
        event.setStrength(event.getStrength() * (1.0F - reduceKnockback));
        if (reduceKnockback >= 1.0F) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        float reduceDamage = event.getEntity().getData(ModAttachments.NINJA_ACTION).getCurrentAction().value().getReduceDamage();
        if (event.getSource().isDirect() && event.getSource().getDirectEntity() != null && !event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setAmount(event.getAmount() * (1.0F - reduceDamage));
            if (reduceDamage >= 1.0F) {
                event.setCanceled(true);
            }
        }
    }
}
