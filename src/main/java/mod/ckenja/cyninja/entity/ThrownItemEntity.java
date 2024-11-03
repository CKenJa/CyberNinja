package mod.ckenja.cyninja.entity;

import mod.ckenja.cyninja.registry.ModEntities;
import mod.ckenja.cyninja.registry.ModItems;
import mod.ckenja.cyninja.registry.NinjaActions;
import mod.ckenja.cyninja.util.NinjaActionUtils;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThrownItemEntity extends ThrowableItemProjectile {
    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownItemEntity(Level level, LivingEntity shooter) {
        super(ModEntities.THROWN_ITEM.get(), shooter, level);
    }

    public ThrownItemEntity(Level level, double x, double y, double z) {
        super(ModEntities.THROWN_ITEM.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItem();
        return (ParticleOptions) (!itemstack.isEmpty() && !itemstack.is(this.getDefaultItem())
                ? new ItemParticleOption(ParticleTypes.ITEM, itemstack)
                : ParticleTypes.ITEM_SNOWBALL);
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
            }
        }
        if (id == 4) {
            for (int i = 0; i < 16; i++) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), this.random.nextDouble() - this.random.nextDouble(), this.random.nextDouble() - this.random.nextDouble(), this.random.nextDouble() - this.random.nextDouble());
            }
        }
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

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.getItem().is(ModItems.SMOKE_BOMB)) {
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.5F);
        }

        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            if (this.getItem().is(ModItems.SMOKE_BOMB)) {
                List<Entity> entities = NinjaActionUtils.getEnemiesInSphere(this.level(), this.position(), 6);
                entities.forEach(entity -> {
                    if (entity instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
                    }

                    if (entity instanceof Mob living) {
                        living.setTarget(null);
                    }
                });

                this.level().broadcastEntityEvent(this, (byte) 4);
                if (this.getOwner() instanceof LivingEntity attacker && NinjaActionUtils.isSmokeBombTrim(this.getItem(), Items.QUARTZ)) {
                    NinjaActionUtils.setEntityWithSummonShadow(attacker, this.position(), new Vec3(-2.0F, 0.0, 0.0F), -30F, NinjaActions.NONE);
                    NinjaActionUtils.setEntityWithSummonShadow(attacker, this.position(), new Vec3(2.0F, 0.0, 0.0F), 30F, NinjaActions.NONE);
                }
            }
            this.discard();
        }
    }
}
