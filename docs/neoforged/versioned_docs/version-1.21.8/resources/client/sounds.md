# Sounds

Sounds, while not required for anything, can make a mod feel much more nuanced and alive. Minecraft offers you various ways to register and play sounds, which will be laid out in this article.

## Terminology

The Minecraft sound engine uses a variety of terms to refer to different things:

- **Sound event**: A sound event is an in-code trigger that tells the sound engine to play a certain sound. `SoundEvent`s are also the things you register to the game.
- **Sound category** or **sound source**: Sound categories are rough groupings of sounds that can be individually toggled. The sliders in the sound options GUI represent these categories: `master`, `block`, `player` etc. In code, they can be found in the `SoundSource` enum.
- **Sound definition**: A mapping of a sound event to one or multiple sound objects, plus some optional metadata. Sound definitions are located in a namespace's [`sounds.json` file][soundsjson].
- **Sound object**: A JSON object consisting of a sound file location, plus some optional metadata.
- **Sound file**: An on-disk sound file. Minecraft only supports `.ogg` sound files.

:::danger
Due to the implementation of OpenAL (Minecraft's audio library), for your sound to have attenuation - that is, for it to get quieter and louder depending on the player's distance to it -, your sound file must be mono (single channel). Stereo (multichannel) sound files will not be subject to attenuation and always play at the player's location, making them ideal for ambient sounds and background music. See also [MC-146721][bug].
:::

## Creating `SoundEvent`s

`SoundEvent`s are [registered objects][registration], meaning that they must be registered to the game through a `DeferredRegister` and be singletons:

```java
public class MySoundsClass {
    // Assuming that your mod id is examplemod
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "examplemod");
    
    // All vanilla sounds use variable range events.
    public static final Holder<SoundEvent> MY_SOUND = SOUND_EVENTS.register(
            "my_sound",
            // Takes in the registry name
            SoundEvent::createVariableRangeEvent
    );
    
    // There is a currently unused method to register fixed range (= non-attenuating) events as well:
    public static final Holder<SoundEvent> MY_FIXED_SOUND = SOUND_EVENTS.register(
            "my_fixed_sound",
            // 16 is the default range of sounds. Be aware that due to OpenAL limitations,
            // values above 16 have no effect and will be capped to 16.
            registryName -> SoundEvent.createFixedRangeEvent(registryName, 16f)
    );
}
```

Of course, don't forget to add your registry to the [mod event bus][modbus] in the [mod constructor][modctor]:

```java
public ExampleMod(IEventBus modBus) {
    MySoundsClass.SOUND_EVENTS.register(modBus);
    // other things here
}
```

And voilà, you have a sound event!

## `sounds.json`

_See also: [sounds.json][mcwikisounds] on the [Minecraft Wiki][mcwiki]_

Now, to connect your sound event to actual sound files, we need to create sound definitions. All sound definitions for a namespace are stored in a single file named `sounds.json`, also known as the sound definitions file, directly in the namespace's root. Every sound definition is a mapping of sound event id (e.g. `my_sound`) to a JSON sound object. Note that the sound event ids do not specify a namespace, as that is already determined by the namespace the sound definitions file is in. An example `sounds.json` would look something like this:

```json5
{
    // Sound definition for the sound event "examplemod:my_sound"
    "my_sound": {
        // List of sound objects. If this contains more than one element, an element will be chosen randomly.
        "sounds": [
            // Only name is required, all other properties are optional.
            {
                // Location of the sound file, relative to the namespace's sounds folder.
                // This example references a sound at assets/examplemod/sounds/sound_1.ogg.
                "name": "examplemod:sound_1",
                // May be "sound" or "event". "sound" causes the name to refer to a sound file.
                // "event" causes the name to refer to another sound event. Defaults to "sound".
                "type": "sound",
                // The volume this sound will be played at. Must be between 0.0 and 1.0 (default).
                "volume": 0.8,
                // The pitch value the sound will be played at.
                // Must be between 0.0 and 2.0. Defaults to 1.0.
                "pitch": 1.1,
                // Weight of this sound when choosing a sound from the sounds list. Defaults to 1.
                "weight": 3,
                // If true, the sound will be streamed from the file instead of loaded all at once.
                // Recommended for sound files that are more than a few seconds long. Defaults to false.
                "stream": true,
                // Manual override for the attenuation distance. Defaults to 16. Ignored by fixed range sound events.
                "attenuation_distance": 8,
                // If true, the sound will be loaded into memory on pack load, instead of when the sound is played.
                // Vanilla uses this for underwater ambience sounds. Defaults to false.
                "preload": true
            },
            // Shortcut for { "name": "examplemod:sound_2" }
            "examplemod:sound_2"
        ]
    },
    "my_fixed_sound": {
        // Optional. If true, replaces sounds from other resource packs instead of adding to them.
        // See the Merging chapter below for more information.
        "replace": true,
        // The translation key of the subtitle displayed when this sound event is triggered.
        "subtitle": "examplemod.my_fixed_sound",
        "sounds": [
            "examplemod:sound_1",
            "examplemod:sound_2"
        ]
    }
}
```

### Merging

Unlike most other resource files, `sounds.json` do not overwrite values in packs below them. Instead, they are merged together and then interpreted as one combined `sounds.json` file. Consider sounds `sound_1`, `sound_2`, `sound_3` and `sound_4` being defined in two `sounds.json` files from two different resource packs RP1 and RP2, where RP2 is placed below RP1:

`sounds.json` in RP1:

```json5
{
    "sound_1": {
        "sounds": [
            "sound_1"
        ]
    },
    "sound_2": {
        "replace": true,
        "sounds": [
            "sound_2"
        ]
    },
    "sound_3": {
        "sounds": [
            "sound_3"
        ]
    },
    "sound_4": {
        "replace": true,
        "sounds": [
            "sound_4"
        ]
    }
}
```

`sounds.json` in RP2:

```json5
{
    "sound_1": {
        "sounds": [
            "sound_5"
        ]
    },
    "sound_2": {
        "sounds": [
            "sound_6"
        ]
    },
    "sound_3": {
        "replace": true,
        "sounds": [
            "sound_7"
        ]
    },
    "sound_4": {
        "replace": true,
        "sounds": [
            "sound_8"
        ]
    }
}
```

The combined (merged) `sounds.json` file the game would then go on and use to load sounds would look something look this (only in memory, this file is never written anywhere):

```json5
{
    "sound_1": {
        // replace false and false: add from lower pack, then from upper pack
        "sounds": [
            "sound_5",
            "sound_1"
        ]
    },
    "sound_2": {
        // replace true in upper pack and false in lower pack: add from upper pack only
        "sounds": [
            "sound_2"
        ]
    },
    "sound_3": {
        // replace false in upper pack and true in lower pack: add from lower pack, then from upper pack
        // Would still discard values from a third resource pack sitting below RP2
        "sounds": [
            "sound_7",
            "sound_3"
        ]
    },
    "sound_4": {
        // replace true and true: add from upper pack only
        "sounds": [
            "sound_8"
        ]
    }
}
```

## Playing Sounds

Minecraft offers various methods to play sounds, and it is sometimes unclear which one should be used. All methods accept a `SoundEvent`, which can either be your own or a vanilla one (vanilla sound events are found in the `SoundEvents` class). For the following method descriptions, client and server refer to the [logical client and logical server][sides], respectively.

### `Level`

- `playSeededSound(Entity entity, double x, double y, double z, Holder<SoundEvent> soundEvent, SoundSource soundSource, float volume, float pitch, long seed)`
    - Client behavior: If the player passed in is the local player, play the sound event to the player at the given location, otherwise no-op.
    - Server behavior: A packet instructing the client to play the sound event to the player at the given location is sent to all players except the one passed in.
    - Usage: Call from client-initiated code that will run on both sides. The server not playing it to the initiating player prevents playing the sound event twice to them. Alternatively, call from server-initiated code (e.g. a [block entity][be]) with a `null` player to play the sound to everyone.
- `playSound(Entity entity, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch)`
    - Forwards to `playSeededSound` with a random seed selected and the holder wrapped around the `SoundEvent`
- `playSound(Entity entity, BlockPos pos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch)`
    - Forwards to the above method with `x`, `y` and `z` taking the values of `pos.getX() + 0.5`, `pos.getY() + 0.5` and `pos.getZ() + 0.5`, respectively.
- `playLocalSound(double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean distanceDelay)`
    - Client behavior: Plays the sound to the player at the given location. Does not send anything to the server. If `distanceDelay` is `true`, delays the sound based on the distance to the player.
    - Server behavior: No-op.
    - Usage: Called from custom packets sent from the server. Vanilla uses this for thunder sounds.
- `playPlayerSound(SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch)`
    - Client behavior: Plays the sound that is bound to the player's location. Does not send anything to the server.
    - Server behavior: No-op.
    - Usage: Vanilla uses this for ambient block sounds.

### `ClientLevel`

- `playLocalSound(BlockPos pos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean distanceDelay)`
    - Forwards to `Level#playLocalSound` with `x`, `y` and `z` taking the values of `pos.getX() + 0.5`, `pos.getY() + 0.5` and `pos.getZ() + 0.5`, respectively.

### `Entity`

- `playSound(SoundEvent soundEvent, float volume, float pitch)`
    - Forwards to `Level#playSound` with `null` as the player, `Entity#getSoundSource` as the sound source, the entity's position for x/y/z, and the other parameters passed in.

### `Player`

- `playSound(SoundEvent soundEvent, float volume, float pitch)` (overrides the method in `Entity`)
    - Forwards to `Level#playSound` with `this` as the player, `SoundSource.PLAYER` as the sound source, the player's position for x/y/z, and the other parameters passed in. As such, the client/server behavior mimics the one from `Level#playSound`:
        - Client behavior: Play the sound event to the client player at the given location.
        - Server behavior: Play the sound event to everyone near the given location except the player this method was called on.

## Datagen

Sound files themselves can of course not be [datagenned][datagen], but `sounds.json` files can. To do so, we extend `SoundDefinitionsProvider` and override the `registerSounds()` method:

```java
public class MySoundDefinitionsProvider extends SoundDefinitionsProvider {
    // Parameters can be obtained from `GatherDataEvent.Client`.
    public MySoundDefinitionsProvider(PackOutput output) {
        // Use your actual mod id instead of "examplemod".
        super(output, "examplemod");
    }

    @Override
    public void registerSounds() {
        // Accepts a Supplier<SoundEvent>, a SoundEvent, or a ResourceLocation as the first parameter.
        add(MySoundsClass.MY_SOUND, SoundDefinition.definition()
            // Add sound objects to the sound definition. Parameter is a vararg.
            .with(
                // Accepts either a string or a ResourceLocation as the first parameter.
                // The second parameter can be either SOUND or EVENT, and can be omitted if the former.
                sound("examplemod:sound_1", SoundDefinition.SoundType.SOUND)
                    // Sets the volume. Also has a double counterpart.
                    .volume(0.8f)
                    // Sets the pitch. Also has a double counterpart.
                    .pitch(1.2f)
                    // Sets the weight.
                    .weight(2)
                    // Sets the attenuation distance.
                    .attenuationDistance(8)
                    // Enables streaming.
                    // Also has a parameterless overload that defers to stream(true).
                    .stream(true)
                    // Enables preloading.
                    // Also has a parameterless overload that defers to preload(true).
                    .preload(true),
                // The shortest we can get.
                sound("examplemod:sound_2")
            )
            // Sets the subtitle.
            .subtitle("sound.examplemod.sound_1")
            // Enables replacing.
            .replace(true)
        );
    }
}
```

As with every data provider, don't forget to register the provider to the event:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createProvider(MySoundDefinitionsProvider::new);
}
```

[bug]: https://bugs.mojang.com/browse/MC-146721
[datagen]: ../index.md#data-generation
[mcwiki]: https://minecraft.wiki
[mcwikisounds]: https://minecraft.wiki/w/Sounds.json
[modbus]: ../../concepts/events.md#event-buses
[modctor]: ../../gettingstarted/modfiles.md#javafml-and-mod
[registration]: ../../concepts/registries.md
[sides]: ../../concepts/sides.md#the-logical-side
[soundsjson]: #soundsjson
