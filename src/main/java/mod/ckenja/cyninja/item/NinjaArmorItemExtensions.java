package mod.ckenja.cyninja.item;

import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class NinjaArmorItemExtensions implements IClientItemExtensions {
    private static final int WHITE = 16777215;

    @Override
    public int getDefaultDyeColor(ItemStack stack) {
        return stack.is(ItemTags.DYEABLE) ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, WHITE)) : 0xFFFFFFFF;
    }
}
