package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModEntityDataSerializer {

    public static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Cyninja.MODID);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Holder<NinjaAction>>> NINJA_ACTION = DATA_SERIALIZERS.register("ninja_action", () -> EntityDataSerializer.forValueType(ByteBufCodecs.holderRegistry(NinjaActions.NINJA_ACTIONS_REGISTRY)));
}