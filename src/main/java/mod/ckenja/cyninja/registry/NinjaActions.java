package mod.ckenja.cyninja.registry;

import bagu_chan.bagus_lib.util.client.AnimationUtil;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.ninja_action.NinjaActionAttachment;
import mod.ckenja.cyninja.entity.SickleEntity;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import mod.ckenja.cyninja.util.NinjaInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;
import java.util.function.Function;

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
                            (getActionData(livingEntity).getCurrentAction() == NinjaActions.NONE ||
                                    getActionData(livingEntity).getCurrentAction() == NinjaActions.SPIN)
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
                if (level.isClientSide()) {
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
                if (livingEntity.level().isClientSide && attachment.getInputs() != null) {
                    //sneakを押してなければnone
                    if (!attachment.getInputs().contains(NinjaInput.SNEAK)) {
                        PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
                        return NONE;
                    }

                    if (attachment.getInputs().contains(NinjaInput.JUMP) && livingEntity.onGround()) {
                        PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
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
                if (!livingEntity.level().isClientSide())
                    AnimationUtil.sendAnimation(livingEntity, ModAnimations.AIR_ROCKET);
                getActionData(livingEntity).decreaseAirJumpCount();
                getActionData(livingEntity).setActionXRot(livingEntity.getXRot());
                getActionData(livingEntity).setActionYRot(livingEntity.yHeadRot);
            })
            .addTickAction(NinjaActionUtils::tickAirRocket)
            .addStopAction(livingEntity -> {
                if (!livingEntity.level().isClientSide())
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
            .transform(sickleAction())
            .addNeedCondition(NinjaActionUtils::isEquipSickle)
            .addHitAction((projectile, hitresult) -> handleHitAction(projectile, hitresult, false))
            .setStartInput(NinjaInput.LEFT_CLICK)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> SICKLE_HOOK = NINJA_ACTIONS.register("sickle_hook", () -> NinjaAction.Builder.newInstance()
            .transform(sickleAction())
            .addNeedCondition(livingEntity -> NinjaActionUtils.isEquipSickleTrim(livingEntity, Items.COPPER_INGOT))
            .addHitAction((projectile, hitresult) -> handleHitAction(projectile, hitresult, true))
            .override(SICKLE_ATTACK)
            .build()
    );

    private static Function<NinjaAction.Builder,NinjaAction.Builder> sickleAction() {
        return builder -> builder
                    .instant()
                    .cooldown(5)
                    .addStartAction(living -> {
                        ItemStack itemstack = living.getMainHandItem();
                        Level level = living.level();
                        playThrowSound(level, living);
                        if (!level.isClientSide) {
                            spawnSickleEntity(living, itemstack);
                        }
                    });
    }

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

    private static void spawnSickleEntity(LivingEntity living, ItemStack itemstack) {
        Level level = living.level();
        SickleEntity sickle = new SickleEntity(level, living, itemstack.copy());
        sickle.setItem(itemstack.copy());
        sickle.setNinjaAction(NinjaActionUtils.getActionData(living).getCurrentAction());
        sickle.shootFromRotation(living, living.getXRot(), living.getYRot(), 0.0F, 1.8F, 1.0F);
        level.addFreshEntity(sickle);
        itemstack.set(ModDataComponents.CHAIN_ONLY, sickle.getUUID());
    }

    private static void handleHitAction(Entity projectile, HitResult hitResult, boolean isHook) {
        if (projectile instanceof SickleEntity sickle) {
            if (hitResult instanceof BlockHitResult blockHitResult) {
                handleBlockHit(sickle, blockHitResult, isHook);
            } else if (hitResult instanceof EntityHitResult entityHitResult) {
                handleEntityHit(sickle, entityHitResult, isHook);
            }
        }
    }

    private static void handleBlockHit(SickleEntity sickle, BlockHitResult blockHitResult, boolean isHook) {
        BlockPos pos = blockHitResult.getBlockPos();
        Level level = sickle.level();
        BlockState state = level.getBlockState(pos);
        SoundType soundType = state.getSoundType(sickle.level(), pos, sickle);
        if (!sickle.isReturning()) {
            if (sickle.canAttach() && isHook) {
                Vec3 vec3 = blockHitResult.getLocation().subtract(sickle.getX(), sickle.getY(), sickle.getZ());
                sickle.setDeltaMovement(vec3);
                sickle.setInGround(true);
                Vec3 vec31 = vec3.normalize().scale(0.05F);
                sickle.setPosRaw(sickle.getX() - vec31.x, sickle.getY() - vec31.y, sickle.getZ() - vec31.z);
            } else {
                level.playSound(null, sickle.getX(), sickle.getY(), sickle.getZ(), soundType.getHitSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
                sickle.setReturning(true);
            }
        }
    }

    private static void handleEntityHit(SickleEntity sickle, EntityHitResult entityHitResult, boolean isHook) {
        Entity entity = entityHitResult.getEntity();
        Entity shooter = sickle.getOwner();
        if (entity != shooter && !sickle.isReturning() && sickle.canAttach()) {
            LivingEntity livingentity = shooter instanceof LivingEntity ? (LivingEntity) shooter : null;
            double damage = 6;
            DamageSource damagesource = sickle.damageSources().mobProjectile(sickle, livingentity);

            if (sickle.getWeaponItem() != null && sickle.level() instanceof ServerLevel serverlevel) {
                damage = EnchantmentHelper.modifyDamage(serverlevel, sickle.getWeaponItem(), entity, damagesource, (float) damage);
            }
            if (entity.hurt(damagesource, (float) damage)) {
                if (entity instanceof LivingEntity hurtEntity) {
                    if (sickle.level() instanceof ServerLevel serverlevel1) {
                        EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel1, hurtEntity, damagesource, sickle.getWeaponItem());
                    }
                }
            } else {
                sickle.deflect(ProjectileDeflection.REVERSE, entity, sickle.getOwner(), false);
                sickle.setDeltaMovement(sickle.getDeltaMovement().scale(0.2));
            }
        }
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