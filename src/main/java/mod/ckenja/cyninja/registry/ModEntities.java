package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.entity.ThrownItemEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(modid = Cyninja.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES_REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Cyninja.MODID);

    public static final Supplier<EntityType<ThrownItemEntity>> THROWN_ITEM = ENTITIES_REGISTRY.register("thrown_item", () -> EntityType.Builder.<ThrownItemEntity>of(ThrownItemEntity::new, MobCategory.MISC).sized(0.3F, 0.3F).build(prefix("thrown_item")));

    private static String prefix(String path) {
        return Cyninja.MODID + "." + path;
    }

    @SubscribeEvent
    public static void registerEntity(EntityAttributeCreationEvent event) {
    }

    @SubscribeEvent
    public static void registerSpawnPlacement(RegisterSpawnPlacementsEvent event) {
    }
}