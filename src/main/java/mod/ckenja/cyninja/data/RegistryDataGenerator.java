package mod.ckenja.cyninja.data;

import mod.ckenja.cyninja.Cyninja;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistryDataGenerator extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();


    public RegistryDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of("minecraft", Cyninja.MODID));
    }
}