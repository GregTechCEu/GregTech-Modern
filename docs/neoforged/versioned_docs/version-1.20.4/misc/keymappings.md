# Key Mappings

A key mapping, or key binding, defines a particular action that should be tied to an input: mouse click, key press, etc. Each action defined by a key mapping can be checked whenever the client can take an input. Furthermore, each key mapping can be assigned to any input through the [Controls option menu][controls].

## Registering a `KeyMapping`

A `KeyMapping` can be registered by listening to the `RegisterKeyMappingsEvent` on the [**mod event bus**][modbus] only on the physical client and calling `#register`.

```java
// In some physical client only class

// Key mapping is lazily initialized so it doesn't exist until it is registered
public static final Lazy<KeyMapping> EXAMPLE_MAPPING = Lazy.of(() -> /*...*/);

@SubscribeEvent // on the mod event bus only on the physical client
public static void registerBindings(RegisterKeyMappingsEvent event) {
  event.register(EXAMPLE_MAPPING.get());
}
```

## Creating a `KeyMapping`

A `KeyMapping` can be created using it's constructor. The `KeyMapping` takes in a [translation key][tk] defining the name of the mapping, the default input of the mapping, and the [translation key][tk] defining the category the mapping will be put within in the [Controls option menu][controls].

:::tip
A `KeyMapping` can be added to a custom category by providing a category [translation key][tk] not provided by vanilla. Custom category translation keys should contain the mod id (e.g. `key.categories.examplemod.examplecategory`).
:::

### Default Inputs

Each key mapping has a default input associated with it. This is provided through InputConstants.Key. Each input consists of an InputConstants.Type, which defines what device is providing the input, and an integer, which defines the associated identifier of the input on the device.

Vanilla provides three types of inputs: `KEYSYM`, which defines a keyboard through the provided `GLFW` key tokens, `SCANCODE`, which defines a keyboard through the platform-specific scancode, and `MOUSE`, which defines a mouse.

:::note
It is highly recommended to use `KEYSYM` over `SCANCODE` for keyboards as `GLFW` key tokens are not tied to any particular system. You can read more on the [GLFW docs][keyinput].
:::

The integer is dependent on the type provided. All input codes are defined in `GLFW`: `KEYSYM` tokens are prefixed with `GLFW_KEY_*` while `MOUSE` codes are prefixed with `GLFW_MOUSE_*`.

```java
new KeyMapping(
  "key.examplemod.example1", // Will be localized using this translation key
  InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
  GLFW.GLFW_KEY_P, // Default key is P
  "key.categories.misc" // Mapping will be in the misc category
)
```

:::note
If the key mapping should not be mapped to a default, the input should be set to `InputConstants#UNKNOWN`. The vanilla constructor will require you to extract the input code via `InputConstants$Key#getValue` while the NeoForge constructor can be supplied the raw input field.
:::

### `IKeyConflictContext`

Not all mappings are used in every context. Some mappings are only used in a GUI, while others are only used purely in game. To avoid mappings of the same key used in different contexts conflicting with each other, an `IKeyConflictContext` can be assigned.

Each conflict context contains two methods: `#isActive`, which defines if the mapping can be used in the current game state, and `#conflicts`, which defines whether the mapping conflicts with a key in the same or different conflict context.

Currently, NeoForge defines three basic contexts through `KeyConflictContext`: `UNIVERSAL`, which is the default meaning the key can be used in every context, `GUI`, which means the mapping can only be used when a `Screen` is open, and `IN_GAME`, which means the mapping can only be used if a `Screen` is not open. New conflict contexts can be created by implementing `IKeyConflictContext`.

```java
new KeyMapping(
  "key.examplemod.example2",
  KeyConflictContext.GUI, // Mapping can only be used when a screen is open
  InputConstants.Type.MOUSE, // Default mapping is on the mouse
  GLFW.GLFW_MOUSE_BUTTON_LEFT, // Default mouse input is the left mouse button
  "key.categories.examplemod.examplecategory" // Mapping will be in the new example category
)
```

### `KeyModifier`

Modders may not want mappings to have the same behavior if a modifier key is held at the same (e.g. `G` vs `CTRL + G`). To remedy this, NeoForge adds an additional parameter to the constructor to take in a `KeyModifier` which can apply control (`KeyModifier#CONTROL`), shift (`KeyModifier#SHIFT`), or alt (`KeyModifier#ALT`) to any input. `KeyModifier#NONE` is the default and will apply no modifier.

A modifier can be added in the [controls option menu][controls] by holding down the modifier key and the associated input.

```java
new KeyMapping(
  "key.examplemod.example3",
  KeyConflictContext.UNIVERSAL,
  KeyModifier.SHIFT, // Default mapping requires shift to be held down
  InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
  GLFW.GLFW_KEY_G, // Default key is G
  "key.categories.misc"
)
```

## Checking a `KeyMapping`

A `KeyMapping` can be checked to see whether it has been clicked. Depending on when, the mapping can be used in a conditional to apply the associated logic.

### Within the Game

Within the game, a mapping should be checked by listening to `ClientTickEvent` on the [**Forge event bus**][forgebus] and checking `KeyMapping#consumeClick` within a while loop. `#consumeClick` will return `true` only the number of times the input was performed and not already previously handled, so it won't infinitely stall the game.

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void onClientTick(ClientTickEvent event) {
  if (event.phase == TickEvent.Phase.END) { // Only call code once as the tick event is called twice every tick
    while (EXAMPLE_MAPPING.get().consumeClick()) {
      // Execute logic to perform on click here
    }
  }
}
```

:::caution
Do not use the `InputEvent`s as an alternative to `ClientTickEvent`. There are separate events for keyboard and mouse inputs only, so they wouldn't handle any additional inputs.
:::

### Inside a GUI

Within a GUI, a mapping can be checked within one of the `GuiEventListener` methods using `IForgeKeyMapping#isActiveAndMatches`. The most common methods which can be checked are `#keyPressed` and `#mouseClicked`. 

`#keyPressed` takes in the `GLFW` key token, the platform-specific scan code, and a bitfield of the held down modifiers. A key can be checked against a mapping by creating the input using `InputConstants#getKey`. The modifiers are already checked within the mapping methods itself.

```java
// In some Screen subclass
@Override
public boolean keyPressed(int key, int scancode, int mods) {
  if (EXAMPLE_MAPPING.get().isActiveAndMatches(InputConstants.getKey(key, scancode))) {
    // Execute logic to perform on key press here
    return true;
  }
  return super.keyPressed(x, y, button);
} 
```

:::note
If you do not own the screen which you are trying to check a **key** for, you can listen to the `Pre` or `Post` events of ScreenEvent.KeyPressed on the [**Forge event bus**][forgebus] instead.
:::

`#mouseClicked` takes in the mouse's x position, y position, and the button clicked. A mouse button can be checked against a mapping by creating the input using `InputConstants$Type#getOrCreate` with the `MOUSE` input.

```java
// In some Screen subclass
@Override
public boolean mouseClicked(double x, double y, int button) {
  if (EXAMPLE_MAPPING.get().isActiveAndMatches(InputConstants.TYPE.MOUSE.getOrCreate(button))) {
    // Execute logic to perform on mouse click here
    return true;
  }
  return super.mouseClicked(x, y, button);
} 
```

:::note
If you do not own the screen which you are trying to check a **mouse** for, you can listen to the `Pre` or `Post` events of ScreenEvent.MouseButtonPressed on the [**Forge event bus**][forgebus] instead.
:::

[modbus]: ../concepts/events.md#event-buses
[controls]: https://minecraft.wiki/w/Options#Controls
[tk]: ../resources/client/i18n.md#components
[keyinput]: https://www.glfw.org/docs/3.3/input_guide.html#input_key
[forgebus]: ../concepts/events.md#registering-an-event-handler
