# Particles

Particles are visual effects commonly spawned using their associated particle type. They can be spawned both client and server [side], but being mostly visual in nature, critical parts exist only on the physical (and logical) client side.

This article covers the construction and use of particle types and particle descriptions. For more rendering-specific information, see the companion [client particles][clientparticle] article.

## Registering `ParticleType`s

Particles are registered using `ParticleType`s. These work similar to `EntityType`s or `BlockEntityType`s, in that there's a `Particle` class - every spawned particle is an instance of that class -, and then there's the `ParticleType` class, holding some common information, that is used for registration. `ParticleType`s are a [registry], which means that we want to register them using a `DeferredRegister` like all other registered objects:

```java
public class MyParticleTypes {
    // Assuming that your mod id is examplemod
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "examplemod");
    
    // The easiest way to add new particle types is reusing vanilla's SimpleParticleType.
    // Implementing a custom ParticleType is also possible, see below.
    public static final Supplier<SimpleParticleType> MY_QUAD_PARTICLE = PARTICLE_TYPES.register(
        // The name of the particle type.
        "my_quad_particle",
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

## Custom `ParticleType`s

While for most cases `SimpleParticleType` suffices, it is sometimes necessary to attach additional data to the particle on the server side. This is where a custom `ParticleType` and an associated custom `ParticleOptions` are required. Let's start with the `ParticleOptions`, as that is where the information is actually stored:

```java
public class MyParticleOptions implements ParticleOptions {
    
    // A map codec defining additional information for the particle, used e.g. in commands.
    // Since there is no information in our type, use a unit map codec;
    // this corresponds to using an empty string in a command.
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

## Particle Descriptions

Particle descriptions are JSON files in the `assets/<namespace>/particles` directory. A particle description has the same name as its associated [particle type][particletype], and consists of a list of textures relative to `assets/<namespace>/textures/particles`.

A particle description looks something like this:

```json5
{
    // A list of textures that will be played in order. Will loop if necessary.
    // Texture locations are relative to the textures/particle folder.
    "textures": [
        // Points to `assets/examplemod/textures/particle/my_particle_0.png`
        "examplemod:my_particle_0",
        "examplemod:my_particle_1",
        "examplemod:my_particle_2",
        "examplemod:my_particle_3"
    ]
}
```

During resource reload, the `ParticleResources` loads all particle descriptions and stitches the textures into the `TextureAtlas#LOCATION_PARTICLES` atlas. Then, a `SpriteSet` is created for each description, containing a list of the specified `TextureAtlasSprite`s.

### Using the Description

To allow a [particle] to make use of its description, the `ParticleType` must be associated with a [`ParticleProvider`][provider] that takes in the `SpriteSet` using the [client-side][side] [mod bus][modbus] [event] `RegisterParticleProvidersEvent`:

```java
public class MyParticleProvider implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet spriteSet;

    // Take in the sprite set provided by the `ParticleResources`.
    public MyParticleProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    // ...
}

// In some client-only event handler

@SubscribeEvent // on the mod event bus only on the physical client
public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
    // #registerSpriteSet MUST be used when dealing with particle descriptions.
    event.registerSpriteSet(MyParticleTypes.MY_PARTICLE.get(), MyParticleProvider::new);
}
```

:::warning
If a particle description is created for a particle type with no associated `ParticleProvider` via `RegisterParticleProvidersEvent#registerSpriteSet`, then a 'Redundant texture list' message will be logged.
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
        spriteSet(MyParticleTypes.MY_SINGLE_PARTICLE.get(), ResourceLocation.fromNamespaceAndPath("examplemod", "my_single_particle"));
        // Adds a multi sprite particle definition, with a vararg parameter. Alternatively accepts an iterable.
        spriteSet(MyParticleTypes.MY_MULTI_PARTICLE.get(),
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle_0"),
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle_1"),
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle_2")
        );
        // Alternative for the above, appends "_<index>" to the base name given, for the given amount of textures.
        spriteSet(MyParticleTypes.MY_ALT_MULTI_PARTICLE.get(),
            // The base name.
            ResourceLocation.fromNamespaceAndPath("examplemod", "my_multi_particle"),
            // The number of textures.
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

## Spawning Particles

As a reminder from before, the server only knows [`ParticleType`s][particletype] and [`ParticleOption`s][options], while the client works directly with `Particle`s provided by `ParticleProvider`s that are associated with a `ParticleType`. Consequently, the ways in which particles are spawned are vastly different depending on the side you are on.

- **Common code**: Call `Level#addParticle` or `Level#addAlwaysVisibleParticle`. This is the preferred way of creating particles that are visible to everyone.
- **Client code**: Use the common code way. Alternatively, create a `new Particle()` with the particle class of your choice and call `Minecraft.getInstance().particleEngine#add(Particle)` with that particle. Note that particles added this way will only display for the client and thus not be visible to other players.
- **Server code**: Call `ServerLevel#sendParticles`. Used in vanilla by the `/particle` command.

[clientparticle]: ../../rendering/particles.md
[datagen]: ../index.md#data-generation
[event]: ../../concepts/events.md
[modbus]: ../../concepts/events.md#event-buses
[options]: #custom-particletypes
[particle]: ../../rendering/particles.md
[particletype]: #registering-particletypes
[provider]: ../../rendering/particles.md#particleprovider
[side]: ../../concepts/sides.md
