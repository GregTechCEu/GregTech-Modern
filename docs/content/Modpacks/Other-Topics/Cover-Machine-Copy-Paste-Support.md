---
title: Adding Copy & Paste support to covers and machines
---

The Machine Memory Card item allows for machine settings and covers to be copied to other machines.

To add extra fields to copy, override the following methods on a machine or cover:
```java
/// Copies the current machine/cover config to a CompoundTag.
public CompoundTag copyConfig(CompoundTag tag);
/// Loads a machine/cover config from a CompoundTag.
public void pasteConfig(ServerPlayer player, CompoundTag tag);
/// Returns a list of items (covers, filters, etc) which are needed to copy and paste this machine.
public List<ItemStack> getItemsRequiredToPaste();
```