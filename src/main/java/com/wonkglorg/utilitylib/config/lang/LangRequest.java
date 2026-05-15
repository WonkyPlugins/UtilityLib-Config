package com.wonkglorg.utilitylib.config.lang;

import com.wonkglorg.utilitylib.config.LangManager;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
	
	private static final Pattern CONDITIONAL_PATTERN = Pattern.compile("<if:(!?)([\\w-]+)>(.*?)(?:<else>(.*?))?</if>", Pattern.DOTALL);
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
	 * Any registered conditionals that should be parsed by the lang
	 */
	private final Map<String, Boolean> conditionals = new HashMap<>();
	/**
	 * Map of all replacements applied to this request
	 */
	private final Map<String, String> replacements = new HashMap<>();
	
	private final Map<String, Component> componentReplacements = new HashMap<>();
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
	}
	
	public LangRequest forceLocale(boolean forceLocale) {
		this.forceLocale = forceLocale;
		return this;
	}
	
	/**
	 * Adds a conditional value to the request resolver
	 *
	 * @param key the key of the condition
	 */
	public LangRequest conditional(String key, boolean value) {
		conditionals.put(key.toLowerCase(Locale.ROOT), value);
		return this;
	}
	
	/**
	 * Replaces the given value with its replacement
	 */
	public LangRequest replace(String value, String replacement) {
		if(replacement == null){
			replacement = "";
		}
		replacements.put(value, replacement);
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
		//invalidate pattern if one was already generated
		pattern = null;
		componentReplacements.put(value, replacement);
		return this;
	}
	
	/**
	 * @return the raw value found in the config without any modifications
	 */
	public List<String> getRawResult() {
		return getRawResult(locale);
	}
	
	/**
	 * @return the raw value found in the config without any modifications
	 */
	public List<String> getRawResult(Locale locale) {
		LangConfig config;
		var configOptional = langManager.getAnyValidLangConfig(locale);
		if(configOptional.isPresent()){
			config = configOptional.get();
		} else {
			logger.log(Level.INFO, "No lang file could be loaded for request: " + key + " using default value!");
			List<String> arrayList = new ArrayList<>();
			arrayList.add(defaultValue);
			return arrayList;
		}
		
		if(config.isUpdateRequest()){
			config.updateReplacerMap();
		}
		
		if(!config.contains(key)){
			List<String> arrayList = new ArrayList<>();
			arrayList.add(defaultValue);
			return arrayList;
		}
		
		if(config.isList(key)){
			return config.getStringList(key);
		} else {
			List<String> arrayList = new ArrayList<>();
			arrayList.add(config.getString(key, defaultValue));
			return arrayList;
		}
	}
	
	/**
	 * @return the string or first entry of the list from the config without any modifications
	 */
	public String getRawResultSingleLine() {
		return getRawResultSingleLine(locale);
	}
	
	/**
	 * @return the string or first entry of the list from the config without any modifications
	 */
	public String getRawResultSingleLine(Locale locale) {
		LangConfig config;
		var configOptional = langManager.getAnyValidLangConfig(locale);
		if(configOptional.isPresent()){
			config = configOptional.get();
		} else {
			logger.log(Level.INFO, "No lang file could be loaded for request: " + key + " using default value!");
			return defaultValue;
		}
		
		if(config.isUpdateRequest()){
			config.updateReplacerMap();
		}
		
		if(!config.contains(key)){
			return defaultValue;
		}
		
		if(config.isList(key)){
			return config.getStringList(key).getFirst();
		} else {
			return config.getString(key, defaultValue);
		}
	}
	
	/**
	 * Resolves any conditionals and placeholders in the input string (does not resolve component replacements)
	 */
	public String processRawResult(String toResolve) {
		toResolve = applyConditionals(toResolve);
		return applyPlaceHolders(toResolve);
	}
	
	/**
	 * Resolves any conditionals and placeholders in the input (does not resolve component replacements)
	 */
	public List<String> processRawResult(List<String> toResolve) {
		toResolve.replaceAll(this::processRawResult);
		return toResolve;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * Updates the input with all replacements found in the populated replacement map
	 *
	 * @param input to modify
	 * @return modified input
	 */
	private String applyPlaceHolders(String input) {
		//global replacers
		for(var replacement : langManager.getReplacerMap().entrySet()){
			input = input.replace(replacement.getKey(), replacement.getValue());
		}
		if(replacements.isEmpty()){
			return input;
		}
		//per request replacers
		for(var replacement : replacements.entrySet()){
			input = input.replace(replacement.getKey(), replacement.getValue());
		}
		return input;
	}
	
	/**
	 * Resolved all component placeholders and merges them into one component output
	 *
	 * @param toComponent the function to convert components with
	 * @param value the input value
	 * @return the constructed component
	 */
	private Component applyComponentPlaceholders(Function<String, Component> toComponent, String value) {
		Matcher matcher = pattern.matcher(value);
		
		List<Component> subComponents = new ArrayList<>();
		int last = 0;
		
		while(matcher.find()){
			if(matcher.start() > last){
				subComponents.add(toComponent.apply(value.substring(last, matcher.start())));
			}
			
			String componentKey = matcher.group();
			subComponents.add(componentReplacements.getOrDefault(componentKey, toComponent.apply(componentKey)));
			
			last = matcher.end();
		}
		
		if(last < value.length()){
			subComponents.add(toComponent.apply(value.substring(last)));
		}
		return Component.join(JoinConfiguration.noSeparators(), subComponents);
	}
	
	/**
	 * Resolves any conditionals found in the result
	 *
	 * @param input the raw result to process
	 * @return resolved string
	 */
	private String applyConditionals(String input) {
		if(conditionals.isEmpty() || !input.contains("<if:")){
			return input;
		}
		
		Matcher matcher = CONDITIONAL_PATTERN.matcher(input);
		StringBuilder builder = new StringBuilder();
		
		while(matcher.find()){
			boolean negate = !matcher.group(1).isEmpty();
			String conditionKey = matcher.group(2).toLowerCase(Locale.ROOT);
			
			String trueValue = matcher.group(3);
			String falseValue = matcher.group(4);
			
			boolean condition = conditionals.getOrDefault(conditionKey, false);
			
			if(negate){
				condition = !condition;
			}
			
			String replacement = condition ? trueValue : (falseValue != null ? falseValue : "");
			
			matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
		}
		
		matcher.appendTail(builder);
		
		return builder.toString();
	}
	
	/**
	 *
	 * @param toComponent the converter to use
	 * @return the output as a component
	 */
	private List<Component> toComponent(Function<String, Component> toComponent) {
		return toComponent(toComponent, locale);
	}
	
	/**
	 *
	 * @param toComponent the converter to use
	 * @return the output as a component
	 */
	private List<Component> toComponent(Function<String, Component> toComponent, Locale locale) {
		List<Component> components = new ArrayList<>();
		List<String> results = processRawResult(getRawResult(locale));
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
			components.add(applyComponentPlaceholders(toComponent, resultValue));
		}
		return components;
	}
	
	/**
	 * @param toComponent the component function
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	private Component toSingleComponent(Function<String, Component> toComponent) {
		return toSingleComponent(toComponent, locale);
	}
	
	/**
	 * @param toComponent the component function
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	private Component toSingleComponent(Function<String, Component> toComponent, Locale locale) {
		String rawResult = processRawResult(getRawResultSingleLine(locale));
		if(componentReplacements.isEmpty()){
			return toComponent.apply(rawResult);
		}
		
		if(pattern == null){
			pattern = Pattern.compile(componentReplacements.keySet().stream().map(Pattern::quote).collect(Collectors.joining("|")));
		}
		return applyComponentPlaceholders(toComponent, rawResult);
	}
	
	/**
	 * @return the output as a component
	 */
	
	public List<Component> toComponent() {
		return toComponent(MiniMessage.miniMessage()::deserialize);
	}
	
	/**
	 * @return the first line of the config defined value as a component (this cuts off any other lines also specified in the same key, should be used for lines that only have one value it can have.
	 */
	
	public Component toSingleComponent() {
		return toSingleComponent(MiniMessage.miniMessage()::deserialize);
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
		if(audience instanceof Player player && !forceLocale){
			toComponent(toComponent, player.locale()).forEach(audience::sendMessage);
		} else {
			toComponent(toComponent).forEach(audience::sendMessage);
		}
	}
}
