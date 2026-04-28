# Using Configuration Tasks

The networking protocol for the client and server has a specific phase where the server can configure the client before the player actually joins the game.
This phase is called the configuration phase, and is for example used by the vanilla server to send the resource pack information to the client.

This phase can also be used by mods to configure the client before the player joins the game.

## Registering a configuration task
The first step to using the configuration phase is to register a configuration task.
This can be done by registering a new configuration task in the `OnGameConfigurationEvent` event.
```java
@SubscribeEvent // on the mod event bus
public static void register(final OnGameConfigurationEvent event) {
    event.register(new MyConfigurationTask());
}
```
The `OnGameConfigurationEvent` event is fired on the mod bus, and exposes the current listener used by the server to configure the relevant client.
A modder can use the exposed listener to figure out if the client is running the mod, and if so, register a configuration task.

## Implementing a configuration task
A configuration task is a simple interface: `ICustomConfigurationTask`.
This interface has two methods: `void run(Consumer<CustomPacketPayload> sender);`, and `ConfigurationTask.Type type();` which returns the type of the configuration task.
The type is used to identify the configuration task.
An example of a configuration task is shown below:
```java
public record MyConfigurationTask implements ICustomConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(new ResourceLocation("mymod:my_task"));
    
    @Override
    public void run(final Consumer<CustomPacketPayload> sender) {
        final MyData payload = new MyData();
        sender.accept(payload);
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}
```

## Acknowledging a configuration task
Your configuration is executed on the server, and the server needs to know when the next configuration task can be executed.
This is done by acknowledging the execution of said configuration task.

There are two primary ways of achieving this:

### Capturing the listener
When the client does not need to acknowledge the configuration task, then the listener can be captured, and the configuration task can be acknowledged directly on the server side.
```java
public record MyConfigurationTask(ServerConfigurationListener listener) implements ICustomConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(new ResourceLocation("mymod:my_task"));
    
    @Override
    public void run(final Consumer<CustomPacketPayload> sender) {
        final MyData payload = new MyData();
        sender.accept(payload);
        listener.finishCurrentTask(type());
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}
```
To use such a configuration task, the listener needs to be captured in the `OnGameConfigurationEvent` event.
```java
@SubscribeEvent // on the mod event bus
public static void register(final OnGameConfigurationEvent event) {
    event.register(new MyConfigurationTask(event.listener()));
}
```
Then the next configuration task will be executed immediately after the current configuration task has completed, and the client does not need to acknowledge the configuration task.
Additionally, the server will not wait for the client to properly process the send payloads.

### Acknowledging the configuration task
When the client needs to acknowledge the configuration task, then you will need to send your own payload to the client:
```java
public record AckPayload() implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("mymod:ack");
    
    @Override
    public void write(final FriendlyByteBuf buffer) {
        // No data to write
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
```
When a payload from a server side configuration task is properly processed you can send this payload to the server to acknowledge the configuration task.
```java
public void onMyData(MyData data, ConfigurationPayloadContext context) {
    context.submitAsync(() -> {
        blah(data.name());
    })
    .exceptionally(e -> {
        // Handle exception
        context.packetHandler().disconnect(Component.translatable("my_mod.configuration.failed", e.getMessage()));
        return null;
    })
    .thenAccept(v -> {
        context.replyHandler().send(new AckPayload());
    });     
}
```
Where `onMyData` is the handler for the payload that was sent by the server side configuration task.

When the server receives this payload it will acknowledge the configuration task, and the next configuration task will be executed:
```java
public void onAck(AckPayload payload, ConfigurationPayloadContext context) {
    context.taskCompletedHandler().onTaskCompleted(MyConfigurationTask.TYPE);
}
```
Where `onAck` is the handler for the payload that was sent by the client.

## Stalling the login process
When the configuration is not acknowledged, then the server will wait forever, and the client will never join the game.
So it is important to always acknowledge the configuration task, unless the configuration task failed, then you can disconnect the client.
