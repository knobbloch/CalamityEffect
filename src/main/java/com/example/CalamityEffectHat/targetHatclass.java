package com.example.CalamityEffectHat;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.ai.EntityAIBase;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class targetHatclass extends EntityAIBase {
	
	int k; 
    protected final EntityAINearestAttackableTarget.Sorter sorter;
	protected final Predicate <? super EntityLivingBase > targetEntitySelector;
	EntityLivingBase targetEntity;
	protected final EntityLiving taskOwner;
	protected boolean shouldCheckSight;
	//private final boolean nearbyOnly;
	private int targetUnseenTicks;
	private int targetSearchStatus;
    private int targetSearchDelay;
    //protected int unseenMemoryTicks;
    protected EntityLivingBase target;
    protected double oldFollowRange;

	public targetHatclass(EntityLiving entityLivingBaseIn, boolean checkSight) {
		{
	        //this.unseenMemoryTicks = 60;
	        this.taskOwner = entityLivingBaseIn;
	        this.shouldCheckSight = checkSight;
	        //this.nearbyOnly = false;
		}
		sorter = new EntityAINearestAttackableTarget.Sorter(entityLivingBaseIn);
        targetEntitySelector = new Predicate<EntityLivingBase>()
        {
            public boolean apply(@Nullable EntityLivingBase player)
            {
                if (player != null)
                	if (EntitySelectors.NOT_SPECTATING.apply(player) && (!checkSight || taskOwner.getEntitySenses().canSee(player))&& isSuitableTarget(player, checkSight)/*isSuitableTarget(player, false)*/) {
	                	return testtarget(player);
                	}
                return false;
            }
        };
	}

	@Override
	public boolean shouldExecute() {
		if (CalamityEffectHat.entitywithhatHashSet.isEmpty()) 
    		return false;
		Iterator<EntityLivingBase> it = CalamityEffectHat.entitywithhatHashSet.iterator();
		List<EntityLivingBase> list = Lists.<EntityLivingBase>newArrayList();
		while (it.hasNext()) {
			EntityLivingBase e = it.next();
			if (isSuitableTarget(e, false))
				list.add(e);
		}
		Collections.sort(list, sorter);
		if (list.size() > 0) {
			targetEntity = list.get(0);
        }
		else {
			return false;
		}
        return true;
    }
	
	public void startExecuting()
    {
		if (this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) == null) {
    		this.taskOwner.getAttributeMap().registerAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
    		oldFollowRange = 0.0D;
    	}
		else {
			oldFollowRange = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
		}
    	this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(120.0D);
        taskOwner.setAttackTarget(targetEntity);
        //CalamityEffectHat.logger.info("TARGETHATCLASS.STARTEXECUTING: "+ taskOwner);
    }

	protected double getTargetDistance()
    {
        return 128;
    }
	
	public boolean shouldContinueExecuting() {
			return isSuitableTarget(targetEntity, false) && testtarget(targetEntity);
	}
	 public void resetTask()
	    {
	        this.taskOwner.setAttackTarget((EntityLivingBase)null);
	        this.target = null;
	        this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(oldFollowRange);
	        //CalamityEffectHat.logger.info("TARGETHATCLASS.resettask: "+ taskOwner);
	    }

	
	protected AxisAlignedBB getTargetableArea()
    {
        return this.taskOwner.getEntityBoundingBox().grow(128, 12.0D, 128);
    }
	public static boolean isSuitableTarget(EntityLiving attacker, @Nullable EntityLivingBase target, boolean includeInvincibles, boolean checkSight)
    {
        if (target == null)
        {
            return false;
        }
        else if (target == attacker)
        {
            return false;
        }
        else if (!target.isEntityAlive())
        {
            return false;
        }
        else if (!attacker.canAttackClass(target.getClass()))
        {
            return false;
        }
        else if (attacker.isOnSameTeam(target))
        {
            return false;
        }
        else
        {
            if (attacker instanceof IEntityOwnable && ((IEntityOwnable)attacker).getOwnerId() != null)
            {
                if (target instanceof IEntityOwnable && ((IEntityOwnable)attacker).getOwnerId().equals(((IEntityOwnable)target).getOwnerId()))
                {
                    return false;
                }

                if (target == ((IEntityOwnable)attacker).getOwner())
                {
                    return false;
                }
            }
            else if (target instanceof EntityPlayer && !includeInvincibles && ((EntityPlayer)target).capabilities.disableDamage)
            {
                return false;
            }

            return !checkSight || attacker.getEntitySenses().canSee(target);
        }
    }

    protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles)
    {
        if (!isSuitableTarget(taskOwner, target, includeInvincibles, shouldCheckSight))
        {
            return false;
        }
        else if (taskOwner instanceof EntityCreature && !((EntityCreature)taskOwner).isWithinHomeDistanceFromPosition(new BlockPos(target)))
        {
            return false;
        }
        else
        {
            /*if (nearbyOnly)
            {
                if (--targetSearchDelay <= 0)
                {
                    targetSearchStatus = 0;
                }

                if (targetSearchStatus == 0)
                {
                    targetSearchStatus = canEasilyReach(target) ? 1 : 2;
                }

                if (targetSearchStatus == 2)
                {
                    return false;
                }
            }*/

            return true;
        }
    }
    private boolean canEasilyReach(EntityLivingBase target)
    {
        this.targetSearchDelay = 10 + this.taskOwner.getRNG().nextInt(5);
        Path path = this.taskOwner.getNavigator().getPathToEntityLiving(target);

        if (path == null)
        {
            return false;
        }
        else
        {
            PathPoint pathpoint = path.getFinalPathPoint();

            if (pathpoint == null)
            {
                return false;
            }
            else
            {
                int i = pathpoint.x - MathHelper.floor(target.posX);
                int j = pathpoint.z - MathHelper.floor(target.posZ);
                return (double)(i * i + j * j) <= 2.25D;
            }
        }
    }
	
	public static boolean testtarget (EntityLivingBase player) {
		return PotionMod.iseffecton (player);
	}
}

	

