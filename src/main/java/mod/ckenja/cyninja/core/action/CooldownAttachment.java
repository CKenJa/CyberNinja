package mod.ckenja.cyninja.core.action;

import mod.ckenja.cyninja.infrastructure.registry.ActionRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CooldownAttachment implements INBTSerializable<CompoundTag> {
    private final Map<Action, Integer> cooldownMap = new HashMap<>();

    public void update() {
        cooldownMap.replaceAll((key,value)->value -1);
        cooldownMap.entrySet().removeIf(entry ->entry.getValue()<=0);
    }

    public void setCooldown(Action action) {
        cooldownMap.putIfAbsent(action, action.getStartCondition().getCooldown());
    }

    public boolean isCooldownFinished(Action action) {
        return !cooldownMap.containsKey(action);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        cooldownMap.forEach((action, cooldown) -> {
            ResourceLocation actionKey = ActionRegistry.getRegistry().getKey(action);
            if (actionKey != null) {
                nbt.putInt(actionKey.toString(), cooldown);
            }
        });
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("Cooldown")) {
            CompoundTag cooldownTag = nbt.getCompound("Cooldown");
            cooldownTag.getAllKeys().forEach(key -> {
                Optional<Holder.Reference<Action>> action = ActionRegistry.getRegistry().getHolder(ResourceLocation.parse(key));
                action.ifPresent(actionReference -> cooldownMap.put(actionReference.value(), cooldownTag.getInt(key)));
            });
        }
    }
}
