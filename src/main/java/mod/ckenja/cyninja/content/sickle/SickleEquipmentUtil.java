package mod.ckenja.cyninja.content.sickle;

import mod.ckenja.cyninja.infrastructure.registry.ModDataComponents;
import mod.ckenja.cyninja.infrastructure.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;

public class SickleEquipmentUtil {

    private static boolean isEquipSickle(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().is(ModItems.CHAIN_SICKLE);
    }

    public static boolean isEquipSickleNotOnlySickle(LivingEntity livingEntity) {
        return isEquipSickle(livingEntity) && livingEntity.getMainHandItem().get(ModDataComponents.CHAIN_ONLY) == null;
    }

    public static boolean isEquipSickleOnlySickle(LivingEntity livingEntity) {
        return isEquipSickle(livingEntity) && livingEntity.getMainHandItem().get(ModDataComponents.CHAIN_ONLY) != null;
    }
}