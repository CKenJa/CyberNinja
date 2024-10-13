package mod.ckenja.cyninja.util;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public enum EquipmentRequest {
    FULL_ARMOR(NinjaActionUtils::isWearingNinja),
    //KATANA(entity-> entity.getMainHandItem().is(Items.DIAMOND_SWORD)),
    ;
    private final Predicate<LivingEntity> condition;
    EquipmentRequest(Predicate<LivingEntity> condition){
        this.condition = condition;
    }
    public boolean test(LivingEntity entity){
        return condition.test(entity);
    }
}
