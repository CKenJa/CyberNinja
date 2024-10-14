package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.ninja_action.NinjaAction;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class VectorUtil {
    public static void moveToLookingWay(LivingEntity livingEntity, float speed, Holder<NinjaAction> ninjaActionHolder) {
        livingEntity.moveRelative(speed, new Vec3(0, 0, ninjaActionHolder.value().getMoveSpeed()));
        livingEntity.hasImpulse = true;
    }

    public static void moveToLookingWay(LivingEntity livingEntity, float speed) {
        livingEntity.moveRelative(speed, new Vec3(0, 0, 1.0F));
        livingEntity.hasImpulse = true;
    }

    public static Vector3f movePartToVec(ModelPart part) {
        return new Vector3f(part.xRot, part.yRot, part.zRot);
    }

    public static void moveVecToPart(Vector3f vector3f, ModelPart part) {
        part.xRot = vector3f.x;
        part.yRot = vector3f.y;
        part.zRot = vector3f.z;
    }

    public static void moveVecToPartWithAdd(Vector3f vector3f, ModelPart part) {
        part.xRot += vector3f.x;
        part.yRot += vector3f.y;
        part.zRot += vector3f.z;
    }
}
