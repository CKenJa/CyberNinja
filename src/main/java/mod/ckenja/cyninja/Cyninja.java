package mod.ckenja.cyninja;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Cyninja.MODID)
public class Cyninja
{
    public static final String MODID = "cyninja";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Cyninja(IEventBus modEventBus, ModContainer modContainer)
    {
    }
}
