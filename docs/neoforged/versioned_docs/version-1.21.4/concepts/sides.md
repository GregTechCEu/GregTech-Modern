---
sidebar_position: 2
---
# Sides

Like many other programs, Minecraft follows a client-server concept, where the client is responsible for displaying the data, while the server is responsible for updating them. When using these terms, we have a fairly intuitive understanding of what we mean... right?

Turns out, not so much. A lot of the confusion stems from Minecraft having two different concepts of sides, depending on the context: the physical and the logical side.

## Logical vs. Physical Side

### The Physical Side

When you open your Minecraft launcher, select a Minecraft installation and press play, you boot up a **physical client**. The word "physical" is used here in the sense of "this is a client program". This especially means that client-side functionality, such as all the rendering stuff, is available here and can be used as needed. In contrast, the **physical server**, also known as dedicated server, is what opens when you launch a Minecraft server JAR. While the Minecraft server comes with a rudimentary GUI, it is missing all client-only functionality. Most notably, this means that various client classes are missing from the server JAR. Calling these classes on the physical server will lead to missing class errors, i.e. crashes, so we need to safeguard against this.

### The Logical Side

The logical side is mainly focused on the internal program structure of Minecraft. The **logical server** is where the game logic runs. Things like time and weather changing, entity ticking, entity spawning, etc. all run on the server. All kinds of data, such as inventory contents, are the server's responsibility as well. The **logical client**, on the other hand, is responsible for displaying everything there is to display. Minecraft keeps all the client code in an isolated `net.minecraft.client` package, and runs it in a separate thread called the Render Thread, while everything else is considered common (i.e. client and server) code.

### What's the Difference?

The difference between physical and logical sides is best exemplified by two scenarios:

- The player joins a **multiplayer** world. This is fairly straightforward: The player's physical (and logical) client connects to a physical (and logical) server somewhere else - the player does not care where; so long as they can connect, that's all the client knows of, and all the client needs to know.
- The player joins a **singleplayer** world. This is where things get interesting. The player's physical client spins up a logical server and then, now in the role of the logical client, connects to that logical server on the same machine. If you are familiar with networking, you can think of it as a connection to `localhost` (only conceptually; there are no actual sockets or similar involved).

These two scenarios also show the main problem with this: If a logical server can work with your code, that alone doesn't guarantee that a physical server will be able to work with as well. This is why you should always test with dedicated servers to check for unexpected behavior. `NoClassDefFoundError`s and `ClassNotFoundException`s due to incorrect client and server separation are among the most common errors there are in modding. Another common mistake is working with static fields and accessing them from both logical sides; this is particularly tricky because there's usually no indication that something is wrong.

:::tip
If you need to transfer data from one side to another, you must [send a packet][networking].
:::

In the NeoForge codebase, the physical side is represented by an enum called `Dist`, while the logical side is represented by an enum called `LogicalSide`.

:::info
Historically, server JARs have had classes the client did not. This is not the case anymore in modern versions; physical servers are a subset of physical clients, if you will.
:::

## Performing Side-Specific Operations

### `Level#isClientSide()`

This boolean check will be your most used way to check sides. Querying this field on a `Level` object establishes the  **logical** side the level belongs to: If this field is `true`, the level is running on the logical client. If the field is `false`, the level is running on the logical server. It follows that the physical server will always contain `false` in this field, but we cannot assume that `false` implies a physical server, since this field can also be `false` for the logical server inside a physical client (i.e. a singleplayer world).

Use this check whenever you need to determine if game logic and other mechanics should be run. For example, if you want to damage the player every time they click your block, or have your machine process dirt into diamonds, you should only do so after ensuring `#isClientSide` is `false`. Applying game logic to the logical client can cause desynchronization (ghost entities, desynchronized stats, etc.) in the best case, and crashes in the worst case.

:::tip
This check should be used as your go-to default. Whenever you have a `Level` available, use this check.
:::

### `FMLEnvironment.dist`

`FMLEnvironment.dist` is the **physical** counterpart to a `Level#isClientSide()` check. If this field is `Dist.CLIENT`, you are on a physical client. If the field is `Dist.DEDICATED_SERVER`, you are on a physical server.


#### `@Mod`

Checking the physical environment is important when dealing with client-only classes. The recommended way to separate code that should only be executed on one physical client is by specifying a separate [`@Mod` annotation][mod], setting the `dist` parameter to the physical side the mod class should be loaded on:

```java
@Mod("examplemod")
public class ExampleMod {
    public ExampleMod(IEventBus modBus) {
        // Perform logic in that should be executed on both sides
    }
}

@Mod(value = "examplemod", dist = Dist.CLIENT) 
public class ExampleModClient {
    public ExampleModClient(IEventBus modBus) {
        // Perform logic in that should only be executed on the physical client
        Minecraft.getInstance().whatever();
    }
}

@Mod(value = "examplemod", dist = Dist.DEDICATED_SERVER) 
public class ExampleModDedicatedServer {
    public ExampleModDedicatedServer(IEventBus modBus) {
        // Perform logic in that should only be executed on the physical server
    }
}
```

:::tip
Mods are generally expected to work on either side. This especially means that if you are developing a client-only mod, you should verify that the mod actually runs on a physical client, and no-op in the event that it does not.
:::

[networking]: ../networking/index.md
[mod]: ../gettingstarted/modfiles.md#javafml-and-mod
