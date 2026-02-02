# Synced Actions

```java
public class MuiTestMachine extends MetaMachine implements IMuiMachine {

    public MuiTestMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    public int number = 0;

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 168);

        var numberSyncValue = new IntSyncValue(() -> this.number, (newValue) -> this.number = newValue);
        syncManager.syncValue("number", numberSyncValue);

        syncManager.registerServerSyncedAction("randomButtonPressed", (packet) -> {
            this.number = getLevel().getRandom().nextInt();
        });

        Column contents = new Column();
        contents.child(IKey.dynamic(() -> Component.literal("Number: " + number))
                .asWidget()
                .width(100)
                .height(16)
                .margin(4));

        contents.child(new ButtonWidget<>()
                .onMousePressed((x, y, button) -> {
                    if (button == 0) {
                        syncManager.callSyncedAction("randomButtonPressed");
                    }
                    return true;
                })
                .size(16));
        return panel.child(contents);
    }
}

```

Synced actions are useful when you need a client-side input (e.g. a button press) to trigger an action on the server.  

In this case we create a synced action called `randomButtonPressed`, and when the button is pressed on the client, the `randomButtonPressed` signal is sent to the server and the relevant action is executed.

If you need to pass other data with your action, you can pass a `(packet) -> {...}` as second argument to `callSyncedAction` and serialize your data there, and then deserialize it from the `packet` argument in the synced action definition.