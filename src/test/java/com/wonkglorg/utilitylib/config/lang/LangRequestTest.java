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
import java.util.logging.Logger;

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
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "test-key", "Nothing Found").replace("%placeholder%", "Resolved");
		
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
		
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "test-key", "Nothing Found").replace("%placeholder%", "Resolved");
		
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		
		Mockito.when(config.contains("test-key")).thenReturn(true);
		
		// ---------------- BASIC IF / ELSE ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found [if:isSet]Value %placeholder%[else]Nothing[/if]");
		
		langRequest.replace("isSet", false);
		Assert.assertEquals(List.of("Found Nothing"), langRequest.processRawResult(langRequest.getRawResult()));
		
		langRequest.replace("isSet", true);
		Assert.assertEquals(List.of("Found Value Resolved"), langRequest.processRawResult(langRequest.getRawResult()));
		
		// ---------------- IF WITHOUT ELSE ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found [if:isSet]Value %placeholder%[/if]");
		
		langRequest.replace("isSet", true);
		Assert.assertEquals(List.of("Found Value Resolved"), langRequest.processRawResult(langRequest.getRawResult()));
		
		langRequest.replace("isSet", false);
		Assert.assertEquals(List.of("Found "), langRequest.processRawResult(langRequest.getRawResult()));
		
		// ---------------- NOT OPERATOR ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Found [if:!isSet]Value %placeholder%[/if]");
		
		langRequest.replace("isSet", false);
		Assert.assertEquals(List.of("Found Value Resolved"), langRequest.processRawResult(langRequest.getRawResult()));
		
		langRequest.replace("isSet", true);
		Assert.assertEquals(List.of("Found "), langRequest.processRawResult(langRequest.getRawResult()));
		
		// ---------------- MULTI CONDITION ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Result [if:isSet && !banned]OK[else]DENIED[/if]");
		
		langRequest.replace("isSet", true);
		langRequest.replace("banned", false);
		
		Assert.assertEquals(List.of("Result OK"), langRequest.processRawResult(langRequest.getRawResult()));
		
		langRequest.replace("banned", true);
		
		Assert.assertEquals(List.of("Result DENIED"), langRequest.processRawResult(langRequest.getRawResult()));
		
		// ---------------- NESTED CONDITIONS ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("[if:isSet]Start [if:!banned]Allowed[else]Blocked[/if][/if]");
		
		langRequest.replace("isSet", true);
		langRequest.replace("banned", false);
		
		Assert.assertEquals(List.of("Start Allowed"), langRequest.processRawResult(langRequest.getRawResult()));
		
		langRequest.replace("banned", true);
		
		Assert.assertEquals(List.of("Start Blocked"), langRequest.processRawResult(langRequest.getRawResult()));
		
		// ---------------- NUMERIC COMPARISON ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Coins [if:%coins% > 10]Rich[else]Poor[/if]");
		
		langRequest.replace("%coins%", 15);
		
		Assert.assertEquals(List.of("Coins Rich"), langRequest.processRawResult(langRequest.getRawResult()));
		
		langRequest.replace("%coins%", 5);
		
		Assert.assertEquals(List.of("Coins Poor"), langRequest.processRawResult(langRequest.getRawResult()));
		
		// ---------------- EMPTY ELSE EDGE CASE ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("Test [if:false]YES[else][/if]");
		
		Assert.assertEquals(List.of("Test "), langRequest.processRawResult(langRequest.getRawResult()));
		
		// ---------------- COMPLEX REALISTIC SHOP STRING ----------------
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<gold>%number%. %user% trades <blue>%item-count% <gray>%item% " +
																			   "<gold>for [if:isFree]<aqua>Free[else]<blue>%price% <gray>%secondary-item%[/if]! " +
																			   "<gold>(Overfilled)");
		
		langRequest.replace("isFree", true);
		
		Assert.assertEquals("<gold>%number%. %user% trades <blue>%item-count% <gray>%item% " + "<gold>for <aqua>Free! <gold>(Overfilled)",
				langRequest.processRawResult(langRequest.getRawResultSingleLine()));
	}
	
	@Test
	public void canResolveMath() {
		LangManager langManager = Mockito.mock(LangManager.class);
		Mockito.when(langManager.getLogger()).thenReturn(Logger.getLogger(LangRequestTest.class.getName()));
		LangConfig config = Mockito.mock(LangConfig.class);
		LangRequest langRequest = new LangRequest(langManager, Locale.ENGLISH, "test-key", "Nothing Found").replace("%amount%", 4);
		
		Mockito.when(langManager.getAnyValidLangConfig(Locale.ENGLISH)).thenReturn(Optional.of(config));
		Mockito.when(config.contains("test-key")).thenReturn(true);
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("1+1=<math>1+1</math>");
		Assert.assertEquals(List.of("1+1=2"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("1+%amount%=<math>1+%amount%</math>");
		Assert.assertEquals(List.of("1+4=5"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>2*%amount%</math>");
		Assert.assertEquals(List.of("8"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>2/%amount%</math>");
		Assert.assertEquals(List.of("0.5"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>10-4</math>");
		Assert.assertEquals(List.of("6"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>2+3*4</math>");
		Assert.assertEquals(List.of("14"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>(2+3)*4</math>");
		Assert.assertEquals(List.of("20"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>((2+3)*4)+1</math>");
		Assert.assertEquals(List.of("21"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>2.5*4</math>");
		Assert.assertEquals(List.of("10"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>5/2</math>");
		Assert.assertEquals(List.of("2.5"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>-5+10</math>");
		Assert.assertEquals(List.of("5"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>2-10</math>");
		Assert.assertEquals(List.of("-8"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>(%amount%+2)*3</math>");
		Assert.assertEquals(List.of("18"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math> ( 2 + 3 ) * 4 </math>");
		Assert.assertEquals(List.of("20"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>1+1</math> + <math>2+2</math>");
		Assert.assertEquals(List.of("2 + 4"), langRequest.processRawResult(langRequest.getRawResult()));
		
		Mockito.when(config.getString("test-key", "Nothing Found")).thenReturn("<math>(%amount%+A)*3</math>");
		Assert.assertEquals(List.of("!INVALID_OPERATION!"), langRequest.processRawResult(langRequest.getRawResult()));
	}
	
}
