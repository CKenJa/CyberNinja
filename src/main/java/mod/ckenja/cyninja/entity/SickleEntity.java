package mod.ckenja.cyninja.entity;

import mod.ckenja.cyninja.registry.ModEntities;
import mod.ckenja.cyninja.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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

public class SickleEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> ATTACH = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);
    @Nullable
    private ItemStack firedFromWeapon = null;

    private int flyTick;

    public SickleEntity(EntityType<? extends SickleEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SickleEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SICKLE.get(), shooter, level);
    }

    public SickleEntity(Level level, LivingEntity shooter, ItemStack stack) {
        this(level, shooter);
        if (stack != null && level instanceof ServerLevel serverlevel) {
            if (stack.isEmpty()) {
                throw new IllegalArgumentException("Invalid weapon firing an arrow");
            }
            this.setAttach(true);
            this.firedFromWeapon = stack.copy();
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
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CHAIN_SICKLE.asItem();
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItem();
        return (ParticleOptions) (!itemstack.isEmpty() && !itemstack.is(this.getDefaultItem())
                ? new ItemParticleOption(ParticleTypes.ITEM, itemstack)
                : new ItemParticleOption(ParticleTypes.ITEM, this.getDefaultItem().getDefaultInstance()));
    }

    /**
     * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
        }
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        Entity shooter = getOwner();
        if (result.getEntity() != getOwner() && !this.isReturning() && this.canAttach()) {
            LivingEntity livingentity = shooter instanceof LivingEntity ? (LivingEntity) shooter : null;
            double d0 = 6;
            DamageSource damagesource = this.damageSources().mobProjectile(this, livingentity);

            if (this.getWeaponItem() != null && this.level() instanceof ServerLevel serverlevel) {
                d0 = (double) EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), entity, damagesource, (float) d0);
            }
            if (result.getEntity().hurt(damagesource, (float) d0)) {
                if (entity instanceof LivingEntity hurtEntity) {
                    if (this.level() instanceof ServerLevel serverlevel1) {
                        EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel1, hurtEntity, damagesource, this.getWeaponItem());
                    }
                }
            } else {
                this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
                this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            }

        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos pos = result.getBlockPos();
        BlockState state = this.level().getBlockState(pos);
        SoundType soundType = state.getSoundType(this.level(), pos, this);
        if (!isReturning()) {
            if (!this.canAttach()) {
                this.level().playSound(null, getX(), getY(), getZ(), soundType.getHitSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
                this.setReturning(true);
            } else {
                Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
                this.setDeltaMovement(vec3);
                this.setInGround(true);
                Vec3 vec31 = vec3.normalize().scale((double) 0.05F);
                this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.flyTick++;

        Entity entity = getOwner();

        if (!this.isInGround() && !isReturning()) {
            if (this.flyTick >= 60 && entity != null) {
                setReturning(true);
            }
        }

        if (this.shouldFall()) {
            this.setInGround(false);
        }

        if (!this.isInGround() && this.canAttach() && !this.isReturning()) {
            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (!blockstate.isAir()) {
                VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
                if (!voxelshape.isEmpty()) {
                    Vec3 vec31 = this.position();

                    for (AABB aabb : voxelshape.toAabbs()) {
                        if (aabb.move(blockpos).contains(vec31)) {
                            this.setInGround(true);
                            break;
                        }
                    }
                }
            }
        }

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if (hitresult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }

        this.checkInsideBlocks();

        if (entity != null && entity.isAlive()) {
            if (this.isInGround() && canAttach()) {
                Vec3 vec3d3 = new Vec3(getX() - entity.getX(), getY() - entity.getEyeY(), getZ() - entity.getZ());
                double d0 = 0.2;
                entity.setDeltaMovement(entity.getDeltaMovement().scale(0.95D).add(vec3d3.normalize().scale(d0)));

            }
            if (entity.isShiftKeyDown()) {
                setReturning(true);
                setAttach(false);
                this.setInGround(false);
            }
        } else if (this.isInGround()) {
            this.setReturning(true);
        }

        if (entity != null && !shouldReturnToThrower() && isReturning()) {
            drop(getX(), getY(), getZ());
        } else if (entity != null && isReturning()) {
            if (this.canAttach()) {
                this.setAttach(false);
            }
            this.noPhysics = true;
            Vec3 vec3d3 = new Vec3(entity.getX() - getX(), entity.getEyeY() - getY(), entity.getZ() - getZ());
            double d0 = 0.3;
            this.setDeltaMovement(getDeltaMovement().scale(0.95D).add(vec3d3.normalize().scale(d0)));
        }

        Vec3 vec3 = this.getDeltaMovement();
        double d5 = vec3.x;
        double d6 = vec3.y;
        double d1 = vec3.z;


        double d7 = this.getX() + d5;
        double d2 = this.getY() + d6;
        double d3 = this.getZ() + d1;
        this.updateRotation();
        float f = 0.99F;
        float f1 = 0.05F;
        if (this.isInWater()) {
            for (int j = 0; j < 4; ++j) {
                float f2 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25D, d2 - d6 * 0.25D, d3 - d1 * 0.25D, d5, d6, d1);
            }

            f = this.getWaterInertia();
        }
        if (this.isReturning()) {
            this.setDeltaMovement(vec3.scale((double) f));
        }
        if (!this.isNoGravity() && !this.isReturning() && !this.isInGround()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double) this.getGravity(), vec34.z);
        }

        this.setPos(d7, d2, d3);

        this.checkInsideBlocks();
    }

    private boolean shouldFall() {
        return this.isInGround() && this.level().noCollision(this.getBoundingBox().inflate(0.001D));
    }

    private boolean shouldReturnToThrower() {
        Entity entity = getOwner();
        if (entity != null && entity.isAlive())
            return (this.distanceToSqr(entity) > 3 && !entity.isSpectator());
        return false;
    }

    @Override
    public void playerTouch(Player entityIn) {
        super.playerTouch(entityIn);
        if (this.flyTick >= 10 && entityIn == getOwner()) {
            drop(getOwner().getX(), getOwner().getY(), getOwner().getZ());
        }
    }

    public void drop(double x, double y, double z) {
        if (!this.level().isClientSide) {
            this.level().addFreshEntity(new ItemEntity(this.level(), x, y, z, getItem().split(1)));
            this.discard();
        }
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.firedFromWeapon;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Attach", this.canAttach());
        nbt.putBoolean("Returning", this.isReturning());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setAttach(nbt.getBoolean("Attach"));
        this.setReturning(nbt.getBoolean("Returning"));
    }

    public boolean canAttach() {
        return this.entityData.get(ATTACH);
    }

    protected void setAttach(boolean attach) {
        this.entityData.set(ATTACH, attach);
    }

    public void setReturning(boolean returning) {
        this.entityData.set(RETURNING, returning);
    }

    public boolean isReturning() {
        return this.entityData.get(RETURNING);
    }

    public void setInGround(boolean inGround) {
        this.entityData.set(IN_GROUND, inGround);
    }

    public boolean isInGround() {
        return this.entityData.get(IN_GROUND);
    }

    protected float getWaterInertia() {
        return 0.6F;
    }

}
