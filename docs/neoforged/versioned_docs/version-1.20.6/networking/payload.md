# Registering Payloads

Payloads are a way to send arbitrary data between the client and the server. They are registered using the `PayloadRegistrar` from the `RegisterPayloadHandlersEvent` event.

```java
@SubscribeEvent // on the mod event bus
public static void register(final RegisterPayloadHandlersEvent event) {
    // Sets the current network version
    final PayloadRegistrar registrar = event.registrar("1");
}
```

Assuming we want to send the following data:

```java
public record MyData(String name, int age) {}
```

Then we can implement the `CustomPacketPayload` interface to create a payload that can be used to send and receive this data.

```java
public record MyData(String name, int age) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<MyData> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation("mymod", "my_data"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // 'name' will be encoded and decoded as a string
    // 'age' will be encoded and decoded as an integer
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<ByteBuf, MyData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        MyData::name,
        ByteBufCodecs.VAR_INT,
        MyData::age,
        MyData::new
    );
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

As you can see from the example above the `CustomPacketPayload` interface requires us to implement the `type` method. The `type` method is responsible for returning a unique identifier for this payload. We then also need a reader to register this later on with the `StreamCodec` to read and write the payload data.

Finally, we can register this payload with the registrar:

```java
@SubscribeEvent // on the mod event bus
public static void register(final RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar("1");
    registrar.playBidirectional(
        MyData.TYPE,
        MyData.STREAM_CODEC,
        new DirectionalPayloadHandler<>(
            ClientPayloadHandler::handleData,
            ServerPayloadHandler::handleData
        )
    );
}
```

Dissecting the code above we can notice a couple of things:
- The registrar has `play*` methods, that can be used for registering payloads which are sent during the play phase of the game.
    - Not visible in this code are the methods `configuration*` and `common*`; however, they can also be used to register payloads for the configuration phase. The `common` method can be used to register payloads for both the configuration and play phase simultaneously.
- The registrar uses a `*Bidirectional` method, that can be used for registering payloads which are sent to both the logical server and logical client.
    - Not visible in this code are the methods `*ToClient` and `*ToServer`; however, they can also be used to register payloads to only the logical client or only the logical server, respectively.
- The type of the payload is used as a unique identifier for the payload.
- The [stream codec][streamcodec] is used to read and write the payload to and from the buffer sent across the network
- The payload handler is a callback for when the payload arrives on one of the logical sides.
    - If a `*Bidirectional` method is used, a `DirectionalPayloadHandler` can be used to provide two separate payload handlers for each of the logical sides.

Now that we have registered the payload we need to implement a handler. For this example we will specifically take a look at the client side handler, however the server side handler is very similar.

```java
public class ClientPayloadHandler {
    
    public static void handleData(final MyData data, final IPayloadContext context) {
        // Do something with the data, on the network thread
        blah(data.name());
        
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
            blah(data.age());
        })
        .exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
            return null;
        });
    }
}
```

Here a couple of things are of note:

- The handling method here gets the payload, and a contextual object.
- The handler of the payload method is invoked on the networking thread, so it is important to do all the heavy work here, instead of blocking the main game thread.
- If you want to run code on the main game thread you can use `enqueueWork` to submit a task to the main thread.
    - The method will return a `CompletableFuture` that will be completed on the main thread.
    - Notice: A `CompletableFuture` is returned, this means that you can chain multiple tasks together, and handle exceptions in a single place.
    - If you do not handle the exception in the `CompletableFuture` then it will be swallowed, **and you will not be notified of it**.

With your own payloads you can then use those to configure the client and server using [Configuration Tasks][configuration].

## Sending Payloads

`CustomPacketPayload`s are sent across the network using vanilla's packet system by wrapping the payload via `ServerboundCustomPayloadPacket` when sending to the server, or `ClientboundCustomPayloadPacket` when sending to the client. Payloads sent to the client can only contain at most 1 MiB of data while payloads to the server can only contain less than 32 KiB. 

All payloads are sent via `Connection#send` with some level of abstraction; however, it is generally inconvenient to call these methods if you want to send packets to multiple people based on a given condition. Therefore, `PacketDistributor` contains a number of convenience implementations to send payloads. There is only one method to send packets to the server (`sendToServer`); however, there are numerous methods to send packets to the client depending on which players should receive the payload.

```java
// ON THE CLIENT

// Send payload to server
PacketDistributor.sendToServer(new MyData(...));

// ON THE SERVER

// Send to one player (ServerPlayer serverPlayer)
PacketDistributor.sendToPlayer(serverPlayer, new MyData(...));

/// Send to all players tracking this chunk (ServerLevel serverLevel, ChunkPos chunkPos)
PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunkPos, new MyData(...));

/// Send to all connected players
PacketDistributor.sendToAllPlayers(new MyData(...));
```

See the `PacketDistributor` class for more implementations.

[configuration]: ./configuration-tasks.md
[streamcodec]: ./streamcodecs.md
