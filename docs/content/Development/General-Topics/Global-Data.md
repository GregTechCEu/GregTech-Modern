---
title: Global Caches / Data
---


# Storing Data Globally

In certain cases (e.g. in a cache that holds all currently loaded instances of a machine), you might need to store data
in a global (static and mutable) variable.

When doing so, you need to ensure that remote and serverside instances don't get mixed up.


## Using `SideLocal<T>`

!!! warning inline end "Not yet merged<br>_Branch: `mi-ender-link`_"

To make working with this requirement easier, You can use `SideLocal<T>` to store your global data.
It is similar to Java's `ThreadLocal`, but operates on the game's sides instead.

If you are currently on the remote side (`GTCEuAPI.isClientThread()` / on the client's `main` thread), it will return the
remote side's instance of your data. Otherwise, you will get the server side's instance.

??? example "Example Usage"
    
    ```java
    public class MyCache {
        private static SideLocal<Map<UUID, MyData>> cache = new SideLocal<>(HashMap::new);
    
        public static void cacheData(UUID id, MyData data) {
            cache.get().put(id, data);
        }
    
        public static MyData getData(UUID id) {
            return cache.get().get(id);
        }
    }
    ```
    
    Alternatively to passing an initializer for both instances to `SideLocal`'s constructor, you can also supply
    separate instances for the remote and server side.