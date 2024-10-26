package mod.ckenja.cyninja.item;

import mod.ckenja.cyninja.entity.SickleEntity;
import mod.ckenja.cyninja.registry.ModDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class ChainAndSickleItem extends Item {
    public ChainAndSickleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean isSelected) {
        if (!level.isClientSide() && stack.get(ModDataComponents.CHAIN_ONLY) != null && this.getThrownEntity(level, stack) == null) {
            stack.remove(ModDataComponents.CHAIN_ONLY);
        }
    }


    @Nullable
    private SickleEntity getThrownEntity(Level level, ItemStack stack) {
        if (level instanceof ServerLevel server) {
            UUID id = stack.get(ModDataComponents.CHAIN_ONLY);
            if (id != null) {
                Entity e = server.getEntity(id);
                if (e instanceof SickleEntity) {
                    return (SickleEntity) e;
                }
            }
        }

        return null;
    }

    private static boolean isTooDamagedToUse(ItemStack p_353073_) {
        return p_353073_.getDamageValue() >= p_353073_.getMaxDamage() - 1;
    }

}
