package com.wonkglorg.utilitylib.config;

import com.wonkglorg.utilitylib.config.lang.LangRequest;
import com.wonkglorg.utilitylib.config.types.Config;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A Lang manager to handle retrieving and sending per user translatable messages.
 *
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public final class LangManager{
	/**
	 * All Lang Managers and their plugin registered namespace
	 */
	private static final Map<String, LangManager> LANG_MANAGER_MAP = new ConcurrentHashMap<>();
	
	public static final String NO_LOCALE_FOUND_FOR_FILE = "No locale found for file: ";
	/**
	 * The Logger instance
	 */
	@Getter
	private final Logger logger;
	/**
	 * The lang map which contains all the language configs
	 */
	private final Map<Locale, LangConfig> langMap = new ConcurrentHashMap<>();
	/**
	 * The default language
	 */
	private Locale defaultLang = Locale.ENGLISH;
	
	/**
	 * Maps locales with the same base language name to its language name (to easier assign all relevant names to this from file alone)
	 */
	private static final Map<String, Set<Locale>> shortNameToLocaleMapper = new ConcurrentHashMap<>();
	
	static {
		for(Locale locale : Locale.getAvailableLocales()){
			shortNameToLocaleMapper.computeIfAbsent(locale.getLanguage(), k -> new HashSet<>()).add(locale);
		}
	}
	
	/**
	 * Creates a new instance of the LangManager or returns the already registered one for this plugin
	 *
	 * @param plugin the plugin to create the instance for
	 * @return the created instance
	 */
	public static LangManager getInstance(JavaPlugin plugin) {
		if(!LANG_MANAGER_MAP.containsKey(plugin.namespace())){
			LANG_MANAGER_MAP.put(plugin.namespace(), new LangManager(plugin.getLogger()));
		}
		return LANG_MANAGER_MAP.get(plugin.namespace());
	}
	
	/**
	 * Creates a new instance of the LangManager or returns the already registered one for this plugin
	 *
	 * @param namespace the plugin namespace to create the instance for
	 * @return the created instance
	 */
	public static LangManager getInstance(String namespace) {
		LANG_MANAGER_MAP.computeIfAbsent(namespace.toLowerCase(), k -> new LangManager());
		return LANG_MANAGER_MAP.get(namespace);
	}
	
	public static boolean hasInstance(JavaPlugin plugin) {
		return LANG_MANAGER_MAP.containsKey(plugin.namespace());
	}
	
	public LangManager() {
		logger = Logger.getLogger(LangManager.class.getName());
	}
	
	public LangManager(Logger logger) {
		this.logger = logger;
	}
	
	/**
	 * Sets the default language and the default config
	 *
	 * @param defaultLang the default language
	 * @param defaultConfig the default config
	 */
	public synchronized void setDefaultLang(LangConfig defaultConfig, Locale defaultLang) {
		langMap.put(defaultLang, defaultConfig);
		this.defaultLang = defaultLang;
		defaultConfig.silentLoad();
	}
	
	/**
	 * Sets the default language to use when no user language could be determined
	 *
	 * @param defaultLang the default language locale
	 */
	public synchronized void setDefaultLang(Locale defaultLang) {
		this.defaultLang = defaultLang;
	}
	
	/**
	 * Adds a language to the lang manager this is for single locales specifically to get a whole subsection of locales registered use the preferred method {@link #addLanguage(LangConfig, String, String...)} where each string is its {@link Locale#getLanguage()} definition
	 *
	 * @param locale 1 or more locale this config should apply to
	 * @param languageConfig the language config
	 */
	public synchronized void addLanguage(LangConfig languageConfig, Locale locale, Locale... extraLocale) {
		langMap.putIfAbsent(locale, languageConfig);
		for(Locale loc : extraLocale){
			langMap.putIfAbsent(loc, languageConfig);
		}
		languageConfig.silentLoad();
	}
	
	/**
	 * Adds a language to the lang manager this is a more generic version of {@link #addLanguage(LangConfig, Locale, Locale...)},
	 * since multiple locales share the same language code this method allows to add all common locales based on its language code(example:
	 * <pre>
	 *     {@code "en" -> en_US, en_GB, en_CA, etc...}
	 * @param languageConfig the language config
	 * @param langName the name of the language derived from {@link Locale#getLanguage()}
	 * @param extraLangNames extra names to add to the language
	 */
	public synchronized void addLanguage(LangConfig languageConfig, String langName, String... extraLangNames) {
		Set<Locale> locales = shortNameToLocaleMapper.get(langName);
		if(locales == null){
			logger.log(Level.WARNING, NO_LOCALE_FOUND_FOR_FILE + langName);
			return;
		}
		for(Locale locale : locales){
			addLanguage(languageConfig, locale);
		}
		for(String extraLangName : extraLangNames){
			locales = shortNameToLocaleMapper.get(extraLangName);
			if(locales == null){
				logger.log(Level.WARNING, NO_LOCALE_FOUND_FOR_FILE + extraLangName);
				continue;
			}
			for(Locale locale : locales){
				addLanguage(languageConfig, locale);
			}
		}
	}
	
	/**
	 * Saves all the language files
	 */
	public synchronized void save() {
		langMap.values().forEach(Config::save);
	}
	
	public synchronized void silentSave() {
		langMap.values().forEach(Config::silentSave);
	}
	
	public synchronized void load() {
		langMap.values().forEach(Config::silentLoad);
		
	}
	
	public synchronized void silentLoad() {
		langMap.values().forEach(Config::silentLoad);
		
		if(defaultLang == null){
			logger.log(Level.WARNING, "No default language selected!");
		}
	}
	
	public synchronized Config getDefaultLang() {
		try{
			return langMap.get(defaultLang);
		} catch(Exception e){
			return null;
		}
	}
	
	public synchronized void addAllLangFilesFromPath(String... paths) {
		if(paths.length == 0){
			return;
		}
		String first = paths[0];
		String[] more = Arrays.copyOfRange(paths, 1, paths.length);
		Path path = Path.of(first, more);
		addAllLangFilesFromPath(path);
	}
	
	/**
	 * Adds all language files from a given path, the path is relative to the plugins data folder, the language files should be named after the language they represent as per {@link Locale#getLanguage()} standard naming conventions (this does not copy them from the resources folder should be used to let the plugin user add more languages on their own without code changes)
	 */
	public synchronized void addAllLangFilesFromPath(Plugin plugin, Path path) {
		addAllLangFilesFromPath(plugin.getDataPath().resolve(path));
	}
	
	/**
	 * Adds all language files from a given path, the path is relative to the execution folder, the language files should be named after the language they represent as per {@link Locale#getLanguage()} standard naming conventions (this does not copy them from the resources folder should be used to let the plugin user add more languages on their own without code changes)
	 */
	public synchronized void addAllLangFilesFromPath(Path path) {
		try(Stream<Path> files = Files.list(path)){
			files.forEach(p -> {
				if(!Files.isRegularFile(p)){
					return;
				}
				if(!p.toString().endsWith(".yml") || !p.toString().endsWith(".yaml")){
					return;
				}
				
				String fileName = p.getFileName().toString();
				Set<Locale> locales = shortNameToLocaleMapper.get(fileName.replace(".yml", ""));
				if(locales == null){
					logger.log(Level.WARNING, NO_LOCALE_FOUND_FOR_FILE + fileName);
					return;
				}
				
				for(Locale locale : locales){
					LangConfig langConfig = new LangConfig(path);
					addLanguage(langConfig, locale);
				}
			});
		} catch(IOException e){
			logger.severe("Unable to load lang files from dir " + e.getMessage());
		}
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param key the key to look up
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final String key) {
		return new LangRequest(this, null, key, key);
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param key the key to look up
	 * @param defaultValue a default value to return if no value was found for the given key
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final String key, final String defaultValue) {
		return new LangRequest(this, null, key, defaultValue);
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param locale the locale to use (falls back to the default if not available)
	 * @param key the key to look up
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final Locale locale, final String key) {
		return new LangRequest(this, locale, key, key);
	}
	
	/**
	 * Requests a value from the lang file.
	 *
	 * @param locale the locale to use (falls back to the default if not available)
	 * @param key the key to look up
	 * @param defaultValue a default value to return if no value was found for the given key
	 * @return a {@link LangRequest} object
	 */
	public LangRequest request(final Locale locale, final String key, final String defaultValue) {
		return new LangRequest(this, locale, key, defaultValue);
	}
	
	/**
	 * Gets any valid language config to use (first checks if the locale is present, then the default locale, then any locale)
	 *
	 * @param locale the locale to get the language config for
	 * @return the language config or empty if none could be found
	 */
	public Optional<LangConfig> getAnyValidLangConfig(final Locale locale) {
		if(langMap.isEmpty()){
			return Optional.empty();
		}
		
		if(locale != null && langMap.containsKey(locale)){
			return Optional.of(langMap.get(locale));
		}
		
		if(langMap.containsKey(defaultLang)){
			return Optional.of(langMap.get(defaultLang));
		}
		return Optional.of(langMap.values().iterator().next());
	}
	
	/**
	 * Gets all stored languages
	 *
	 * @return the map of all languages
	 */
	public synchronized Map<Locale, LangConfig> getAllLangs() {
		return langMap;
	}
}