---
title: "Migrating from LDLib SyncData"
---
# Migrating from LDLib SyncData

### Simple example

This simple example covers the majority of use cases when adding sync/save fields to a standard machine, machine trait or cover.

#### With LDLib:
```java
class CustomMachine extends SimpleTieredMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CustomMachine.class,
            SimpleTieredMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter 
    @Persisted
    @DescSynced
    @RequireRerender
    protected int customIntValue;
    
    @Persisted(key = "customNBTKey")
    protected String customStringValue;

    public void setCustomIntValue(int newValue) {
        this.customIntValue = newValue;
    }
}
```

#### New System:
```java
class CustomMachine extends SimpleTieredMachine {
    @Getter 
    @SaveField
    @SyncToClient
    protected int customIntValue;
    
    @SaveField(nbtKey = "customNBTKey")
    protected String customStringValue;
    
    public void setCustomIntValue(int newValue) {
        this.customIntValue = newValue;
        ////// IMPORTANT: markClientSyncFieldDirty must be called to update client synced fields.
        getSyncDataHolder().markClientSyncFieldDirty("customIntValue");
    }
}

```

### General migration guidelines

- Remove all `ManagedFieldHolder` fields.
- Replace `FieldManagedStorage` fields with `SyncDataHolder` fields.
- Replace `IEnhancedManaged` objects with `ISyncManaged`.
- Replace `IAsyncAutoSyncBlockEntity`, `IAutoPersistBlockEntity`, `IAutoSyncBlockEntity` and `IManagedBlockEntity` by extending `ManagedSyncBlockEntity`.

### Annotations

!!! warning
Client sync fields **do not** automatically detect changes. When changing a client sync field, call `ISyncManaged.syncDataHolder.markClientSyncFieldDirty(FIELD_NAME)`

- `@DescSynced` -> `@SyncToClient`
- `@RequireRerender` -> `@RerenderOnChanged`
- `@Persisted` -> `@SaveField`
- `@UpdateListener` -> `@ClientFieldChangeListener` on listener method.
- `@DropSaved` - Removed, make machines implement `IDropSaveMachine` instead
- `@ReadOnlyManaged` and `@LazyManaged` See usage docs for instructions on complex sync objects 

### Other changes

 - `saveCustomPersistedData` & `loadCustomPersistedData` methods, and serialization of custom data types - See `ValueTransformer<T>` and `ValueTransformers` classes.