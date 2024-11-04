package mod.ckenja.cyninja.entity;

import mod.ckenja.cyninja.registry.ModEntities;
import mod.ckenja.cyninja.registry.ModItems;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class SmokeBombEntity extends ThrowableItemProjectile {
    private boolean smoking = false;
    private int smoking_time = 0;
    private boolean inGround = false;

    public SmokeBombEntity(EntityType<? extends SmokeBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SmokeBombEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SMOKE_BOMB.get(), shooter, level);
    }

    public SmokeBombEntity(Level level, double x, double y, double z) {
        super(ModEntities.SMOKE_BOMB.get(), x, y, z, level);
    }


    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        int i = 3;
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), (float) i);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SMOKE_BOMB.value();
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItem();
        return !itemstack.isEmpty()
                ? new ItemParticleOption(ParticleTypes.ITEM, itemstack)
                : ParticleTypes.ITEM_SNOWBALL;
    }

    /**
     * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {

        if (id == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                smoking = true;
            }
        }
        if (id == 4) {
            inGround = true;
        }
    }

    public void tick() {
        if (!inGround) {
            super.tick();
        }
        if (!smoking) {
            return;
        }

        if (level().isClientSide) {
            for (int i = 0; i < 64; i++) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), (this.random.nextDouble() - this.random.nextDouble()) * 0.4, (this.random.nextDouble() - this.random.nextDouble()) * 0.4, (this.random.nextDouble() - this.random.nextDouble()) * 0.4);
            }
        } else {
            List<Entity> entities = NinjaActionUtils.getEnemiesInSphere(this.level(), this.position(), 6);
            entities.forEach(entity -> {
                if (entity instanceof LivingEntity living)
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40));

                if (entity instanceof Mob mob)
                    mob.setTarget(null);
            });

            if (this.getOwner() instanceof LivingEntity attacker && NinjaActionUtils.isSmokeBombTrim(this.getItem(), Items.LAPIS_LAZULI)) {
                entities.forEach(entity -> {
                    if (entity instanceof LivingEntity living)
                        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600));
                });
            }

            if (this.getOwner() instanceof LivingEntity attacker && NinjaActionUtils.isSmokeBombTrim(this.getItem(), Items.EMERALD)) {
                Random random = new Random();
                List<LivingEntity> livings = entities.stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .map(entity -> (LivingEntity)entity)
                                .toList();
                livings.forEach(entity -> {
                    if (entity instanceof Mob mob) {
                        LivingEntity target = livings.get(random.nextInt(livings.size()));
                        if (mob != target) {
                            mob.setTarget(target);
                        }
                    }
                });
            }

            smoking_time++;
            if (10 < smoking_time) {
                if (this.getOwner() instanceof LivingEntity attacker && NinjaActionUtils.isSmokeBombTrim(this.getItem(), Items.QUARTZ)) {
                    NinjaActionUtils.setEntityWithSummonShadow(attacker, this.position(), new Vec3(-2.0F, 0.0, 0.0F), -30F, NinjaActions.NONE);
                    NinjaActionUtils.setEntityWithSummonShadow(attacker, this.position(), new Vec3(2.0F, 0.0, 0.0F), 30F, NinjaActions.NONE);
                }
                discard();
            }
        }
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.5F);

        if (!this.level().isClientSide) {
            smoking = true;
            this.level().broadcastEntityEvent(this, (byte) 3);
            if (result instanceof BlockHitResult) {
                this.level().broadcastEntityEvent(this, (byte) 4);
                inGround = true;
            }
        }
        if (result instanceof BlockHitResult) {
            Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
            this.setDeltaMovement(vec3);
            Vec3 vec31 = vec3.normalize().scale(0.05F);
            this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);

            this.setDeltaMovement(0, getDeltaMovement().y,0);
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("InGround", inGround);
        nbt.putBoolean("Smoking", smoking);
        nbt.putInt("SmokingTime", smoking_time);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        inGround = (nbt.getBoolean("InGround"));
        smoking = (nbt.getBoolean("Smoking"));
        smoking_time = (nbt.getInt("SmokingTime"));
    }
}
