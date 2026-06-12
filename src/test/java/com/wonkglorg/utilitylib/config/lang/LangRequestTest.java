package com.wonkglorg.utilitylib.config.lang;

import com.wonkglorg.utilitylib.config.LangManager;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

class LangRequestTest{
	
	private LangManager lang;
	private LangConfig english;
	private LangConfig german;
	
	@BeforeEach
	void setUp() {
		lang = new LangManager();
		english = new LangConfig(Path.of("test", "request-test.yml"));
		german = new LangConfig(Path.of("test", "request-test-german.yml"));
		lang.addLanguage(english, Locale.ENGLISH);
		lang.addLanguage(german, Locale.GERMAN);
	}
	
	@Test
	void canReturnRawSingleInput() {
		LangRequest request = lang.request("raw.single", "No Value");
		assertEquals("Found Value", request.getRawResultSingleLine());
		
		request = lang.request("raw.list", "No Value");
		assertEquals("Found Value", request.getRawResultSingleLine());
		
		request = lang.request("invalid.path", "No Value");
		assertEquals("No Value", request.getRawResultSingleLine());
		request = lang.request("invalid.path");
		assertEquals("invalid.path", request.getRawResultSingleLine());
	}
	
	@Test
	void canReturnRawInput() {
		LangRequest request = lang.request("raw.single", "No Value");
		assertEquals(List.of("Found Value"), request.getRawResult());
		
		request = lang.request("raw.list", "No Value");
		assertEquals(List.of("Found Value", "Found Value 2"), request.getRawResult());
		
		request = lang.request("invalid.path", "No Value");
		assertEquals(List.of("No Value"), request.getRawResult());
		request = lang.request("invalid.path");
		assertEquals(List.of("invalid.path"), request.getRawResult());
	}
	
	@Test
	void canPickRightConfig() {
		LangManager tempLang = new LangManager();
		
		assertEquals(Optional.empty(), tempLang.getAnyValidLangConfig(Locale.ENGLISH));
		
		tempLang.addLanguage(english, Locale.ENGLISH);
		tempLang.addLanguage(german, Locale.GERMAN, Locale.GERMANY);
		assertEquals(3, tempLang.getAllLangs().size());
		
		assertEquals(Optional.of(english), tempLang.getAnyValidLangConfig(Locale.ENGLISH));
		assertEquals(Optional.of(german), tempLang.getAnyValidLangConfig(Locale.GERMAN));
		assertEquals(Optional.of(german), tempLang.getAnyValidLangConfig(Locale.GERMANY));
		
		//returns any config it can find
		assertNotEquals(Optional.empty(), tempLang.getAnyValidLangConfig(Locale.FRENCH));
		tempLang.setDefaultLang(Locale.ENGLISH);
		//returns the english one
		assertEquals(Optional.of(english), tempLang.getAnyValidLangConfig(Locale.FRENCH));
	}
	
	@Test
	void canIdentifyLangSpecificPlaceholdersAndTemplates() {
		assertEquals(1, english.getTemplateMap().size());
		assertEquals(2, english.getPlaceholderMap().size());
		assertTrue(english.getTemplateMap().containsKey("%currency-display%"));
		assertTrue(english.getPlaceholderMap().containsKey("%euro%"));
		assertTrue(english.getPlaceholderMap().containsKey("%usd%"));
	}
	
	@Test
	void canReplacePlaceholders() {
		assertEquals(List.of("We are using Euro"), lang.request("placeholder.single").toStringResult());
		assertEquals(List.of("We are using Euro", "We are not using USD"), lang.request("placeholder.list").toStringResult());
		
		assertEquals("We are using Euro", lang.request("placeholder.single").toSingleStringResult());
		assertEquals("We are using Euro", lang.request("placeholder.list").toSingleStringResult());
		
		//locale replacements take priority
		assertEquals("We are using Yen", lang.request("placeholder.single").replace("%euro%", "Yen").toSingleStringResult());
	}
	
	@Test
	void canReplaceConditionals() {
		LangRequest request = lang.request("condition.positive");
		request.replace("%hasValue%", true);
		assertEquals("Found Value", request.toSingleStringResult());
		request.replace("%hasValue%", false);
		assertEquals("Found Nothing", request.toSingleStringResult());
		
		request = lang.request("condition.negative");
		request.replace("%hasValue%", true);
		assertEquals("Found Nothing", request.toSingleStringResult());
		request.replace("%hasValue%", false);
		assertEquals("Found Value", request.toSingleStringResult());
		
		request = lang.request("condition.if");
		request.replace("%isBest%", true);
		assertEquals("Found the best Value", request.toSingleStringResult());
		request.replace("%isBest%", false);
		assertEquals("Found Value", request.toSingleStringResult());
		
		request = lang.request("condition.multi-and");
		request.replace("%isSet%", true);
		request.replace("%banned%", false);
		assertEquals("Result OK", request.toSingleStringResult());
		request.replace("%isSet%", false);
		request.replace("%banned%", false);
		assertEquals("Result DENIED", request.toSingleStringResult());
		request.replace("%isSet%", true);
		request.replace("%banned%", true);
		assertEquals("Result DENIED", request.toSingleStringResult());
		request.replace("%isSet%", false);
		request.replace("%banned%", true);
		assertEquals("Result DENIED", request.toSingleStringResult());
		
		request = lang.request("condition.multi-or");
		request.replace("%isOffline%", false);
		request.replace("%banned%", false);
		assertEquals("Result OK", request.toSingleStringResult());
		request.replace("%isOffline%", true);
		request.replace("%banned%", false);
		assertEquals("Result OK", request.toSingleStringResult());
		request.replace("%isOffline%", false);
		request.replace("%banned%", true);
		assertEquals("Result OK", request.toSingleStringResult());
		request.replace("%isOffline%", true);
		request.replace("%banned%", true);
		assertEquals("Result DENIED", request.toSingleStringResult());
		
		request = lang.request("condition.nested");
		request.replace("%requestStart%", false);
		request.replace("%banned%", false);
		assertEquals("", request.toSingleStringResult());
		request.replace("%requestStart%", true);
		assertEquals("Start Allowed", request.toSingleStringResult());
		request.replace("%banned%", true);
		assertEquals("Start Blocked", request.toSingleStringResult());
		
		request = lang.request("condition.numeric-bigger");
		request.replace("%coins%", 9);
		assertEquals("Rich False", request.toSingleStringResult());
		request.replace("%coins%", 10);
		assertEquals("Rich False", request.toSingleStringResult());
		request.replace("%coins%", 11);
		assertEquals("Rich True", request.toSingleStringResult());
		
		request = lang.request("condition.numeric-smaller");
		request.replace("%coins%", 9);
		assertEquals("Poor True", request.toSingleStringResult());
		request.replace("%coins%", 10);
		assertEquals("Poor False", request.toSingleStringResult());
		request.replace("%coins%", 11);
		assertEquals("Poor False", request.toSingleStringResult());
		
		request = lang.request("condition.equals");
		request.replace("%coins%", 9);
		assertEquals("You do not have 10 coins", request.toSingleStringResult());
		request.replace("%coins%", 10);
		assertEquals("You do have 10 coins", request.toSingleStringResult());
		request.replace("%coins%", 11);
		assertEquals("You do not have 10 coins", request.toSingleStringResult());
	}
	
	@Test
	void canResolveMath() {
		LangRequest request = lang.request("math.add-placeholder");
		request.replace("%amount%", 4);
		assertEquals("5", request.toSingleStringResult());
		
		test("math.add", "2");
		test("math.subtract", "0");
		test("math.multiply", "8");
		test("math.divide", "0.5");
		test("math.correct-order", "14");
		test("math.correct-order-brackets", "20");
		test("math.correct-order-all", "21");
		test("math.negative-number-add", "5");
		test("math.number-add-negative", "-8");
		//test("math.nested", "6");
		test("math.invalid", "!INVALID_OPERATION!");
	}
	
	private void test(String key, String expectedResult) {
		var request = lang.request(key);
		assertEquals(expectedResult, request.toSingleStringResult());
	}
	
}
