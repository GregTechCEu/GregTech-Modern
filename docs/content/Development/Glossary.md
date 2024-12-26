---
icon: "material/information-box"
title: "Development Glossary"
search:
    boost: 100
---


<!--
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  IMPORTANT  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    !!                                                                               !!
    !!   When editing this document, always keep the entries in alphabetical order   !!
    !!                                                                               !!
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-->


# :material-information-box: Development Glossary

!!! info
    This is an overview of technical terms that are commonly used in this documentation.  
    If you're not sure what something means (or how it applies to the current context), please refer to this page.


## Client Side

The part of the game running on a player's computer.

It always hosts the [Remote Side](#remote-side).  
In singleplayer mode, the [Server Side](#server-side) is hosted on the client as well. In multiplayer mode, the client
connects to a dedicated server instead.


## Remote Side

!!! info inline end "See also: [Server Side](#server-side)"

The remote side is the part of the game that is **connected to** the game's server side.  
It always runs on the [client](#client-side).

This side may not have the same amount of data available to it as the server does (see [SyncData](../Development/SyncData/index.md) if you
need to automatically synchronize certain data to the remote side).
It also does not perform any tick update logic.


## Server Side

!!! info inline end "See also: [Remote Side](#remote-side)"

The server side is what one or more players connect to.  
In singleplayer mode, it runs on the [client](#client-side). In multiplayer mode, it runs on a dedicated server.

This side usually has all of the world's data available to it and runs tick update logic.  
This is therefore, where [TPS](#tps) impact becomes relevant. In general, use 


## TPS

Short for "ticks per second". Should stay at exactly 20.

See [Tick Updates](General-Topics/Tick-Updates.md) and [Optimization](General-Topics/Optimization.md) for techniques
on how to reduce performance impact.