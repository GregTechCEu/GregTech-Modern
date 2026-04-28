---
sidebar_position: 3
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Events

One of NeoForge's main features is the event system. Events are fired for various things that happen in the game. For example, there are events for when the player right clicks, when a player or another entity jumps, when blocks are rendered, when the game is loaded, etc. A modder can subscribe event handlers to each of these events, and then perform their desired behavior inside these event handlers.

Events are fired on their respective event bus. The most important bus is `NeoForge.EVENT_BUS`, also known as the **game** bus. Besides that, during startup, a mod bus is spawned for each loaded mod and passed into the mod's constructor. Many mod bus events are fired in parallel (as opposed to main bus events that always run on the same thread), dramatically increasing startup speed. See [below][modbus] for more information.

## Registering an Event Handler

There are multiple ways to register event handlers. Common for all of those ways is that every event handler is a method with a single event parameter and no result (i.e. return type `void`).

### `IEventBus#addListener`

The simplest way to register method handlers is by registering their method reference, like so:

```java
@Mod("yourmodid")
public class YourMod {
    public YourMod(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(YourMod::onLivingJump);
    }

    // Heals an entity by half a heart every time they jump.
    private static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        // Only heal on the server side
        if (!entity.level().isClientSide()) {
            entity.heal(1);
        }
    }
}
```

### `@SubscribeEvent`

Alternatively, event handlers can be annotation-driven by creating an event handler method and annotating it with `@SubscribeEvent`. Then, you can pass an instance of the encompassing class to the event bus, registering all `@SubscribeEvent`-annotated event handlers of that instance:

```java
public class EventHandler {
    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            entity.heal(1);
        }
    }
}

@Mod("yourmodid")
public class YourMod {
    public YourMod(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(new EventHandler());
    }
}
```

You can also do it statically. Simply make all event handlers static, and instead of a class instance, pass in the class itself:

```java
public class EventHandler {
	@SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            entity.heal(1);
        }
    }
}

@Mod("yourmodid")
public class YourMod {
    public YourMod(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }
}
```

### `@EventBusSubscriber`

<Tabs defaultValue="latest">
<TabItem value="latest" label="Latest">

We can go one step further and also annotate the event handler class with `@EventBusSubscriber`. This annotation is discovered automatically by NeoForge, allowing you to remove all event-related code from the mod constructor. In essence, it is equivalent to calling `NeoForge.EVENT_BUS.register(EventHandler.class)` and `modBus.register(EventHandler.class)` at the end of the mod constructor. This means that all handlers must be static, too.

</TabItem>
<TabItem value="21.5.78" label="[21.5.0,21.5.78]">

We can go one step further and also annotate the event handler class with `@EventBusSubscriber`. This annotation is discovered automatically by NeoForge, allowing you to remove all event-related code from the mod constructor. In essence, it is equivalent to calling `NeoForge.EVENT_BUS.register(EventHandler.class)` at the end of the mod constructor. This means that all handlers must be static, too.

</TabItem>
</Tabs>

While not required, it is highly recommended to specify the `modid` parameter in the annotation, in order to make debugging easier (especially when it comes to mod conflicts).

```java
@EventBusSubscriber(modid = "yourmodid")
public class EventHandler {
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            entity.heal(1);
        }
    }
}
```

## Event Options

### Fields and Methods

Fields and methods are probably the most obvious part of an event. Most events contain context for the event handler to use, such as an entity causing the event or a level the event occurs in.

### Hierarchy

In order to use the advantages of inheritance, some events do not directly extend `Event`, but one of its subclasses, for example `BlockEvent` (which contains block context for block-related events) or `EntityEvent` (which similarly contains entity context) and its subclasses `LivingEvent` (for `LivingEntity`-specific context) and `PlayerEvent` (for `Player`-specific context). These context-providing super events are `abstract` and cannot be listened to.

:::danger
If you listen to an `abstract` event, your game will crash, as this is never what you want. You always want to listen to one of the subevents instead.
:::

```mermaid
graph TD;
    Event-->BlockEvent;
    BlockEvent-->BlockDropsEvent;
    Event-->EntityEvent;
    EntityEvent-->LivingEvent;
    LivingEvent-->PlayerEvent;
    PlayerEvent-->CanPlayerSleepEvent;

    class Event,BlockEvent,EntityEvent,LivingEvent,PlayerEvent red;
    class BlockDropsEvent,CanPlayerSleepEvent blue;
```

### Cancellable Events

Some events implement the `ICancellableEvent` interface. These events can be cancelled using `#setCanceled(boolean canceled)`, and the cancellation status can be checked using `#isCanceled()`. If an event is cancelled, other event handlers for this event will not run, and some kind of behavior that is associated with "cancelling" is enabled. For example, cancelling `LivingChangeTargetEvent` will prevent the entity's target entity from changing.

Event handlers can opt to explicitly receive cancelled events. This is done by setting the `receiveCanceled` boolean parameter in `IEventBus#addListener` (or `@SubscribeEvent`, depending on your way of attaching the event handlers) to true.

### TriStates and Results

Some events have three potential return states represented by `TriState`, or a `Result` enum directly on the event class. The return states can typically either cancel the action the event is handling (`TriState#FALSE`), force the action to run (`TriState#TRUE`), or execute default Vanilla behavior (`TriState#DEFAULT`).

An event with three potential return states has some `set*` method to set the desired outcome.

```java
// In some event handler class

@SubscribeEvent // on the game event bus
public static void renderNameTag(RenderNameTagEvent.CanRender event) {
    // Uses TriState to set the return state
    event.setCanRender(TriState.FALSE);
}

@SubscribeEvent // on the game event bus
public static void mobDespawn(MobDespawnEvent event) {
    // Uses a Result enum to set the return state
    event.setResult(MobDespawnEvent.Result.DENY);
}
```

### Priority

Event handlers can optionally get assigned a priority. The `EventPriority` enum contains five values: `HIGHEST`, `HIGH`, `NORMAL` (default), `LOW` and `LOWEST`. Event handlers are executed from highest to lowest priority. If they have the same priority, they fire in registration order on the main bus, which is roughly related to mod load order, and in exact mod load order on the mod bus (see below).

Priorities can be defined by setting the `priority` parameter in `IEventBus#addListener` or `@SubscribeEvent`, depending on how you attach event handlers. Note that priorities are ignored for events that are fired in parallel.

### Sided Events

Some events are only fired on one [side][side]. Common examples include the various render events, which are only fired on the client. Since client-only events generally need to access other client-only parts of the Minecraft codebase, they need to be registered accordingly.

Event handlers that use `IEventBus#addListener()` should check the current physical side via `FMLEnvironment.dist` or the `Dist` parameter in your made mod constructor and add the listener in a separate client-only class, as outlined in the article on [sides][side].

Event handlers that use `@EventBusSubscriber` can specify the side as the `value` parameter of the annotation, for example `@EventBusSubscriber(value = Dist.CLIENT, modid = "yourmodid")`.

## Event Buses

While most events are posted on the `NeoForge.EVENT_BUS`, some events are posted on the mod event bus instead. These are generally called mod bus events. Mod bus events can be distinguished from regular events by their superinterface `IModBusEvent`.

<Tabs defaultValue="latest">
<TabItem value="latest" label="Latest">

The mod event bus is passed to you as a parameter in the mod constructor, and you can then subscribe mod bus events to it. If you use `@EventBusSubscriber`, the event will automatically be subscribed to the correct bus.

</TabItem>
<TabItem value="21.5.78" label="[21.5.0,21.5.78]">

The mod event bus is passed to you as a parameter in the mod constructor, and you can then subscribe mod bus events to it. If you use `@EventBusSubscriber`, you can also set the bus as an annotation parameter, like so: `@EventBusSubscriber(bus = Bus.MOD, modid = "yourmodid")`. The default bus is `Bus.GAME`.

</TabItem>
</Tabs>

### The Mod Lifecycle

Most mod bus events are what is known as lifecycle events. Lifecycle events run once in every mod's lifecycle during startup. Many of them are fired in parallel by subclassing `ParallelDispatchEvent`; if you want to run code from one of these events on the main thread, enqueue them using `#enqueueWork(Runnable runnable)`.

The lifecycle generally follows the following order:

- The mod constructor is called. Register your event handlers here, or in the next step.
- All `@EventBusSubscriber`s are called.
- `FMLConstructModEvent` is fired.
- The registry events are fired, these include [`NewRegistryEvent`][newregistry], [`DataPackRegistryEvent.NewRegistry`][newdatapackregistry] and, for each registry, [`RegisterEvent`][registerevent].
- `FMLCommonSetupEvent` is fired. This is where various miscellaneous setup happens.
- The [sided][side] setup is fired: `FMLClientSetupEvent` if on a physical client, and `FMLDedicatedServerSetupEvent` if on a physical server.
- `InterModComms` are handled (see below).
- `FMLLoadCompleteEvent` is fired.

#### `InterModComms`

`InterModComms` is a system that allows modders to send messages to other mods for compatibility features. The class holds the messages for mods, all methods are thread-safe to call. The system is mainly driven by two events: `InterModEnqueueEvent` and `InterModProcessEvent`.

During `InterModEnqueueEvent`, you can use `InterModComms#sendTo` to send messages to other mods. These methods accept the id of the mod to send the message to, the key associated with the message data (to distinguish between different messages), and a `Supplier` holding the message data. The sender can be optionally specified as well.

Then, during `InterModProcessEvent`, you can use `InterModComms#getMessages` to get a stream of all received messages as `IMCMessage` objects. These hold the sender of the data, the intended receiver of the data, the data key, and the supplier for the actual data.

### Other Mod Bus Events

Next to the lifecycle events, there are a few miscellaneous events that are fired on the mod event bus, mostly for legacy reasons. These are generally events where you can register, set up, or initialize various things. Most of these events are not ran in parallel in contrast to the lifecycle events. A few examples:

- `RegisterColorHandlersEvent.Block`, `.ItemTintSources`, `.ColorResolvers` 
- `ModelEvent.BakingCompleted`
- `TextureAtlasStitchedEvent`

:::warning
Most of these events are planned to be moved to the game event bus in a future version.
:::

[modbus]: #event-buses
[newdatapackregistry]: registries.md#custom-datapack-registries
[newregistry]: registries.md#custom-registries
[registerevent]: registries.md#registerevent
[side]: sides.md
