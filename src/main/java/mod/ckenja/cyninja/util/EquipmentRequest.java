package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public enum EquipmentRequest {
    FULL_ARMOR(NinjaActionUtils::isWearingNinja),
    KATANA(entity-> entity.getMainHandItem().is(ModItems.KATANA)),
    ;
    private final Predicate<LivingEntity> condition;

    EquipmentRequest(Predicate<LivingEntity> condition){
        this.condition = condition;
    }

    public boolean test(LivingEntity entity){
        return condition.test(entity);
    }
}
