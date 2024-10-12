package mod.ckenja.cyninja.registry;

import bagu_chan.bagus_lib.util.client.AnimationUtil;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.attachment.NinjaActionAttachment;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static net.minecraft.resources.ResourceKey.createRegistryKey;

@EventBusSubscriber(modid = Cyninja.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NinjaActions {
    public static final ResourceKey<Registry<NinjaAction>> NINJA_ACTIONS_REGISTRY = createRegistryKey(ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "ninja_skill"));

    public static final DeferredRegister<NinjaAction> NINJA_ACTIONS = DeferredRegister.create(NINJA_ACTIONS_REGISTRY, Cyninja.MODID);
    public static final DeferredHolder<NinjaAction, NinjaAction> NONE = NINJA_ACTIONS.register("none", () -> new NinjaAction(NinjaAction.Builder.newInstance().loop()));

    public static final DeferredHolder<NinjaAction, NinjaAction> SLIDE = NINJA_ACTIONS.register("slide", () -> new NinjaAction(NinjaAction.Builder.newInstance().startAndEnd(0, 15).speed(1.5F).next(livingEntity -> {
        if (!livingEntity.onGround() || livingEntity.horizontalCollision) {
            return NONE;
        }

        return null;
    }).setNeedCondition(livingEntity -> {
        return livingEntity.onGround() && livingEntity.isSprinting() && NinjaActionUtils.getActionData(livingEntity).getNinjaAction().value() == NinjaActions.NONE.value();
    }).setReduceDamage(1.0F).setReduceKnockback(1.0F).setCanJump(false).setHitBox(EntityDimensions.scalable(0.6F, 0.6F)).addTickAction(NinjaActionUtils::checkSlideAttack)));

    public static final DeferredHolder<NinjaAction, NinjaAction> JUMP = NINJA_ACTIONS.register("jump", () -> new NinjaAction(NinjaAction.Builder.newInstance().startAndEnd(0, 10000).next(livingEntity -> {
        if (livingEntity.onGround()) {
            return NONE;
        }

        return null;
    }).setNeedCondition(livingEntity -> {
        NinjaActionAttachment attachment = NinjaActionUtils.getActionData(livingEntity);

        return !livingEntity.onGround() && !livingEntity.isInWater() && attachment.getNinjaAction().value() == NinjaActions.NONE.value();
    })));

    public static final DeferredHolder<NinjaAction, NinjaAction> AIR_JUMP = NINJA_ACTIONS.register("air_jump", () -> new NinjaAction(NinjaAction.Builder.newInstance().startAndEnd(0, 1).nextOfTimeout(livingEntity -> {
        return NinjaActions.AIR_JUMP_FINISH;
    }).setNeedCondition(livingEntity -> {
        NinjaActionAttachment attachment = NinjaActionUtils.getActionData(livingEntity);
        return !livingEntity.onGround() && attachment.getActionTick() >= 3 && attachment.getNinjaAction().value() == NinjaActions.JUMP.value()
                && (!(livingEntity instanceof Player player) || !player.getAbilities().flying);
    }).addTickAction(NinjaActionUtils::tickAirJump).addStartAction(livingEntity -> {
        if (!livingEntity.level().isClientSide()) {
            AnimationUtil.sendAnimation(livingEntity, ModAnimations.AIR_JUMP);
        }
    })));


    public static final DeferredHolder<NinjaAction, NinjaAction> AIR_JUMP_FINISH = NINJA_ACTIONS.register("air_jump_finish", () -> new NinjaAction(NinjaAction.Builder.newInstance().loop().next(livingEntity -> {
        if (livingEntity.onGround()) {
            return NONE;
        }

        if (livingEntity instanceof Player player && player.getAbilities().flying) {
            return NONE;
        }

        return null;
    }).addStopAction(livingEntity -> {
        if (!livingEntity.level().isClientSide()) {
            AnimationUtil.sendStopAnimation(livingEntity, ModAnimations.AIR_JUMP);
        }
    })));


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