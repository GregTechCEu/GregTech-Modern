---
title: "Migrating from LDLib SyncData"
---
# Migrating from LDLib SyncData

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