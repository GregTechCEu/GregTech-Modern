# Particles

Particles are 2D effects that polish the game and add immersion. They can be spawned both client and server [side], but being mostly visual in nature, critical parts exist only on the physical (and logical) client side.

## Registering Particles

### `ParticleType`

Particles are registered using `ParticleType`s. These work similar to `EntityType`s or `BlockEntityType`s, in that there's a `Particle` class - every spawned particle is an instance of that class -, and then there's the `ParticleType` class, holding some common information, that is used for registration. `ParticleType`s are a [registry], which means that we want to register them using a `DeferredRegister` like all other registered objects:

```java
public class MyParticleTypes {
    // Assuming that your mod id is examplemod
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "examplemod");
    
    // The easiest way to add new particle types is reusing vanilla's SimpleParticleType.
    // Implementing a custom ParticleType is also possible, see below.
    public static final Supplier<SimpleParticleType> MY_PARTICLE = PARTICLE_TYPES.register(
        // The name of the particle type.
        "my_particle",
        // The supplier. The boolean parameter denotes whether setting the Particles option in the
        // video settings to Minimal will affect this particle type or not; this is false for
        // most vanilla particles, but true for e.g. explosions, campfire smoke, or squid ink.
        () -> new SimpleParticleType(false)
    );
}
```

:::info
A `ParticleType` is only necessary if you need to work with particles on the server side. The client can also use `Particle`s directly.
:::

### `Particle`

A `Particle` is what is later spawned into the world and displayed to the player. While you may extend `Particle` and implement things yourself, in many cases it will be better to extend `TextureSheetParticle` instead, as this class provides helpers for things such as animating and scaling, and also does the actual rendering for you (all of which you'd need to implement yourself if extending `Particle` directly).

Most properties of `Particle`s are controlled by fields such as `gravity`, `lifetime`, `hasPhysics`, `friction`, etc. The only two methods that make sense to implement yourself are `tick` and `move`, both of which do exactly what you'd expect. As such, custom particle classes are often short, consisting e.g. only of a constructor that sets some fields and lets the superclass handle the rest. A basic implementation would look somewhat like this:

```java
public class MyParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    
    // First four parameters are self-explanatory. The SpriteSet parameter is provided by the
    // ParticleProvider, see below. You may also add additional parameters as needed, e.g. xSpeed/ySpeed/zSpeed.
    public MyParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;
        this.gravity = 0; // Our particle floats in midair now, because why not.

        // We set the initial sprite here since ticking is not guaranteed to set the sprite
        // before the render method is called.
        this.setSpriteFromAge(spriteSet);
    }
    
    @Override
    public void tick() {
        // Set the sprite for the current particle age, i.e. advance the animation.
        this.setSpriteFromAge(spriteSet);
        // Let super handle further movement. You may replace this with your own movement if needed.
        // You may also override move() if you only want to modify the built-in movement.
        super.tick();
    }
}
```

### `ParticleProvider`

Next, particle types must register a `ParticleProvider`. `ParticleProvider` is a client-only class responsible for actually creating our `Particle`s through the `createParticle` method. While more elaborate code can be included here, many particle providers are as simple as this:

```java
// The generic type of ParticleProvider must match the type of the particle type this provider is for.
public class MyParticleProvider implements ParticleProvider<SimpleParticleType> {
    // A set of particle sprites.
    private final SpriteSet spriteSet;
    
    // The registration function passes a SpriteSet, so we accept that and store it for further use.
    public MyParticleProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }
    
    // This is where the magic happens. We return a new particle each time this method is called!
    // The type of the first parameter matches the generic type passed to the super interface.
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
            double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        // We don't use the type and speed, and pass in everything else. You may of course use them if needed.
        return new MyParticle(level, x, y, z, spriteSet);
    }
}
```

Your particle provider must then be associated with the particle type in the [client-side][side] [mod bus][modbus] [event] `RegisterParticleProvidersEvent`:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
    // There are multiple ways to register providers, all differing in the functional type they provide in the
    // second parameter. For example, #registerSpriteSet represents a Function<SpriteSet, ParticleProvider<?>>:
    event.registerSpriteSet(MyParticleTypes.MY_PARTICLE.get(), MyParticleProvider::new);
    // Other methods include #registerSprite, which is essentially a Supplier<TextureSheetParticle>,
    // and #registerSpecial, which maps to a Supplier<Particle>. See the source code of the event for further info.
}
```

### Particle Descriptions

Finally, we must associate our particle type with a texture. Similar to how items are associated with an item model, we associate our particle type with what is known as a particle description. A particle description is a JSON file in the `assets/<namespace>/particles` directory and has the same name as the particle type (so for example `my_particle.json` for the above example). The particle definition JSON has the following format:

```json5
{
    // A list of textures that will be played in order. Will loop if necessary.
    // Texture locations are relative to the textures/particle folder.
    "textures": [
        "examplemod:my_particle_0",
        "examplemod:my_particle_1",
        "examplemod:my_particle_2",
        "examplemod:my_particle_3"
    ]
}
```

A particle definition is required when using a particle that takes in a `SpriteSet`, which is done when registering a particle provider via `registerSpriteSet` or `registerSprite`. They must **not** be provided for particle providers registered via `#registerSpecial`.

:::danger
A mismatched list of sprite set particle factories and particle definition files, i.e. a particle description without a corresponding particle factory, or vice versa, will throw an exception!
:::

:::note
While particle descriptions must have providers registered a certain way, they are only used if the `RenderType` of the `ParticleRenderType` (set via `Particle#getRenderType`) uses `TextureAtlas#LOCATION_PARTICLES` for the shader texture. For vanilla particle render types, these are `PARTICLE_SHEET_OPAQUE` and `PARTICLE_SHEET_TRANSLUCENT`.
:::

### Datagen

Particle definition files can also be [datagenned][datagen] by extending `ParticleDescriptionProvider` and overriding the `#addDescriptions()` method:

```java
public class MyParticleDescriptionProvider extends ParticleDescriptionProvider {
    // Get the parameters from `GatherDataEvent.Client`.
    public MyParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    // Assumes that all the referenced particles actually exists. Replace "examplemod" with your mod id.
    @Override
    protected void addDescriptions() {
        // Adds a single sprite particle definition with the file at
        // assets/examplemod/textures/particle/my_single_particle.png.
        sprite(MyParticleTypes.MY_SINGLE_PARTICLE.get(), ResourceLocation.fromNamespaceAndPath("examplemod", "my_single_particle"));
        // Adds a multi sprite particle definition, with a vararg parameter. Alternatively accepts a list.
        spriteSet(MyParticleTypes.MY_MULTI_PARTICLE.get(),
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle_0"),
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle_1"),
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle_2")
        );
        // Alternative for the above, appends "_<index>" to the base name given, for the given amount of textures.
        spriteSet(MyParticleTypes.MY_ALT_MULTI_PARTICLE.get(),
            // The base name.
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle"),
            // The amount of textures.
            3,
            // Whether to reverse the list, i.e. start at the last element instead of the first.
            false
        );
    }
}
```

Don't forget to add the provider to the `GatherDataEvent.Client`:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createProvider(MyParticleDescriptionProvider::new);
}
```

### Custom `ParticleType`s

While for most cases `SimpleParticleType` suffices, it is sometimes necessary to attach additional data to the particle on the server side. This is where a custom `ParticleType` and an associated custom `ParticleOptions` are required. Let's start with the `ParticleOptions`, as that is where the information is actually stored:

```java
public class MyParticleOptions implements ParticleOptions {
    
    // Read and write information, typically for use in commands
    // Since there is no information in this type, this will be an empty string
    public static final MapCodec<MyParticleOptions> CODEC = MapCodec.unit(new MyParticleOptions());

    // Read and write information to the network buffer.
    public static final StreamCodec<ByteBuf, MyParticleOptions> STREAM_CODEC = StreamCodec.unit(new MyParticleOptions());

    // Does not need any parameters, but may define any fields necessary for the particle to work.
    public MyParticleOptions() {}

    @Override
    public ParticleType<?> getType() {
        // Return the registered particle type
    }
}
```

We then use this `ParticleOptions` implementation in our custom `ParticleType`...

```java
public class MyParticleType extends ParticleType<MyParticleOptions> {
    // The boolean parameter again determines whether to limit particles at lower particle settings.
    // See implementation of the MyParticleTypes class near the top of the article for more information.
    public MyParticleType(boolean overrideLimiter) {
        // Pass the deserializer to super.
        super(overrideLimiter);
    }

    @Override
    public MapCodec<MyParticleOptions> codec() {
        return MyParticleOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, MyParticleOptions> streamCodec() {
        return MyParticleOptions.STREAM_CODEC;
    }
}
```

... and reference it during [registration][registry]:

```java
public static final Supplier<MyParticleType> MY_CUSTOM_PARTICLE = PARTICLE_TYPES.register(
    "my_custom_particle",
    () -> new MyParticleType(false)
);
```

The registered particle is then passed into `ParticleOptions#getType`:

```java
public class MyParticleOptions implements ParticleOptions {
    
    // ...

    @Override
    public ParticleType<?> getType() {
        return MY_CUSTOM_PARTICLE.get();
    }
}
```

## Spawning Particles

As a reminder from before, the server only knows `ParticleType`s and `ParticleOption`s, while the client works directly with `Particle`s provided by `ParticleProvider`s that are associated with a `ParticleType`. Consequently, the ways in which particles are spawned are vastly different depending on the side you are on.

- **Common code**: Call `Level#addParticle` or `Level#addAlwaysVisibleParticle`. This is the preferred way of creating particles that are visible to everyone.
- **Client code**: Use the common code way. Alternatively, create a `new Particle()` with the particle class of your choice and call `Minecraft.getInstance().particleEngine#add(Particle)` with that particle. Note that particles added this way will only display for the client and thus not be visible to other players.
- **Server code**: Call `ServerLevel#sendParticles`. Used in vanilla by the `/particle` command.

[datagen]: ../index.md#data-generation
[event]: ../../concepts/events.md
[modbus]: ../../concepts/events.md#event-buses
[registry]: ../../concepts/registries.md#methods-for-registering
[side]: ../../concepts/sides.md
