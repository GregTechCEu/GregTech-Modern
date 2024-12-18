---
title: Using SyncData
---


# How To Use SyncData Annotations

For serializing and synchronizing fields, LDLib's annotation-based SyncData system is used.

Please also refer to the [LDLib Wiki](https://github.com/Low-Drag-MC/LDLib-Architectury/wiki/SyncData-Annotations).


## Overview

Here is an overview of the most important annotations:

```java
public class MyMachine {
    @Persisted // (1)
    private String myPersistedField;
    
    @DescSynced // (2)
    private String myClientsideRelevantField;
    
    @DescSynced @RequireRerender // (3)
    private IO io;
    
    @DescSynced @UpdateListener(methodName = "runAdditionalUpdate") // (4)
    private int fieldWithAdditionalClientUpdateLogic;
    
    
    private void runAdditionalUpdate(int newValue, int oldValue) {
        // Run additional clientside update code here
    }
}
```

1. This field is automatically serialized to and deserialized from NBT data, that will be stored with its container.  
   By default, `@Persisted` only applies on the server side.

2. For fields that need to be available on the client (or more specifically, remote) side, you can annotate them with
   `@DescSynced` to make them available there as well.  
   Any changes made to the field on the server side will automatically be synchronized to the client side.

3. If a change of a synced field's value requires rerendering the containing block (e.g. for different overlays based
   on a cover's IO direction), simply add the `@RequireRerender` annotation to it.  
   Its renderer's code will then be called again every time the field changes.

4. In some cases, a field may require some additional code to run on the client/remote side when it has been synced.  
   The `@UpdateListener` annotation allows you to define a method to be called in that case.