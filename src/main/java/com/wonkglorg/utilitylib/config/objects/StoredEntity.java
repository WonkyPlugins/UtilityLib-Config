package com.wonkglorg.utilitylib.config.objects;

import static com.wonkglorg.utilitylib.config.objects.ValueResolver.ALWAYS;
import static com.wonkglorg.utilitylib.config.objects.ValueResolver.attributeResolver;
import static com.wonkglorg.utilitylib.config.objects.ValueResolver.create;
import static com.wonkglorg.utilitylib.config.objects.ValueResolver.equipmentResolver;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an entities data stored inside a config to restore.
 */
public class StoredEntity implements ConfigurationSerializable{
	private final Map<String, Object> data;
	private EntityType entityType;
	/**
	 * Registered data extracts to serialize entity data
	 */
	private static final Map<String, ValueResolver<? extends Entity, ?>> VALUE_RESOLVERS = new HashMap<>();
	
	public StoredEntity(Entity entity) {
		this.entityType = entity.getType();
		data = new HashMap<>();
		extractData(entity);
	}
	
	public StoredEntity(ConfigurationSection section) {
		entityType = EntityType.valueOf(section.getString("type"));
		data = (Map<String, Object>) section.get("data", new HashMap<>());
	}
	
	public void spawnEntity(Location location) {
		Entity entity = location.getWorld().spawnEntity(location, entityType);
		applyChanges(entity);
	}
	
	private void applyChanges(Entity entity) {
		for(var resolver : VALUE_RESOLVERS.values()){
			if(resolver.matches(entity)){
				resolver.apply(entity, data.getOrDefault(resolver.getKey(), null));
			}
		}
	}
	
	private void extractData(Entity entity) {
		for(var resolver : VALUE_RESOLVERS.values()){
			if(resolver.matches(entity)){
				Object extract = resolver.extract(entity);
				if(extract == null) continue;
				data.put(resolver.getKey(), extract);
			}
		}
	}
	
	public static <T extends Entity, V> void registerResolver(ValueResolver<T, V> resolver) {
		VALUE_RESOLVERS.put(resolver.getKey(), resolver);
	}
	
	static {
		registerResolver(create("customName", ALWAYS, Nameable::customName, Nameable::customName));
		registerResolver(create("customNameVisible", ALWAYS, Entity::setCustomNameVisible, Entity::isCustomNameVisible));
		registerResolver(create("health", LivingEntity.class::isInstance, LivingEntity::setHealth, LivingEntity::getHealth));
		registerResolver(create("age", Ageable.class::isInstance, Ageable::setAge, Ageable::getAge));
		registerResolver(create("villager.profession", Villager.class::isInstance, Villager::setProfession, Villager::getProfession));
		registerResolver(create("villager.villagerType", Villager.class::isInstance, Villager::setVillagerType, Villager::getVillagerType));
		registerResolver(create("creeper.powered", Creeper.class::isInstance, Creeper::setPowered, Creeper::isPowered));
		registerResolver(create("creeper.ignited", Creeper.class::isInstance, Creeper::setIgnited, Creeper::isIgnited));
		registerResolver(create("sheep.color", Sheep.class::isInstance, Sheep::setColor, Sheep::getColor));
		
		for(var equipment : EquipmentSlot.values()){
			registerResolver(equipmentResolver("equipment." + equipment.name().toLowerCase(), equipment));
		}
		
		registerResolver(attributeResolver("attribute.scale", Attribute.SCALE));
		registerResolver(attributeResolver("attribute.max_health", Attribute.MAX_HEALTH));
		registerResolver(attributeResolver("attribute.follow_range", Attribute.FOLLOW_RANGE));
		registerResolver(attributeResolver("attribute.knockback_resistance", Attribute.KNOCKBACK_RESISTANCE));
		registerResolver(attributeResolver("attribute.movement_speed", Attribute.MOVEMENT_SPEED));
		registerResolver(attributeResolver("attribute.flying_speed", Attribute.FLYING_SPEED));
		registerResolver(attributeResolver("attribute.attack_damage", Attribute.ATTACK_DAMAGE));
		registerResolver(attributeResolver("attribute.attack_knockback", Attribute.ATTACK_KNOCKBACK));
		registerResolver(attributeResolver("attribute.attack_speed", Attribute.ATTACK_SPEED));
		registerResolver(attributeResolver("attribute.armor", Attribute.ARMOR));
		registerResolver(attributeResolver("attribute.armor_toughness", Attribute.ARMOR_TOUGHNESS));
		registerResolver(attributeResolver("attribute.fall_damage_multiplier", Attribute.FALL_DAMAGE_MULTIPLIER));
		registerResolver(attributeResolver("attribute.luck", Attribute.LUCK));
		registerResolver(attributeResolver("attribute.max_absorbtion", Attribute.MAX_ABSORPTION));
		registerResolver(attributeResolver("attribute.safe_fall_distance", Attribute.SAFE_FALL_DISTANCE));
		registerResolver(attributeResolver("attribute.scale", Attribute.SCALE));
		registerResolver(attributeResolver("attribute.step_height", Attribute.STEP_HEIGHT));
		registerResolver(attributeResolver("attribute.gravity", Attribute.GRAVITY));
		registerResolver(attributeResolver("attribute.jump_strength", Attribute.JUMP_STRENGTH));
		registerResolver(attributeResolver("attribute.burning_time", Attribute.BURNING_TIME));
		registerResolver(attributeResolver("attribute.camera_distance", Attribute.CAMERA_DISTANCE));
		registerResolver(attributeResolver("attribute.explosion_knockback_resistance", Attribute.EXPLOSION_KNOCKBACK_RESISTANCE));
		registerResolver(attributeResolver("attribute.movement_efficiency", Attribute.MOVEMENT_EFFICIENCY));
		registerResolver(attributeResolver("attribute.oxygen_bonus", Attribute.OXYGEN_BONUS));
		registerResolver(attributeResolver("attribute.water_movement_efficiency", Attribute.WATER_MOVEMENT_EFFICIENCY));
		registerResolver(attributeResolver("attribute.tempt_range", Attribute.TEMPT_RANGE));
		registerResolver(attributeResolver("attribute.block_interaction_range", Attribute.BLOCK_INTERACTION_RANGE));
		registerResolver(attributeResolver("attribute.entity_interaction_range", Attribute.ENTITY_INTERACTION_RANGE));
		registerResolver(attributeResolver("attribute.block_break_speed", Attribute.BLOCK_BREAK_SPEED));
		registerResolver(attributeResolver("attribute.mining_efficiency", Attribute.MINING_EFFICIENCY));
		registerResolver(attributeResolver("attribute.sneak_speed", Attribute.SNEAKING_SPEED));
		registerResolver(attributeResolver("attribute.submerged_mining_speed", Attribute.SUBMERGED_MINING_SPEED));
		registerResolver(attributeResolver("attribute.sweeping_damage_ratio", Attribute.SWEEPING_DAMAGE_RATIO));
		registerResolver(attributeResolver("attribute.spawn_reinforcements", Attribute.SPAWN_REINFORCEMENTS));
		registerResolver(attributeResolver("attribute.waypoint_transmit_range", Attribute.WAYPOINT_TRANSMIT_RANGE));
		registerResolver(attributeResolver("attribute.waypoint_receive_range", Attribute.WAYPOINT_RECEIVE_RANGE));
	}
	
	@Override
	public @NonNull Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("type", entityType.name());
		map.put("data", data);
		return map;
	}
}
