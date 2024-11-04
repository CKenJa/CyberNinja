package mod.ckenja.cyninja.util;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.entity.NinjaFaker;
import mod.ckenja.cyninja.item.KatanaItem;
import mod.ckenja.cyninja.item.NinjaArmorItem;
import mod.ckenja.cyninja.item.ShurikenItem;
import mod.ckenja.cyninja.item.SmokebombItem;
import mod.ckenja.cyninja.network.ResetFallServerPacket;
import mod.ckenja.cyninja.network.SetActionToServerPacket;
import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.ninja_action.NinjaActionAttachment;
import mod.ckenja.cyninja.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Predicate;

import static mod.ckenja.cyninja.registry.NinjaActions.NONE;
import static mod.ckenja.cyninja.util.VectorUtil.moveToLookingWay;

public class NinjaActionUtils {

    private static final ResourceLocation SLIDE_STEP_ID = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "slide_step");
    private static final ResourceLocation HEAVY_GRAVITY_ID = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "heavy_gravity");

    public static void doAirJump(LivingEntity livingEntity) {
        Vec3 delta = livingEntity.getDeltaMovement();
        //架空の床を踏んで飛ぶイメージなので、上向きの運動量に定数を設定する
        livingEntity.setDeltaMovement(delta.x, 0.6F, delta.z);
        livingEntity.hasImpulse = true;

        livingEntity.resetFallDistance();
        getActionData(livingEntity).decreaseAirJumpCount();
    }

    public static void mirrorImageDo(LivingEntity living) {
        NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, -180F, NinjaActions.SLIDE,20);
        if(NinjaActionUtils.isWearingNinjaTrim(living, Items.COPPER_INGOT)){
            NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, 120F, NinjaActions.SLIDE, 20);
            NinjaActionUtils.setEntityWithSummonShadow(living, living.position(), Vec3.ZERO, -120F, NinjaActions.SLIDE, 20);
        }

    }

    private static void knockback(Level p_335716_, LivingEntity attacker) {
        p_335716_.levelEvent(2013, attacker.getOnPos(), 750);
        p_335716_.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(3.5), knockbackPredicate(attacker))
                .forEach(p_347296_ -> {
                    Vec3 vec3 = p_347296_.position().subtract(attacker.position());
                    double d0 = getKnockbackPower(attacker, p_347296_, vec3);
                    Vec3 vec31 = vec3.normalize().scale(d0);
                    if (d0 > 0.0) {
                        p_347296_.push(vec31.x, 0.7F, vec31.z);
                        if (p_347296_ instanceof ServerPlayer serverplayer) {
                            serverplayer.connection.send(new ClientboundSetEntityMotionPacket(serverplayer));
                        }
                    }
                });
    }

    public static void resetEntitiesTarget(Level level, Vec3 position){
        List<Entity> entities = NinjaActionUtils.getEnemiesInSphere(level, position, 6);
        entities.forEach(entity -> {
            if (entity instanceof Mob mob)
                mob.setTarget(null);
        });

    }

    private static Predicate<LivingEntity> knockbackPredicate(LivingEntity owner) {
        return p_344407_ -> {
            boolean flag;
            boolean flag1;
            boolean flag2;
            boolean flag6;
            label62:
            {
                flag = !p_344407_.isSpectator();
                flag1 = p_344407_ != owner;
                flag2 = !owner.isAlliedTo(p_344407_);
                if (p_344407_ instanceof TamableAnimal tamableanimal && tamableanimal.isTame() && owner.getUUID().equals(tamableanimal.getOwnerUUID())) {
                    flag6 = true;
                    break label62;
                }

                flag6 = false;
            }

            boolean flag3;
            label55:
            {
                flag3 = !flag6;
                if (p_344407_ instanceof ArmorStand armorstand && armorstand.isMarker()) {
                    flag6 = false;
                    break label55;
                }

                flag6 = true;
            }

            boolean flag4 = flag6;
            boolean flag5 = owner.distanceToSqr(p_344407_) <= Math.pow(3.5, 2.0);
            return flag && flag1 && flag2 && flag3 && flag4 && flag5;
        };
    }

    private static double getKnockbackPower(LivingEntity owner, LivingEntity p_338630_, Vec3 p_338866_) {
        return (3.5 - p_338866_.length())
                * 0.7F
                * (double) (owner.fallDistance > 5.0F ? 2 : 1)
                * (1.0 - p_338630_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static void startHeavyFall(LivingEntity livingEntity) {
        NinjaActionUtils.getActionData(livingEntity).decreaseAirJumpCount();

        livingEntity.playSound(SoundEvents.SMITHING_TABLE_USE, 1.0F, 1.0F);
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.GRAVITY);
        if (attributeinstance != null && !attributeinstance.hasModifier(HEAVY_GRAVITY_ID)) {
            attributeinstance.addTransientModifier(new AttributeModifier(HEAVY_GRAVITY_ID, 0.08F, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void stopHeavyFall(LivingEntity livingEntity) {
        Level level = livingEntity.level();

        if (!level.isClientSide()) {
            knockback(level, livingEntity);
        }
        livingEntity.resetFallDistance();

        livingEntity.playSound(SoundEvents.ANVIL_PLACE, 1.0F, 1.5F);
        livingEntity.playSound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 1.25F);
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.GRAVITY);
        if (attributeinstance != null && !attributeinstance.hasModifier(HEAVY_GRAVITY_ID)) {
            attributeinstance.removeModifier(HEAVY_GRAVITY_ID);
        }
    }

    public static void tickHeavyFall(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        Vec3 look = livingEntity.getLookAngle();

        if (!level.isClientSide()) {
            List<Entity> list = level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(1.0F).move(look.reverse().scale(2.0F)));
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable() && !entity.isAlliedTo(livingEntity)) {
                        entity.hurt(livingEntity.damageSources().source(DamageTypes.MOB_ATTACK, livingEntity), 8F);
                    }
                }
            }
        }
    }

    public static void tickHeavyAirJump(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        Vec3 delta = livingEntity.getDeltaMovement();
        Vec3 look = livingEntity.getLookAngle();
        livingEntity.setDeltaMovement(delta.x, 0.6F, delta.z);
        livingEntity.moveRelative(0.6F, new Vec3(livingEntity.xxa, 0, livingEntity.zza));
        livingEntity.hasImpulse = true;

        NinjaActionUtils.getActionData(livingEntity).decreaseAirJumpCount();

        livingEntity.resetFallDistance();
        livingEntity.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value());

        if (!level.isClientSide) {
            List<Entity> list = level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(1.0F).move(look.reverse().scale(2.0F)));
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.isAttackable() && !entity.isAlliedTo(livingEntity)) {
                        entity.hurt(livingEntity.damageSources().source(DamageTypes.MOB_ATTACK, livingEntity), 8F);
                    }
                }
            }
        } else {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), delta.x * -2, delta.y * -2, delta.z * -2);
        }
    }

    public static void tickAirRocket(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        NinjaActionAttachment attachment = getActionData(livingEntity);
        Vec3 look = livingEntity.calculateViewVector(attachment.getActionXRot(), attachment.getActionYRot());

        livingEntity.setDeltaMovement(look.x * 0.8F, look.y * 0.8F, look.z * 0.8F);
        //livingEntity.push(look.scale(0.08F));
        livingEntity.hasImpulse = true;

        livingEntity.resetFallDistance();
        livingEntity.playSound(SoundEvents.WIND_CHARGE_BURST.value());

        if (level.isClientSide) {
            Vec3 delta = livingEntity.getDeltaMovement();

            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), delta.x * -2, delta.y * -2, delta.z * -2);
        }
    }

    public static void checkWallSlide(LivingEntity livingEntity) {
        Vec3 vec3 = livingEntity.getDeltaMovement();
        //slide to looking way
        if (vec3.y < 0.0F) {
            livingEntity.setDeltaMovement(vec3.x, vec3.y * 0.6F, vec3.z);
            if (livingEntity.level().isClientSide()) {
                PacketDistributor.sendToServer(new ResetFallServerPacket());
            }
            livingEntity.resetFallDistance();
            livingEntity.hasImpulse = true;
        }
    }

    public static void attackEntities(LivingEntity attacker, List<Entity> victims, float damage, float knockback, ResourceKey<DamageType> damageType) {
        for(Entity victim: victims){
            if (victim.isAttackable() && !victim.isAlliedTo(attacker)) {
                victim.hurt(attacker.damageSources().source(damageType, attacker), damage);
                if (victim instanceof LivingEntity livingVictim) {
                    double d0 = attacker.getX() - livingVictim.getX();
                    double d1 = attacker.getZ() - livingVictim.getZ();
                    livingVictim.knockback(knockback, d0, d1);
                }
                //これはノックバックしたときのみ必要なのか、それともダメージを受けた時も必要なのか
                attacker.hasImpulse = true;
            }
        }
    }


    public static void spawnSprintParticle(LivingEntity livingEntity) {
        BlockPos blockpos = livingEntity.getOnPosLegacy();
        Level level = livingEntity.level();

        BlockState blockstate = level.getBlockState(blockpos);
        if (!blockstate.addRunningEffects(level, blockpos, livingEntity))
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                Vec3 vec3 = livingEntity.getDeltaMovement();
                BlockPos blockpos1 = livingEntity.blockPosition();
                double d0 = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * (double) livingEntity.getDimensions(livingEntity.getPose()).width();
                double d1 = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * (double) livingEntity.getDimensions(livingEntity.getPose()).width();
                if (blockpos1.getX() != blockpos.getX()) {
                    d0 = Mth.clamp(d0, (double) blockpos.getX(), (double) blockpos.getX() + 1.0);
                }

                if (blockpos1.getZ() != blockpos.getZ()) {
                    d1 = Mth.clamp(d1, (double) blockpos.getZ(), (double) blockpos.getZ() + 1.0);
                }

                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), d0, livingEntity.getY() + 0.1, d1, vec3.x * -4.0, 1.5, vec3.z * -4.0);
            }
    }

    public static NinjaActionAttachment getActionData(LivingEntity livingEntity) {
        return livingEntity.getData(ModAttachments.NINJA_ACTION.get());
    }

    public static boolean isWearingFullNinjaSuit(LivingEntity livingEntity) {
        int i = 0;
        for (ItemStack itemstack : livingEntity.getArmorAndBodyArmorSlots()) {
            if ((itemstack.getItem() instanceof NinjaArmorItem)) {
                i++;
            }
        }

        return i >= 4;
    }

    public static boolean isKatanaTrim(ItemStack stack, Item item) {
        if ((stack.getItem() instanceof KatanaItem)) {
            ArmorTrim armorTrim = stack.get(DataComponents.TRIM);
            if (armorTrim != null && armorTrim.material().value().ingredient().value() == item) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSmokeBombTrim(ItemStack stack, Item item) {
        if ((stack.getItem() instanceof SmokebombItem)) {
            ArmorTrim armorTrim = stack.get(DataComponents.TRIM);
            if (armorTrim != null && armorTrim.material().value().ingredient().value() == item) {
                return true;
            }
        }

        return false;
    }

    public static boolean isShurikenBombtrim(ItemStack stack, Item item) {
        if ((stack.getItem() instanceof ShurikenItem)) {
            ArmorTrim armorTrim = stack.get(DataComponents.TRIM);
            if (armorTrim != null && armorTrim.material().value().ingredient().value() == item) {
                return true;
            }
        }

        return false;
    }

    public static boolean isWearingNinjaTrim(LivingEntity livingEntity, Item item) {
        for (ItemStack itemstack : livingEntity.getArmorAndBodyArmorSlots()) {
            if ((itemstack.getItem() instanceof NinjaArmorItem)) {
                ArmorTrim armorTrim = itemstack.get(DataComponents.TRIM);
                if (armorTrim != null && armorTrim.material().value().ingredient().value() == item) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWearingNinjaForWolf(Mob livingEntity) {
        if (!(livingEntity.getBodyArmorItem().getItem() instanceof NinjaArmorItem ninjaArmorItem)) {
            return false;
        }


        return true;
    }

    public static List<Entity> getEnemiesInSphere(Level level, Vec3 position, double r) {
        AABB aabb = new AABB(position.x-r,position.y-r,position.z-r,position.x+r,position.y+r,position.z+r);
        return level.getEntitiesOfClass(Entity.class,aabb).stream()
                .filter(entity -> entity.position().distanceTo(position) <= r)
                .toList();
    }

    public static boolean canAirJump(LivingEntity livingEntity) {
        return getActionData(livingEntity).canAirJump(livingEntity);
    }

    public static void syncAction(LivingEntity entity, Holder<NinjaAction> action) {
        getActionData(entity).syncAction(entity, action);
    }

    public static void setAction(LivingEntity entity, Holder<NinjaAction> action) {
        getActionData(entity).setAction(entity, action);
    }

    public static boolean isEquipKatana(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().is(ModItems.KATANA.asItem());
    }

    public static boolean setEntityWithSummonShadow(LivingEntity living, Vec3 pos, Vec3 offset, float yRot, Holder<NinjaAction> actionHolder) {
        return setEntityWithSummonShadow(living, pos, offset, yRot, actionHolder, 300);
    }

    public static boolean setEntityWithSummonShadow(LivingEntity living, Vec3 pos, Vec3 offset, float yRot, Holder<NinjaAction> actionHolder, int duration) {
        if (!(living instanceof NinjaFaker)) {
            CompoundTag compoundtag = new CompoundTag();
            living.saveWithoutId(compoundtag);
            if (!living.level().isClientSide()) {
                NinjaFaker faker = ModEntities.NINJA_FAKER.get().create(living.level());
                for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                    ItemStack itemstack = living.getItemBySlot(equipmentslot);
                    if (!itemstack.isEmpty()) {
                        faker.setItemSlot(equipmentslot, itemstack.copy());
                        faker.setDropChance(equipmentslot, 0.0F);
                    }
                }
                faker.readAdditionalSaveData(compoundtag);
                faker.setDataUuid(living.getUUID());
                faker.setXRot(living.getXRot());
                faker.setYRot(living.getYRot() + yRot);
                faker.setPos(pos.add(VectorUtil.getInputVector(offset, 0, faker.getYRot())));
                VectorUtil.moveRelativeActionY(faker, 0.5F, offset);
                faker.setOnGround(false);
                faker.setDuration(duration);

                if (living.level().addFreshEntity(faker)) {
                    syncAction(faker, actionHolder);

                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isEquipSickle(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().is(ModItems.CHAIN_SICKLE);
    }

    public static boolean isEquipSickleNotOnlySickle(LivingEntity livingEntity) {
        return isEquipSickle(livingEntity) && livingEntity.getMainHandItem().get(ModDataComponents.CHAIN_ONLY) == null;
    }

    public static boolean isEquipSickleOnlySickle(LivingEntity livingEntity) {
        return isEquipSickle(livingEntity) && livingEntity.getMainHandItem().get(ModDataComponents.CHAIN_ONLY) != null;
    }

    public static boolean isEquipKatanaTrim(LivingEntity livingEntity, Item item) {
        if (livingEntity.getMainHandItem().is(ModItems.KATANA)) {
            ArmorTrim armorTrim = livingEntity.getMainHandItem().get(DataComponents.TRIM);
            if (armorTrim != null && armorTrim.material().value().ingredient().value() == item) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEquipSickleTrim(LivingEntity livingEntity, Item item) {
        if (livingEntity.getMainHandItem().is(ModItems.CHAIN_SICKLE)) {
            if (livingEntity.getMainHandItem().get(ModDataComponents.CHAIN_ONLY) == null) {
                ArmorTrim armorTrim = livingEntity.getMainHandItem().get(DataComponents.TRIM);
                if (armorTrim != null && armorTrim.material().value().ingredient().value() == item) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean keyUp(LivingEntity livingEntity, NinjaInput input) {
        NinjaActionAttachment data = getActionData(livingEntity);
        return data.getPreviousInputs().contains(input) &&
                !data.getCurrentInputs().contains(input);
    }

    public static void startSlide(LivingEntity livingEntity) {
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.STEP_HEIGHT);
        if (attributeinstance != null && !attributeinstance.hasModifier(SLIDE_STEP_ID)) {
            livingEntity.getAttribute(Attributes.STEP_HEIGHT).addTransientModifier(new AttributeModifier(SLIDE_STEP_ID, 0.5F, AttributeModifier.Operation.ADD_VALUE));
        }
        getActionData(livingEntity).setActionYRot(livingEntity.yHeadRot);

        getActionData(livingEntity).decreaseAirSlideCount();
        Vec3 vec3 = livingEntity.getDeltaMovement();
        livingEntity.setDeltaMovement(vec3.x, 0, vec3.z);
        livingEntity.resetFallDistance();
        moveToLookingWay(livingEntity, 1F, NinjaActions.SLIDE);
    }

    public static void stopSlide(LivingEntity livingEntity) {
        AttributeInstance attributeinstance = livingEntity.getAttribute(Attributes.STEP_HEIGHT);
        if (attributeinstance != null) {
            attributeinstance.removeModifier(SLIDE_STEP_ID);
        }
    }

    public static Holder<NinjaAction> nextSlide(LivingEntity livingEntity) {
            NinjaActionAttachment attachment = getActionData(livingEntity);
            //壁にぶつかったら止まる。そして減速
            if (livingEntity.horizontalCollision) {
                Vec3 delta = livingEntity.getDeltaMovement();
                livingEntity.setDeltaMovement(delta.x * 0.45F, delta.y, delta.z * 0.45F);
                return NONE;
            }
            // jumpで止まる
            if (livingEntity.level().isClientSide && attachment.getCurrentInputs() != null) {
                //sneakを押してなければnone
                if (!attachment.getCurrentInputs().contains(NinjaInput.SNEAK)) {
                    if (livingEntity.level().isClientSide()) {
                        PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
                    }
                    return NONE;
                }

                if (attachment.getCurrentInputs().contains(NinjaInput.JUMP) && livingEntity.onGround()) {
                    if (livingEntity.level().isClientSide()) {
                        PacketDistributor.sendToServer(new SetActionToServerPacket(NONE));
                    }
                    return NONE;
                }
            }
            // 一定時間経過かつ減速で止まる
            if (livingEntity.getDeltaMovement().horizontalDistance() < 0.2F) {
                return NONE;
            }
            return null;
    }
}
