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

Here, we create a basic SyncValue for an integer. This takes a `Supplier<Integer>` and a `Consumer<Integer>`, more commonly known as a getter and a setter. If the value is changed, either on the server or the client, the value gets serialized and sent to the other side, so the other side knows about the value.  

If you try to access values that aren't synced or don't have a SyncValue or SyncHandler, they will be `0` or `null`.  