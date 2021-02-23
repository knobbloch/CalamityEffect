package com.example.CalamityEffectHat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class PotionMod extends Potion{
	
	protected PotionMod() {
		super(true, 1);
		this.setPotionName("calamity");	
	}
	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier){
		CalamityEffectHat.entitywithhatHashSet.remove(entityLivingBaseIn);
	}
	
	@Override
	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
		if (hatclass.isonhatclass(entityLivingBaseIn)) {
			entityLivingBaseIn.addPotionEffect(new PotionEffect(CalamityEffectHat.PotionMod1, 9*20-1));
		}
	}
	
	@Override
	public boolean isReady(int duration, int amplifier){
		return (duration % 40 == 1);
	}
	
	@Override
	public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		CalamityEffectHat.entitywithhatHashSet.add(entityLivingBaseIn);
	}
	
	public static boolean iseffecton (EntityLivingBase entityLivingBaseIn){
		return CalamityEffectHat.entitywithhatHashSet.contains(entityLivingBaseIn);
	}
}
