package mod.ckenja.cyninja.registry;

import net.minecraft.world.entity.raid.Raid;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class RaiderEnumParams {
    @SuppressWarnings("unused")
    public static final EnumProxy<Raid.RaiderType> CYBER_ILLAGER = new EnumProxy<>(
            Raid.RaiderType.class, ModEntities.CYBER_ILLAGER, new int[]{0, 0, 0, 1, 1, 1, 2, 2}
    );
}