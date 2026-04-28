# Screens

Screens are typically the base of all Graphical User Interfaces (GUIs) in Minecraft: taking in user input, verifying it on the server, and syncing the resulting action back to the client. They can be combined with [menus] to create an communication network for inventory-like views, or they can be standalone which modders can handle through their own [network] implementations.

Screens are made up of numerous parts, making it difficult to fully understand what a 'screen' actually is in Minecraft. As such, this document will go over each of the screen's components and how it is applied before discussing the screen itself.

## Relative Coordinates

Whenever anything is rendered, there needs to be some identifier which specifies where it will appear. With numerous abstractions, most of Minecraft's rendering calls takes in an x, y, and z value in a coordinate plane. X values increase from left to right, y from top to bottom, and z from far to near. However, the coordinates are not fixed to a specified range. They can change depending on the size of the screen and the scale at which is specified within the options. As such, extra care must be taken to make sure the values of the coordinates while rendering scale properly to the changeable screen size.

Information on how to relativize your coordinates will be within the [screen] section.

:::caution
If you choose to use fixed coordinates or incorrectly scale the screen, the rendered objects may look strange or misplaced. An easy way to check if you relativized your coordinates correctly is to click the 'Gui Scale' button in your video settings. This value is used as the divisor to the width and height of your display when determining the scale at which a GUI should render.
:::

## Gui Graphics

Any GUI rendered by Minecraft is typically done using `GuiGraphics`. `GuiGraphics` is the first parameter to almost all rendering methods; it contains basic methods to render commonly used objects. These fall into five categories: colored rectangles, strings, and textures, items, and tooltips. There is also an additional method for rendering a snippet of a component (`#enableScissor` / `#disableScissor`). `GuiGraphics` also exposes the `PoseStack` which applies the transformations necessary to properly render where the component should be rendered. Additionally, colors are in the [ARGB][argb] format.

### Colored Rectangles

Colored rectangles are drawn through a position color shader. There are three types of colored rectangles that can be drawn.

First, there is a colored horizontal and vertical one-pixel wide line, `#hLine` and `#vLine` respectively. `#hLine` takes in two x coordinates defining the left and right (inclusively), the top y coordinate, and the color. `#vLine` takes in the left x coordinate, two y coordinates defining the top and bottom (inclusively), and the color.

Second, there is the `#fill` method, which draws a rectangle to the screen. The line methods internally call this method. This takes in the left x coordinate, the top y coordinate, the right x coordinate, the bottom y coordinate, and the color.

Finally, there is the `#fillGradient` method, which draws a rectangle with a vertical gradient. This takes in the right x coordinate, the bottom y coordinate, the left x coordinate, the top y coordinate, the z coordinate, and the bottom and top colors.

### Strings

Strings are drawn through its `Font`, typically consisting of their own shaders for normal, see through, and offset mode. There are two alignment of strings that can be rendered, each with a back shadow: a left-aligned string (`#drawString`) and a center-aligned string (`#drawCenteredString`). These both take in the font the string will be rendered in, the string to draw, the x coordinate representing the left or center of the string respectively, the top y coordinate, and the color.

:::note
Strings should typically be passed in as [`Component`s][component] as they handle a variety of usecases, including the two other overloads of the method.
:::

### Textures

Textures are drawn through blitting, hence the method name `#blit`, which, for this purpose, copies the bits of an image and draws them directly to the screen. These are drawn through a position texture shader. While there are many different `#blit` overloads, we will only discuss two static `#blit`s.

The first static `#blit` takes in six integers and assumes the texture being rendered is on a 256 x 256 PNG file. It takes in the left x and top y screen coordinate, the left x and top y coordinate within the PNG, and the width and height of the image to render.

:::tip
The size of the PNG file must be specified so that the coordinates can be normalized to obtain the associated UV values.
:::

The static `#blit` which the first calls expands this to nine integers, only assuming the image is on a PNG file. It takes in the left x and top y screen coordinate, the z coordinate (referred to as the blit offset), the left x and top y coordinate within the PNG, the width and height of the image to render, and the width and height of the PNG file.

#### Blit Offset

The z coordinate when rendering a texture is typically set to the blit offset. The offset is responsible for properly layering renders when viewing a screen. Renders with a smaller z coordinate are rendered in the background and vice versa where renders with a larger z coordinate are rendered in the foreground. The z offset can be set directly on the `PoseStack` itself via `#translate`. Some basic offset logic is applied internally in some methods of `GuiGraphics` (e.g. item rendering).

:::caution
When setting the blit offset, you must reset it after rendering your object. Otherwise, other objects within the screen may be rendered in an incorrect layer causing graphical issues. It is recommended to push the current pose before translating and then popping after all rendering at the offset is completed.
:::

## Renderable

`Renderable`s are essentially objects that are rendered. These include screens, buttons, chat boxes, lists, etc. `Renderable`s only have one method: `#render`. This takes in the `GuiGraphics` used to render things to the screen, the x and y positions of the mouse scaled to the relative screen size, and the tick delta (how many ticks have passed since the last frame).

Some common renderables are screens and 'widgets': interactable elements which typically render on the screen such as `Button`, its subtype `ImageButton`, and `EditBox` which is used to input text on the screen.

## GuiEventListener

Any screen rendered in Minecraft implements `GuiEventListener`. `GuiEventListener`s are responsible for handling user interaction with the screen. These include inputs from the mouse (movement, clicked, released, dragged, scrolled, mouseover) and keyboard (pressed, released, typed). Each method returns whether the associated action affected the screen successfully. Widgets like buttons, chat boxes, lists, etc. also implement this interface.

### ContainerEventHandler

Almost synonymous with `GuiEventListener`s are their subtype: `ContainerEventHandler`s. These are responsible for handling user interaction on screens which contain widgets, managing which is currently focused and how the associated interactions are applied. `ContainerEventHandler`s add three additional features: interactable children, dragging, and focusing.

Event handlers hold children which are used to determine the interaction order of elements. During the mouse event handlers (excluding dragging), the first child in the list that the mouse hovers over has their logic executed.

Dragging an element with the mouse, implemented via `#mouseClicked` and `#mouseReleased`, provides more precisely executed logic.

Focusing allows for a specific child to be checked first and handled during an event's execution, such as during keyboard events or dragging the mouse. Focus is typically set through `#setFocused`. In addition, interactable children can be cycled using `#nextFocusPath`, selecting the child based upon the `FocusNavigationEvent` passed in.

:::note
Screens implement `ContainerEventHandler` through `AbstractContainerEventHandler`, which adds in the setter and getter logic for dragging and focusing children.
:::

## NarratableEntry

`NarratableEntry`s are elements which can be spoken about through Minecraft's accessibility narration feature. Each element can provide different narration depending on what is hovered or selected, prioritized typically by focus, hovering, and then all other cases.

`NarratableEntry`s have three methods: one which determines the priority of the element (`#narrationPriority`), one which determines whether to speak the narration (`#isActive`), and finally one which supplies the narration to its associated output, spoken or read (`#updateNarration`). 

:::note
All widgets from Minecraft are `NarratableEntry`s, so it typically does not need to be manually implemented if using an available subtype.
:::

## The Screen Subtype

With all of the above knowledge, a basic screen can be constructed. To make it easier to understand, the components of a screen will be mentioned in the order they are typically encountered.

First, all screens take in a `Component` which represents the title of the screen. This component is typically drawn to the screen by one of its subtypes. It is only used in the base screen for the narration message.

```java
// In some Screen subclass
public MyScreen(Component title) {
    super(title);
}
```

### Initialization

Once a screen has been initialized, the `#init` method is called. The `#init` method sets the initial settings inside the screen from the `ItemRenderer` and `Minecraft` instance to the relative width and height as scaled by the game. Any setup such as adding widgets or precomputing relative coordinates should be done in this method. If the game window is resized, the screen will be reinitialized by calling the `#init` method.

There are three ways to add a widget to a screen, each serving a separate purpose:

Method                 | Description
:---:                  | :---
`#addWidget`           | Adds a widget that is interactable and narrated, but not rendered.
`#addRenderableOnly`   | Adds a widget that will only be rendered; it is not interactable or narrated.
`#addRenderableWidget` | Adds a widget that is interactable, narrated, and rendered.

Typically, `#addRenderableWidget` will be used most often.

```java
// In some Screen subclass
@Override
protected void init() {
    super.init();

    // Add widgets and precomputed values
    this.addRenderableWidget(new EditBox(/* ... */));
}
```

### Ticking Screens

Screens also tick using the `#tick` method to perform some level of client side logic for rendering purposes. The most common example is the `EditBox` for the blinking cursor.

```java
// In some Screen subclass
@Override
public void tick() {
    super.tick();

    // Add ticking logic for EditBox in editBox
    this.editBox.tick();
}
```

### Input Handling

Since screens are subtypes of `GuiEventListener`s, the input handlers can also be overridden, such as for handling logic on a specific [key press][keymapping].

### Rendering the Screen

Finally, screens are rendered through the `#render` method provided by being a `Renderable` subtype. As mentioned, the `#render` method draws the everything the screen has to render every frame, such as the background, widgets, tooltips, etc. By default, the `#render` method only renders the widgets to the screen.

The two most common things rendered within a screen that is typically not handled by a subtype is the background and the tooltips.

The background can be rendered using `#renderBackground`, with one method taking in a v Offset for the options background whenever a screen is rendered when the level behind it cannot be.

Tooltips are rendered through `GuiGraphics#renderTooltip` or `GuiGraphics#renderComponentTooltip` which can take in the text components being rendered, an optional custom tooltip component, and the x / y relative coordinates on where the tooltip should be rendered on the screen.

```java
// In some Screen subclass

// mouseX and mouseY indicate the scaled coordinates of where the cursor is in on the screen
@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Background is typically rendered first
    this.renderBackground(graphics);

    // Render things here before widgets (background textures)

    // Then the widgets if this is a direct child of the Screen
    super.render(graphics, mouseX, mouseY, partialTick);

    // Render things after widgets (tooltips)
}
```

### Closing the Screen

When a screen is closed, two methods handle the teardown: `#onClose` and `#removed`.

`#onClose` is called whenever the user makes an input to close the current screen. This method is typically used as a callback to destroy and save any internal processes in the screen itself. This includes sending packets to the server.

`#removed` is called just before the screen changes and is released to the garbage collector. This handles anything that hasn't been reset back to its initial state before the screen was opened.

```java
// In some Screen subclass

@Override
public void onClose() {
    // Stop any handlers here

    // Call last in case it interferes with the override
    super.onClose();
}

@Override
public void removed() {
    // Reset initial states here

    // Call last in case it interferes with the override
    super.removed()
;}
```

## `AbstractContainerScreen`

If a screen is directly attached to a [menu][menus], then an `AbstractContainerScreen` should be subclassed instead. An `AbstractContainerScreen` acts as the renderer and input handler of a menu and contains logic for syncing and interacting with slots. As such, only two methods typically need to be overridden or implemented to have a working container screen. Once again, to make it easier to understand, the components of a container screen will be mentioned in the order they are typically encountered.

An `AbstractContainerScreen` typically requires three parameters: the container menu being opened (represented by the generic `T`), the player inventory (only for the display name), and the title of the screen itself. Within here, a number of positioning fields can be set:

Field             | Description
:---:             | :---
`imageWidth`      | The width of the texture used for the background. This is typically inside a PNG of 256 x 256 and defaults to 176.
`imageHeight`     | The height of the texture used for the background. This is typically inside a PNG of 256 x 256 and defaults to 166.
`titleLabelX`     | The relative x coordinate of where the screen title will be rendered.
`titleLabelY`     | The relative y coordinate of where the screen title will be rendered.
`inventoryLabelX` | The relative x coordinate of where the player inventory name will be rendered.
`inventoryLabelY` | The relative y coordinate of where the player inventory name will be rendered.

:::caution
In a previous section, it mentioned that precomputed relative coordinates should be set in the `#init` method. This still remains true, as the values mentioned here are not precomputed coordinates but static values and relativized coordinates.

The image values are static and non changing as they represent the background texture size. To make things easier when rendering, two additional values (`leftPos` and `topPos`) are precomputed in the `#init` method which marks the top left corner of where the background will be rendered. The label coordinates are relative to these values.

The `leftPos` and `topPos` is also used as a convenient way to render the background as they already represent the position to pass into the `#blit` method.
:::caution

```java
// In some AbstractContainerScreen subclass
public MyContainerScreen(MyMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);

    this.titleLabelX = 10;
    this.inventoryLabelX = 10;

    /*
     - If the 'imageHeight' is changed, 'inventoryLabelY' must also be
     - changed as the value depends on the 'imageHeight' value.
     -/
}
```

### Menu Access

As the menu is passed into the screen, any values that were within the menu and synced (either through slots, data slots, or a custom system) can now be accessed through the `menu` field.

### Container Tick

Container screens tick within the `#tick` method when the player is alive and looking at the screen via `#containerTick`. This essentially takes the place of `#tick` within container screens, with its most common usage being to tick the recipe book.

```java
// In some AbstractContainerScreen subclass
@Override
protected void containerTick() {
    super.containerTick();

    // Tick things here
}
```

### Rendering the Container Screen

The container screen is rendered across three methods: `#renderBg`, which renders the background textures, `#renderLabels`, which renders any text on top of the background, and `#render` which encompass the previous two methods in addition to providing a grayed out background and tooltips.

Starting with `#render`, the most common override (and typically the only case) adds the background, calls the super to render the container screen, and finally renders the tooltips on top of it.

```java
// In some AbstractContainerScreen subclass
@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(graphics);
    super.render(graphics, mouseX, mouseY, partialTick);

    /*
     - This method is added by the container screen to render
     - the tooltip of the hovered slot.
     -/
    this.renderTooltip(graphics, mouseX, mouseY);
}
```

Within the super, `#renderBg` is called to render the background of the screen. The most standard representation uses three method calls: two for setup and one to draw the background texture.

```java
// In some AbstractContainerScreen subclass

// The location of the background texture (assets/<namespace>/<path>)
private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(MOD_ID, "textures/gui/container/my_container_screen.png");

@Override
protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    /*
     - Sets the texture location for the shader to use. While up to
     - 12 textures can be set, the shader used within 'blit' only
     - looks at the first texture index.
     -/
    RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);

    /*
     - Renders the background texture to the screen. 'leftPos' and
     - 'topPos' should already represent the top left corner of where
     - the texture should be rendered as it was precomputed from the
     - 'imageWidth' and 'imageHeight'. The two zeros represent the
     - integer u/v coordinates inside the 256 x 256 PNG file.
     -/
    graphics.blit(BACKGROUND_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
}
```

Finally, `#renderLabels` is called to render any text above the background, but below the tooltips. This simply calls uses the font to draw the associated components.

```java
// In some AbstractContainerScreen subclass
@Override
protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);

    // Assume we have some Component 'label'
    // 'label' is drawn at 'labelX' and 'labelY'
    graphics.drawString(this.font, this.label, this.labelX, this.labelY, 0x404040);
}
```

:::note
When rendering the label, you do **not** need to specify the `leftPos` and `topPos` offset. Those have already been translated within the `PoseStack` so everything within this method is drawn relative to those coordinates.
:::

## Registering an AbstractContainerScreen

To use an `AbstractContainerScreen` with a menu, it needs to be registered. This can be done by calling `register` within the `RegisterMenuScreensEvent` on the [**mod event bus**][modbus].

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(MY_MENU.get(), MyContainerScreen::new);
}
```

[menus]: ./menus.md
[network]: ../networking/index.md
[screen]: #the-screen-subtype
[argb]: https://en.wikipedia.org/wiki/RGBA_color_model#ARGB32
[component]: ../resources/client/i18n.md#components
[keymapping]: ../misc/keymappings.md#inside-a-gui
[modbus]: ../concepts/events.md#event-buses
