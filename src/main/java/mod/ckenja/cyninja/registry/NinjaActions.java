package mod.ckenja.cyninja.registry;

import bagu_chan.bagus_lib.util.client.AnimationUtil;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.util.EquipmentRequest;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import mod.ckenja.cyninja.util.NinjaInput;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    public static final ResourceKey<Registry<NinjaAction>> NINJA_ACTIONS_REGISTRY = createRegistryKey(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "ninja_skill"));

    public static final DeferredRegister<NinjaAction> NINJA_ACTIONS = DeferredRegister.create(NINJA_ACTIONS_REGISTRY, Cyninja.MODID);
    public static final DeferredHolder<NinjaAction, NinjaAction> NONE = NINJA_ACTIONS.register("none", () -> NinjaAction.Builder.newInstance().loop().build());

    public static final DeferredHolder<NinjaAction, NinjaAction> SLIDE = NINJA_ACTIONS.register("slide", () -> NinjaAction.Builder.newInstance()
            .addNeedCondition(livingEntity ->
                    !livingEntity.isInFluidType() &&
                    getActionData(livingEntity).getNinjaAction().value() == NinjaActions.NONE.value()
            )
            .addNeedCondition(EquipmentRequest.FULL_ARMOR::test)
            .setInput(NinjaInput.SNEAK, NinjaInput.SPRINT)
            .startAndEnd(0, 8)
            .speed(3F)
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
            .setCanJump(false)
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
                moveToLookingWay(slider,0.2F, NinjaActions.SLIDE);
            })
            .next(livingEntity -> {
                //壁にぶつかったら止まる
                if (!livingEntity.onGround() || livingEntity.horizontalCollision) {
                    return NONE;
                }
                return null;
            }).build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> WALL_SLIDE = NINJA_ACTIONS.register("wall_slide", () -> NinjaAction.Builder.newInstance()
            .addNeedCondition(livingEntity ->
                    !livingEntity.onGround() &&
                    livingEntity.horizontalCollision &&
                    !livingEntity.isInFluidType()
            )
            .addNeedCondition(EquipmentRequest.FULL_ARMOR::test)
            .loop()
            .startAndEnd(0, 1)
            .setNoBob(true)
            .addTickAction(NinjaActionUtils::checkWallSlide)
            .next(livingEntity -> livingEntity.onGround() || !livingEntity.horizontalCollision ? NONE : null)
            .priority(850)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> WALL_JUMP = NINJA_ACTIONS.register("wall_jump", () -> NinjaAction.Builder.newInstance()
            .setInput(NinjaInput.JUMP)
            .startAndEnd(0, 1)
            .nextOfTimeout(livingEntity -> NinjaActions.NONE)
            .addNeedCondition(livingEntity -> getActionData(livingEntity).canAirJump(livingEntity, NinjaActions.WALL_SLIDE.value()))
            .addStartAction(livingEntity -> {
                Vec3 delta = livingEntity.getDeltaMovement();
                livingEntity.setDeltaMovement(delta.x, 1F, delta.z);
                livingEntity.resetFallDistance();
                getActionData(livingEntity).resetAirJumpCount();
            })
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> HEAVY_AIR_JUMP = NINJA_ACTIONS.register("heavy_air_jump", () -> NinjaAction.Builder.newInstance()
            .setInput(NinjaInput.JUMP)
            .startAndEnd(0, 1)
            .addNeedCondition(NinjaActionUtils::canAirJump)
            .addNeedCondition(living -> NinjaActionUtils.isWearingNinjaTrim(living, Items.IRON_INGOT))
            .addTickAction(NinjaActionUtils::tickHeavyAirJump)
            .priority(900)
            .build()
    );

    public static final DeferredHolder<NinjaAction, NinjaAction> AIR_ROCKET = NINJA_ACTIONS.register("air_rocket", () -> NinjaAction.Builder.newInstance()
            .setInput(NinjaInput.JUMP)
            .startAndEnd(0, 8)
            .addNeedCondition(NinjaActionUtils::canAirJump)
            .addNeedCondition(living -> NinjaActionUtils.isWearingNinjaTrim(living, Items.GOLD_INGOT))
            .addStartAction(livingEntity -> {
                if (!livingEntity.level().isClientSide())
                    AnimationUtil.sendAnimation(livingEntity, ModAnimations.AIR_ROCKET);
                getActionData(livingEntity).decreaseAirJumpCount();
            })
            .addTickAction(NinjaActionUtils::tickAirRocket)
            .addStopAction(livingEntity -> {
                if (!livingEntity.level().isClientSide())
                    AnimationUtil.sendStopAnimation(livingEntity, ModAnimations.AIR_ROCKET);
            })
            .priority(900)
            .build()
    );


    public static final DeferredHolder<NinjaAction, NinjaAction> AIR_JUMP = NINJA_ACTIONS.register("air_jump", () -> NinjaAction.Builder.newInstance()
            .setInput(NinjaInput.JUMP)
            .startAndEnd(0, 1)
            .addNeedCondition(NinjaActionUtils::canAirJump)
            .addStartAction(NinjaActionUtils::doAirJump)
            .build()
    );

    public static final DeferredHolder<NinjaAction,NinjaAction> SPIN = NINJA_ACTIONS.register("spin", () -> NinjaAction.Builder.newInstance()
            .addNeedCondition(livingEntity -> !livingEntity.onGround())
            .addNeedCondition(EquipmentRequest.KATANA::test)
            .setInput(NinjaInput.LEFT_CLICK)
            .startAndEnd(2, 12)
            .setReduceDamage(1.0F)
            .setReduceKnockback(1.0F)
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