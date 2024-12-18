package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cyninja.MODID);

    public static final Supplier<CreativeModeTab> CYBER_NINJA = CREATIVE_MODE_TABS.register("cyber_ninja", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .title(Component.translatable("itemGroup." + Cyninja.MODID + ".main_tab"))
            .icon(() -> ModItems.SHURIKEN.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.acceptAll(Stream.of(
                        ModItems.NINJA_HELMET,
                        ModItems.NINJA_CHESTPLATE,
                        ModItems.NINJA_LEGGINGS,
                        ModItems.NINJA_BOOTS,
                        ModItems.CYBER_TRIM_SMITHING_TEMPLATE,
                        ModItems.SHURIKEN,
                                ModItems.SMOKE_BOMB,
                                ModItems.KATANA,
                                ModItems.CHAIN_SICKLE,
                                ModItems.CYBER_NINJA_SPAWN_EGG
                ).map(sup ->
                        sup.get().getDefaultInstance()).toList()
                );
            }).build());

}
