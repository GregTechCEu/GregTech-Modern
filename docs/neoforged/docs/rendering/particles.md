# Client Particles

Particles are visual effects that polish the game and add immersion. Being mostly visual in nature, critical parts exist only on the physical (and logical) client [side].

This article covers the rendering-specific aspects of the particle. For more information on particles types, which are typically used to spawn particles; and particle descriptions, which can specify a particle's sprites, see the companion [particle types][particletype] article. 

## The `Particle` class

A `Particle` defines the client representation of what is spawned in the world and displayed to the player. Most properties and basic physics are controlled by fields such as `gravity`, `lifetime`, `hasPhysics`, `friction`, etc. The only two methods that are commonly overridden are `tick` and `move`, both of which do exactly as their name implies. As such, most custom particles are often short, consisting only a of a constructor that sets the desired fields with the occasional override in the two methods.

The two most common methods for constructing a particle are through subclassing `SingleQuadParticle` for one of its implementations (e.g. `SimpleAnimatedParticle`), which which blits a look-facing texture to the screen; or directly subclassing `Particle`, which gives full control of the [features] being submitted for rendering.

## A Single Quad

Particles that extend `SingleQuadParticle` draw a single quad with some atlas sprite to the screen. There are many helpers provided in the class, from setting the size of the particle (via the `quadSize` field or `scale` method), to tinting the texture (via `setColor` and `setAlpha`). However, the two most important things about a quad particle is the `TextureAtlasSprite` used as the texture, and where that sprite is obtained and rendered through `SingleQuadParticle.Layer`.

First, the `TextureAtlasSprite` is passed into the constructor, either as itself or more likely a `SpriteSet`, representing the texture over its lifetime. Initially, the sprite is set to the protected `sprite` field, but it can be updated during `tick` by calling `setSprite` or `setSpriteFromAge`, respectively.

:::tip
If the `age` or `lifetime` field is updated in the particle constructor, `setSpriteFromAge` should be called to display the appropriate texture.
:::

Then, during the [feature submission process][features], the `SingleQuadParticle.Layer` determines what atlas to use along with the pipeline used to draw the quad to the screen. Vanilla provides six layers by default:

| Layer                | Texture Atlas | For                                                    |
|:--------------------:|:-------------:|:-------------------------------------------------------|
| `OPAQUE_TERRAIN`     | Blocks        | Particles that use block textures with no transparency |
| `TRANSLUCENT_TERRAIN`| Blocks        | Particles that use block textures with transparency    |
| `OPAQUE_ITEMS`       | Items         | Particles that use item textures with no transparency  |
| `TRANSLUCENT_ITEMS`  | Items         | Particles that use item textures with transparency     |
| `OPAQUE`             | Particles     | Particles with no transparency                         |
| `TRANSLUCENT`        | Particles     | Particles with transparency                            |

For ease of convenience, if using one of the vanilla layers, you can call `SingleQuadParticle.Layer#bySprite` and pass in the texture to determine what layer your particle should be in.

Custom layers can be easily created by calling the constructor.

```java
public class MyQuadParticle extends SingleQuadParticle {

    public static final SingleQuadParticle.Layer EXAMPLE_LAYER = new SingleQuadParticle.Layer(
        // Whether the particle will have textures that are not fully opaque.
        true,
        // The texture atlas used to get the sprite from.
        // This should match `TextureAtlasSprite#atlasLocation`.
        TextureAtlas.LOCATION_PARTICLES,
        // The render pipeline used to draw the particle.
        // Custom render pipelines should be based from `RenderPipelines#PARTICLE_SNIPPET`
        // to specify the available uniforms and samplers.
        RenderPipelines.WEATHER_DEPTH_WRITE
    );

    private final SpriteSet spriteSet;

    // First four parameters are self-explanatory.
    // The sprite set or atlas sprite are typically given through the provider, see below.
    // Additional parameters can be added as needed, e.g., xSpeed/ySpeed/zSpeed.
    public MyQuadParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        // Initial sprite set in constructor
        super(level, x, y, z, spriteSet.first());
        this.spriteSet = spriteSet;
        this.gravity = 0; // Our particle floats in midair now, because why not.
    }

    @Override
    public void tick() {
        // Let super handle movement.
        // You may replace this with your own movement if needed.
        // You may also override move() if you only want to modify the built-in movement.
        super.tick();

        // Set the sprite for the current particle age, i.e. advance the animation.
        this.setSpriteFromAge(this.spriteSet);
    }

    @Override
    protected abstract SingleQuadParticle.Layer getLayer() {
        // Sets the layer used to get and submit the texture.
        return EXAMPLE_LAYER;
    }
}
```

:::warning
Particles whose `SingleQuadParticle.Layer` uses `TextureAtlas#LOCATION_PARTICLES` must have an associated [particle description][description]. Otherwise, the textures required by the particle will not be added to the atlas.
:::

## Particle Groups and Render States

If a particle requires something more complex than a quad, then it will need its own `ParticleGroup<P>`, where `P` is the type of the `Particle`. `ParticleGroup`s are responsible for ticking a defined subset of `Particle`s, removing them once `Particle#isAlive` returns false. Each group can queue up to 16,384 particles, evicting the oldest once full. 

```java
// Let's assume we have the following particle class
public class ComplexParticle extends Particle {

    // You are not required to use these fields or store these values.
    // It is up to you to determine what you wish to render and get the
    // appropriate data.
    private final Model.Simple model;
    private final SpriteId sprite;

    public ComplexParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.model = StandingSignRenderer.createSignModel(
            Minecraft.getInstance().getEntityModels(), WoodType.OAK, PlainSignBlock.Attachment.GROUND
        );
        this.sprite = Sheets.getSignSprite(WoodType.OAK);
    }

    public Model.Simple model() {
        return this.model;
    }

    public SpriteId sprite() {
        return this.sprite;
    }
}

// We can create a basic particle group like so
public class ComplexParticleGroup extends ParticleGroup<ComplexParticle> {

    public ComplexParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    // ...
}
```

Once a `Particle` has been added to the `ParticleGroup`, it is extracted during [feature submission][features] to a `ParticleGroupRenderState` via `ParticleGroup#extractRenderState`. `ParticleGroupRenderState` is a mix between a render state containing the extracted particle and a handler to submit the particle elements for rendering (via `#submit`).

```java
// The particle group render state
public record ComplexParticleRenderState(List<ComplexParticleRenderState.Entry> entries) implements ParticleGroupRenderState {

    // Each entry represents a particle in the group
    public record Entry(Model.Simple model, SpriteId sprite, PoseStack pose) {}

    @Override
    public void submit(SubmitNodeCollector collector, CameraRenderState camera) {
        // Submit the particle elements to render
        for (ComplexParticleRenderState.Entry entry : this.entries) {
            collector.submitModel(...);
        }
    }
}

// And in the group...
public class ComplexParticleGroup extends ParticleGroup<ComplexParticle> {

    // ...

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        // Extract the render state from the particles
        List<ComplexParticleRenderState.Entry> entries = new ArrayList<>();

        for (ComplexParticle particle : this.particles) {
            PoseStack pose = new PoseStack();
            pose.pushPose();
            pose.mulPose(camera.rotation());
            entries.add(new ComplexParticleRenderState.Entry(particle.model(), particle.sprite(), pose));
        }

        return new ComplexParticleRenderState(entries);
    }
}
```

On its own, a `Particle` does not know what `ParticleGroup` it belongs to, nor does the `ParticleEngine` know that the group exists. These are all linked together using a `ParticleRenderType`: a unique identifier for the group. The `ParticleRenderType` is linked to the `ParticleGroup` via the [client-side][side] [mod bus][modbus] [event] `RegisterParticleGroupsEvent`. Then, a `Particle` can use the group by setting `Particle#getGroup` to the created type.

```java
// Create the render type
// The string passed in should be a stringified `Identifier`
public static final ParticleRenderType COMPLEX = new ParticleRenderType("examplemod:complex");

@SubscribeEvent // on the mod event bus only on the physical client
public static void registerParticleProviders(RegisterParticleGroupsEvent event) {
    // Link the render type to the particle group
    event.register(COMPLEX, ComplexParticleGroup::new);
}

public class ComplexParticle extends Particle {

    // ...

    @Override
    public ParticleRenderType getGroup() {
        // Tell the particle to render using the particle group
        return COMPLEX;
    }
}
```

## `ParticleProvider`

Once a particle for some particle type has been created, the particle type must be linked through a `ParticleProvider`. `ParticleProvider` is a client-only class responsible for actually creating our `Particle`s from the `ParticleEngine` via `createParticle`. While more elaborate code can be included here, many particle providers are as simple as this:

```java
// The generic type of ParticleProvider must match the type of the particle type this provider is for.
public class MyQuadParticleProvider implements ParticleProvider<SimpleParticleType> {

    // A set of particle sprites.
    private final SpriteSet spriteSet;

    // The registration function passes a SpriteSet, so we accept that and store it for further use.
    // If your particle does not require a SpriteSet, this constructor can be omitted.
    public MyParticleProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    // This is where the magic happens. We return a new particle each time this method is called!
    // The type of the first parameter matches the generic type passed to the super interface.
    @Override
    @Nullable
    public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random
    ) {
        // We don't use the type, speed deltas, or engine random.
        return new MyQuadParticle(level, x, y, z, this.spriteSet);
    }
}
```

Your particle provider must then be associated with the particle type in the [client-side][side] [mod bus][modbus] [event] `RegisterParticleProvidersEvent`:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
    // There are multiple ways to register providers, all differing in the functional type they provide in the
    // second parameter. For example, #registerSpriteSet represents a Function<SpriteSet, ParticleProvider<?>>:
    event.registerSpriteSet(MyParticleTypes.MY_QUAD_PARTICLE.get(), MyQuadParticleProvider::new);

    // #registerSpecial, on the other hand, maps to a ParticleProvider<?>.
    // This should be used if the sprite is not obtained from the particle description.
}
```

:::warning
If `registerSpriteSet` is used, then the particle type must also have an associated [particle description][description]. Otherwise, an exception will be thrown stating it 'Failed to load description'.
:::

[description]: ../resources/client/particles.md
[event]: ../concepts/events.md
[features]: feature.md
[modbus]: ../concepts/events.md#event-buses
[particletype]: ../resources/client/particles.md
[registry]: ../concepts/registries.md#methods-for-registering
[side]: ../concepts/sides.md
