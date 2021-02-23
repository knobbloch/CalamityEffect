package com.example.CalamityEffectHat;

import net.minecraft.entity.EntityLivingBase;

import com.google.common.collect.Multimap;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class AttackMobPassive extends EntityAIBase {

	public AttackMobPassive(EntityLiving creature, double speedIn) {
		this.attacker = creature;
        this.world = creature.world;
        this.speedTowardsTarget = speedIn;
        //this.longMemory = useLongMemory;
        this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() 
	{       
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

	    if (entitylivingbase == null)
	    {
	    	return false;
	    }
	    else if (!entitylivingbase.isEntityAlive())
	    {
	    	return false;
	    }
	    else
	    {
	    	this.path = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
            if (this.path != null)
            {
                return true;
            }
	        else
	        {
	        	return this.getAttackReachSqr(entitylivingbase) >= this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ)&& targetHatclass.testtarget(entitylivingbase);
	        }
	        }	
	}
    
	@Override
	public boolean shouldContinueExecuting() {
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isEntityAlive())
        {
            return false;
        }
        else if (!(this.attacker instanceof EntityCreature && ((EntityCreature)this.attacker).isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))))
        {
            return false;
        }
        else
        {
            return !(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer)entitylivingbase).isSpectator() && !((EntityPlayer)entitylivingbase).isCreative()&& targetHatclass.testtarget(entitylivingbase);
        }
	}
	
    World world;
    protected EntityLiving attacker;
    protected int attackTick;
    double speedTowardsTarget;
    Path path;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    protected final int attackInterval = 20;
    private int failedPathFindingPenalty = 0;
    private double oldAttackDamage;

    public void startExecuting()
    {		
    	if (this.attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
    		this.attacker.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    		oldAttackDamage = 0;
    	}
    	else {
    		oldAttackDamage = this.attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
    		this.attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
    		}
    	//CalamityEffectHat.logger.info("AttackMobPassive:startExecuting "+ attacker);
        this.attacker.getNavigator().setPath(this.path, this.speedTowardsTarget);
        this.delayCounter = 0;
    }

    public void resetTask()
    {
    	EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
    	this.attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(oldAttackDamage);
        if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative()))
        {
            this.attacker.setAttackTarget((EntityLivingBase)null);
        }

        this.attacker.getNavigator().clearPath();
        //CalamityEffectHat.logger.info("AttackMobPassive:resetTask"+ attacker);
    }

    public void updateTask()
    {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        --this.delayCounter;

        if (this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entitylivingbase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.attacker.getRNG().nextFloat() < 0.05F))
        {
            this.targetX = entitylivingbase.posX;
            this.targetY = entitylivingbase.getEntityBoundingBox().minY;
            this.targetZ = entitylivingbase.posZ;
            this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);

            if (d0 > 1024.0D)
            {
                this.delayCounter += 10;
            }
            else if (d0 > 256.0D)
            {
                this.delayCounter += 5;
            }

            if (!this.attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget))
            {
                this.delayCounter += 15;
            }
        }

        this.attackTick = Math.max(this.attackTick - 1, 0);
        this.checkAndPerformAttack(entitylivingbase, d0);
    }

    protected void checkAndPerformAttack(EntityLivingBase p_190102_1_, double p_190102_2_)
    {	
    	float f = (float)attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int i = 0;
        double d0 = this.getAttackReachSqr(p_190102_1_);

        if (p_190102_2_ <= d0 && this.attackTick <= 0)
        {
            attackTick = 20;
            boolean k;
            k = p_190102_1_.attackEntityFrom(DamageSource.causeMobDamage(attacker), 1.0F);
            if (p_190102_1_ instanceof EntityLivingBase)
            {
                f += EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), ((EntityLivingBase)p_190102_1_).getCreatureAttribute());
                i += EnchantmentHelper.getKnockbackModifier(attacker);
            }
            if (k)
            {
                if (i > 0 && p_190102_1_ instanceof EntityLivingBase)
                {
                    ((EntityLivingBase)p_190102_1_).knockBack(attacker, (float)i * 0.5F, (double)MathHelper.sin(attacker.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(attacker.rotationYaw * 0.017453292F)));
                    attacker.motionX *= 0.6D;
                    attacker.motionZ *= 0.6D;
                }

                int j = EnchantmentHelper.getFireAspectModifier(attacker);

                if (j > 0)
                {
                	p_190102_1_.setFire(j * 4);
                }
                //CalamityEffectHat.logger.info("DIRT BLOCK02 >> {}" + attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()  + k);
            }	
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget)
    {
        return (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F + attackTarget.width);
    }
}
