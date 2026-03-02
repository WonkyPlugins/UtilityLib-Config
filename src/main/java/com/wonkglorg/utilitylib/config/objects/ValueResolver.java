package com.wonkglorg.utilitylib.config.objects;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ValueResolver<T, V>{
	public static final Predicate<Entity> ALWAYS = e -> true;
	private final String key;
	private final Predicate<Entity> matches;
	private final BiConsumer<T, V> apply;
	private final Function<T, V> extract;
	
	public ValueResolver(String key, Predicate<Entity> matches, BiConsumer<T, V> apply, Function<T, V> extract) {
		this.key = key;
		this.matches = matches;
		this.apply = apply;
		this.extract = extract;
	}
	
	public static <T, V> ValueResolver<T, V> create(String key, Predicate<Entity> matches, BiConsumer<T, V> apply, Function<T, V> extract) {
		return new ValueResolver<>(key, matches, apply, extract);
	}
	
	public static ValueResolver<LivingEntity, Double> attributeResolver(String key, Attribute attribute) {
		return new ValueResolver<>(key,
				LivingEntity.class::isInstance,
				(entity, value) -> setAttribute(entity, attribute, value),
				entity -> getAttribute(entity, attribute));
	}
	
	public static ValueResolver<LivingEntity, ItemStack> equipmentResolver(String key, EquipmentSlot equipmentSlot) {
		return new ValueResolver<>(key,
				ValueResolver::hasEquipment,
				(entity, value) -> entity.getEquipment().setItem(equipmentSlot, value),
				entity -> entity.getEquipment().getItem(equipmentSlot));
	}
	
	public String getKey() {
		return key;
	}
	
	public boolean matches(Entity entity) {
		return matches.test(entity);
	}
	
	public void apply(Entity entity, Object value) {
		if(value == null) return;
		//noinspection unchecked
		apply.accept((T) entity, (V) value);
	}
	
	public V extract(Entity entity) {
		//noinspection unchecked
		return extract.apply((T) entity);
	}
	
	public static Double getAttribute(LivingEntity attributable, Attribute attribute) {
		AttributeInstance instance = attributable.getAttribute(attribute);
		if(instance == null) return null;
		return instance.getValue();
	}
	
	public static void setAttribute(LivingEntity attributable, Attribute attribute, Double value) {
		if(value == null) return;
		AttributeInstance instance = attributable.getAttribute(attribute);
		if(instance == null) return;
		instance.setBaseValue(value);
	}
	
	public static boolean hasEquipment(Entity entity) {
		if(entity instanceof LivingEntity living){
			return living.getEquipment() != null;
		}
		return false;
	}
}
