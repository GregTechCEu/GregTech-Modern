# Registering Payloads

Payloads are a way to send arbitrary data between the client and the server. They are registered using the `IPayloadRegistrar` that can be retrieved for a given namespace from the `RegisterPayloadHandlerEvent` event.
```java
@SubscribeEvent // on the mod event bus
public static void register(final RegisterPayloadHandlerEvent event) {
    final IPayloadRegistrar registrar = event.registrar("mymod");
}
```

Assuming we want to send the following data:
```java
public record MyData(String name, int age) {}
```

Then we can implement the `CustomPacketPayload` interface to create a payload that can be used to send and receive this data.
```java
public record MyData(String name, int age) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("mymod", "my_data");
    
    public MyData(final FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readInt());
    }
    
    @Override
    public void write(final FriendlyByteBuf buffer) {
        buffer.writeUtf(name());
        buffer.writeInt(age());
    }
    
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
```
As you can see from the example above the `CustomPacketPayload` interface requires us to implement the `write` and `id` methods. The `write` method is responsible for writing the data to the buffer, and the `id` method is responsible for returning a unique identifier for this payload.
We then also need a reader to register this later on, here we can use a custom constructor to read the data from the buffer.

Finally, we can register this payload with the registrar:
```java
@SubscribeEvent // on the mod event bus
public static void register(final RegisterPayloadHandlerEvent event) {
    final IPayloadRegistrar registrar = event.registrar("mymod");
    registrar.play(MyData.ID, MyData::new, handler -> handler
            .client(ClientPayloadHandler.getInstance()::handleData)
            .server(ServerPayloadHandler.getInstance()::handleData));
}
```
Dissecting the code above we can notice a couple of things:
- The registrar has a `play` method, that can be used for registering payloads which are send during the play phase of the game.
  - Not visible in this code are the methods `configuration` and `common`, however they can also be used to register payloads for the configuration phase. The `common` method can be used to register payloads for both the configuration and play phase simultaneously.
- The constructor of `MyData` is used as a method reference to create a reader for the payload.
- The third argument for the registration method is a callback that can be used to register the handlers for when the payload arrives at either the client or server side.
  - The `client` method is used to register a handler for when the payload arrives at the client side.
  - The `server` method is used to register a handler for when the payload arrives at the server side.
  - There is additionally a secondary registration method `play` on the registrar itself that accepts a handler for both the client and server side, this can be used to register a handler for both sides at once.

Now that we have registered the payload we need to implement a handler.
For this example we will specifically take a look at the client side handler, however the server side handler is very similar.
```java
public class ClientPayloadHandler {
    
    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();
    
    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }
    
    public void handleData(final MyData data, final PlayPayloadContext context) {
        // Do something with the data, on the network thread
        blah(data.name());
        
        // Do something with the data, on the main thread
        context.workHandler().submitAsync(() -> {
            blah(data.age());
        })
        .exceptionally(e -> {
            // Handle exception
            context.packetHandler().disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
            return null;
        });
    }
}
```
Here a couple of things are of note: 
- The handling method here gets the payload, and a contextual object. The contextual object is different for the play and configuration phase, and if you register a common packet, then it will need to accept the super type of both contexts.
- The handler of the payload method is invoked on the networking thread, so it is important to do all the heavy work here, instead of blocking the main game thread.
- If you want to run code on the main game thread you can use the `workHandler` of the context to submit a task to the main thread.
  - The `workHandler` will return a `CompletableFuture` that will be completed on the main thread, and can be used to submit tasks to the main thread.
  - Notice: A `CompletableFuture` is returned, this means that you can chain multiple tasks together, and handle exceptions in a single place.
  - If you do not handle the exception in the `CompletableFuture` then it will be swallowed, **and you will not be notified of it**.

Now that you know how you can facilitate the communication between the client and the server for your mod, you can start implementing your own payloads.
With your own payloads you can then use those to configure the client and server using [Configuration Tasks][]

[Configuration Tasks]: ./configuration-tasks.md
