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

To add support for an additional type, call `ValueTransformers.registerClassTransformer(Class<T> cls, ValueTransformer<T> transformer)` or `ValueTransformers.registerInterfaceTransformer(Class<T> cls, ValueTransformer<T> transformer)`

!!! note
    Registering a transformer as a class transformer will only apply that transformer to objects of the *exact* same type. To also match subclasses or interface implementations, register a transformer as an interface transformer.

The `ValueTransformer<T>` abstract class defines how a value of type `T` should be serialised.

```java
public abstract class ValueTransformer<T> {

    // If this type cannot be instanced purely from a serialised tag.
    // All complex types typically have mustProvideObject overriden to true 
    public boolean mustProvideObject() {
        return false;
    }
    
    // A method for serialising a value into a tag
    // Called when serialising a value to be sent to the client
    public Tag serializeClientSyncNBT(@Nullable T value, ISyncManaged holder) {
      return serializeNBT(value, holder);
    }

    // A method for deserialising a value from a tag
    // Called when deserialising a value on the client.
    // If mustProvideObject == true, currentVal is the currently saved value.
    public T deserializeClientNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal) {
      return deserializeNBT(tag, holder, currentVal);
    }


  // A method for serialising a value into a tag.
  // The holder param is the object this sync value is attached to
  public abstract Tag serializeNBT(T value, ISyncManaged holder);
    
  // A method for deserialising a value from a tag
  // If mustProvideObject == true, currentVal is the currently saved value.
  public abstract T deserializeNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal);
}
```

Some types may be too complex to be processed using this system. For more complex NBT interactions, use the `@FieldDataModifier` and `@CustomDataField` annotations.