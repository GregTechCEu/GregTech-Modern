# Sync Basics

## Basics of Syncing
To display dynamic values on the client, you have to send the data from the server to the client. When making UIs, it is very important to keep track of if and how your data is being synced to the client. There are a few ways to do so, which will be discussed below.  

When opening a UI, a copy of the UI is created both on the server and on the client. It is important to note here that the server's copy will have access to most everything about e.g. the machine, block state, the world etc., but the client's copy will not.  

To sync this data back and forth, you need to use `SyncHandler`s. These will send your data from the server to the client when it updates.  

Because of this, you cannot just use values you create SyncHandlers for directly in the client. An example:

```java

var tickSyncValue = new IntSyncValue(() -> this.ticks, (newValue) -> this.ticks = newValue);
for(int i=0;i<tickSyncValue.getValue(); i++){ 
    //...
}
```
This would not work, intSyncValue hasn't had time to send data over yet and thus int is still 0 on the client side.

If you try to access values on the client that aren't synced or don't have a `SyncValue` or `SyncHandler`, they will have a default value, but they will not reflect the values or changes happening on the server.



## Method 1: Dynamic Widgets
The first method is using dynamic widgets, which update every frame regardless of what happens. 
This method is easiest if you just need to sync some data over and display or edit it in a single widget. 
Some examples are:

- `IKey.dynamic(Supplier<Component>)` - Queries the supplier every frame to retrieve the component to display  
- `new DynamicDrawable(Supplier<IDrawable>)` - Queries the supplier every frame to retrieve the drawable to display  


!!! Note
    To convert IKeys or Drawables to Widgets, you need to chain `.asWidget()`

```java
public class MuiTestMachine extends MetaMachine implements IMuiMachine {

    public int ticks = 0;

    public MuiTestMachine(BlockEntityCreationInfo info) {
        super(info);
        this.subscribeServerTick(() -> ticks++);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 168);
        var tickSyncValue = new IntSyncValue(() -> this.ticks, (newValue) -> this.ticks = newValue);
        syncManager.syncValue("tickSyncValue", tickSyncValue);

        var column = Flow.column();

        column.child(IKey.dynamic(() -> Component.literal("Ticks: " + this.ticks)) // note that this is a Supplier<Component> instead of a Component
                .asWidget()
                .margin(4));

        column.child(
                new DynamicDrawable(() -> { // note that this is a Supplier<IDrawable> instead of an IDrawable
                    if (ticks % 40 < 20) { // flip every second
                        return GTGuiTextures.BUTTON_FLUID_OUTPUT;
                    } else {
                        return GTGuiTextures.BUTTON_ITEM_OUTPUT;
                    }
                })
                .asWidget()
                .background(GTGuiTextures.BACKGROUND_STEEL)
        );

        panel.child(column);

        return panel;
    }
}
```

Here, we create a basic `SyncValue` for an integer. This takes a `Supplier<Integer>` and a `Consumer<Integer>`, more commonly known as a getter and a setter. Generally speaking, `SyncValue`s will take a `Supplier` and `Consumer` of the type of value they are syncing.  

If the value returned by the getter changed on the server, the value gets serialized and sent to the client by the `SyncManager`. The `SyncHandler`'s value can always be manually updated, for example to do client-to-server syncing.

Then, the value on the client (being set every time the server sends an update) is retrieved every frame by the lambdas used in the dynamic widgets.

## Method 2: DynamicLinkedSyncHandler

This method is great for widgets whose structure and layout can change depending on your synced values.

```java
public class MuiTestMachine extends MetaMachine implements IMuiMachine {

    public int ticks = 0;

    public MuiTestMachine(BlockEntityCreationInfo info) {
        super(info);
        this.subscribeServerTick(() -> ticks++);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 168);
        var tickSyncValue = new IntSyncValue(() -> this.ticks, (newValue) -> this.ticks = newValue);
        syncManager.syncValue("tickSyncValue", tickSyncValue);

        DynamicLinkedSyncHandler<IntSyncValue> dynamicLinkedSyncHandler = new DynamicLinkedSyncHandler<>(tickSyncValue)
                .widgetProvider((widgetSyncManager, intSyncHandler) -> {
                    var list = new ListWidget<>()
                            .widthRel(1)
                            .coverChildrenHeight()
                            .crossAxisAlignment(Alignment.CrossAxis.START);
                    int tickValue = intSyncHandler.getValue(); // It is also possible to just reference this.ticks directly
                    int amountOfItems = 1 + (tickValue % 200) / 20;
                    for (int i = 0; i < amountOfItems; i++) {
                        list.child(IKey.str("Value nr. " + (i + 1)).asWidget()); // No need for IKey.dynamic since we have the value as a variable here, inside the lambda
                    }
                    return list;
                });

        panel.child(new DynamicSyncedWidget<>()
                .widthRel(1)
                .coverChildrenHeight()
                .syncHandler(dynamicLinkedSyncHandler)
                .padding(3));

        return panel;
    }
}
```

This method works in three steps:

The first step is creating a `SyncHandler` and registering it to the `PanelSyncManager`.  
The second step is creating a `DynamicLinkedSyncHandler` based on the first `SyncHandler`. This is effectively a wrapper class to provide your widget whenever your initial `SyncHandler` updates.  
The third step is creating a `DynamicSyncedWidget` with that `DynamicLinkedSyncHandler` as its `SyncHandler`.  

This effectively lets us create a new "version" of the widget whenever our value (in this case the `ticks` int) changes. Furthermore, in this example we have the actual values of the things we want to sync when constructing our widget tree on the client, allowing for much greater customization.

## Method 3: Types that take SyncHandlers
There are some widgets that have built in support for working directly with SyncHandlers.

```java
public class MuiTestMachine extends MetaMachine implements IMuiMachine {

    public boolean buttonPressed = false;

    public MuiTestMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 168);

        var column = Flow.column().paddingTop(3);

        column.child(
                IKey.dynamic(() -> Component.literal("Pressed: " + this.buttonPressed))
                    .asWidget());

        var buttonSyncValue = new BooleanSyncValue(() -> this.buttonPressed, (newValue) -> this.buttonPressed = newValue);
        column.child(new ToggleButton().value(buttonSyncValue));

        panel.child(column);

        return panel;
    }
}
```

Note that in this case, the ToggleButton takes care of registering the SyncValue for us, so we do not register it to the syncManager ourselves. This method is great for simple functions using widgets that support it.