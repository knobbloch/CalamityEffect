package com.example.CalamityEffectHat;


import com.google.common.base.Predicate;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class FlyingAttack extends EntityAIBase
{
    private final EntityLiving entity;
    private final Predicate<EntityLiving> followPredicate;
    private final double speedModifier;
    private final PathNavigate navigation;
    private int timeToRecalcPath;
	Path path;
	double speedTowardsTarget;
	protected int attackTick;

    public FlyingAttack(final EntityLiving p_i47417_1_, double p_i47417_2_)
    {	
        this.entity = p_i47417_1_;
        this.followPredicate = new Predicate<EntityLiving>()
        {
            public boolean apply(@Nullable EntityLiving p_apply_1_)
            {
                return p_apply_1_ != null && p_i47417_1_.getClass() != p_apply_1_.getClass();
            }
        };
        this.speedModifier = p_i47417_2_;
        this.navigation = p_i47417_1_.getNavigator();
        this.setMutexBits(3);

        if (!(p_i47417_1_.getNavigator() instanceof PathNavigateGround) && !(p_i47417_1_.getNavigator() instanceof PathNavigateFlying))
        {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
        this.speedTowardsTarget = p_i47417_2_;
    }

    public boolean shouldExecute()
    {	
    	//ExampleMod.logger.info("shouldExecute" + this.entity + this.entity.getAttackTarget() != null/*targetHatclass.testtarget(this.entity.getAttackTarget())*/);
    	if (targetHatclass.testtarget(this.entity.getAttackTarget())) {
    		//ExampleMod.logger.info("shouldExecute" + this.entity + this.entity.getNavigator().getPath());
    		
    		this.path = this.entity.getNavigator().getPathToEntityLiving(this.entity.getAttackTarget());
    	}
    	return targetHatclass.testtarget(this.entity.getAttackTarget());
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
       //if (this.entity.getNavigator().getPath() == null)
    	   //CalamityEffectHat.logger.info("shouldContinueExecuting" +  this.entity + this.entity.getNavigator().getPath() + ( !this.navigation.noPath() && targetHatclass.testtarget(this.entity.getAttackTarget())));
       EntityLivingBase entitylivingbase = this.entity.getAttackTarget();
       if (!targetHatclass.testtarget(this.entity.getAttackTarget())) {
    	   return false;
       }
       else if (entitylivingbase == null)
	   {
	       return false;
	   }
	   else if (!entitylivingbase.isEntityAlive())
	   {
	       return false;
	   }
	   else if (!(this.entity instanceof EntityCreature && ((EntityCreature)this.entity).isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))))
	   {
	       return false;
	   }
	   else
	   {
	       return !(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer)entitylivingbase).isSpectator() && !((EntityPlayer)entitylivingbase).isCreative();
	   }
    	
    	//return  !this.navigation.noPath() && targetHatclass.testtarget(this.entity.getAttackTarget());
    }
    public void startExecuting()
    {	
    	if (this.entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
    		this.entity.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    	}
    	this.entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.timeToRecalcPath = 0;     
        this.entity.getNavigator().setPath(this.path, this.speedTowardsTarget);
        //CalamityEffectHat.logger.info("startExecuting" + this.entity + this.entity.getNavigator().getPath() + this.path + this.speedTowardsTarget);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {	
    	this.entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0D);
    	//CalamityEffectHat.logger.info("resetTask"+  this.entity + this.entity.getNavigator().getPath());
        this.navigation.clearPath();
        this.entity.setAttackTarget((EntityLivingBase)null);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
    	double p0 = this.entity.getDistanceSq(this.entity.getAttackTarget().posX, this.entity.getAttackTarget().getEntityBoundingBox().minY, this.entity.getAttackTarget().posZ);
        if (this.entity.getAttackTarget() != null)// && !this.entity.getLeashed()
        {
            this.entity.getLookHelper().setLookPositionWithEntity(this.entity.getAttackTarget(), 10.0F, (float)this.entity.getVerticalFaceSpeed());

            if (--this.timeToRecalcPath <= 0)
            {
                this.timeToRecalcPath = 0;//10;
                double d0 = this.entity.posX - this.entity.getAttackTarget().posX;
                double d1 = this.entity.posY - this.entity.getAttackTarget().posY;
                double d2 = this.entity.posZ - this.entity.getAttackTarget().posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                this.navigation.tryMoveToEntityLiving(this.entity.getAttackTarget(), this.speedModifier);
                //CalamityEffectHat.logger.info("updateTask"+  this.entity + this.entity.getNavigator().getPath());
            }
        }
        this.attackTick = Math.max(this.attackTick - 1, 0);
        this.checkAndPerformAttack(this.entity.getAttackTarget(), p0);
    }
    protected void checkAndPerformAttack(EntityLivingBase p_190102_1_, double p_190102_2_)
    {	
    	float f = (float)entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int i = 0;
        double d0 = this.getAttackReachSqr(p_190102_1_);

        if (p_190102_2_ <= d0 && this.attackTick <= 0)
        {
            attackTick = 20;
            boolean k;
            k = p_190102_1_.attackEntityFrom(DamageSource.causeMobDamage(entity), 1.0F);
            if (p_190102_1_ instanceof EntityLivingBase)
            {
                f += EnchantmentHelper.getModifierForCreature(entity.getHeldItemMainhand(), ((EntityLivingBase)p_190102_1_).getCreatureAttribute());
                i += EnchantmentHelper.getKnockbackModifier(entity);
            }
            if (k)
            {
                if (i > 0 && p_190102_1_ instanceof EntityLivingBase)
                {
                    ((EntityLivingBase)p_190102_1_).knockBack(entity, (float)i * 0.5F, (double)MathHelper.sin(entity.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(entity.rotationYaw * 0.017453292F)));
                    entity.motionX *= 0.6D;
                    entity.motionZ *= 0.6D;
                }

                int j = EnchantmentHelper.getFireAspectModifier(entity);

                if (j > 0)
                {
                	p_190102_1_.setFire(j * 4);
                }
                //CalamityEffectHat.logger.info("DIRT BLOCK02 >> {}" + entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()  + k);
            }	
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget)
    {
        return (double)(this.entity.width * 2.0F * this.entity.width * 2.0F + attackTarget.width);
    }
}