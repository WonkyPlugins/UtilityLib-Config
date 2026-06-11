# UtilityLib - Config

![alt text](https://github.com/Wonkglorg/Minecraft-UtilityLib/blob/master/Logo.png?raw=true)

## Index

* [Introduction](#introduction)
* [Requirements](#requirements)
* [Installation](#installation)
* [Overview](#overview)
* [Guides](#guide)
* [Credits](#credits)

## <a name="introduction"></a>Introduction

Lightweight Config and Language manager library.

## <a name="requirements"></a>Requirements

* Spigot
* Minecraft 1.16.* and above
* JAVA 16 or above

## <a name="installation"></a>Installation

Repository

```yml
<repository>
<id>jitpack.io</id>
<url>https://jitpack.io</url>
</repository>
```

Adding the dependency

```yml
<dependency>
<groupId>com.github.Wonkglorg</groupId>
<artifactId>UtilityLib-Config</artifactId>
<version>version</version>
</dependency>
```

## <a name="overview"></a>Overview

### Initialisation

Before using the either of the Managers they need to be instantiated by a static createInstance function (this should be
used before calling any of its methods, preferably in the onLoad Section of the plugin.

```java
import com.wonkglorg.utilitylib.config.ConfigManager;
import com.wonkglorg.utilitylib.config.LangManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin{
	private final ConfigManager configManager;
	private final LangManager langManager;
	
	@Override
	
	public void onLoad() {
		configManager = ConfigManager.getInstance(this);
		langManager = LangManager.getInstance(this);
	}
	
	@Override
	public void onEnable() {
	
	}
	
	@Override
	public void onDisable() {
	
	}
}
```

## ConfigManager

Once the instance is created, it can be used to register any valid config

```java

@Override
public void onEnable() {
	configManager.add("items", new Config(this, Path.of("subdirectory", "items.yml")));
}
```

Once the instance is asigned it can be called anywhere by its defined name "items" these can be retrieved by the
ConfigManager#getConfig function

## Langmanager

Langmanager works very similar but instead of a dedicated name works by a system of Locale defined configs, this defines
when to show what config files value, a default lang can also be defined to use if no valid lang was found for the
desired Locale

```java

import com.wonkglorg.utilitylib.config.LangManager;
import com.wonkglorg.utilitylib.config.types.LangConfig;

@Override
public void onEnable() {
	langManager.setDefaultLang(new LangConfig(this, "path/to/en-us.yml"), Locale.ENGLISH);
	langManager.addLanguage(new LangConfig(this, "path/to/de.yml"), Locale.GERMAN);
	//or auto detect all lang files from a specific path
	langManager.addAllLangFilesFromPath(Path.of("path", "to", "langs"));
}
```

To retrieve a value the Langmanager#request function can be used which determines the best value to return based on
inputs and allows further modifying the request. each config entry can be a single value or a list of values.

### Placeholders

Replace resolved any placeholders defined in the lang yml, minimessage components can be resolved internaly without
extra parsing

Code
```java
   lang.request("command.givehead.inventory-full").replace("%target%",target.getName()).sendToAudience(sender);
```

Config

```yml
command:
  givehead:
    inventory-fill: The inventory of %target% is full.
```

### Conditional Values

Conditional values can be used to adjust the flow either via dedicated conditional flags or == < and > operators

Code

```java
   boolean hasTarget = target != null;
   int remainingSlots = 0;
   lang.request("command.givehead-inventory.full")
       .replace("%target%", hasTarget ? target.getName() : null)
       .replace("%remaining-slots%",remainingSlots)
       .conditional("%hasTarget%",hasTarget) 
       .sendToAudience(sender);
```

Config

```yml
command:
  givehead:
    is-valid-target: [if:%hasTarget%]Adding Head to targets inventory![else]Invalid Target![/if]
    full-inventory: [if:%remainingSlots%>0]Added Head, the target has<math>%remainingSlots%-1</math> slots free! [else]The inventory of %target% is full![/if]
```


### Math Operations

Math operations allow for basic math handling including + - * / () and nested operations.

Code

```java
   lang.request("command.transactions.result")
       .replace("%amount-sold%", 20)
       .replace("%price-per-item%",2)
       .sendToAudience(sender);
```

Config

```yml
command:
  givehead:
    inventory-full: You sold <math>%amount-sold%*%price-per-item%</math>
```


## <a name="credits"></a>Credits

This plugin is being developed by [Wonkglorg](https://gitlab.com/u/Wonkglorg).
