package com.example.CalamityEffectHat;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;




public class hatclass extends ItemArmor {
	int k = 0;
	static public EntityLiving z = null ;
	static public boolean flag = false; 
	
	public hatclass () {
		super(Hat, 0, EntityEquipmentSlot.HEAD);
	}
	public static final ArmorMaterial Hat = EnumHelper.addArmorMaterial("froggyHatMaterial", "froggyHatMaterial", 166, new int[] {4,7,9,5}, 10, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
	    return CalamityEffectHat.MODID + ":"+ "textures/hat.png";
	}
	
	public static boolean isonhatclass (EntityLivingBase event) {
		ItemStack i = event.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
    	return (i != null && i.getItem() == CalamityEffectHat.exampleItem) ; 	
	}
}