package mod.ckenja.cyninja.item;

import mod.ckenja.cyninja.entity.SickleEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ChainAndSickleItem extends Item {
    public ChainAndSickleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!isTooDamagedToUse(itemstack)) {
            itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.SNOWBALL_THROW,
                    SoundSource.NEUTRAL,
                    0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
            );
            if (!level.isClientSide) {
                SickleEntity sickle = new SickleEntity(level, player, itemstack.copy());
                sickle.setItem(itemstack.copy());
                sickle.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 1.0F);
                level.addFreshEntity(sickle);
                itemstack.shrink(1);
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }

        return InteractionResultHolder.fail(itemstack);
    }

    private static boolean isTooDamagedToUse(ItemStack p_353073_) {
        return p_353073_.getDamageValue() >= p_353073_.getMaxDamage() - 1;
    }

}
