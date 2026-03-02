package com.wonkglorg.utilitylib.config.types;

import com.wonkglorg.utilitylib.config.objects.StoredEntity;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * @author Wonkglorg
 */
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class Config extends YamlConfiguration{
	protected final JavaPlugin plugin;
	protected final String name;
	protected final Path sourcePath;
	protected final Path destinationPath;
	protected final File file;
	protected final Logger logger;
	
	/**
	 * Creates a new file at the specified location or copies an existing one from the resource folder based on the sourcePath,
	 * if nothing could be found in the sourcePath it creates a new one. DestinationPath will automatically point to the plugin data folder.
	 *
	 * @param plugin plugin instance
	 * @param sourcePath path inside the resources folder of your plugin
	 * @param destinationPath path to copy this file to
	 */
	public Config(@NotNull JavaPlugin plugin, @NotNull Path sourcePath, @NotNull Path destinationPath) {
		this.plugin = plugin;
		this.name = destinationPath.getFileName().toString();
		this.sourcePath = sourcePath;
		this.destinationPath = destinationPath.startsWith(plugin.getDataFolder().toString()) ? destinationPath : Path.of(plugin.getDataFolder()
																															   .toString(),
				destinationPath.toString());
		logger = plugin.getLogger();
		file = new File(this.destinationPath.toString());
		silentLoad();
	}
	
	/**
	 * Creates a new file at the specified location or copies an existing one from the resource folder based on the name,
	 * if nothing could be found in the resource folder it creates a new one. name will automatically point to the base of the plugin data folder
	 *
	 * @param plugin plugin instance
	 * @param name Both the name for destination and source
	 */
	public Config(@NotNull JavaPlugin plugin, @NotNull String name) {
		this(plugin, Path.of(name), Path.of(plugin.getDataFolder().getPath(), name));
	}
	
	/**
	 * Creates a new file at the specified location or copies an existing one from the resource folder based on the path,
	 * if nothing could be found in the resource folder it creates a new one. path will automatically point to the base of the plugin data folder
	 *
	 * @param plugin plugin instance
	 * @param path both the source and destination path
	 */
	public Config(@NotNull JavaPlugin plugin, @NotNull Path path) {
		this(plugin, path, Path.of(plugin.getDataFolder().getPath(), path.toString()));
	}
	
	private Config(@NotNull Path sourcePath) {
		this.plugin = null;
		this.name = sourcePath.getFileName().toString();
		this.sourcePath = sourcePath;
		this.destinationPath = null;
		this.file = sourcePath.toFile();
		this.logger = getLogger("Config");
		silentLoad();
	}
	
	/**
	 * This does not save the resource into the plugin dir but references one from a third location
	 *
	 * @param path the
	 */
	public static Config fromExternalPath(@NotNull Path path) {
		return new Config(path);
	}
	
	/**
	 * Gets a section of the config at the set path.
	 *
	 * @param path path inside yml config.
	 * @param deep deep search to get children of children
	 * @return {@link Set} of results.
	 */
	public Set<String> getKeys(String path, boolean deep) {
		if(path == null || path.isBlank()){
			return getKeys(deep);
		}
		ConfigurationSection section = getConfigurationSection(path);
		if(section != null){
			return section.getKeys(deep);
		}
		return new HashSet<>();
	}
	
	/**
	 * gets a section of the config at the set path with a value to automatically cast to
	 *
	 * @param path path inside yml config if blank, uses the root of the config
	 * @param <T> type of the map
	 * @return {@link Map} of results.
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getEntries(String path) {
		if(path == null || path.isBlank()){
			return (Map<String, T>) getValues(false);
		}
		ConfigurationSection section = getConfigurationSection(path);
		if(section != null){
			return (Map<String, T>) section.getValues(false);
		}
		return Map.of();
	}
	
	public @Nullable String getParentPath(@NotNull String path) {
		ConfigurationSection currentSection = getConfigurationSection(path);
		if(currentSection == null){
			return null;
		}
		
		ConfigurationSection configurationSection = currentSection.getParent();
		if(configurationSection == null){
			return null;
		}
		
		return configurationSection.getCurrentPath();
	}
	
	public void setItemStack(String path, ItemStack itemStack) {
		set(path, itemStack.serialize());
	}
	
	public void setLocation(String path, Location location) {
		set(path, location.serialize());
	}
	
	public void updateConfig() {
		
		FileConfiguration existing = YamlConfiguration.loadConfiguration(file);
		
		FileConfiguration newConfig = YamlConfiguration.loadConfiguration(sourcePath.toFile());
		
		for(Entry<String, Object> entry : newConfig.getValues(true).entrySet()){
			if(!existing.contains(entry.getKey())){
				existing.set(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void load() {
		try{
			checkFile();
			load(file);
			logger.log(Level.INFO, "Loaded data from " + name + "!");
		} catch(InvalidConfigurationException | IOException e){
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.WARNING, "Error loading data from " + name + "!");
		}
	}
	
	public void silentLoad() {
		try{
			checkFile();
			load(file);
		} catch(InvalidConfigurationException | IOException e){
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.WARNING, "Error loading data from " + name + "!");
		}
	}
	
	public void save() {
		try{
			checkFile();
			save(file);
			logger.log(Level.INFO, "Saved data to " + name + "!");
		} catch(IOException e){
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.WARNING, "Error saving data to " + name + "!");
		}
	}
	
	public void silentSave() {
		try{
			checkFile();
			save(file);
		} catch(IOException e){
			logger.log(Level.WARNING, "Error saving data to " + name + "!");
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public String name() {
		return name;
	}
	
	public String path() {
		return destinationPath.toString();
	}
	
	/**
	 * Checks if file exists in path, else create the file and all parent directories needed.
	 */
	protected void checkFile() throws NoSuchFileException {
		if(!file.exists()){
			if(destinationPath == null){
				throw new NoSuchFileException("External Resource does not exist!");
			}
			
			file.getParentFile().mkdirs();
			InputStream inputStream = plugin.getResource(sourcePath.toString().replace(File.separatorChar, '/'));
			if(inputStream != null){
				try{
					Files.copy(inputStream, destinationPath);
				} catch(IOException e){
					logger.log(Level.SEVERE, "Error Copying data from " + sourcePath + " to destination " + destinationPath);
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			} else {
				try{
					file.createNewFile();
				} catch(IOException e){
					throw new IllegalStateException("Cannot create file " + sourcePath + "!", e);
				}
			}
		}
	}
	
	public StoredEntity getEntity(@NotNull String path) {
		ConfigurationSection section = getConfigurationSection(path);
		if(section == null) return null;
		return new StoredEntity(section);
	}
	
	public void setEntity(@NotNull String path, Entity entity) {
		if(entity == null){
			set(path, null);
			return;
		}
		set(path, new StoredEntity(entity));
	}
	
	@Override
	public String toString() {
		return String.format("ConfigYML[path=%s,name=%s]", destinationPath.toString(), name);
	}
	
}