package mod.ckenja.cyninja.infrastructure.event;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.content.item.NinjaArmorItem;
import mod.ckenja.cyninja.core.action.Action;
import mod.ckenja.cyninja.core.action.EquipmentCondition;
import mod.ckenja.cyninja.core.action.ActionAttachment;
import mod.ckenja.cyninja.infrastructure.registry.ModAttachments;
import mod.ckenja.cyninja.infrastructure.registry.ModItemTags;
import mod.ckenja.cyninja.infrastructure.registry.NinjaActions;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
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
        if (event.getEntity() instanceof Player player) {
            ActionAttachment data = player.getData(ModAttachments.ACTION);
            Holder<Action> currentAction = data.getCurrentAction();
            if (currentAction != NinjaActions.NONE.value() && event.getEntity() instanceof LivingEntity) {
                data.getCurrentAction().value().getHitBox().ifPresent(event::setNewSize);
            }
        }
    }


    @SubscribeEvent
    public static void fallEvent(LivingFallEvent event) {
        if (EquipmentCondition.isNinjaFullSuit.test(event.getEntity()))
            event.setDistance(event.getDistance() - 4);//スーツを着てたら落下ダメージ軽減
        if (event.getEntity().getData(ModAttachments.ACTION).getCurrentAction().value() == NinjaActions.HEAVY_FALL.value())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void tickEvent(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            //basic action handle
            ActionAttachment actionData = livingEntity.getData(ModAttachments.ACTION);
            actionData.pretick(livingEntity);
        }
    }


    @SubscribeEvent
    public static void tickEvent(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            //basic action handle
            ActionAttachment actionData = livingEntity.getData(ModAttachments.ACTION);
            /*if (EquipmentCondition.isNinjaFullSuit.test(livingEntity) && actionData.getCurrentAction().value() != NinjaActions.NONE.value()) {
                NinjaActionUtils.setAction(livingEntity, NinjaActions.NONE);
            } else {*/
                actionData.tick(livingEntity);
                livingEntity.getData(ModAttachments.STATES).tick(livingEntity);
            livingEntity.getData(ModAttachments.COOLDOWN).update();
            //}
        }
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        float reduceKnockback = event.getEntity().getData(ModAttachments.ACTION).getCurrentAction().value().getReduceKnockback();
        event.setStrength(event.getStrength() * (1.0F - reduceKnockback));
        if (reduceKnockback >= 1.0F) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void parryOnImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity living) {
            if (living.getData(ModAttachments.ACTION).getCurrentAction().value().getReduceDamage() >= 1.0F) {
                living.playSound(SoundEvents.BREEZE_DEFLECT);
                event.getProjectile().deflect(ProjectileDeflection.MOMENTUM_DEFLECT, event.getProjectile().getOwner(), event.getProjectile().getOwner(), true);
                event.setCanceled(true);
            }

            if (isDamageSourceBlocked(living, event.getEntity())) {
                if (new EquipmentCondition(EquipmentSlotGroup.HAND, ModItemTags.KATANA, Items.IRON_INGOT).test(living)) {//TODO 発動しているかどうかに条件を変える
                    if (living.attackAnim > 0.0F) {
                        living.playSound(SoundEvents.BREEZE_DEFLECT);
                        event.getProjectile().deflect(ProjectileDeflection.MOMENTUM_DEFLECT, event.getProjectile().getOwner(), event.getProjectile().getOwner(), true);

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    public static boolean isDamageSourceBlocked(LivingEntity owner, Entity entity) {
        if (entity instanceof AbstractArrow abstractarrow && abstractarrow.getPierceLevel() > 0)
            return false;

        Vec3 pos = entity.position();
        Vec3 viewVector = owner.calculateViewVector(0.0F, owner.getYHeadRot());
        Vec3 posToOwner = pos.vectorTo(owner.position());
        return new Vec3(posToOwner.x, 0.0, posToOwner.z).dot(viewVector) < 0.0;
    }

    public static boolean isDamageSourceBlocked(LivingEntity owner, DamageSource source) {
        Entity entity = source.getDirectEntity();
        if (entity instanceof AbstractArrow abstractarrow && abstractarrow.getPierceLevel() > 0)
            return false;

        if (source.is(DamageTypeTags.BYPASSES_SHIELD))
            return false;

        if(!owner.isBlocking())
            return false;

        Vec3 pos = source.getSourcePosition();
        if (pos == null)
            return false;

        Vec3 viewVector = owner.calculateViewVector(0.0F, owner.getYHeadRot());
        Vec3 posToOwner = pos.vectorTo(owner.position());
        return new Vec3(posToOwner.x, 0.0, posToOwner.z).dot(viewVector) < 0.0;
    }

    @SubscribeEvent
    public static void parryOnHurt(LivingIncomingDamageEvent event) {
        float reduceDamage = event.getEntity().getData(ModAttachments.ACTION).getCurrentAction().value().getReduceDamage();
        if (event.getSource().isDirect() && event.getSource().getDirectEntity() != null && !event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setAmount(event.getAmount() * (1.0F - reduceDamage));
            if (reduceDamage >= 1.0F) {
                event.setCanceled(true);
            }
        }

        if (new EquipmentCondition(EquipmentSlotGroup.HAND, ModItemTags.KATANA, Items.IRON_INGOT).test(event.getEntity()) &&
                event.getEntity().attackAnim > 0.0F &&
                event.getSource().isDirect() &&
                event.getSource().getDirectEntity() != null &&
                !event.getSource().is(DamageTypeTags.IS_EXPLOSION) &&
                !event.getSource().is(DamageTypeTags.IS_PROJECTILE) &&
                isDamageSourceBlocked(event.getEntity(), event.getSource())
        ) {
            event.setAmount(0.0F);
            event.setCanceled(true);
        }
    }
}
