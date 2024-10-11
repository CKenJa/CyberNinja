package mod.ckenja.cyninja;

import mod.ckenja.cyninja.item.data.NinjaActionData;
import mod.ckenja.cyninja.ninja_skill.NinjaAction;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Cyninja.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void scaleEvent(EntityEvent.Size event) {
        //If Player's inventory is null. don't check
        if (event.getEntity() instanceof Player player && player.getInventory() != null) {
            Holder<NinjaAction> ninjaAction = NinjaActionUtils.getAction(player);
            if (ninjaAction != null && ninjaAction.value() != NinjaActions.NONE.value() && ninjaAction.value().getHitBox().isPresent()) {
                event.setNewSize(ninjaAction.value().getHitBox().get());
            }
        }
    }

    @SubscribeEvent
    public static void tickEvent(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            //basic action handle
            NinjaActionData actionData = NinjaActionUtils.getActionData(livingEntity);
            if (actionData != null) {
                actionData.tick(livingEntity);
                if (!actionData.ninjaActionHolder().value().isLoop()) {
                    if (!actionData.isActionStop()) {
                        NinjaActionUtils.setActionData(livingEntity, actionData.setActionTick(actionData.actionTick() + 1));
                    } else {
                        NinjaActionUtils.setActionData(livingEntity, actionData.setAction(actionData.ninjaActionHolder().value().getNextOfTimeout().apply(livingEntity)));
                    }
                }
            }
        }
    }
}
