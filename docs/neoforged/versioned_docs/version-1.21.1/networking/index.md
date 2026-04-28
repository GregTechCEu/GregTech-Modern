# Networking

Communication between servers and clients is the backbone of a successful mod implementation.

There are two primary goals in network communication:

1. Making sure the client view is "in sync" with the server view
    - The flower at coordinates (X, Y, Z) just grew
1. Giving the client a way to tell the server that something has changed about the player
    - the player pressed a key

The most common way to accomplish these goals is to pass messages between the client and the server. These messages will usually be structured, containing data in a particular arrangement, for easy sending and receiving.

There is a technique provided by NeoForge to facilitate communication mostly built on top of [netty]. This technique can be used by listening for the `RegisterPayloadHandlersEvent` event, and then registering a specific type of [payloads], its reader, and its handler function to the registrar.

[netty]: https://netty.io "Netty Website"
[payloads]: ./payload.md "Registering custom Payloads"
