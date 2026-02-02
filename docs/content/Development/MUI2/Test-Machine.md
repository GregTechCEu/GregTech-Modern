# Making a MUI2 Test Machine
To make a basic machine to test your UI, simply create the following class:

```java title="MultiMachines.java"
public class MuiTestMachine extends MetaMachine implements IMuiMachine {

    public MuiTestMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 168);
        // Do stuff with your panel here, add children, etc.
        // For example:
        panel.child(IKey.str("Test machine")
                .asWidget()
                .margin(4));

        return panel;
    }
}
```

and in your machines definitions class, add the following entry:

```java
    public static final MachineDefinition MUI_TEST_MACHINE = REGISTRATE
            .machine("mui_test", MuiTestMachine::new)
            .model(createOverlayCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/machine/part/computer_monitor")))
            .register();
```

Make sure to run datagen after making the initial machine to register the lang keys, model, etc. Running datagen afterward for UI changes is not required.