package mod.ckenja.cyninja.core.action;

import com.google.common.collect.Maps;
import mod.ckenja.cyninja.infrastructure.registry.ModItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import java.util.Collection;
import java.util.Map;

public class EquipmentCondition {
    private final EquipmentSlotGroup equipmentSlotGroup;
    private final Item trim;
    private final TagKey<Item> tag;

    public static final EquipmentCondition isNinjaFullSuit = new EquipmentCondition(EquipmentSlotGroup.ARMOR, ModItemTags.NINJA_ARMOR);

    public EquipmentCondition(EquipmentSlotGroup equipmentSlot, Item trim) {
        this.equipmentSlotGroup = equipmentSlot;
        this.trim = trim;
        this.tag = null;
    }

    public EquipmentCondition(EquipmentSlotGroup equipmentSlot, TagKey<Item> tag) {
        this.equipmentSlotGroup = equipmentSlot;
        this.tag = tag;
        this.trim = null;
    }

    public EquipmentCondition(EquipmentSlotGroup equipmentSlot, TagKey<Item> tag, Item trim) {
        this.equipmentSlotGroup = equipmentSlot;
        this.trim = trim;
        this.tag = tag;
    }

    public boolean test(LivingEntity livingEntity) {
        Collection<ItemStack> items = getSlotItems(livingEntity).values();
        return items.stream().allMatch(this::tagCheck) && items.stream().anyMatch(this::trimCheck);
    }

    private boolean trimCheck(ItemStack itemStack) {
        if(trim == null)
            return true;
        ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
        return armorTrim != null && armorTrim.material().value().ingredient().value() == trim;
    }

    private boolean tagCheck(ItemStack itemStack) {
        if(tag == null)
            return true;
        return itemStack.is(tag);
    }

    //From net.minecraft.world.enchantment.Enchantments
    public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity p_44685_) {
        Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);

        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (matchingSlot(equipmentslot)) {
                ItemStack itemstack = p_44685_.getItemBySlot(equipmentslot);
                if (!itemstack.isEmpty()) {
                    map.put(equipmentslot, itemstack);
                }
            }
        }

        return map;
    }

    public boolean matchingSlot(EquipmentSlot slot) {
        return this.equipmentSlotGroup.test(slot);
    }
}