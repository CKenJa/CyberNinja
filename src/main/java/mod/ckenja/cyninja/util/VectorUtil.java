package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.ninja_action.NinjaAction;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
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

    public static void moveToYRotLookingWay(LivingEntity livingEntity, float speed, Holder<NinjaAction> ninjaActionHolder) {
        moveRelativeActionY(livingEntity, speed, new Vec3(0, 0, ninjaActionHolder.value().getMoveSpeed()));
        livingEntity.hasImpulse = true;
    }

    public static void moveToYRotLookingWay(LivingEntity livingEntity, float speed) {
        moveRelativeActionY(livingEntity, speed, new Vec3(0, 0, 1.0F));
        livingEntity.hasImpulse = true;
    }

    public static void moveRelativeActionY(LivingEntity livingEntity, float p_19921_, Vec3 p_19922_) {
        Vec3 vec3 = getInputVector(p_19922_, p_19921_, NinjaActionUtils.getActionData(livingEntity).getActionYRot());
        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(vec3));
    }

    private static Vec3 getInputVector(Vec3 p_20016_, float p_20017_, float p_20018_) {
        double d0 = p_20016_.lengthSqr();
        if (d0 < 1.0E-7) {
            return Vec3.ZERO;
        } else {
            Vec3 vec3 = (d0 > 1.0 ? p_20016_.normalize() : p_20016_).scale((double) p_20017_);
            float f = Mth.sin(p_20018_ * (float) (Math.PI / 180.0));
            float f1 = Mth.cos(p_20018_ * (float) (Math.PI / 180.0));
            return new Vec3(vec3.x * (double) f1 - vec3.z * (double) f, vec3.y, vec3.z * (double) f1 + vec3.x * (double) f);
        }
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
