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

The `ValueTransformer<T>` abstract class defines how a value of type `T` should be serialized.

To add support for an additional type, call `ValueTransformers.registerTransformer(Class<T> cls, ValueTransformer<T> transformer)` or `ValueTransformers.registerTransformerSupplier(Class<T> cls, Supplier<ValueTransformer<T>> func)`

Additionally, fields can be explicitly directed to use a specific value transformer:
```java
/**
 * Example from HullMachine.java. This example shows serialization of an AE2 class which may or may not be loaded at runtime.
 */

@SaveField(nbtKey = "grid_node")
private final Object gridNodeHost;

private static class GridNodeHostTransformer implements ValueTransformer<Object> {

  @Override
  public Tag serializeNBT(Object value, TransformerContext<Object> context) {
    if (GTCEu.Mods.isAE2Loaded() &&
            (context.currentValue()) instanceof IGridConnectedBlockEntity connectedBlockEntity) {
      var compound = new CompoundTag();
      connectedBlockEntity.getMainNode().saveToNBT(compound);
      return compound;
    }
    return new CompoundTag();
  }

  @Override
  public @Nullable Object deserializeNBT(Tag tag, TransformerContext<Object> context) {
    if (GTCEu.Mods.isAE2Loaded() &&
            context.currentValue() instanceof IGridConnectedBlockEntity connectedBlockEntity &&
            tag instanceof CompoundTag c) {
      connectedBlockEntity.getMainNode().loadFromNBT(c);
    }
    return null;
  }
}

static {
  ClassSyncData.getClassData(HullMachine.class).setCustomTransformerForField("gridNodeHost",
          new GridNodeHostTransformer());
}

```