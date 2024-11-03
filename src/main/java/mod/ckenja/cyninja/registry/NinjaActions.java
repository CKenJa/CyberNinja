package mod.ckenja.cyninja.registry;

import bagu_chan.bagus_lib.util.client.AnimationUtil;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.item.ChainAndSickleItem;
import mod.ckenja.cyninja.ninja_action.NinjaActionAttachment;
import mod.ckenja.cyninja.entity.SickleEntity;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.ninja_action.NinjaActionAttachment;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import mod.ckenja.cyninja.util.NinjaInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;

import static mod.ckenja.cyninja.util.NinjaActionUtils.getActionData;
import static mod.ckenja.cyninja.util.VectorUtil.moveToLookingWay;
import static net.minecraft.resources.ResourceKey.createRegistryKey;

@EventBusSubscriber(modid = Cyninja.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NinjaActions {
    private static final ResourceLocation SLIDE_STEP_ID = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "slide_step");


    public static final ResourceKey<Registry<NinjaAction>> NINJA_ACTIONS_REGISTRY = createRegistryKey(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "ninja_skill"));

    public static final DeferredRegister<NinjaAction> NINJA_ACTIONS = DeferredRegister.create(NINJA_ACTIONS_REGISTRY, Cyninja.MODID);
    public static final DeferredHolder<NinjaAction, NinjaAction> NONE = NINJA_ACTIONS.register("none", () -> NinjaAction.Builder.newInstance().loop().build());

    public static final DeferredHolder<NinjaAction, NinjaAction> SLIDE = NINJA_ACTIONS.register("slide", () -> NinjaAction.Builder.newInstance()
            .addNeedCondition(livingEntity ->
                    !(livingEntity.isInFluidType() || livingEntity.isInWater()) &&
                    getActionData(livingEntity).canAirSlideCount() &&
                            (getActionData(livingEntity).getCurrentAction().is(NinjaActions.NONE) ||
                                    getActionData(livingEntity).getCurrentAction().is(NinjaActions.SPIN))
            )
            .addNeedCondition(NinjaActionUtils::isWearingFullNinjaSuit)
            .setStartInput(NinjaInput.SNEAK, NinjaInput.SPRINT)
            .cooldown(4)
            .loop()
            .speed(3F)
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .setCanVanillaAction(false)
            .setNoBob(true)
            .setHitBox(EntityDimensions.scalable(0.6F, 0.6F))
            .addTickAction(slider->{
                Level level = slider.level();
                if (level.isClientSide) {
                    NinjaActionUtils.spawnSprintParticle(slider);
                } else {
                    List<Entity> entities = level.getEntities(slider, slider.getBoundingBox());
                    NinjaActionUtils.attackEntities(slider, entities, 6F, 0.8F, DamageTypes.MOB_ATTACK);
                }
            })
            .addStartAction(livingEntity -> {
                AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.STEP_HEIGHT);
                if (attributeinstance != null && !attributeinstance.hasModifier(SLIDE_STEP_ID)) {
                    livingEntity.getAttribute(Attributes.STEP_HEIGHT).addTransientModifier(new AttributeModifier(SLIDE_STEP_ID, (double) 0.5F, AttributeModifier.Operation.ADD_VALUE));
                }
                getActionData(livingEntity).setActionYRot(livingEntity.yHeadRot);

                getActionData(livingEntity).decreaseAirSlideCount();
                Vec3 vec3 = livingEntity.getDeltaMovement();
                livingEntity.setDeltaMovement(vec3.x, 0, vec3.z);
                livingEntity.resetFallDistance();
                moveToLookingWay(livingEntity, 1F, NinjaActions.SLIDE);
            })
            .addStopAction(livingEntity -> {
                AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.STEP_HEIGHT);
                if (attributeinstance != null) {
                    livingEntity.getAttribute(Attributes.STEP_HEIGHT).removeModifier(SLIDE_STEP_ID);
                }
            })
            .next(livingEntity -> {
                NinjaActionAttachment attachment = getActionData(livingEntity);
                //壁にぶつかったら止まる。そして減速
                if (livingEntity.horizontalCollision) {
                    Vec3 delta = livingEntity.getDeltaMovement();
                    livingEntity.setDeltaMovement(delta.x * 0.45F, delta.y, delta.z * 0.45F);
                    return NONE;
                }
                // jumpで止まる
                if (livingEntity.level().isClientSide && attachment.getCurrentInputs() != null) {
                    //sneakを押してなければnone
                    if (!attachment.getCurrentInputs().contains(NinjaInput.SNEAK)) {
                        if (livingEntity instanceof LocalPlayer localPlayer) {
                            PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
                        }
                        return NONE;
                    }

                    if (attachment.getCurrentInputs().contains(NinjaInput.JUMP) && livingEntity.onGround()) {
                        if (livingEntity instanceof LocalPlayer localPlayer) {
                            PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
                        }
                        return NONE;
                    }
                }
                // 一定時間経過かつ減速で止まる
                if (livingEntity.getDeltaMovement().horizontalDistance() < 0.2F) {
                    return NONE;
                }
                return null;
            }).build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> MIRROR_IMAGE = NINJA_ACTIONS.register("mirror_image", () -> NinjaAction.Builder.newInstance()
            .setStartInput(NinjaInput.SNEAK, NinjaInput.SPRINT)
            .instant()
            .addNeedCondition(living -> NinjaActionUtils.isWearingNinjaTrim(living, Items.QUARTZ))
            .addStartAction(NinjaActionUtils::mirrorImageDo)
            .priority(900)
            .inject(SLIDE)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> HEAVY_FALL = NINJA_ACTIONS.register("heavy_fall", () -> NinjaAction.Builder.newInstance()
            .addNeedCondition(livingEntity ->
                    livingEntity.fallDistance > 2.0F && !livingEntity.onGround() &&
                            !(livingEntity.isInFluidType() || livingEntity.isInWater()) && NinjaActionUtils.getActionData(livingEntity).getCurrentAction().value() == NinjaActions.NONE.value()
            )
            .setStartInput(NinjaInput.SNEAK)
            .addNeedCondition(NinjaActionUtils::isWearingFullNinjaSuit)
            .addNeedCondition(living -> NinjaActionUtils.isWearingNinjaTrim(living, Items.NETHERITE_INGOT))
            .loop()
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .addStartAction(NinjaActionUtils::startHeavyFall)
            .addTickAction(NinjaActionUtils::tickHeavyFall)
            .addStopAction(NinjaActionUtils::stopFall)
            .cooldown(4)
            .next(livingEntity ->
                    livingEntity.onGround() || livingEntity.isInFluidType() || livingEntity.isInWater() ? NONE : null)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> WALL_SLIDE = NINJA_ACTIONS.register("wall_slide", () -> NinjaAction.Builder.newInstance()
            .addNeedCondition(livingEntity ->
                    !livingEntity.onGround() &&
                    livingEntity.horizontalCollision &&
                    !(livingEntity.isInFluidType() || livingEntity.isInWater())
            )
            .addNeedCondition(NinjaActionUtils::isWearingFullNinjaSuit)
            .loop()
            .setNoBob(true)
            .addTickAction(NinjaActionUtils::checkWallSlide)
            .next(livingEntity -> livingEntity.onGround() || !livingEntity.horizontalCollision ? NONE : null)
            .priority(850)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> WALL_JUMP = NINJA_ACTIONS.register("wall_jump", () -> NinjaAction.Builder.newInstance()
            .setStartInput(NinjaInput.JUMP)
            .instant()
            .addNeedCondition(livingEntity -> getActionData(livingEntity).canJump(livingEntity, NinjaActions.WALL_SLIDE))
            .addStartAction(livingEntity -> {
                Vec3 delta = livingEntity.getDeltaMovement();
                livingEntity.setDeltaMovement(delta.x, 1F, delta.z);
                livingEntity.resetFallDistance();
                getActionData(livingEntity).resetAirJumpCount();
            })
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> AIR_JUMP = NINJA_ACTIONS.register("air_jump", () -> NinjaAction.Builder.newInstance()
            .setStartInput(NinjaInput.JUMP)
            .instant()
            .addNeedCondition(NinjaActionUtils::canAirJump)
            .addStartAction(NinjaActionUtils::doAirJump)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> HEAVY_AIR_JUMP = NINJA_ACTIONS.register("heavy_air_jump", () -> NinjaAction.Builder.newInstance()
            .setStartInput(NinjaInput.JUMP)
            .instant()
            .addNeedCondition(living -> NinjaActionUtils.isWearingNinjaTrim(living, Items.IRON_INGOT))
            .addStartAction(NinjaActionUtils::tickHeavyAirJump)
            .priority(900)
            .inject(AIR_JUMP)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> AIR_ROCKET = NINJA_ACTIONS.register("air_rocket", () -> NinjaAction.Builder.newInstance()
            .startAndEnd(0, 20)
            .addNeedCondition(living -> NinjaActionUtils.isWearingNinjaTrim(living, Items.GOLD_INGOT))
            .addStartAction(livingEntity -> {
                if (!livingEntity.level().isClientSide)
                    AnimationUtil.sendAnimation(livingEntity, ModAnimations.AIR_ROCKET);
                getActionData(livingEntity).decreaseAirJumpCount();
                getActionData(livingEntity).setActionXRot(livingEntity.getXRot());
                getActionData(livingEntity).setActionYRot(livingEntity.yHeadRot);
            })
            .addTickAction(NinjaActionUtils::tickAirRocket)
            .addStopAction(livingEntity -> {
                if (!livingEntity.level().isClientSide)
                    AnimationUtil.sendStopAnimation(livingEntity, ModAnimations.AIR_ROCKET);
            })
            .priority(900)
            .override(AIR_JUMP)
            .build()
    );

    public static final DeferredHolder<NinjaAction,NinjaAction> SPIN = NINJA_ACTIONS.register("spin", () -> NinjaAction.Builder.newInstance()
            .addNeedCondition(livingEntity -> !livingEntity.onGround())
            .addNeedCondition(NinjaActionUtils::isEquipKatana)
            .setStartInput(NinjaInput.LEFT_CLICK)
            .startAndEnd(2, 12)
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .cooldown(4)
            .addTickAction(attacker->{
                List<Entity> entities = NinjaActionUtils.getEnemiesInSphere(attacker.level(), attacker.position(), 2.5);
                NinjaActionUtils.attackEntities(attacker, entities, 6F, 0.8F, DamageTypes.MOB_ATTACK);
                attacker.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value());
                attacker.level().addParticle(ParticleTypes.SWEEP_ATTACK, attacker.getX(), attacker.getY(), attacker.getZ(), 0,0,0);
            })
            .next(livingEntity -> {
                if (livingEntity.onGround() || livingEntity.horizontalCollision) {
                    return NONE;
                }
                return null;
            })
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> SICKLE_ATTACK = NINJA_ACTIONS.register("sickle_attack", () -> NinjaAction.Builder.newInstance()
            .instant()
            .cooldown(5)
            .addStartAction(entity -> throwSickle(entity, false))
            .addNeedCondition(NinjaActionUtils::isEquipSickleNotOnlySickle)
            .addNeedCondition(entity -> NinjaActionUtils.keyUp(entity, NinjaInput.LEFT_CLICK))
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> SICKLE_HOOK = NINJA_ACTIONS.register("sickle_hook", () -> NinjaAction.Builder.newInstance()
            .instant()
            .cooldown(5)
            .addStartAction(entity -> throwSickle(entity, true))
            .addNeedCondition(livingEntity -> NinjaActionUtils.isEquipSickleTrim(livingEntity, Items.COPPER_INGOT))
            .override(SICKLE_ATTACK)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> SICKLE_RETURN = NINJA_ACTIONS.register("sickle_return", () -> NinjaAction.Builder.newInstance()
            .instant()
            .setStartInput(NinjaInput.LEFT_CLICK)
            .addStartAction(living -> {
                ItemStack item = living.getMainHandItem();
                Level level = living.level();
                if (level.isClientSide)
                    return;
                if(!(item.getItem() instanceof ChainAndSickleItem sickle))
                    return;
                SickleEntity entity = sickle.getThrownEntity(level, item);
                if (entity == null)
                    return;
                entity.setReturning(true);
                entity.setAttach(false);
                entity.setInGround(false);
                entity.setDeltaMovement(Vec3.ZERO);
            })
            .addNeedCondition(NinjaActionUtils::isEquipSickleOnlySickle)
            .addNeedCondition(entity -> NinjaActionUtils.getActionData(entity).isCooldownFinished(NinjaActionAttachment.getActionOrOveride(SICKLE_ATTACK.value(),entity)))
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

    private static Registry<NinjaAction> registry;

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        registry = event.create(new RegistryBuilder<>(NINJA_ACTIONS_REGISTRY).sync(true));
    }

    public static Registry<NinjaAction> getRegistry() {
        if (registry == null) {
            throw new IllegalStateException("Registry not yet initialized");
        }
        return registry;
    }
}