package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.ninja_action.NinjaAction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class VectorUtil {
    public static void moveToLookingWay(LivingEntity livingEntity, float speed, Holder<NinjaAction> ninjaActionHolder) {
        livingEntity.moveRelative(speed, new Vec3(0, 0, ninjaActionHolder.value().getMoveSpeed()));

    }

    public static void moveToLookingWay(LivingEntity livingEntity, float speed) {
        livingEntity.moveRelative(speed, new Vec3(0, 0, 1.0F));

    }
}
