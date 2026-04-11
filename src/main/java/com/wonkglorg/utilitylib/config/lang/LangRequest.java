package com.wonkglorg.utilitylib.config.lang;

import com.wonkglorg.utilitylib.config.LangManager;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Language Value request which can be further modified with additional properties and modifiers.
 */
@SuppressWarnings("unused")
public class LangRequest{
	private final Logger logger;
	/**
	 * The lang manager this request was returned by
	 */
	private final LangManager langManager;
	/**
	 * The locale used for the initial request
	 */
	private final Locale locale;
	/**
	 * The key used for the initial request
	 */
	private final String key;
	/**
	 * The defaultValue used for the initial request
	 */
	private final String defaultValue;
	/**
	 * Map of all replacements applied to this request
	 */
	private final Map<String, String> replacements = new HashMap<>();
	
	private final Map<String, Component> componentReplacements = new HashMap<>();
	/**
	 * The initial returned result without any other modifications
	 */
	private final List<String> initialResult;
	/**
	 * The current result made after all values are collected
	 */
	private List<String> result;
	/**
	 * Pattern for component replacers
	 */
	private Pattern pattern;
	/**
	 * Weather or not the initially provided locale should be forced. If false certain methods may re request the message in the given language such as {@link #sendToAudience(Audience)}
	 */
	private boolean forceLocale = false;
	
	public LangRequest(LangManager langManager, Locale locale, String key, String defaultValue) {
		logger = langManager.getLogger();
		this.langManager = langManager;
		this.locale = locale;
		this.key = key;
		this.defaultValue = defaultValue;
		this.initialResult = getValue(this.locale, key, defaultValue);
		this.result = initialResult;
	}
	
	public LangRequest forceLocale(boolean forceLocale) {
		this.forceLocale = forceLocale;
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, String replacement) {
		if(replacement == null) return this;
		replacements.put(value, replacement);
		for(int i = 0; i < result.size(); i++){
			String input = result.get(i);
			input = input.replace(value, replacement);
			result.set(i, input);
		}
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, char replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, short replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, int replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, long replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, double replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, float replacement) {
		return replace(value, String.valueOf(replacement));
	}
	
	public LangRequest replace(String value, Component replacement) {
		if(replacement == null) return this;
		pattern = null;
		componentReplacements.put(value, replacement);
		return this;
	}
	
	public List<String> getUnmodifiedResult() {
		return initialResult;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * Updates the input with all replacements found in the populated replacement map
	 *
	 * @param inputs to modify
	 * @return modified input
	 */
	private List<String> updateResult(List<String> inputs) {
		
		for(int i = 0; i < inputs.size(); i++){
			String input = inputs.get(i);
			for(var replacement : replacements.entrySet()){
				input = input.replace(replacement.getKey(), replacement.getValue());
			}
			inputs.set(i, input);
		}
		
		return inputs;
	}
	
	public List<String> getResult() {
		return result;
	}
	
	private List<Component> toComponent(Function<String, Component> toComponent, List<String> results) {
		List<Component> components = new ArrayList<>();
		if(componentReplacements.isEmpty()){
			for(var resultValue : results){
				components.add(toComponent.apply(resultValue));
			}
			
			return components;
		}
		
		if(pattern == null){
			pattern = Pattern.compile(componentReplacements.keySet().stream().map(Pattern::quote).collect(Collectors.joining("|")));
		}
		for(var resultValue : results){
			Matcher matcher = pattern.matcher(resultValue);
			
			List<Component> subComponents = new ArrayList<>();
			int last = 0;
			
			while(matcher.find()){
				if(matcher.start() > last){
					subComponents.add(toComponent.apply(resultValue.substring(last, matcher.start())));
				}
				
				String key = matcher.group();
				subComponents.add(componentReplacements.getOrDefault(key, toComponent.apply(key)));
				
				last = matcher.end();
			}
			
			if(last < resultValue.length()){
				subComponents.add(toComponent.apply(resultValue.substring(last)));
			}
			
			components.add(Component.join(JoinConfiguration.noSeparators(), subComponents));
			
		}
		
		return components;
	}
	
	private Component toSingleComponent(Function<String, Component> toComponent, List<String> resultValue) {
		String first = resultValue.getFirst();
		if(componentReplacements.isEmpty()){
			return toComponent.apply(first);
		}
		
		if(pattern == null){
			pattern = Pattern.compile(componentReplacements.keySet().stream().map(Pattern::quote).collect(Collectors.joining("|")));
		}
		Matcher matcher = pattern.matcher(first);
		
		List<Component> subComponents = new ArrayList<>();
		int last = 0;
		
		while(matcher.find()){
			if(matcher.start() > last){
				subComponents.add(toComponent.apply(first.substring(last, matcher.start())));
			}
			
			String key = matcher.group();
			subComponents.add(componentReplacements.getOrDefault(key, toComponent.apply(key)));
			
			last = matcher.end();
		}
		
		if(last < first.length()){
			subComponents.add(toComponent.apply(first.substring(last)));
		}
		
		return Component.join(JoinConfiguration.noSeparators(), subComponents);
	}
	
	/**
	 *
	 * @param toComponent the converter to use
	 * @return the output as a component
	 */
	public List<Component> toComponent(Function<String, Component> toComponent) {
		return toComponent(toComponent, result);
	}
	
	/**
	 * @return the output as a component
	 */
	public List<Component> toComponent() {
		return toComponent(MiniMessage.miniMessage()::deserialize, result);
	}
	
	/**
	 * @param toComponent the component function
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	public Component toSingleComponent(Function<String, Component> toComponent) {
		return toSingleComponent(toComponent, result);
	}
	
	/**
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	public Component toSingleComponent() {
		return toSingleComponent(MiniMessage.miniMessage()::deserialize, result);
	}
	
	/**
	 * Sends the request's result to the given audience using the MiniMessage formatting.
	 *
	 * @param audience if the audience is a {@link Player} requests their locale to modify the message with unless {@link #forceLocale} is set to true.
	 */
	public void sendToAudience(@NotNull Audience audience) {
		sendToAudience(audience, MiniMessage.miniMessage()::deserialize);
	}
	
	/**
	 * Sends the request's result to the given audience using the MiniMessage formatting.
	 *
	 * @param audience if the audience is a {@link Player} requests their locale to modify the message with unless {@link #forceLocale} is set to true.
	 * @param toComponent the function to use turning the result into a component to send
	 */
	public void sendToAudience(@NotNull Audience audience, Function<String, Component> toComponent) {
		if(audience instanceof Player player){
			if(player.locale() == locale || forceLocale || langManager.getAllLangs().size() == 1){
				toComponent(toComponent, this.result).forEach(audience::sendMessage);
			} else {
				this.result = updateResult(getValue(this.locale, key, defaultValue));
				toComponent(toComponent, this.result).forEach(audience::sendMessage);
			}
		} else {
			toComponent(toComponent, this.result).forEach(audience::sendMessage);
		}
	}
	
	/**
	 * Gets a value from the language file with global replacements applied
	 *
	 * @param locale the locale to get the value from
	 * @param key the key to get by
	 * @param defaultValue the default value to return if no value was found
	 * @return the returned result or the value if no result was found
	 */
	private List<String> getValue(@Nullable final Locale locale, @NotNull final String key, @Nullable String defaultValue) {
		LangConfig config;
		defaultValue = defaultValue != null ? defaultValue : key;
		var configOptional = langManager.getAnyValidLangConfig(locale);
		if(configOptional.isPresent()){
			config = configOptional.get();
		} else {
			logger.log(Level.INFO, "No lang file could be loaded for request: " + key + " using default value!");
			return List.of(defaultValue);
		}
		
		if(config.isUpdateRequest()){
			config.updateReplacerMap();
		}
		
		if(config.isList(key)){
			List<String> results = config.getStringList(key);
			
			for(int i = 0; i < results.size(); i++){
				String edited = results.get(i);
				
				for(var entry : langManager.getReplacerMap().entrySet()){
					edited = edited.replace(entry.getKey(), entry.getValue());
				}
				
				for(var entry : config.getReplacerMap().entrySet()){
					edited = edited.replace(entry.getKey(), entry.getValue());
				}
				
				results.set(i, edited);
			}
			
			return results;
			
		} else {
			String resultString = config.getString(key, defaultValue);
			if(langManager.getReplacerMap().isEmpty() && config.getReplacerMap().isEmpty()){
				List<String> strings = new ArrayList<>();
				strings.add(resultString);
				return strings;
			}
			
			for(var entry : langManager.getReplacerMap().entrySet()){
				resultString = resultString.replace(entry.getKey(), entry.getValue());
			}
			
			for(var entry : config.getReplacerMap().entrySet()){
				resultString = resultString.replace(entry.getKey(), entry.getValue());
			}
			List<String> strings = new ArrayList<>();
			strings.add(resultString);
			return strings;
		}
	}
	
}
