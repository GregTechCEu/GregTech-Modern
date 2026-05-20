---
title: Global Caches / Data
---


# Storing Data Globally

In certain cases (e.g. in a cache that holds all currently loaded instances of a machine), you might need to store data
in a global (static and mutable) variable.

When doing so, you need to ensure that remote and serverside instances don't get mixed up.
