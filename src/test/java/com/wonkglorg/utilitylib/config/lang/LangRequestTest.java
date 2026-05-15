package com.wonkglorg.utilitylib.config.lang;

import com.wonkglorg.utilitylib.config.LangManager;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LangRequestTest{
	
	@Test
	public void canReturnRawSingleInput() {
		LangManager langManager = Mockito.mock(LangManager.class);
		LangConfig config = Mockito.mock(LangConfig.class);
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "test-key", "Nothing Found");
		
		//return element found
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		Mockito.when(config.contains("test-key")).thenReturn(true);
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found Value");
		Assert.assertEquals("Found Value", langRequest.getRawResultSingleLine());
		
		//return first element in list when single result queries a list
		List<String> list = new ArrayList<>();
		list.add("Value 1");
		list.add("Value 2");
		Mockito.when(config.getStringList("test-key")).thenReturn(list);
		Mockito.when(config.isList("test-key")).thenReturn(true);
		Assert.assertEquals("Value 1", langRequest.getRawResultSingleLine());
	}
	
	@Test
	public void canReturnDefaultRawSingleInput() {
		LangManager langManager = Mockito.mock(LangManager.class);
		LangConfig config = Mockito.mock(LangConfig.class);
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "not-valid-key", "Nothing Found");
		
		//return element found
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		Mockito.when(config.contains("test-key")).thenReturn(true);
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found Value");
		Assert.assertEquals("Nothing Found", langRequest.getRawResultSingleLine());
		
		//return first element in list when single result queries a list
		List<String> list = new ArrayList<>();
		list.add("Value 1");
		list.add("Value 2");
		Mockito.when(config.getStringList("test-key")).thenReturn(list);
		Mockito.when(config.isList("test-key")).thenReturn(true);
		Assert.assertEquals("Nothing Found", langRequest.getRawResultSingleLine());
	}
	
	@Test
	public void canReturnRawInput() {
		LangManager langManager = Mockito.mock(LangManager.class);
		LangConfig config = Mockito.mock(LangConfig.class);
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "test-key", "Nothing Found");
		
		//return element found
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		Mockito.when(config.contains("test-key")).thenReturn(true);
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found Value");
		Assert.assertEquals(List.of("Found Value"), langRequest.getRawResult());
		
		//return first element in list when single result queries a list
		List<String> list = new ArrayList<>();
		list.add("Value 1");
		list.add("Value 2");
		Mockito.when(config.getStringList("test-key")).thenReturn(list);
		Mockito.when(config.isList("test-key")).thenReturn(true);
		Assert.assertEquals(List.of("Value 1", "Value 2"), langRequest.getRawResult());
	}
	
	@Test
	public void canReturnDefaultRawInput() {
		LangManager langManager = Mockito.mock(LangManager.class);
		LangConfig config = Mockito.mock(LangConfig.class);
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "not-valid-key", "Nothing Found");
		
		//return element found
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		Mockito.when(config.contains("test-key")).thenReturn(true);
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found Value");
		Assert.assertEquals(List.of("Nothing Found"), langRequest.getRawResult());
		
		//return first element in list when single result queries a list
		List<String> list = new ArrayList<>();
		list.add("Value 1");
		list.add("Value 2");
		Mockito.when(config.getStringList("test-key")).thenReturn(list);
		Mockito.when(config.isList("test-key")).thenReturn(true);
		Assert.assertEquals(List.of("Nothing Found"), langRequest.getRawResult());
	}
	
	@Test
	public void canPickRightConfig() {
		JavaPlugin plugin = Mockito.mock(JavaPlugin.class);
		Mockito.when(plugin.namespace()).thenReturn("testplugin");
		LangManager langManager = LangManager.getInstance(plugin);
		LangConfig configEng = Mockito.mock(LangConfig.class);
		LangConfig configGer = Mockito.mock(LangConfig.class);
		
		Assert.assertEquals(Optional.empty(), langManager.getAnyValidLangConfig(Locale.ENGLISH));
		
		langManager.addLanguage(configEng, Locale.ENGLISH);
		langManager.addLanguage(configGer, Locale.GERMAN, Locale.GERMANY);
		
		Assert.assertEquals(3, langManager.getAllLangs().size());
		
		Assert.assertEquals(Optional.of(configEng), langManager.getAnyValidLangConfig(Locale.ENGLISH));
		Assert.assertEquals(Optional.of(configGer), langManager.getAnyValidLangConfig(Locale.GERMAN));
		Assert.assertEquals(Optional.of(configGer), langManager.getAnyValidLangConfig(Locale.GERMANY));
		
		//returns any config it can find
		Assert.assertNotEquals(Optional.empty(), langManager.getAnyValidLangConfig(Locale.FRENCH));
		langManager.setDefaultLang(Locale.ENGLISH);
		//returns the english one
		Assert.assertEquals(Optional.of(configEng), langManager.getAnyValidLangConfig(Locale.FRENCH));
	}
	
	@Test
	public void canReplacePlaceholders() {
		LangManager langManager = Mockito.mock(LangManager.class);
		LangConfig config = Mockito.mock(LangConfig.class);
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "test-key", "Nothing Found").replace("%placeholder%","Resolved");
		
		//return element found
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		Mockito.when(config.contains("test-key")).thenReturn(true);
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found Value %placeholder%");
		Assert.assertEquals(List.of("Found Value Resolved"), langRequest.processRawResult(langRequest.getRawResult()));
	}
	
	@Test
	public void canReplaceConditionals() {
		LangManager langManager = Mockito.mock(LangManager.class);
		LangConfig config = Mockito.mock(LangConfig.class);
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "test-key", "Nothing Found")
				.replace("%placeholder%","Resolved");
		
		//return element found
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		Mockito.when(config.contains("test-key")).thenReturn(true);
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found <if:isSet>Value %placeholder%<else>Nothing</if>");
		//not flags were so don't process this besides placeholder replacements
		Assert.assertEquals(List.of("Found <if:isSet>Value Resolved<else>Nothing</if>"), langRequest.processRawResult(langRequest.getRawResult()));
		langRequest.conditional("isSet",false);
		Assert.assertEquals(List.of("Found Nothing"), langRequest.processRawResult(langRequest.getRawResult()));
		langRequest.conditional("isSet",true);
		Assert.assertEquals(List.of("Found Value Resolved"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found <if:isSet>Value %placeholder%</if>");
		Assert.assertEquals(List.of("Found Value Resolved"), langRequest.processRawResult(langRequest.getRawResult()));
		langRequest.conditional("isSet",false);
		Assert.assertEquals(List.of("Found "), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found <if:!isSet>Value %placeholder%</if>");
		Assert.assertEquals(List.of("Found Value Resolved"), langRequest.processRawResult(langRequest.getRawResult()));
		langRequest.conditional("isSet",true);
		Assert.assertEquals(List.of("Found "), langRequest.processRawResult(langRequest.getRawResult()));
	}
	
}
