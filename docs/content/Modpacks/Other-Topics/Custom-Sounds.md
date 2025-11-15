---
title: Custom Sounds
---


## Creating a Custom Sound

!!! Warning
    Registering custom sounds is currently only supported in Java, though you can use your sound in kubejs scripts once it's defined.

To add a new sound, a sounds class is required. 
This class prepares for registrate to register the sounds. 
An example of a custom sound can be found below.

```java
import static com.examplemod.common.registry.ExampleRegistration.REGISTRATE;

public class ExampleSound {

    public static final SoundEntry MICROVERSE = REGISTRATE.sound("microverse").build();

    public static void init() {}
}
```

Before you run datagen, the sound needs to be prepared for use. For a sound to be registered it must be in .ogg format and be inside `assets/examplemod/sounds`. 
!!! note "mono vs. stereo audio"

    Your audio file should be mono, as Minecraft's attentuation logic only works with single-channel audio. Stereo sounds won't fade out (they'll be played at the same volume at any distance from the source) and should only be used for background tracks such as the main menu music. 

After you make this class, prepare your sound, and initialize it in your main mod class, you want to setup datagen for the sounds.
It's a bit more complicated than normal datagen, so an example can be found below.

```java
@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExampleDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();

        if (event.includeClient()) {
            event.getGenerator().addProvider(
                    true,
                    new SoundEntryBuilder.SoundEntryProvider(packOutput, examplemod.MOD_ID));
        }
    }
}

```
