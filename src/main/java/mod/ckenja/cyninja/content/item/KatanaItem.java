package mod.ckenja.cyninja.content.item;

import mod.ckenja.cyninja.core.action.EquipmentCondition;
import mod.ckenja.cyninja.infrastructure.registry.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class KatanaItem extends Item {
    public KatanaItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttackBlock(BlockState p_43291_, Level p_43292_, BlockPos p_43293_, Player p_43294_) {
        return !p_43294_.isCreative();
    }

    @Override
    public float getAttackDamageBonus(Entity enemy, float damage, DamageSource source){
        ItemStack item = source.getWeaponItem();
        if(item == null || !(source.getDirectEntity() instanceof LivingEntity attacker))
            return 0;

        if (new EquipmentCondition(EquipmentSlotGroup.HAND, ModItemTags.KATANA, Items.COPPER_INGOT).test(attacker) && enemy instanceof Mob mob && mob.getTarget() != attacker) {
            return (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        if (new EquipmentCondition(EquipmentSlotGroup.HAND, ModItemTags.KATANA, Items.DIAMOND).test(attacker)) {
            return (float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * attacker.getDeltaMovement().length());
        }
        return 0;
    }

    @Override
    public boolean hurtEnemy(ItemStack p_43278_, LivingEntity enemy, LivingEntity attacker) {
        if (new EquipmentCondition(EquipmentSlotGroup.HAND, ModItemTags.KATANA, Items.REDSTONE).test(attacker)) {
            if (!enemy.isAlive()) {
                attacker.heal(2);
            }
        }
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack p_345553_, LivingEntity enemy, LivingEntity player) {
        p_345553_.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return net.neoforged.neoforge.common.ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }
}
