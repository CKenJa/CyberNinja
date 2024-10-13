package mod.ckenja.cyninja.registry;

import mod.ckenja.cyninja.Cyninja;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimPattern;

public class ModTrimPatterns {
    public static final ResourceKey<TrimPattern> CYBER_TRIM = registryKey("cyber");

    public static void bootstrap(BootstrapContext<TrimPattern> p_321567_) {
        register(p_321567_, ModItems.CYBER_TRIM_SMITHING_TEMPLATE.asItem(), CYBER_TRIM);
    }

    public static void register(BootstrapContext<TrimPattern> p_321509_, Item p_267097_, ResourceKey<TrimPattern> p_267079_) {
        TrimPattern trimpattern = new TrimPattern(p_267079_.location(), BuiltInRegistries.ITEM.wrapAsHolder(p_267097_), Component.translatable(Util.makeDescriptionId("trim_pattern", p_267079_.location())), false);
        p_321509_.register(p_267079_, trimpattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String p_266889_) {
        return ResourceKey.create(Registries.TRIM_PATTERN, ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, p_266889_));
    }
}
