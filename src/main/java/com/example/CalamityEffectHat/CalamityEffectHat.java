package com.example.CalamityEffectHat;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityParrot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.common.*;
import net.minecraft.client.*;
import net.minecraft.util.*;
import net.minecraftforge.event.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;

import net.minecraftforge.registries.*;

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import net.minecraft.item.*;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

@Mod.EventBusSubscriber
@Mod(modid = CalamityEffectHat.MODID, name = CalamityEffectHat.NAME, version = CalamityEffectHat.VERSION)
public class CalamityEffectHat
{
    public static final String MODID = "calamityeffecthat";
    public static final String NAME = "Test Mod";
    public static final String VERSION = "1.0";

    public static Logger logger;
    
    public static Potion PotionMod1;
    
    public static hatclass exampleItem;
    public static final HashSet<EntityLivingBase> entitywithhatHashSet = new HashSet<>(); 
    static Map<String, String> EntityAttackMap = new HashMap<>();
    static Map<Class<?>, AttackAndParameters> ClassObjectMap= new HashMap<>();
	
    public class AttackAndParameters{
    	String AttackType;
    	String MovementSpeed;
    	int TaskNumber;
	    int TargetTaskNumber;
    }
    
    public static class CreatureEntry extends IForgeRegistryEntry.Impl<CreatureEntry> {
    	String CreatureClass;
	    String AttackType;
    }
    
    public static class CreatureEntryList{
    	public ArrayList<CreatureEntry> zz = new ArrayList<CreatureEntry>();
    }
    
    public static class jsonclassDeserializer implements JsonDeserializer<CreatureEntryList> {
		@Override
		public CreatureEntryList deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				{
					CreatureEntryList result = new CreatureEntryList();
					JsonArray jsonObject = json.getAsJsonArray();
					for (JsonElement entry : jsonObject) {
						CreatureEntry dwarf = context.deserialize(entry, CreatureEntry.class);
						dwarf.AttackType = entry.toString();
						result.zz.add(dwarf);
				}
				return result;
		}
    }
    }
    
    public CalamityEffectHat()
    {
    	exampleItem = new hatclass();
    	exampleItem.setUnlocalizedName("exampleitem");
    	exampleItem.setRegistryName("exampleitem"); 
    	
    	PotionMod1 = new PotionMod();
    	PotionMod1.setRegistryName("calamity");

        MinecraftForge.EVENT_BUS.register((Object)this);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(hatclass.class); 
    }

    @EventHandler
    public void postInit (FMLPostInitializationEvent E)
    {
    	//CalamityEffectHat.logger.info("POSTINIT!!!!!!!!!!!");
    	IForgeRegistry<CreatureEntry> a = GameRegistry.findRegistry(CreatureEntry.class);
    	for (ResourceLocation foreach : a.getKeys()) {
    		try {
    		Gson gson = new Gson();
		 	AttackAndParameters act2 = gson.fromJson(a.getValue(foreach).AttackType, AttackAndParameters.class);
		 	Class<?> act = Class.forName(a.getValue(foreach).CreatureClass);
		 	ClassObjectMap.put(act, act2);
    		}
    		 catch (ClassNotFoundException e) {
    			 //CalamityEffectHat.logger.info("POSTINIT Failed!!!!!!!!!!!");
 			}
    	}
    }
    
    @SubscribeEvent
    public void HurtFunction(LivingHurtEvent event) {
    	if (entitywithhatHashSet.contains(event.getEntityLiving())) {
    		event.getEntityLiving().setHealth(event.getEntityLiving().getHealth()-1.0F);
    	}		
    }
    
    @SubscribeEvent
    public void onEquipment(LivingEquipmentChangeEvent event) {
    	EntityLivingBase op = event.getEntityLiving();
    	if (hatclass.isonhatclass(op)) {
	        op.addPotionEffect(new PotionEffect(CalamityEffectHat.PotionMod1, 9*20-1, 0, true, true));
    	}
    }
    
    
    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(exampleItem);	
    }
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
    	ModelLoader.setCustomModelResourceLocation(exampleItem, 0, new ModelResourceLocation(exampleItem.getRegistryName(), "inventory"));
    }
    
    @SideOnly(Side.SERVER)
    @SubscribeEvent
	static public void onUpdate(LivingUpdateEvent event) {
	}
    
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onEntitySettingTarget(LivingSetAttackTargetEvent event){
    	String str = "Set attackTarget: " + event.getEntityLiving() + "  " + event.getTarget() + "//";
    }
    
    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> evt) {
    	 evt.getRegistry().register(PotionMod1);
    	
    }
    
    @SubscribeEvent
    static public void newregistry (RegistryEvent.NewRegistry event) {
    	net.minecraftforge.registries.RegistryBuilder<CreatureEntry> builder = new net.minecraftforge.registries.RegistryBuilder<CreatureEntry>();
    	builder.setType(CreatureEntry.class);
    	ResourceLocation key = new ResourceLocation(CalamityEffectHat.MODID, "registryforCalamityEffectHat");
    	builder.setName(key);
    	builder.setDefaultKey(key);
    	builder.create();
    }
    
    @SubscribeEvent
    public static void registerFill (RegistryEvent.Register<CreatureEntry> e) {
        //MinecraftForge.EVENT_BUS.register(hatclass.class);
        
        ResourceLocation loc = new ResourceLocation(CalamityEffectHat.MODID, "config/filejson.json");
        Gson jgson = new GsonBuilder().registerTypeAdapter(CreatureEntryList.class, new jsonclassDeserializer()).create();
        Reader reader = null;
        try {
        	InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
        	reader = new InputStreamReader(in);
        } catch (Exception ex){
        }
        CreatureEntryList List = (reader != null) ? jgson.fromJson(reader, CreatureEntryList.class) : new CreatureEntryList();
        
        for (CreatureEntry foreach : List.zz) {
        	EntityAttackMap.put(foreach.CreatureClass, foreach.AttackType);  
        	e.getRegistry().register(foreach.setRegistryName(new ResourceLocation(CalamityEffectHat.MODID, foreach.CreatureClass)));
        }
    }

    
    @SubscribeEvent
    static public void spawn(EntityJoinWorldEvent  event) {
    	int i = 0;
    	if (event.getEntity() instanceof EntityZombie) {//delete import too
    		EntityLiving o = (EntityLiving)event.getEntity();
    		o.setCanPickUpLoot (true);
    	}
    	if (event.getEntity() instanceof EntityLiving) {
    		EntityLiving o = (EntityLiving)event.getEntity();
    		if (o instanceof EntityParrot) {
	    		for (Class<?> each : ClassObjectMap.keySet()) {
	        	}
    		}
    		AttackAndParameters parameters = ClassObjectMap.get(o.getClass());
    		if (parameters != null) {
	    		Set<EntityAITasks.EntityAITaskEntry> p = o.targetTasks.taskEntries;
	    		EntityAITasks.EntityAITaskEntry[] arr = p.toArray(new EntityAITasks.EntityAITaskEntry[0]);
	    		for (i = 0; i < arr.length; i++) {
	    		if (arr[i].priority >= parameters.TargetTaskNumber) {
		    			o.targetTasks.removeTask(arr[i].action);
		    			o.targetTasks.addTask(arr[i].priority + 1, arr[i].action);
	    			}
	    		}
    			Set<EntityAITasks.EntityAITaskEntry> p2 = o.tasks.taskEntries;
	    		EntityAITasks.EntityAITaskEntry[] arr2 = p2.toArray(new EntityAITasks.EntityAITaskEntry[0]);
	    		for (i = 0; i < arr2.length; i++) {
	    			if (arr2[i].priority >= parameters.TaskNumber) {
		    			o.tasks.removeTask(arr2[i].action);
		    			o.tasks.addTask(arr2[i].priority + 1, arr2[i].action);
	    			}	
	    		}
	    		String text = parameters.MovementSpeed;
	    		double value = Double.parseDouble(text);
	    		EntityAIBase a;
	    		if (parameters.AttackType.equals("FlyingAttack")) {
	    			a = new FlyingAttack(o, value);
	    		}
	    		else if (parameters.AttackType.equals("AttackMobPassive")) {
	    			a = new AttackMobPassive(o, value);
	    		}
	    		else {
	    			a = null;
	    		}
	    		if (a != null) {
	    			o.tasks.addTask(parameters.TaskNumber, a);
	    		}
	    		o.targetTasks.addTask(parameters.TargetTaskNumber, new targetHatclass(o, true));
    		}
    		}
    }
}

