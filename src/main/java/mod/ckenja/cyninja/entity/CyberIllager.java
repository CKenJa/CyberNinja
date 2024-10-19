package mod.ckenja.cyninja.entity;

import mod.ckenja.cyninja.entity.goal.JumpGoal;
import mod.ckenja.cyninja.entity.goal.SlideGoal;
import mod.ckenja.cyninja.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class CyberIllager extends AbstractIllager {
    public CyberIllager(EntityType<? extends CyberIllager> p_32105_, Level p_32106_) {
        super(p_32105_, p_32106_);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new SlideGoal(this, UniformInt.of(200, 300)));
        this.goalSelector.addGoal(5, new JumpGoal(this, UniformInt.of(200, 600)));
        this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.0F, true));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false).setUnseenMemoryTicks(300));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 25.0).add(Attributes.MAX_HEALTH, 32.0);
    }


    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_32921_, DifficultyInstance p_32922_, MobSpawnType p_32923_, @Nullable SpawnGroupData p_32924_) {
        this.populateDefaultEquipmentSlots(p_32921_.getRandom(), p_32922_);
        this.populateDefaultEquipmentEnchantments(p_32921_, p_32921_.getRandom(), p_32922_);
        return super.finalizeSpawn(p_32921_, p_32922_, p_32923_, p_32924_);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource p_217055_, DifficultyInstance p_217056_) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KATANA.asItem()));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.NINJA_HELMET.asItem()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NINJA_CHESTPLATE.asItem()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.NINJA_LEGGINGS.asItem()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.NINJA_BOOTS.asItem()));
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_) {

    }

    @Override
    protected @org.jetbrains.annotations.Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33034_) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }
}
