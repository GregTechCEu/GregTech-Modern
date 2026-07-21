---
title: "Usage"
---

## Usage

### Registering classes with the sync system

At the core of the system is the `ISyncManaged` interface, which allows for fields to have sync annotations.

All `ISyncManaged` objects must belong to a root managed sync object. There are two default types of root sync objects:

- `ManagedSyncBlockEntity` is a block entity storing its data through the system. The `Block` class using this `BlockEntity` must implement `ManagedSyncEntityBlock`.
- `ManagedSavedData` is a `SavedData` object that stores data through the system.

#### Example of `ISyncManaged` usage
```java
class MySyncObject implements ISyncManaged {
    
    // Any class that directly implements ISyncManaged must have a SyncDataHolder:
     @Getter
     protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
     
    // ISyncManaged objects should be attached to a parent sync managed object,
    // unless the sync managed object is a blockentity
    // ISyncManaged classes must implement a getter for their parent sync object
    @Getter
    private final MetaMachine parentSyncObject; 
    
    @SaveField
    @SyncToClient
    private BlockPos syncPos = BlockPos.ZERO;
    
    @Getter
    private String syncString = "";
     
    public MySyncObject(MetaMachine machine) {
        this.parentSyncObject = machine;
    }
    
    public void doChanges() {
      syncPos = new BlockPos(100, 50, 100);
      // Client sync fields do not update automatically.
      getSyncDataHolder().markClientSyncFieldDirty("syncPos");
      
      setSyncString("abcd");
    }
    
    // It is often good practice to wrap client sync fields in getters/setters, and have the setter update the sync status.
    public void setSyncString(String syncString) {
        this.syncString = syncString;
        getSyncDataHolder().markClientSyncFieldDirty("syncString");
    }
    
    // Called on the client side when the given sync field is updated.
    @ClientFieldChangeListener(fieldName="syncString")
    private void onSyncStringChanged() {
        
    }
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

To add support for an additional type, call `ValueTransformers.registerTransformer(Class<T> cls, ValueTransformer<T> transformer)` or `ValueTransformers.registerGenericTransformerSupplier(Class<T> cls, Supplier<ValueTransformer<T>> func)`

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