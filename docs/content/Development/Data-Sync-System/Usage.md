---
title: "Usage"
---

## Usage

### Registering classes with the sync system

At the core of the system is the interface `ISyncManaged`, which represents a class that to be synchronised with the client or saved.
All block entities which should be synchronised or saved must extend the abstract class `ManagedSyncBlockEntity`.

!!! warning 
  Block entities that inherit `ManagedSyncBlockEntity` must call `ManagedSyncBlockEntity::updateTick`***every tick*** within their ticker, or they will not be saved.

```java
class MySyncObject implements ISyncManaged {
    // Any class that directly implements ISyncManaged must have the following:
     @Getter
     protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    
    
    /**
     * Function called when the SyncDataHolder requests a rerender
     */
    void scheduleRenderUpdate();

    /**
     * Function called to notify the server that this object has been updated and must be synced to clients
     */
    void markAsChanged();
}
```

### Registering fields to be managed by the system
See [Annotations](Annotations.md)

### Type compatibility
The following field types are supported by default:
- Any class implementing `ISyncManaged`
- Any class implementing `INBTSerializable<Tag>`
- All primitive types
- If `T`, `K` are supported types:
   - `T[]`
   - `Set<T>`
   - `List<T>`,
   - `Map<T, K>`
- `String`
- `ItemStack`
- `FluidStack`
- `UUID`
- `BlockPos`
- `CompoundTag`
- `GTRecipe`
- `GTRecipeType`
- `MachineRenderState`
- `Material`
- `Component`

### Adding support for additional types

The `ValueTransformer<T>` abstract class defines how a value of type `T` should be serialised.

To add support for an additional type, call `ValueTransformers.registerClassTransformer(Class<T> cls, ValueTransformer<T> transformer)` or `ValueTransformers.registerInterfaceTransformer(Class<T> cls, ValueTransformer<T> transformer)`

!!! note
    Registering a transformer as a class transformer will only apply that transformer to objects of the *exact* same type. To also match subclasses or interface implementations, register a transformer as an interface transformer.


Some types may be too complex to be processed using this system. For more complex NBT interactions, use the `@FieldDataModifier` and `@CustomDataField` annotations.