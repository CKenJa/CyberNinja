package mod.ckenja.cyninja.entity;

import mod.ckenja.cyninja.ninja_action.NinjaAction;
import mod.ckenja.cyninja.registry.ModEntities;
import mod.ckenja.cyninja.registry.ModEntityDataSerializer;
import mod.ckenja.cyninja.registry.ModItems;
import mod.ckenja.cyninja.registry.NinjaActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public class SickleEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> ATTACH = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(SickleEntity.class, EntityDataSerializers.BOOLEAN);
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
        builder.define(NINJA_ACTION, this.registryAccess().lookupOrThrow(NinjaActions.NINJA_ACTIONS_REGISTRY).getOrThrow(NinjaActions.SICKLE_ATTACK.getKey()));
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
        if (this.getOwner() instanceof LivingEntity owner) {
            this.getNinjaAction().value().hitAction(this, result);
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.getOwner() instanceof LivingEntity owner) {
            this.getNinjaAction().value().hitAction(this, result);
        }

    }

    @Override
    public void tick() {
        if (!this.isInGround()) {
            this.flyTick++;
        }

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
            if (!this.level().isClientSide) {
                this.discard();
            }
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
            if (!this.level().isClientSide) {
                this.discard();
            }
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
        nbt.putString("variant", this.getNinjaAction().unwrapKey().orElse(NinjaActions.SICKLE_ATTACK.getKey()).location().toString());

    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setAttach(nbt.getBoolean("Attach"));
        this.setReturning(nbt.getBoolean("Returning"));
        Optional.ofNullable(ResourceLocation.tryParse(nbt.getString("ninja_action")))
                .map(location -> ResourceKey.create(NinjaActions.NINJA_ACTIONS_REGISTRY, location))
                .flatMap(key -> this.registryAccess().lookupOrThrow(NinjaActions.NINJA_ACTIONS_REGISTRY).get(key))
                .ifPresent(this::setNinjaAction);
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


    public void setNinjaAction(Holder<NinjaAction> ninjaAction) {
        this.entityData.set(NINJA_ACTION, ninjaAction);
    }

    public Holder<NinjaAction> getNinjaAction() {
        return this.entityData.get(NINJA_ACTION);
    }


    protected float getWaterInertia() {
        return 0.6F;
    }

}
