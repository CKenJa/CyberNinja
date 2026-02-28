package mod.ckenja.cyninja.infrastructure.attachment;

import mod.ckenja.cyninja.core.util.NinjaInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.EnumSet;

public class InputAttachment implements INBTSerializable<CompoundTag> {


    private EnumSet<NinjaInput> previousInputs = EnumSet.noneOf(NinjaInput.class);
    private EnumSet<NinjaInput> currentInputs = EnumSet.noneOf(NinjaInput.class);

    public void update(ClientTickEvent.Pre event) {
        previousInputs = currentInputs;
        currentInputs = EnumSet.noneOf(NinjaInput.class);
        Options options = Minecraft.getInstance().options;
        if (options.keyShift.isDown())
            currentInputs.add(NinjaInput.SNEAK);
        if (options.keyJump.isDown())
            currentInputs.add(NinjaInput.JUMP);
        if (options.keySprint.isDown())
            currentInputs.add(NinjaInput.SPRINT);
        if (options.keyUse.isDown())
            currentInputs.add(NinjaInput.LEFT_CLICK);
    }

    public boolean keyUp(NinjaInput input) {
        return previousInputs.contains(input) &&
                !currentInputs.contains(input);
    }

    public boolean keyDown(NinjaInput input) {
        return !previousInputs.contains(input) &&
                currentInputs.contains(input);
    }

    public EnumSet<NinjaInput> getCurrentInputs() {
        return currentInputs;
    }

    public EnumSet<NinjaInput> getPreviousInputs() {
        return previousInputs;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

    }
}
