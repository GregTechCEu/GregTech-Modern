# Basic Sync Example

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

        panel.child(IKey.dynamic(() -> Component.literal("Ticks: "+ this.ticks))
                .asWidget()
                .margin(4));

        return panel;
    }
}
```

Here, we create a basic `SyncValue` for an integer. This takes a `Supplier<Integer>` and a `Consumer<Integer>`, more commonly known as a getter and a setter. Generally speaking, `SyncValue`s will take a `Supplier` and `Consumer` of the type of value they are syncing.  

If the value returned by the getter changed on the server, the value gets serialized and sent to the client by the `SyncManager`. The `SyncHandler`'s value can always be manually updated, for example to do client-to-server syncing.

If you try to access values on the client that aren't synced or don't have a `SyncValue` or `SyncHandler`, they will have a default value, but they will not reflect the values or changes happening on the server.