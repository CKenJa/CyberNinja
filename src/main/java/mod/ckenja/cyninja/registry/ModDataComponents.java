package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.item.data.NinjaActionData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Cyninja.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<NinjaActionData>> NINJA_ACTION_DATA = register(
            "ninja_action_data",
            p_341845_ -> p_341845_.persistent(NinjaActionData.CODEC).networkSynchronized(NinjaActionData.DIRECT_STREAM_CODEC).cacheEncoding()
    );

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String p_332092_, UnaryOperator<DataComponentType.Builder<T>> p_331261_) {
        return DATA_COMPONENT_TYPES.register(p_332092_, () -> p_331261_.apply(DataComponentType.builder()).build());
    }
}
