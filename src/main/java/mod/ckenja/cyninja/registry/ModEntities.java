package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.entity.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(modid = Cyninja.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES_REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Cyninja.MODID);

    public static final Supplier<EntityType<CyberIllager>> CYBER_ILLAGER = ENTITIES_REGISTRY.register("cyber_illager", () -> EntityType.Builder.of(CyberIllager::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build(prefix("cyber_illager")));
    public static final Supplier<EntityType<NinjaFaker>> NINJA_FAKER = ENTITIES_REGISTRY.register("ninja_faker", () -> EntityType.Builder.of(NinjaFaker::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build(prefix("ninja_faker")));

    public static final Supplier<EntityType<ThrownItemEntity>> THROWN_ITEM = ENTITIES_REGISTRY.register("thrown_item", () -> EntityType.Builder.<ThrownItemEntity>of(ThrownItemEntity::new, MobCategory.MISC).sized(0.3F, 0.3F).clientTrackingRange(8).setUpdateInterval(20).build(prefix("thrown_item")));
    public static final Supplier<EntityType<SmokeBombEntity>> SMOKE_BOMB = ENTITIES_REGISTRY.register("smoke_bomb", () -> EntityType.Builder.<SmokeBombEntity>of(SmokeBombEntity::new, MobCategory.MISC).sized(0.3F, 0.3F).clientTrackingRange(8).setUpdateInterval(20).build(prefix("smoke_bomb")));
    public static final Supplier<EntityType<SickleEntity>> SICKLE = ENTITIES_REGISTRY.register("sickle", () -> EntityType.Builder.<SickleEntity>of(SickleEntity::new, MobCategory.MISC).sized(0.3F, 0.3F).clientTrackingRange(8).setUpdateInterval(1).build(prefix("sickle")));

    private static String prefix(String path) {
        return Cyninja.MODID + "." + path;
    }

    @SubscribeEvent
    public static void registerEntity(EntityAttributeCreationEvent event) {
        event.put(CYBER_ILLAGER.get(), CyberIllager.createAttributes().build());
        event.put(NINJA_FAKER.get(), NinjaFaker.createMonsterAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacement(RegisterSpawnPlacementsEvent event) {
        event.register(CYBER_ILLAGER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}