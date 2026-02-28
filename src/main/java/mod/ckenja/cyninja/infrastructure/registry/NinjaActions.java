package mod.ckenja.cyninja.infrastructure.registry;

import bagu_chan.bagus_lib.util.client.AnimationUtil;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.content.NinjaActionHandlers;
import mod.ckenja.cyninja.content.sickle.SickleEntity;
import mod.ckenja.cyninja.content.sickle.ChainAndSickleItem;
import mod.ckenja.cyninja.content.sickle.SickleEquipmentUtil;
import mod.ckenja.cyninja.core.action.Action;
import mod.ckenja.cyninja.core.action.EquipmentCondition;
import mod.ckenja.cyninja.core.action.ActionAttachment;
import mod.ckenja.cyninja.core.util.JumpHelper;
import mod.ckenja.cyninja.core.util.NinjaActionUtils;
import mod.ckenja.cyninja.core.util.NinjaInput;
import mod.ckenja.cyninja.infrastructure.attachment.ActionStatesAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;

import static mod.ckenja.cyninja.infrastructure.registry.ActionRegistry.NINJA_ACTIONS_REGISTRY;
import static net.minecraft.world.entity.EquipmentSlotGroup.ARMOR;

public class NinjaActions {
    public static final DeferredRegister<Action> NINJA_ACTIONS = DeferredRegister.create(NINJA_ACTIONS_REGISTRY, Cyninja.MODID);

    public static final DeferredHolder<Action, Action> NONE = NINJA_ACTIONS.register("none", () -> Action.Builder.newInstance().loop().build());

    public static final DeferredHolder<Action, Action> SLIDE = NINJA_ACTIONS.register("slide", () -> Action.Builder.newInstance()
            .startPredicate(livingEntity -> !NinjaActionUtils.isInFluid(livingEntity))
            .startPredicateWithData(data -> data.getCurrentAction().is(NinjaActions.NONE) || data.getCurrentAction().is(NinjaActions.SPIN))
            .startPredicate(entity -> entity.getData(ModAttachments.STATES).canAirSlideCount())
            .setStartInput(NinjaInput.SNEAK, NinjaInput.SPRINT)
            .cooldown(4)
            .loop()
            .speed(0.3F)
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .setCanVanillaAction(false)
            .setNoBob(true)
            .setHitBox(EntityDimensions.scalable(0.6F, 0.6F))
            .addTickAction(slider -> {
                Level level = slider.level();
                if (level.isClientSide) {
                    NinjaActionUtils.spawnSprintParticle(slider);
                } else {
                    List<Entity> entities = level.getEntities(slider, slider.getBoundingBox());
                    NinjaActionUtils.attackEntities(slider, entities, 6F, 0.8F, DamageTypes.MOB_ATTACK);
                }
            })
            .addStartAction(NinjaActionHandlers::startSlide)
            .addStopAction(NinjaActionHandlers::stopSlide)
            .next(NinjaActionHandlers::nextSlide)
            .build()
    );

    public static final DeferredHolder<Action, Action> VANISH = NINJA_ACTIONS.register("vanish", () -> Action.Builder.newInstance()
            .equipmentCondition(ARMOR, Items.REDSTONE)
            .loop()
            .speed(0.3F)
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .setCanVanillaAction(false)
            .setNoBob(true)
            .setHitBox(EntityDimensions.scalable(0.6F, 0.6F))
            .addTickAction(slider -> {
                Level level = slider.level();
                if (level.isClientSide) {
                    NinjaActionUtils.spawnSprintParticle(slider);
                }
            })
            .addStartAction(NinjaActionHandlers::startSlide)
            .addStartAction(living -> NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, 0F, NinjaActions.NONE))
            .addStartAction(living -> living.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100)))
            .addStopAction(NinjaActionHandlers::stopSlide)
            .addStartAction(living -> NinjaActionUtils.resetEntitiesTarget(living.level(), living.position()))
            .next(NinjaActionHandlers::nextSlide)
            .build()
    );


    public static final DeferredHolder<Action, Action> SHADOW_SLIDE = NINJA_ACTIONS.register("shadow_slide", () -> Action.Builder.newInstance()
            .instant()
            .equipmentCondition(ARMOR, Items.COPPER_INGOT)
            .addStartAction(living -> {
                NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, 30F, NinjaActions.SLIDE, 20);
                NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, -30F, NinjaActions.SLIDE, 20);
            })
            .addStartAction(living -> NinjaActionUtils.resetEntitiesTarget(living.level(), living.position()))
            .priority(900)
            .inject(SLIDE)
            .build()
    );

    public static final DeferredHolder<Action, Action> MIRROR_IMAGE = NINJA_ACTIONS.register("mirror_image", () -> Action.Builder.newInstance()
            .instant()
            .equipmentCondition(ARMOR, Items.QUARTZ)
            .addStartAction(NinjaActionHandlers::mirrorImageDo)
            .addStartAction(living -> NinjaActionUtils.resetEntitiesTarget(living.level(), living.position()))
            .priority(900)
            .inject(SLIDE)
            .build()
    );

    public static final DeferredHolder<Action, Action> HEAVY_FALL = NINJA_ACTIONS.register("heavy_fall", () -> Action.Builder.newInstance()
            .startPredicate(livingEntity -> livingEntity.fallDistance > 2.0F && NinjaActionUtils.isInAir(livingEntity))
            .startPredicateWithData(data -> data.getCurrentAction().value() == NinjaActions.NONE.value())
            .setStartInput(NinjaInput.SNEAK)
            .equipmentCondition(EquipmentCondition.isNinjaFullSuit)
            .loop()
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .addStartAction(NinjaActionHandlers::startHeavyFall)
            .addTickAction(NinjaActionHandlers::tickHeavyFall)
            .addStopAction(NinjaActionHandlers::stopHeavyFall)
            .cooldown(4)
            .next(livingEntity -> Optional.ofNullable(
                    NinjaActionUtils.isInAir(livingEntity) ? null : NONE))
            .build()
    );

    public static final DeferredHolder<Action, Action> WALL_SLIDE = NINJA_ACTIONS.register("wall_slide", () -> Action.Builder.newInstance()
            .startPredicate(livingEntity -> livingEntity.horizontalCollision && NinjaActionUtils.isInAir(livingEntity))
            .equipmentCondition(EquipmentCondition.isNinjaFullSuit)
            .loop()
            .setNoBob(true)
            .addTickAction(NinjaActionHandlers::checkWallSlide)
            .next(livingEntity -> Optional.ofNullable(
                    livingEntity.onGround() || !livingEntity.horizontalCollision ? NONE : null))
            .priority(850)
            .build()
    );

    public static final DeferredHolder<Action, Action> WALL_JUMP = NINJA_ACTIONS.register("wall_jump", () -> Action.Builder.newInstance()
            .setStartInput(NinjaInput.JUMP)
            .instant()
            .equipmentCondition(EquipmentCondition.isNinjaFullSuit)
            .startPredicate(livingEntity -> JumpHelper.canJump(livingEntity, NinjaActions.WALL_SLIDE))
            .addStartAction(livingEntity -> {
                Vec3 delta = livingEntity.getDeltaMovement();
                livingEntity.setDeltaMovement(delta.x, 1F, delta.z);
                livingEntity.resetFallDistance();
                livingEntity.getData(ModAttachments.STATES).resetAirJumpCount();
            })
            .build()
    );

    public static final DeferredHolder<Action, Action> AIR_JUMP = NINJA_ACTIONS.register("air_jump", () -> Action.Builder.newInstance()
            .setStartInput(NinjaInput.JUMP)
            .instant()
            .equipmentCondition(EquipmentCondition.isNinjaFullSuit)
            .startPredicate(JumpHelper::canAirJump)
            .addStartAction(NinjaActionHandlers::doAirJump)
            .build()
    );

    public static final DeferredHolder<Action, Action> HEAVY_AIR_JUMP = NINJA_ACTIONS.register("heavy_air_jump", () -> Action.Builder.newInstance()
            .setStartInput(NinjaInput.JUMP)
            .instant()
            .equipmentCondition(ARMOR, Items.IRON_INGOT)
            .addStartAction(NinjaActionHandlers::tickHeavyAirJump)
            .priority(900)
            .inject(AIR_JUMP)
            .build()
    );

    public static final DeferredHolder<Action, Action> AIR_ROCKET = NINJA_ACTIONS.register("air_rocket", () -> Action.Builder.newInstance()
            .startAndEnd(0, 20)
            .equipmentCondition(ARMOR, Items.GOLD_INGOT)
            .addStartAction(livingEntity -> {
                if (!livingEntity.level().isClientSide)
                    AnimationUtil.sendAnimation(livingEntity, ModAnimations.AIR_ROCKET);
                ActionStatesAttachment state = livingEntity.getData(ModAttachments.STATES);
                state.decreaseAirJumpCount();
                state.setActionXRot(livingEntity.getXRot());
                state.setActionYRot(livingEntity.yHeadRot);
            })
            .addTickAction(NinjaActionHandlers::tickAirRocket)
            .addStopAction(livingEntity -> {
                if (!livingEntity.level().isClientSide)
                    AnimationUtil.sendStopAnimation(livingEntity, ModAnimations.AIR_ROCKET);
            })
            .priority(900)
            .override(AIR_JUMP)
            .build()
    );

    public static final DeferredHolder<Action, Action> SPIN = NINJA_ACTIONS.register("spin", () -> Action.Builder.newInstance()
            .startPredicate(livingEntity -> !livingEntity.onGround())
            .equipmentCondition(new EquipmentCondition(EquipmentSlotGroup.HAND, ModItemTags.KATANA))
            .equipmentCondition(EquipmentCondition.isNinjaFullSuit)
            .setStartInput(NinjaInput.LEFT_CLICK)
            .startAndEnd(2, 12)
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .cooldown(4)
            .addTickAction(attacker -> {
                List<Entity> entities = NinjaActionUtils.getEnemiesInSphere(attacker.level(), attacker.position(), 2.5);
                NinjaActionUtils.attackEntities(attacker, entities, 6F, 0.8F, DamageTypes.MOB_ATTACK);
                attacker.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value());
                attacker.level().addParticle(ParticleTypes.SWEEP_ATTACK, attacker.getX(), attacker.getY(), attacker.getZ(), 0, 0, 0);
            })
            .next(livingEntity -> {
                if (livingEntity.onGround() || livingEntity.horizontalCollision) {
                    return Optional.of(NONE);
                }
                return Optional.empty();
            })
            .build()
    );

    public static final DeferredHolder<Action, Action> RAIN = NINJA_ACTIONS.register("rain", () -> Action.Builder.newInstance()
            .instant()
            .addStartAction(living -> {
                Vec3 position = living.position();
                double width = living.getBbWidth();
                AABB aabb = new AABB(position.x - width, position.y - 100, position.z - width, position.x + width, position.y, position.z + width);
                List<Entity> list = living.level().getEntitiesOfClass(Entity.class, aabb)
                        .stream().filter(entity -> {
                            for (double y = position.y; y > entity.getY(); y--) {
                                if (!entity.level().isEmptyBlock(new BlockPos((int) (position.x + width / 2), (int) y, (int) (position.z + width / 2)))) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .toList();
                NinjaActionUtils.attackEntities(living, list, 1F, 0F, DamageTypes.MOB_ATTACK);
            })
            .startPredicate(living -> !living.onGround())
            .equipmentCondition(ARMOR, Items.LAPIS_LAZULI)
            .build()
    );

    public static final DeferredHolder<Action, Action> SICKLE_ATTACK = NINJA_ACTIONS.register("sickle_attack", () -> Action.Builder.newInstance()
            .instant()
            .cooldown(5)
            .addStartAction(entity -> throwSickle(entity, false))
            .equipmentCondition(EquipmentCondition.isNinjaFullSuit)
            .startPredicate(SickleEquipmentUtil::isEquipSickleNotOnlySickle)
            .startPredicate(entity -> entity.getData(ModAttachments.INPUT).keyUp(NinjaInput.LEFT_CLICK))
            .build()
    );

    public static final DeferredHolder<Action, Action> SICKLE_HOOK = NINJA_ACTIONS.register("sickle_hook", () -> Action.Builder.newInstance()
            .instant()
            .cooldown(5)
            .addStartAction(entity -> throwSickle(entity, true))
            .equipmentCondition(new EquipmentCondition(EquipmentSlotGroup.HAND, ModItemTags.SICKLE, Items.COPPER_INGOT))
            .override(SICKLE_ATTACK)
            .build()
    );

    public static final DeferredHolder<Action, Action> SICKLE_RETURN = NINJA_ACTIONS.register("sickle_return", () -> Action.Builder.newInstance()
            .instant()
            .setStartInput(NinjaInput.LEFT_CLICK)
            .addStartAction(living -> {
                ItemStack item = living.getMainHandItem();
                Level level = living.level();
                if (level.isClientSide)
                    return;
                if (!(item.getItem() instanceof ChainAndSickleItem sickle))
                    return;
                SickleEntity entity = sickle.getThrownEntity(level, item);
                if (entity == null)
                    return;
                entity.setReturning(true);
                entity.setAttach(false);
                entity.setInGround(false);
                entity.setDeltaMovement(Vec3.ZERO);
            })
            .startPredicate(SickleEquipmentUtil::isEquipSickleOnlySickle)
            .startPredicate(entity -> entity.getData(ModAttachments.COOLDOWN).isCooldownFinished(ActionAttachment.getActionOrOverride(SICKLE_ATTACK.value(), entity)))
            .build()
    );

    private static void playThrowSound(Level level, LivingEntity living) {
        level.playSound(
                null,
                living.getX(),
                living.getY(),
                living.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
    }

    private static void throwSickle(LivingEntity living, boolean isHook) {
        ItemStack itemstack = living.getMainHandItem();
        Level level = living.level();
        playThrowSound(level, living);
        if (!level.isClientSide) {
            spawnSickleEntity(living, itemstack, isHook);
        }
    }

    private static void spawnSickleEntity(LivingEntity living, ItemStack itemstack, boolean isHook) {
        Level level = living.level();
        SickleEntity sickle = new SickleEntity(level, living, itemstack.copy());
        sickle.setItem(itemstack.copy());
        sickle.shootFromRotation(living, living.getXRot(), living.getYRot(), 0.0F, 1.8F, 1.0F);
        sickle.setAttach(isHook);
        if (isHook) sickle.setChainLength(128);
        level.addFreshEntity(sickle);
        itemstack.set(ModDataComponents.CHAIN_ONLY, sickle.getUUID());
    }
}