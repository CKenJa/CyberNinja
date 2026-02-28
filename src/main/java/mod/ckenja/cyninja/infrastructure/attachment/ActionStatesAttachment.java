package mod.ckenja.cyninja.infrastructure.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class ActionStatesAttachment implements INBTSerializable<CompoundTag> {
    private int airJumpCount;
    private int airSlideCount;

    private float actionXRot;
    private float actionYRot;

    public void tick(LivingEntity user) {
        if (user.onGround()) {
            resetAirJumpCount();
            resetAirSlideCount();
        }
    }

    public void resetAirJumpCount() {
        airJumpCount = 1;
    }

    public void decreaseAirJumpCount() {
        airJumpCount--;
    }

    public boolean canAirJump() {
        return airJumpCount > 0;
    }

    public void resetAirSlideCount() {
        airSlideCount = 1;
    }

    public void decreaseAirSlideCount() {
        airSlideCount--;
    }

    public boolean canAirSlideCount() {
        return airSlideCount > 0;
    }


    public float getActionXRot() {
        return actionXRot;
    }

    public void setActionXRot(float actionXRot) {
        this.actionXRot = actionXRot;
    }

    public float getActionYRot() {
        return actionYRot;
    }

    public void setActionYRot(float actionYRot) {
        this.actionYRot = actionYRot;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

    }
}