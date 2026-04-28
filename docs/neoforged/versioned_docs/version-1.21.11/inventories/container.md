---
sidebar_position: 0
---
# Containers

A popular use case of [block entities][blockentity] is to store items of some kind. Some of the most essential [blocks][block] in Minecraft, such as the furnace or the chest, use block entities for this purpose. To store items on something, Minecraft uses `Container`s.

The `Container` interface defines methods such as `#getItem`, `#setItem` and `#removeItem` that can be used to query and update the container. Since it is an interface, it does not actually contain a backing list or other data structure, that is up to the implementing system.

Due to this, `Container`s can not only be implemented on block entities, but any other class as well. Notable examples include entity inventories, as well as common modded [items][item] such as backpacks.

:::warning
NeoForge provides the `ItemStacksResourceHandler` class as a replacement for `Container`s in many places. It should be used wherever possible in favor of `Container`, as it allows for cleaner interaction with other `Container`s/`ItemStacksResourceHandler`s.

The main reason this article exists is for reference in vanilla code, or if you are developing mods on multiple loaders. Always use `ItemStacksResourceHandler` in your own code if possible! Docs on that are a work in progress.
:::

## Basic Container Implementation

Containers can be implemented in any way you like, so long as you satisfy the dictated methods (as with any other interface in Java). However, it is common to use a `NonNullList<ItemStack>` with a fixed length as a backing structure. Single-slot containers may also simply use an `ItemStack` field instead.

For example, a basic implementation of `Container` with a size of 27 slots (one chest) could look like this:

```java
public class MyContainer implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(
            // The size of the list, i.e. the amount of slots in our container.
            27,
            // The default value to be used in place of where you'd use null in normal lists.
            ItemStack.EMPTY
    );

    // The amount of slots in our container.
    @Override
    public int getContainerSize() {
        return 27;
    }

    // Whether the container is considered empty.
    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    // Return the item stack in the specified slot.
    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    // Call this when changes are done to the container, i.e. when item stacks are added, modified, or removed.
    // For example, you could call BlockEntity#setChanged here.
    @Override
    public void setChanged() {

    }

    // Remove the specified amount of items from the given slot, returning the stack that was just removed.
    // We defer to ContainerHelper here, which does this as expected for us.
    // However, we must call #setChanged manually.
    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        this.setChanged();
        return stack;
    }

    // Remove all items from the specified slot, returning the stack that was just removed.
    // We again defer to ContainerHelper here, and we again have to call #setChanged manually.
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(this.items, slot);
        this.setChanged();
        return stack;
    }

    // Set the given item stack in the given slot. Limit to the max stack size of the container first.
    @Override
    public void setItem(int slot, ItemStack stack) {
        stack.limitSize(this.getMaxStackSize(stack));
        this.items.set(slot, stack);
        this.setChanged();
    }

    // Whether the container is considered "still valid" for the given player. For example, chests and
    // similar blocks check if the player is still within a given distance of the block here.
    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // Clear the internal storage, setting all slots to empty again.
    @Override
    public void clearContent() {
        items.clear();
        this.setChanged();
    }
}
```

### `SimpleContainer`

The `SimpleContainer` class is a basic implementation of a container with some sprinkles on top, such as the ability to add `ContainerListener`s. It can be used if you need a container implementation that doesn't have any special requirements.

### `BaseContainerBlockEntity`

The `BaseContainerBlockEntity` class is the base class of many important block entities in Minecraft, such as chests and chest-like blocks, the various furnace types, hoppers, dispensers, droppers, brewing stands and a few others.

Aside from `Container`, it also implements the `MenuProvider` and `Nameable` interfaces:

- `Nameable` defines a few methods related to setting (custom) names and, aside from many block entities, is implemented by classes such as `Entity`. This uses the [`Component` system][component].
- `MenuProvider`, on the other hand, defines the `#createMenu` method, which allows an [`AbstractContainerMenu`][menu] to be constructed from the container. This means that using this class is not desirable if you want a container without an associated GUI, for example in jukeboxes.

`BaseContainerBlockEntity` bundles all calls we would normally make to our `NonNullList<ItemStack>` through two methods `#getItems` and `#setItems`, drastically reducing the amount of boilerplate we need to write. An example implementation of a `BaseContainerBlockEntity` could look like this:

```java
public class MyBlockEntity extends BaseContainerBlockEntity {
    // The container size. This can of course be any value you want.
    public static final int SIZE = 9;
    // Our item stack list. This is not final due to #setItems existing.
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    // The constructor, like before.
    public MyBlockEntity(BlockPos pos, BlockState blockState) {
        super(MY_BLOCK_ENTITY.get(), pos, blockState);
    }

    // The container size, like before.
    @Override
    public int getContainerSize() {
        return SIZE;
    }

    // The getter for our item stack list.
    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    // The setter for our item stack list.
    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    // The display name of the menu. Don't forget to add a translation!
    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.examplemod.myblockentity");
    }

    // The menu to create from this container. See below for what to return here.
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }
}
```

Keep in mind that this class is a `BlockEntity` and a `Container` at the same time. This means that you can use the class as a supertype for your block entity to get a functioning block entity with a pre-implemented container.

:::note
`BlockEntity`s that implement `Container` handle dropping their contents by default. If you choose not to implement `Container`, then you will need to handle the [removal logic][beremove].
:::

### `WorldlyContainer`

`WorldlyContainer` is a sub-interface of `Container` that allows accessing slots of the given `Container` by `Direction`. It is mainly intended for block entities that only expose parts of their container to a particular side. For example, this could be used by a machine that outputs to one side and takes inputs from all other sides, or vice-versa. A simple implementation of the interface could look like this:

```java
// See BaseContainerBlockEntity methods above. You can of course extend BlockEntity directly
// and implement Container yourself if needed.
public class MyBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    // other stuff here
    
    // Assume that slot 0 is our output and slots 1-8 are our inputs.
    // Further assume that we output to the top and take inputs from all other sides.
    private static final int[] OUTPUTS = new int[]{0};
    private static final int[] INPUTS = new int[]{1, 2, 3, 4, 5, 6, 7, 8};

    // Return an array of exposed slot indices based on the passed Direction.
    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? OUTPUTS : INPUTS;
    }

    // Whether items can be placed through the given side at the given slot.
    // For our example, we return true only if we're not inputing from above and are in the index range [1, 8].
    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return direction != Direction.UP && index > 0 && index < 9;
    }

    // Whether items can be taken from the given side and the given slot.
    // For our example, we return true only if we're pulling from above and from slot index 0.
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.UP && index == 0;
    }
}
```

## Using Containers

Now that we have created containers, let's use them!

Since there is a considerable overlap between `Container`s and `BlockEntity`s, containers are best retrieved by casting the block entity to `Container` if possible:

```java
if (blockEntity instanceof Container container) {
    // do something with the container
}
```

The container can then use the methods we mentioned before, for example:

```java
// Get the first item in the container.
ItemStack stack = container.getItem(0);

// Set the first item in the container to dirt.
container.setItem(0, new ItemStack(Items.DIRT));

// Removes a quantity of (up to) 16 from the third slot.
container.removeItem(2, 16);
```

:::warning
A container may throw an exception if trying to access a slot that is beyond its container size. Alternatively, they may return `ItemStack.EMPTY`, as is the case with (for example) `SimpleContainer`.
:::

### `ContainerUser`s

Living entities that are able to access containers implement `ContainerUser`. Each user defines whether it has a container open and the maximum block distance the entity can interact with the container. A `Container` calls `startOpen` with the `ContainerUser` when the container object is interacted with (e.g., right-clicking a chest), and `stopOpen` once the container object is closed (e.g., leaving the chest menu).

These methods are typically used to keep track of the number of living entities that have the container open through the `ContainerOpenersCounter`, which is used for some entity AI and rendering.

## `Container`s on `ItemStack`s

Until now, we mainly discussed `Container`s on `BlockEntity`s. However, they can also be applied to [`ItemStack`s][itemstack] using the `minecraft:container` [data component][datacomponent]:

```java
// We use SimpleContainer as the superclass here so we don't have to reimplement the item handling logic ourselves.
// Due to implementation details of SimpleContainer, this may lead to race conditions if multiple parties
// can access the container at the same time, so we're just going to assume our mod doesn't allow that.
// You may of course use a different implementation of Container (or implement Container yourself) if needed.
public class MyBackpackContainer extends SimpleContainer {
    // The item stack this container is for. Passed into and set in the constructor.
    private final ItemStack stack;
    
    public MyBackpackContainer(ItemStack stack) {
        // We call super with our desired container size.
        super(27);
        // Setting the stack field.
        this.stack = stack;
        // We load the container contents from the data component (if present), which is represented
        // by the ItemContainerContents class. If absent, we use ItemContainerContents.EMPTY.
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        // Copy the data component contents into our item stack list.
        contents.copyInto(this.getItems());
    }

    // When the contents are changed, we save the data component on the stack.
    @Override
    public void setChanged() {
        super.setChanged();
        this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }
}
```

And voilà, you have created an item-backed container! Call `new MyBackpackContainer(stack)` to create a container for a menu or other use case.

:::warning
Be aware that menus that directly interface with `Container`s must `#copy()` their `ItemStack`s when modifying them, as otherwise the immutability contract on data components is broken. To do this, NeoForge provides the `StackCopySlot` class for you.
:::

## `Container`s on `Entity`s

`Container`s on [`Entity`s][entity] are finicky: whether an entity has a container or not cannot be universally determined. It all depends on what entity you are handling, and as such can require a lot of special-casing.

If you are creating an entity yourself, there is nothing stopping you from implementing `Container` on it directly, though be aware that you will not be able to use superclasses such as `SimpleContainer` (since `Entity` is the superclass).

### `Container`s on `Mob`s

`Mob`s do not implement `Container`, but they implement the `EquipmentUser` interface (among others). This interface defines the methods `#setItemSlot(EquipmentSlot, ItemStack)`, `#getItemBySlot(EquipmentSlot)` and `#setDropChance(EquipmentSlot, float)`. While not related to `Container` code-wise, the functionality is quite similar: we associate slots, in this case equipment slots, with `ItemStack`s.

The most notable difference to `Container` is that there is no list-like order (though `Mob` uses `NonNullList<ItemStack>`s in the background). Access does not work through slot indices, but rather through the seven `EquipmentSlot` enum values: `MAINHAND`, `OFFHAND`, `FEET`, `LEGS`, `CHEST`, `HEAD`, and `BODY` (where `BODY` is used for horse and dog armor).

An example of interaction with the mob's "slots" would look something like this:

```java
// Get the item stack in the HEAD (helmet) slot.
ItemStack helmet = mob.getItemBySlot(EquipmentSlot.HEAD);

// Put bedrock into the mob's FEET (boots) slot.
mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.BEDROCK));

// Enable that bedrock to always drop if the mob is killed.
mob.setDropChance(EquipmentSlot.FEET, 1f);
```

### `InventoryCarrier`

`InventoryCarrier` is an interface implemented by some living entities, such as villagers. It declares a method `#getInventory`, which returns a `SimpleContainer`. This interface is used by non-player entities that need an actual inventory instead of just the equipment slots provided by `EquipmentUser`.

### `Container`s on `Player`s (Player Inventory)

The player's inventory is implemented through the `Inventory` class, a class implementing `Container` as well as the `Nameable` interface mentioned earlier. An instance of that `Inventory` is then stored as a field named `inventory` on the `Player`, accessible via `Player#getInventory`. The inventory can be interacted with like any other container.

The inventory contents are stored in two places:

- The `NonNullList<ItemStack> items` list covers the 36 main inventory slots, including the nine hotbar slots (indices 0-8).
- The `EntityEquipment equipment` map stores the `EquipmentSlot` stacks: the armor slots (`FEET`, `LEGS`, `CHEST`, `HEAD`), `OFFHAND`, `BODY`, and `SADDLE`, in that order.  

When iterating over the inventory contents, it is recommended to iterate over `items`, then over `equipment` using `Inventory#EQUIPMENT_SLOT_MAPPING` for the indices.

[beremove]: ../blockentities/index.md#removing-block-entities
[block]: ../blocks/index.md
[blockentity]: ../blockentities/index.md
[component]: ../resources/client/i18n.md#components
[datacomponent]: ../items/datacomponents.md
[entity]: ../entities/index.md
[item]: ../items/index.md
[itemstack]: ../items/index.md#itemstacks
[menu]: menus.md
