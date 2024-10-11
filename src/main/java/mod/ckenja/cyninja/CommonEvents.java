package mod.ckenja.cyninja;

import mod.ckenja.cyninja.ninja_skill.NinjaAction;
import mod.ckenja.cyninja.util.NinjaAttackUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;

@EventBusSubscriber(modid = Cyninja.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void scaleEvent(EntityEvent.Size event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            Holder<NinjaAction> ninjaAction = NinjaAttackUtils.getAction(livingEntity);
            if (ninjaAction != null && ninjaAction.value().getHitBox().isPresent()) {
                event.setNewSize(ninjaAction.value().getHitBox().get());
            }
        }
    }
}
