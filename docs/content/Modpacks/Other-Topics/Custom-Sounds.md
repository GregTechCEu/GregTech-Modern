---
title: Custom Sounds
---


## Creating a custom sound

!!! Warning
    Registering custom sounds is currently only supported in Java. Although you can call your sound in kubejs once defined.

To add a new sound, a sounds class is required. 
This class prepares for registrate to register the sounds. 
An example of a custom sound can be found below.

```java
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class ExampleSound {

    public static final SoundEntry MICROVERSE = REGISTRATE.sound(examplemod.id("microverse")).build();

    public static void init() {}
}
```

After you make this class and init it inside your main mod class, you want to setup datagen for the sound.
It's a bit more complicated than normal datagen so an example can be found below.

```java
@Mod.EventBusSubscriber(modid = examplemod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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

Finally, you need to actually prepare your sound for use. For a sound to be registered it must be in .ogg format and be inside assets/examplemod/sounds. 
Mono sounds are localized to the area around the machine, while stereo sounds are global.
