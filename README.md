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

import com.wonkglorg.utilitylib.config.LangManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin{
	private final LangManager langManager;
	
	@Override
	
	public void onLoad() {
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

## Config

A Config class is the base of every yml config and can be created using, this will either copy the existing one to the
location if one is defined in the jar file or add any missing values to the existing config if new values have been
added.

```java
new Config(Path.of("subdirectory", "items.yml"));
```

## Langmanager

Langmanager handles any defined langconfigs to find the best match based on the Locale provided. Langconfigs are defined
inside:

`plugin-datafolder/utility-lib/config/mappings.yml` this file will be automatically created on plugin startup.

```yml
lang:
  default-lang: en

  files:
    - path: test/lang/en.yml
      locales:
        - en

    - path: test/lang/de.yml
      locales:
        - de
        - de_AT
}
```

paths support the placeholder `%plugin-dir%` to reference the current plugins data folder, otherwise all paths are
relative to the execution directory

### Request

To retrieve a value the Langmanager#request function can be used which determines the best value to return based on
inputs and allows further modifying the request. each config entry can be a single value or a list of values.

### Placeholders

Replace resolved any placeholders defined in the lang yml, minimessage components can be resolved internaly without
extra parsing

Code

```java
   lang.request("command.givehead.inventory-full")
       .replace("%target%",target.getName())
       .sendToAudience(sender);
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
       .replace("%target%",hasTarget ? target.getName() :null)
       .replace("%remaining-slots%",remainingSlots)
       .replace("%hasTarget%",hasTarget) 
       .sendToAudience(sender);
```

Config

```yml
command:
  givehead:
    is-valid-target: [ if:%hasTarget% ]Adding Head to targets inventory![else]Invalid Target![/if]
    full-inventory: [ if:%remainingSlots%>0 ]Added Head, the target has<math>%remainingSlots%-1</math> slots free! [else]The inventory of %target% is full![/if]
```

### Math Operations

Math operations allow for basic math handling including + - * / () and nested operations.

Code

```java
   lang.request("command.transactions.result")
       .replace("%amount-sold%",20)
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
