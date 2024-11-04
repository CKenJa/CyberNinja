package mod.ckenja.cyninja.entity;

import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class SickleEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> ATTACH = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Float> CHAIN_LENGTH = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.FLOAT);

    @Nullable
    private ItemStack firedFromWeapon = null;

    private int flyTick;

    private static final EntityDataAccessor<Holder<NinjaAction>> NINJA_ACTION = SynchedEntityData.defineId(SickleEntity.class, ModEntityDataSerializer.NINJA_ACTION.get());

    public SickleEntity(EntityType<? extends SickleEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SickleEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SICKLE.get(), shooter, level);
    }

    public SickleEntity(Level level, LivingEntity shooter, ItemStack stack) {
        this(level, shooter);
        if (stack != null && level instanceof ServerLevel) {
            if (stack.isEmpty()) {
                throw new IllegalArgumentException("Invalid weapon firing an sickle");
            }
            setAttach(true);
            firedFromWeapon = stack.copy();
            setChainLength(8);
        }
    }

    public SickleEntity(Level level, double x, double y, double z) {
        super(ModEntities.SICKLE.get(), x, y, z, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACH, false);
        builder.define(RETURNING, false);
        builder.define(IN_GROUND, false);
        builder.define(CHAIN_LENGTH, 8f);
        builder.define(NINJA_ACTION, registryAccess().lookupOrThrow(NinjaActions.NINJA_ACTIONS_REGISTRY).getOrThrow(NinjaActions.SICKLE_ATTACK.getKey()));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CHAIN_SICKLE.asItem();
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = getItem();
        return !itemstack.isEmpty() && !itemstack.is(getDefaultItem())
                ? new ItemParticleOption(ParticleTypes.ITEM, itemstack)
                : new ItemParticleOption(ParticleTypes.ITEM, getDefaultItem().getDefaultInstance());
    }

    /**
     * Called when the sickle hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        Entity owner = getOwner();
        if (entity != owner && !isReturning() && !level().isClientSide) {
            LivingEntity livingentity = owner instanceof LivingEntity ? (LivingEntity) owner : null;
            double damage = 6;
            DamageSource damagesource = damageSources().mobProjectile(this, livingentity);

            if (getWeaponItem() != null && level() instanceof ServerLevel serverlevel) {
                damage = EnchantmentHelper.modifyDamage(serverlevel, getWeaponItem(), entity, damagesource, (float) damage);
            }
            if (entity.hurt(damagesource, (float) damage)) {
                if (entity instanceof LivingEntity hurtEntity) {
                    if (level() instanceof ServerLevel serverlevel1) {
                        EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel1, hurtEntity, damagesource, getWeaponItem());
                    }
                }
            } else {
                deflect(ProjectileDeflection.REVERSE, entity, getOwner(), false);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (canAttach()) {
            setInGround(true);
            /*if(shouldFall()){
                Cyninja.LOGGER.debug("WHY SHOULD FALL");
            }*/
        }
        if (!isReturning() && !isInGround() && !level().isClientSide) {
            Vec3 vec3 = result.getLocation().subtract(getX(), getY(), getZ());
            setDeltaMovement(vec3);
            Vec3 vec31 = vec3.normalize().scale(0.05F);
            setPosRaw(getX() - vec31.x, getY() - vec31.y, getZ() - vec31.z);
            if(!canAttach()) {
                BlockPos pos = result.getBlockPos();
                BlockState state = level().getBlockState(pos);
                SoundType soundType = state.getSoundType(level(), pos, this);
                level().playSound(null, getX(), getY(), getZ(), soundType.getHitSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
                //setReturning(true);
                //反射
                Vec3i hitNormal = result.getDirection().getNormal();
                this.setDeltaMovement(getDeltaMovement().multiply(Math.abs(hitNormal.getX()) < 0.08 ? -1 : 1, Math.abs(hitNormal.getX()) < 0.08 ? -1 : 1 , Math.abs(hitNormal.getX()) < 0.08 ? -1 : 1));
            }
        }
    }

    //From abstractArrow.tick()
    @Override
    public void tick() {
        Entity owner = getOwner();
        flyTick++;

        //inGroundの更新
        //Entity.moveと相性が悪いので一時削除
        /*if (shouldFall() && !level().isClientSide)
            setInGround(false);*/

        if (!isInGround() && canAttach() && !isReturning() && !level().isClientSide) {
            BlockPos blockpos = blockPosition();
            BlockState blockstate = level().getBlockState(blockpos);
            if (!blockstate.isAir()) {
                VoxelShape voxelshape = blockstate.getCollisionShape(level(), blockpos);
                if (!voxelshape.isEmpty()) {
                    for (AABB aabb : voxelshape.toAabbs()) {
                        if (aabb.move(blockpos).contains(position())) {
                            setInGround(true);
                            break;
                        }
                    }
                }
            }
        }

        //使用者関連の処理
        if (owner != null && owner.isAlive()) {
            if (
                    !isReturning() &&
                    owner instanceof LivingEntity living &&
                    living.getMainHandItem().get(ModDataComponents.CHAIN_ONLY) == this.getUUID() &&
                    !level().isClientSide &&
                    !isInGround()//ないとフックが外れる
            ){
                setDeltaMovement(getDeltaMovement().add(getControlForce(owner)));
                hasImpulse = true;
            }
            if (isInGround() && canAttach()) {
                double pullPower = 0.2;
                owner.setDeltaMovement(owner.getDeltaMovement().add(getOwnerToSickle(owner).normalize().scale(pullPower)));
            }
        } else if (isInGround()) {
            setReturning(true);
        }

        //Returningモードの処理
        if (isReturning() && owner != null && !level().isClientSide) {
            if (!shouldReturnToThrower()) {
                 discard();
            } else {
                setAttach(false);
                noPhysics = true;
                setChainLength(Math.min(getChainLength() - 2f,(float) getOwnerToSickle(owner).length()));
            }
        }

        if (!isInGround() && !level().isClientSide) {
            //パーティクル
            if (isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    Vec3 vec3 = this.getDeltaMovement();
                    double dx = vec3.x;
                    double dy = vec3.y;
                    double dz = vec3.z;

                    double posX = this.getX() + dx;
                    double posY = this.getY() + dy;
                    double posZ = this.getZ() + dz;
                    level().addParticle(ParticleTypes.BUBBLE, posX - dx * 0.25D, posY - dy * 0.25D, posZ - dz * 0.25D, dx, dy, dz);
                }
            }

            //位置の更新
            updateRotation();

            if (!isReturning() && !isInGround()) {
                if (getDeltaMovement().length() < 1)
                    applyGravity();
                setDeltaMovement(getDeltaMovement().scale(isInWater() ? getWaterInertia() : 0.99F));
            }

            /*Vec3 vec3 = this.getDeltaMovement();
            double d0 = vec3.x;
            double d1 = vec3.y;
            double d2 = vec3.z;
            if (Math.abs(vec3.x) < 0.003) {
                d0 = 0.0;
            }

            if (Math.abs(vec3.y) < 0.003) {
                d1 = 0.0;
            }

            if (Math.abs(vec3.z) < 0.003) {
                d2 = 0.0;
            }
            this.setDeltaMovement(d0, d1, d2);*/


            if (owner != null && owner.isAlive() && !level().isClientSide)
                fixDeltaMovementWithChain(owner);
        }
        //衝突処理
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitresult) && !level().isClientSide)
            onHit(hitresult);
        /*else if (level().noCollision(getBoundingBox().inflate(0.01d)) && !level().isClientSide){
            Cyninja.LOGGER.debug("t");
        }*/



        if (!isInGround() && !level().isClientSide) {
            move(MoverType.SELF,getDeltaMovement());
            //setPos(position().add(getDeltaMovement()));

            checkInsideBlocks();
        }
    }

    private void fixDeltaMovementWithChain(Entity owner) {
        Vec3 ownerPos = getThrowPosition(owner);
        Vec3 tmpPos = position().add(getDeltaMovement());
        Vec3 relativePos = tmpPos.subtract(ownerPos);
        double currentLength = relativePos.length();
        if (currentLength > getChainLength()){
            Vec3 newPos = ownerPos.add(relativePos.normalize().scale((getChainLength())));
            setDeltaMovement(newPos.subtract(position()));
            hasImpulse = true;
        }
    }

    public Vec3 getControlForce(Entity owner) {
        double controlForceScale = 0.2;

        //常にlengthは1
        Vec3 lookAngle = owner.getLookAngle();

        Vec3 rotationAxis = lookAngle.cross(getOwnerToSickle(owner).normalize());
        // 視線と鎖鎌の位置が10度以内なら動かさない
        if(rotationAxis.length() < Math.sin(Math.toRadians(10)))
            return Vec3.ZERO;

        //常にlengthは1
        Vec3 goalVec = rotationAxis.cross(lookAngle);
        return goalVec.scale(-controlForceScale);
    }

    private Vec3 getOwnerToSickle(Entity owner) {
        return position().subtract(getThrowPosition(owner));
    }

    private Vec3 getThrowPosition(Entity owner) {
        return owner.getEyePosition().add(0,-0.1f,0);
    }

    private boolean shouldFall() {
        return isInGround() && level().noCollision(getBoundingBox().inflate(0.0001d));
    }

    private boolean shouldReturnToThrower() {
        Entity owner = getOwner();
        if (owner != null && owner.isAlive())
            return (distanceToSqr(owner) > 3 && !owner.isSpectator());
        return false;
    }

    private class HasSickleItem implements Predicate<ItemStack> {
        private final UUID uuid;
        private HasSickleItem(UUID uuid){
            this.uuid = uuid;
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStack.get(ModDataComponents.CHAIN_ONLY) == this.uuid;
        }
    }

    @Override
    public void playerTouch(Player entityIn) {
        super.playerTouch(entityIn);
        if (flyTick >= 10 && entityIn == getOwner() && !(canAttach() && !isReturning() && entityIn.getInventory().contains(new HasSickleItem(getUUID())))) {
            if (!level().isClientSide) {
                discard();
            }
        }
    }

    @Override
    public ItemStack getWeaponItem() {
        return firedFromWeapon;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Attach", canAttach());
        nbt.putBoolean("Returning", isReturning());
        nbt.putBoolean("InGround", isInGround());
        nbt.putString("variant", getNinjaAction().unwrapKey().orElse(NinjaActions.SICKLE_ATTACK.getKey()).location().toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setAttach(nbt.getBoolean("Attach"));
        setReturning(nbt.getBoolean("Returning"));
        setInGround(nbt.getBoolean("InGround"));
        Optional.ofNullable(ResourceLocation.tryParse(nbt.getString("ninja_action")))
                .map(location -> ResourceKey.create(NinjaActions.NINJA_ACTIONS_REGISTRY, location))
                .flatMap(key -> registryAccess().lookupOrThrow(NinjaActions.NINJA_ACTIONS_REGISTRY).get(key))
                .ifPresent(this::setNinjaAction);
    }

    public boolean canAttach() {
        return entityData.get(ATTACH);
    }

    public void setAttach(boolean attach) {
        entityData.set(ATTACH, attach);
    }

    public void setReturning(boolean returning) {
        entityData.set(RETURNING, returning);
    }

    public boolean isReturning() {
        return entityData.get(RETURNING);
    }

    public void setInGround(boolean inGround) {
        entityData.set(IN_GROUND, inGround);
    }

    public boolean isInGround() {
        return entityData.get(IN_GROUND);
    }

    public float getChainLength() {
        return entityData.get(CHAIN_LENGTH);
    }

    public void setChainLength(float length) {
        entityData.set(CHAIN_LENGTH, Math.max(0f, length));
    }

    public void setNinjaAction(Holder<NinjaAction> ninjaAction) {
        entityData.set(NINJA_ACTION, ninjaAction);
    }

    public Holder<NinjaAction> getNinjaAction() {
        return entityData.get(NINJA_ACTION);
    }

    protected float getWaterInertia() {
        return 0.6F;
    }
}
