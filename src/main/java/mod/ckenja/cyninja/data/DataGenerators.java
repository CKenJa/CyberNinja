package mod.ckenja.cyninja.data;

import mod.ckenja.cyninja.Cyninja;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Cyninja.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        DatapackBuiltinEntriesProvider datapackProvider = new RegistryDataGenerator(packOutput, event.getLookupProvider());

        CompletableFuture<HolderLookup.Provider> lookupProvider = datapackProvider.getRegistryProvider();
        generator.addProvider(event.includeServer(), datapackProvider);
        generator.addProvider(event.includeClient(), new ItemModelGenerator(packOutput, existingFileHelper));
        BlockTagsProvider blocktags = new BlockTagGenerator(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blocktags);
        generator.addProvider(event.includeServer(), new ItemTagGenerator(packOutput, lookupProvider, blocktags.contentsGetter(), existingFileHelper));
    }
}