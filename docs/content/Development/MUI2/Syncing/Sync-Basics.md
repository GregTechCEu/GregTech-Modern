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

- `Text.dynamic(Supplier<Component>)` - Queries the supplier every frame to retrieve the component to display  
- `new DynamicDrawable(Supplier<IDrawable>)` - Queries the supplier every frame to retrieve the drawable to display  


!!! Note
    To convert Texts or Drawables to Widgets, you need to chain `.asWidget()`

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

        column.child(Text.dynamic(() -> Component.literal("Ticks: " + this.ticks)) // note that this is a Supplier<Component> instead of a Component
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

If you want to update the value from the client, you can call `syncValue.setValue()` on the client. This will also update the value on the server side.

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
                        list.child(Text.str("Value nr. " + (i + 1)).asWidget()); // No need for Text.dynamic since we have the value as a variable here, inside the lambda
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


!!! note
    For even more complex systems where you need to dynamically register sync handlers within the `DynamicLinkedSyncHandler`'s `.widgetProvider(...)`, this can be done by calling `.getOrCreateSyncHandler(...)` on the `widgetSyncManager` parameter of the lambda.

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
                Text.dynamic(() -> Component.literal("Pressed: " + this.buttonPressed))
                    .asWidget());

        var buttonSyncValue = new BooleanSyncValue(() -> this.buttonPressed, (newValue) -> this.buttonPressed = newValue);
        column.child(new ToggleButton().value(buttonSyncValue));

        panel.child(column);

        return panel;
    }
}
```

Note that in this case, the ToggleButton takes care of registering the SyncValue for us, so we do not register it to the syncManager ourselves. This method is great for simple functions using widgets that support it.

A few examples of this would be:

- `new ToggleButton().value(BooleanSyncValue)` - A ToggleButton that affects the Boolean sync value
- `new TextFieldWidget().value(StringSyncValue)` - A TextField that updates the String sync value
- `new ProgressWidget().value(DoubleSyncValue)` - A ProgressWidget (e.g. bar) that shows progress, can also be constructed with `new ProgressWidget().progress(() -> this.progress)`
- `new SliderWidget().value(DoubleSyncValue)` - A SliderWidget that updates the Double sync value

## Method 4: Manually notifying DynamicSyncHandlers
This method is useful when your custom widget needs complex data coming in, like through multiple sync handlers.

```java
public class MuiTestMachine extends MetaMachine implements IMuiMachine {

    public int rows = 0;
    public int columns = 0;
    public int counter = 0;

    public MuiTestMachine(BlockEntityCreationInfo info) {
        super(info);
        this.subscribeServerTick(() -> {
            counter += 1;
            if (counter % 20 == 0) {
                rows = (rows + 1) % 10;
            }
            if (counter % 15 == 0) {
                columns = (columns + 1) % 10;
            }
        });
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 200, 200);

        var rowsSyncValue = new IntSyncValue(() -> this.rows, (newValue) -> this.rows = newValue);
        syncManager.syncValue("rows", rowsSyncValue);

        var columnsSyncValue = new IntSyncValue(() -> this.columns, (newValue) -> this.columns = newValue);
        syncManager.syncValue("columns", columnsSyncValue);

        DynamicSyncHandler gridWidgetHandler = new DynamicSyncHandler().widgetProvider((slotsSyncManger, buffer) -> {

            Flow grid = Flow.column().width(200);
            for (int rowNr = 0; rowNr < this.rows; rowNr++) {
                Flow row = Flow.row();
                for (int columnNr = 0; columnNr < this.columns; columnNr++) {
                    row.child(Text.str(rowNr + ", " + columnNr).asWidget().width(20));
                }
                grid.child(row);
            }
            return grid;
        });

        rowsSyncValue.setChangeListener(() -> {
            gridWidgetHandler.notifyUpdate(buffer -> {});
        });
        columnsSyncValue.setChangeListener(() -> {
            gridWidgetHandler.notifyUpdate(buffer -> {});
        });

        panel.child(new DynamicSyncedWidget<>().syncHandler(gridWidgetHandler));
        return panel;
    }
}
```

This is very similar to method 2, but instead of a `DynamicLinkedSyncHandler` we use a normal `DynamicSyncHandler` where we have to manually let it know when to update. We do this in the change listener of our two `SyncValue`s by calling notifyUpdate. 

Do note there's also a buffer where you can serialize data to, to be consumed in the `.widgetProvider(...)` in the spot where in a `DynamicLinkedSyncHandler` our syncValue would be. It is usually not needed to put anything in this buffer.

## Other sync information

### SyncHandler panel separation

SyncValues are separated across panels. So if you do `mainPanelSyncManager.syncValue("rows", rowsSyncValue);` in one panel, you can't just call `popupSyncManager.getSyncHandlerFromMapKey("rows:0")`.   
You can, however, call `syncManager.getModularSyncManager().getPanelSyncManager("panel name here").getSyncHandlerFromMapKey("rows:0");`.  

### Value.Dynamic
Sometimes you need to quickly create a Value for something that already exists client side. For this you can use `new [Type]Value.Dynamic(...)`.  
For example, if you have a client-only value that's affected by a button, you could do   
`panel.child(new ToggleButton().value(new BoolValue.Dynamic(() -> this.toggled, (newValue) -> this.toggled = newValue)));`  

Another reason to use a dynamic value is if you want to change the type of a variable, e.g. `.value(new DoubleValue.Dynamic(() -> (double) this.x, val -> this.x = (double) val)` where x is an int.  

The third reason is if you want to use a `SyncHandler` in two separate widgets that would both auto-register it.  
```
        var boolSyncValue = new BooleanSyncValue(() -> this.toggled, (newValue) -> this.toggled = newValue);
        panel.child(new ToggleButton().value(boolSyncValue));
        panel.child(new ToggleButton().value(BoolValue.wrap(boolSyncValue)).left(32));
```
Without the wrap around the second boolSyncValue call, it would complain of registration of an already registered SyncValue.

