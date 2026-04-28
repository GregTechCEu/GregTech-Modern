# Screens

Screens are typically the base of all Graphical User Interfaces (GUIs) in Minecraft: taking in user input, verifying it on the server, and syncing the resulting action back to the client. They can be combined with [menus] to create an communication network for inventory-like views, or they can be standalone which modders can handle through their own [network] implementations.

Screens are made up of numerous parts, making it difficult to fully understand what a 'screen' actually is in Minecraft. As such, this document will go over each of the screen's components and how it is applied before discussing the screen itself.

## Rendering a GUI

Rendering a GUI takes place in two steps: the submission phase and the render phase.

The submission phase is responsible for collecting all elements (e.g. buttons, text, items) to render to the screen. Each submission will be stored in the `GuiRenderState` to be processed and then rendered during the render phase. There are four types of elements provided by vanilla: `GuiElementRenderState`, `GuiItemRenderState`, `GuiTextRenderState`, and `PictureInPictureRenderState`. Everything discussed in the below sections takes place during the submission phase by internally creating one of the above render states.

The render phase, as the name implies, renders the elements to the screen. First, `PictureInPictureRenderState`, `GuiItemRenderState`, and `GuiTextRenderState` are prepared and processed into `GuiElementRenderState`s. Then, the elements are sorted before being finally drawn to the screen. Finally, the `GuiRenderState` is reset and ready to be used for the next GUI or render tick.

### Relative Coordinates

Whenever anything is submitted to the render state, there needs to be some coordinates which specifies where the element will be rendered. With numerous abstractions, most of Minecraft's rendering calls accept X and Y coordinates. X values increase from left to right, while Y values increase from top to bottom. However, the coordinates are not fixed to a specified range. Their range can change depending on the size of the screen and the GUI scale specified within the game’s options. As such, extra care must be taken to ensure the coordinates values passed to rendering calls scale properly - are relativized correctly - to the changeable screen size.

Information on how to relativize your coordinates is in the [screen] section.

:::caution
If you choose to use fixed coordinates or incorrectly scale the screen, the rendered objects may look strange or misplaced. An easy way to check if you relativized your coordinates correctly is to click the 'Gui Scale' button in your video settings. This value is used as the divisor to the width and height of your display when determining the scale at which a GUI should render.
:::

### GuiGraphics

Any element submitted to the `GuiRenderState` is typically handled via `GuiGraphics`. `GuiGraphics` is the first parameter to almost every method during the submission phase, containing methods for submitting commonly used objects to be rendered.

`GuiGraphics` exposes the current pose as a `Matrix3x2fStack` to apply any XY transformations:

```java
// For some GuiGraphics graphics

// Push a new matrix onto the stack
graphics.pose().pushMatrix();

// Apply the transformations you want the element to render with

// Takes in some XY offset
graphics.pose().translate(10, 10);
// Takes in some rotation angle in radians
graphics.pose().rotate((float) Math.PI);
// Takes in some XY scalar
graphics.pose().scale(2f, 2f);

// Submit elements to the `GuiRenderState`
graphics.blitSprite(...);

// Pop the matrix to reset the transformations
graphics.pose().popMatrix();
```

Additionally, elements can be cropped to a specific area using `enableScissor` and `disableScissor`:

```java
// For some GuiGraphics graphics

// Enable the scissor with the bounds to render within
graphics.enableScissor(
    // The left X coordinate
    0,
    // The top Y coordinate
    0,
    // The right X coordinate
    10,
    // The bottom Y coordinate
    10
);

// Submit elements to the `GuiRenderState`
graphics.blitSprite(...);

// Disable the scissor to reset the rendering area
graphics.disableScissor();
```

### Node Trees and Strata

When submitting an element to the `GuiRenderState`, it isn't just added to some list. If that were the case, some elements may be completely covered by other elements depending on the order of submission. To get around this hurdle, elements are initially sorted into node trees in some stratum. How an element is sorted is based upon its defined `ScreenArea#bounds`; otherwise, the element will not be submitted for rendering.

The `GuiRenderState` is made up of `GuiRenderState.Node`s as a doubly-linked list, using 'up' and 'down' instead of 'first' and 'last'. Nodes are rendered from 'down' to 'up'. Each node holds its own layer data containing the render states. When an element is initially submitted to `GuiRenderState`, it determines what node to use or create based upon its defined `ScreenArea#bounds`. The node chosen, or created, is one node above the highest node with intersecting elements.

:::warning
Although `ScreenArea#bounds` is marked as nullable, a element being submitted to the render state will not be added if the bounds is not defined. The method is only nullable as elements submitted during the render phase are added to the current node rather than computing its node based on the bounds.
:::

Each node list is known as a stratum within the render state. A render state can have multiple strata by calling `GuiGraphics#nextStratum`, creating a new node list. The new stratum will render above all the previous stratum's elements (e.g., item tooltips). You cannot navigate back to the previous stratum once you call `nextStratum`.

### `GuiElementRenderState`

A `GuiElementRenderState` holds the metadata on how a GUI element is rendered to the screen. The element render state extends `ScreenArea` to  define the `bounds` on the screen. The bounds should always encompass the entire element that is rendered so that it's sorted correctly in the node list. Bounds computation typically takes in some of the parameters below, including the position and pose.

`scissorArea` crops the area where the element can render. If `scissorArea` is `null`, then the entire element is rendered to the screen. Similarly, if the `scissorArea` rectangle does not intersect with the `bounds`, then nothing will be rendered.

The remaining three methods handle the actual rendering of the element. `pipeline` defines the shaders and metadata used by the element. `textureSetup` can specify either `Sampler0`, `Sampler1`, `Sampler2`, or some combination in the fragment shader. Finally, `buildVertices` passes the vertices to upload to the buffer. It takes in the `VertexConsumer` to pass the vertices to, as well as the Z coordinate to use.

NeoForge adds the method `GuiGraphics#submitGuiElementRenderState` to submit a custom element render state if the available methods provided by `GuiGraphics` is not enough.

```java
// For some GuiGraphics graphics
graphics.submitGuiElementRenderState(new GuiElementRenderState() {

    // Store the current pose of the stack
    private final Matrix3x2f pose = new Matrix3x2f(graphics.pose());
    // Store the current scissor area
    @Nullable
    private final ScreenRectangle scissorArea = graphics.peekScissorStack();

    @Override
    public ScreenRectangle bounds() {
        // We will assume the bounds is 0, 0, 10, 10
        
        // Compute the initial rectangle
        ScreenRectangle rectangle = new ScreenRectangle(
            // The XY position
            0, 0,
            // The width and height of the element
            10, 10
        );

        // Transform the rectangle to its appropriate location using the pose
        rectangle = rectangle.transformMaxBounds(this.pose);

        // If there is a scissor area defined, return the intersection of the two rectangles
        // Otherwise, return the full bounds
        return this.scissorArea != null
            ? this.scissorArea.intersection(rectangle)
            : rectangle;
    }

    @Override
    @Nullable
    public ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        // Returns the textures to be used by the samplers in a fragment shader
        // When used by the fragment shader:
        // - Sampler0 typically contains the element texture
        // - Sampler1 typically provides a second element texture, currently only used by the end portal pipeline
        // - Sampler2 typically contains the game's lightmap texture

        // Should generally specify at least one texture in Sampler0
        return TextureSetup.noTexture();
    }

    @Override
    public void buildVertices(VertexConsumer consumer, float z) {
        // Build the vertices using the vertex format specified by the pipeline
        // For GUI, uses quads with position and color
        // Color must be in ARGB format
        consumer.addVertexWith2DPose(this.pose, 0,  0,  z).setUv(0, 0).setColor(0xFFFFFFFF);
        consumer.addVertexWith2DPose(this.pose, 0,  10, z).setUv(0, 1).setColor(0xFFFFFFFF);
        consumer.addVertexWith2DPose(this.pose, 10, 10, z).setUv(1, 1).setColor(0xFFFFFFFF);
        consumer.addVertexWith2DPose(this.pose, 10, 0,  z).setUv(1, 0).setColor(0xFFFFFFFF);
    }
});
```

### Element Ordering

So far, the elements shown above have only been operating on XY coordinates. The Z coordinate, on the other hand, is automatically computed based upon the elements render order. Starting at a Z of `0`, each element is rendered `0.01` in front of the previous element. 3D elements are drawn to a 2D texture before being rendered to the screen, so Z-fighting rarely, if ever, occurs.

During the render phase, each stratum is rendered in order, with the nodes in the node list rendered from 'down' to 'up'. But what about within a given node? This is handled via the `GuiRenderer#ELEMENT_SORT_COMPARATOR`, which sorts elements based on their `GuiElementRenderState#scissorArea`, `pipeline`, then `textureSetup`.

:::warning
Glyphs rendered for text are not sorted and will always render after all elements in the current node.
:::

Elements with no specified `scissorArea` will always be rendered first, followed by the top Y, the bottom Y, the left X, and finally the right X. If the `scissorArea` for two elements match, the sort key of the `pipeline` (via `RenderPipeline#getSortKey`) will be used. The sort key is based on the order that the `RenderPipeline`s are built in, which in vanilla is the classloading of static constants within `RenderPipelines`. If the sort keys match, then the `textureSetup` is used. Elements with no specified `textureSetup` are ordered first, followed by the sort key (via `TextureSetup#getSortKey`) of texture elements.

:::warning
On a technical level, element ordering is not deterministic due to the `RenderPipeline` and `TextureSetup`. This is because the goal of sorting is not determinism, but rather to render the elements with the least amount of pipeline and texture switches possible.
:::

## Methods in `GuiGraphics`

`GuiGraphics` contains methods used to submit commonly used objects for rendering. These fall into six categories: colored rectangles, strings, textures, items, tooltips, and picture-in-pictures. Each of these methods submit an element, inheriting the current pose from `pose` and the scissor area from `peekScissorStack` based on `enableScissor` / `disableScissor`. Any colors provided to the methods must be in [ARGB][argb] format.

### Colored Rectangles

Colored rectangles are submitted using a `ColoredRectangleRenderState`. All fill methods can take in an optional `RenderPipeline` and `TextureSetup` to specify how the rectangle should be rendered. There are three types of colored rectangles that can be submitted.

First, there is a colored horizontal and vertical one-pixel wide line, `hLine` and `vLine` respectively. `hLine` takes in two X coordinates defining the left and right (inclusively), the top Y coordinate, and the color. `vLine` takes in the left X coordinate, two Y coordinates defining the top and bottom (inclusively), and the color.

Second, there is the `fill` method, which submits a rectangle to be drawn to the screen. The line methods internally call this method. This takes in the left X coordinate, the top Y coordinate, the right X coordinate, the bottom Y coordinate, and the color.

Third, there is the `renderOutline` method, which submits four rectangles that are one-pixel wide to act as an outline. This takes in the left X coordinate, the top Y coordinate, the width of the outline, the height of the outline, and the color.

Finally, there is the `fillGradient` method, which draws a rectangle with a vertical gradient. This takes in the left X coordinate, the top Y coordinate, the right X coordinate, the bottom Y coordinate, and the bottom and top colors.

### Strings

Strings, [`Component`s][component], and `FormattedCharSequence`s are submitted using a `GuiTextRenderState`. Each string is drawn through the provided `Font`, which is used to create a `BakedGlyph.GlyphInstance` and optionally a `BakedGlyph.Effect`, using the specified `GlyphRenderTypes#guiPipeline`. The text render state is then transformed into `GlyphRenderState`s and potentially a `GlyphEffectRenderState` per character in the string during the render phase.

There are two alignments strings can be rendered with: a left-aligned string (`drawString`) and a center-aligned string (`drawCenteredString`). These both take in the font the string will be rendered in, the string to draw, the X coordinate representing the left or center of the string respectively, the top Y coordinate, and the color. The left-aligned strings may also take in whether to draw a drop shadow for the text.

If the text should be wrapped within a given bounds, then `drawWordWrap` can be used instead. If the text should have some sort of rectangle backdrop, then `drawStringWithBackdrop` can be used. They both submit a left-aligned string by default.

:::note
Strings should typically be passed in as [`Component`s][component] as they handle a variety of use cases, including the two other overloads of the method.
:::

### Textures

Textures are submitted through a `BlitRenderState`, hence the method name `blit`. The `BlitRenderState` copies the bits of an image and renders them to the screen through the `RenderPipeline` parameter. Each `blit` also takes in a `ResourceLocation`, which represents the absolute location of the texture:

```java
// Points to 'assets/examplemod/textures/gui/container/example_container.png'
private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("examplemod", "textures/gui/container/example_container.png");
```

While there are many different `blit` overloads, we will only discuss two of them.

The first `blit` takes in two integers, then two floats, and finally four more integers, assuming the image is on a PNG file. It takes in the left X and top Y screen coordinate, the left X and top Y coordinate within the PNG, the width and height of the image to render, and the width and height of the PNG file.

:::tip
The size of the PNG file must be specified so that the coordinates can be normalized to obtain the associated UV values.
:::

The second `blit` adds an additional integer at the end which represents the tint color of the image to be drawn. If not specified, the tint color is `0xFFFFFFFF`.

#### `blitSprite`

`blitSprite` is a special implementation of `blit` where the texture is obtained from the GUI texture atlas. Most textures that overlay the background, such as the 'burn progress' overlay in furnace GUIs, are sprites. All sprite textures are relative to `textures/gui/sprites` and do not need to specify the file extension.

```java
// Points to 'assets/examplemod/textures/gui/sprites/container/example_container/example_sprite.png'
private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath("examplemod", "container/example_container/example_sprite");
```

One set of `blitSprite` methods have the same parameters as `blit`, except for the the four integers dealing with the coordinates, width, and height of the PNG.

The other `blitSprite` methods take in more texture information to allow for drawing a portion of the sprite. These methods take in the sprite width and height, the X and Y coordinate in the sprite, the left X and top Y screen coordinate, the tint color, and the width and height of the image to render.

If the sprite size does not match the texture size, then the sprite can be scaled in one of three ways: `stretch`, `tile`, and `nine_slice`. `stretch` stretches the image from the texture size to the screen size. `tile` renders the texture over and over again until it reaches the screen size. `nine_slice` divides the texture into one center, four edges, and four corners to tile the texture to the required screen size.

This is set by adding the `gui.scaling` JSON object in an mcmeta file with the same name of the texture file.

```json5
// For some texture file example_sprite.png
// In example_sprite.png.mcmeta

// Stretch example
{
    "gui": {
        "scaling": {
            "type": "stretch"
        }
    }
}

// Tile example
{
    "gui": {
        "scaling": {
            "type": "tile",
            // The size to begin tiling at
            // This is usually the size of the texture
            "width": 40,
            "height": 40
        }
    }
}

// Nine slice example
{
    "gui": {
        "scaling": {
            "type": "nine_slice",
            // The size to begin tiling at
            // This is usually the size of the texture
            "width": 40,
            "height": 40,
            "border": {
                // The padding of the texture that will be sliced into the border texture
                "left": 1,
                "right": 1,
                "top": 1,
                "bottom": 1
            },
            // When true the center part of the texture will be applied like
            // the stretch type instead of a nine slice tiling.
            "stretch_inner": true
        }
    }
}
```

### Items

Items are submitted using a `GuiItemRenderState`. The item render state is then transformed into a `BlitRenderState` or `OversizedItemRenderState`, depending on the item bounds and client item properties, during the render phase.

`renderItem` takes in an `ItemStack`, in addition to the left X and top Y coordinate on screen. It can optionally take in the holding `LivingEntity`, the current `Level` the stack is in, and a seeded value. There is also an alternative `renderFakeItem` which sets the `LivingEntity` to `null`.

The item decorations - such as the durability bar, cooldown, and count - is handled through `renderItemDecorations`. It takes in the same parameters as the base `renderItem`, in addition to the `Font` and a count text override.

### Tooltips

Tooltips are submitted through a variety of the above render states. The tooltip methods are broken into two categories: 'next frame' and 'immediate'. Both methods takes in the `Font` to render the text, some list of `Component`s, an optional `TooltipComponent` for special rendering, the left X and top Y, a `ClientTooltipPositioner` for adjusting the location, and the background and frame texture.

Next frame tooltips don't actually submit the tooltip on the next frame, but instead defer the tooltip submission until after `Screen#render` is called. The tooltip is added to a new stratum, meaning it will render on top of all elements in the screen. Next frame methods are in the form of `set*Tooltip*ForNextFrame`. They also can take in an additional boolean indicating whether to override the currently deferred tooltip if present, and an `ItemStack` that the rendered tooltip should use.

Immediate tooltips, on the other hand, are submitted immediately when the method is called. Immediate methods are in the form of `renderTooltip`. They also take in the `ItemStack` that the tooltip is hovering over.

### Picture-in-Picture

Picture-in-Picture (PiP) allows for arbitrary objects to be drawn to the screen. Instead of drawing directly to the output, PiP draws the object to an intermediary texture, or a 'picture', that is then submitted to the `GuiRenderState` as a `BlitRenderState` during the render phase. `GuiGraphics` provides methods for maps, entities, player skins, book models, banner pattern, signs, and the profiler chart via `submit*RenderState`.

:::note
Items that exceed the default 16x16 bounds, when `ClientItem.Properties#oversizedInGui` is true, use the `OversizedItemRenderer` PiP as its rendering mechanism.
:::

Each PiP submits a `PictureInPictureRenderState` to render an object to the screen. Similarly to `GuiElementRenderState`, `PictureInPictureRenderState` also extends `ScreenArea` to define its `bounds` and the scissor via `scissorArea`. `PictureInPictureRenderState` then defines the render location and size of the picture, specifying the left X (`x0`), the right X (`x1`), the top Y (`y0`), and the bottom Y (`y1`). The element within the picture can also be `scale`d by some float value. Finally, an additional `pose` can be used to transform the XY coordinates of the picture. By default, this is the identity pose as generally, the rendered object is already transformed within the picture itself. For ease of implementation, the `bounds` can be computed using `PictureInPictureRenderState#getBounds`, though if the `pose` is modified, you will need to implement your own logic.

```java
// Other parameters can be added, but this is the minimum required to implement all methods
public record ExampleRenderState(
    int x0, // The left X
    int x1, // The right X
    int y0, // The top Y
    int y1, // The bottom Y
    float scale, // The scale factor when drawing to the picture
    @Nullable ScreenRectangle scissorArea, // The rendering area
    @Nullable ScreenRectangle bounds // The bounds of the element
) implements PictureInPictureRenderState {

    // Additional constructors
    public ExampleRenderState(int x, int y, int width, int height, @Nullable ScreenRectangle scissorArea) {
        this(
            x, // x0
            x + width, // x1
            y, // y0
            y + height, // y1
            1f, // scale
            scissorArea,
            PictureInPictureRenderState.getBounds(x, y, x + width, y + height, scissorArea)
        );
    }
}
```

To draw and submit the PiP render state to a picture, each PiP has its own `PictureInPictureRenderer<T>`, where `T` is the implemented `PictureInPictureRenderState`. There are numerous methods that can be overridden, allowing the user almost full control of the entire pipeline, but there are three that must be implemented.

First is `getRenderStateClass`, which simply returns the class of the `PictureInPictureRenderState`. In vanilla, this method was used to register what render state the renderer was used for. NeoForge still uses the render state class, but provides registration through an event to map to a dynamic pool of renderers instead of calling `getRenderStateClass`.

Then, there is `getTextureLabel`, which provides a unique debug label for the picture being written to. Finally, there is `renderToTexture`, which actually draws the object to the picture, similar to other render methods.

```java
public class ExampleRenderer extends PictureInPictureRenderer<ExampleRenderState> {

    // Takes in the buffers used to write the object to the picture
    public ExampleRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<ExampleRenderState> getRenderStateClass() {
        // Returns the render state class
        return ExampleRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        // Can be any string, but should be unique
        // Prefix with mod id for greater clarity
        return "examplemod: example pip";
    }

    @Override
    protected void renderToTexture(ExampleRenderState renderState, PoseStack pose) {
        // Modify pose if desired
        // Can push/pop if wanted, but a new `PoseStack` is created for writing to the picture
        pose.translate(...);

        // Render the object to the screen
        VertexConsumer consumer = this.bufferSource.getBuffer(RenderType.lines());
        consumer.addVertex(...).setColor(...).setNormal(...);
        consumer.addVertex(...).setColor(...).setNormal(...);
    }

    // Additional methods

    @Override
    protected void blitTexture(ExampleRenderState renderState, GuiRenderState guiState) {
        // Submits the picture to the gui render state as a `BlitRenderState` by default
        // Override this if you want to modify the `BlitRenderState`
        // Should call `GuiRenderState#submitBlitToCurrentLayer`
        // Bounds can be `null`
        super.blitTexture(renderState, guiState);
    }

    @Override
    protected boolean textureIsReadyToBlit(ExampleRenderState renderState) {
        // When true, this reuses the already written-to picture instead of
        // constructing a new picture and writing to it using `renderToTexture`.
        // This should only be true if it is guaranteed that two elements will
        // be rendered *exactly* the same.
        return super.textureIsReadyToBlit(renderState);
    }

    @Override
    protected float getTranslateY(int scaledHeight, int guiScale) {
        // Sets the initial offset the `PoseStack` is translated by in the Y direction.
        // Common implementations use `scaledHeight / 2f` to center the Y coordinate similar to X.
        return scaledHeight;
    }

    @Override
    public boolean canBeReusedFor(ExampleRenderState state, int textureWidth, int textureHeight) {
        // A NeoForge-added method used to check if this renderer can be reused on a subsequent frame.
        // When true, this will reuse the constructed state and renderer from the previous frame.
        // When false, a new renderer will be created.
        return super.canBeReusedFor(state, textureWidth, textureHeight);
    }
}
```

To use the PiP, the renderer must be registered to `RegisterPictureInPictureRenderersEvent` on the [mod event bus][modbus].

```java
@SubscribeEvent // on the mod event bus
public static void registerPip(RegisterPictureInPictureRenderersEvent event) {
    event.register(
        // The PiP render state class
        ExampleRenderState.class,
        // A factory that takes in the `MultiBufferSource.BufferSource` and returns the PiP renderer
        ExampleRenderer::new
    );
}
```

The PiP render state can then be submitted using the NeoForge-added `GuiGraphics#submitPictureInPictureRenderState`:

```java
// For some GuiGraphics graphics
graphics.submitPictureInPictureRenderState(new ExampleRenderState(
    0, 0,
    10, 10,
    // Get the scissor area from the stack
    graphics.peekScissorStack()
));
```

:::note
NeoForge fixes a bug that prevents multiple instances of a PiP render state to be submitted for any given frame.
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

`NarratableEntry`s have four methods: two which determine the priority of the element when being read (`#narrationPriority` and `#getTabOrderGroup`), one which determines whether to speak the narration (`#isActive`), and finally one which supplies the narration to its associated output, spoken or read (`#updateNarration`). 

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

Once a screen has been initialized, the `#init` method is called. The `init` method sets the initial settings inside the screen from the `Minecraft` instance to the relative width and height as scaled by the game. Any setup such as adding widgets or precomputing relative coordinates should be done in this method. If the game window is resized, the screen will be reinitialized by calling the `init` method.

There are three ways to add a widget to a screen, each serving a separate purpose:

| Method               | Description                                                                   |
|:--------------------:|:------------------------------------------------------------------------------|
|`addWidget`           | Adds a widget that is interactable and narrated, but not rendered.            |
|`addRenderableOnly`   | Adds a widget that will only be rendered; it is not interactable or narrated. |
|`addRenderableWidget` | Adds a widget that is interactable, narrated, and rendered.                   |

Typically, `addRenderableWidget` will be used most often.

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

Screens also tick using the `#tick` method to perform some level of client side logic for rendering purposes.

```java
// In some Screen subclass
@Override
public void tick() {
    super.tick();

    // Execute some logic every frame
}
```

### Input Handling

Since screens are subtypes of `GuiEventListener`s, the input handlers can also be overridden, such as for handling logic on a specific [key press][keymapping].

### Rendering the Screen

Screens submit their elements through `#renderWithTooltip` in three different strata: the background stratum, the render stratum, and the optional tooltip stratum.

The background stratum elements are submitted first via `#renderBackground`, generally containing any blurring or background textures.

:::warning
Blurring, as handled through `GuiGraphics#blurBeforeThisStratum`, can only be called once on any given frame. Attempting to render a second blur will cause an exception to be thrown.
:::

The render stratum elements are submitted next via the `#render` method, provided by being a `Renderable` subtype. This mainly submits widgets and labels, along with setting the tooltips to render in the next stratum.

Finally, the tooltip stratum submits the set tooltip. Tooltips are submitted in the render stratum via `GuiGraphics#setTooltipForNextFrame` or `GuiGraphics#setComponentTooltipFromElementsForNextFrame`, which can take in the text or tooltip components being submitted and the XY relative coordinates on where the tooltip should be rendered on the screen.

```java
// In some Screen subclass

// mouseX and mouseY indicate the scaled coordinates of where the cursor is in on the screen
@Override
public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Submit things on the background stratum
    this.renderTransparentBackground(graphics);
}

@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Submit things before widgets

    // Then the widgets if this is a direct child of the Screen
    super.render(graphics, mouseX, mouseY, partialTick);

    // Submit things after widgets

    // Set the tooltip to be added above everything in this method
    graphics.setTooltipForNextFrame(...);
}
```

### Closing the Screen

When a screen is closed, two methods handle the teardown: `#onClose` and `#removed`.

`onClose` is called whenever the user makes an input to close the current screen. This method is typically used as a callback to destroy and save any internal processes in the screen itself. This includes sending packets to the server.

`removed` is called just before the screen changes and is released to the garbage collector. This handles anything that hasn't been reset back to its initial state before the screen was opened.

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

If a screen is directly attached to a [menu][menus], then an `AbstractContainerScreen` should be subclassed instead. An `AbstractContainerScreen` acts as the screen and input handler of a menu and contains logic for syncing and interacting with slots. As such, only two methods typically need to be overridden or implemented to have a working container screen. Once again, to make it easier to understand, the components of a container screen will be mentioned in the order they are typically encountered.

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
In a previous section, it was mentioned that precomputed relative coordinates should be set in the `#init` method. This still remains true, as the values mentioned here are not precomputed coordinates but static values and relativized coordinates.

The image values are static and non-changing, as they represent the background texture size. To make things easier when rendering, two additional values (`leftPos` and `topPos`) are precomputed in the `init` method, marking the top left corner of where the background will be rendered. The label coordinates are relative to these values.

The `leftPos` and `topPos` is also used as a convenient way to render the background as they already represent the position to pass into `GuiGraphics#blit`.
:::

```java
// In some AbstractContainerScreen subclass
public MyContainerScreen(MyMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);

    this.titleLabelX = 10;
    this.inventoryLabelX = 10;

    // If the 'imageHeight' is changed, 'inventoryLabelY' must also be
    // changed as the value depends on the 'imageHeight' value.
}
```

### Menu Access

As the menu is passed into the screen, any values that were within the menu and synced (either through slots, data slots, or a custom system) can now be accessed through the `menu` field.

### Container Tick

Container screens tick within the `#tick` method when the player is alive and looking at the screen via `#containerTick`. This essentially takes the place of `tick` within container screens, with its most common usage being to tick the recipe book.

```java
// In some AbstractContainerScreen subclass
@Override
protected void containerTick() {
    super.containerTick();

    // Tick things here
}
```

### Rendering the Container Screen

The container screen uses all three strata to submit its elements. First, the background stratum submits the background texture via `#renderBg`. Then, the render stratum submits the widgets like before via `#render` followed by any text via `#renderLabels`. Finally, `AbstractContainerScreen` also provides the helper method `renderTooltip` to submit the tooltip to the tooltip stratum.

Starting with `render`, the most common override (and typically the only case) calls the super to submit the container screen elements, followed by `renderTooltip`.

```java
// In some AbstractContainerScreen subclass
@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Submits the widgets and labels to be rendered
    super.render(graphics, mouseX, mouseY, partialTick);

    // This method is added by the container screen to submit
    // the tooltip of the hovered slot in the tooltip stratum.
    this.renderTooltip(graphics, mouseX, mouseY);
}
```

`renderBg` is called to submit the background elements of the screen to the background stratum.

```java
// In some AbstractContainerScreen subclass

// The location of the background texture (assets/<namespace>/<path>)
private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/container/my_container_screen.png");

@Override
protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    // Submits the background texture. 'leftPos' and 'topPos' should
    // already represent the top left corner of where the texture
    // should be rendered as it was precomputed from the 'imageWidth'
    // and 'imageHeight'. The two zeros represent the integer u/v
    // coordinates inside the PNG file, whose size is represented by
    // the last two integers (typically 256 x 256).
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        BACKGROUND_LOCATION,
        this.leftPos, this.topPos,
        0, 0,
        this.imageWidth, this.imageHeight,
        256, 256
    );
}
```

`renderLabels` is called to submit any text after the widgets in the render stratum. This calls `drawString` with the screen font to submit the associated components.

```java
// In some AbstractContainerScreen subclass
@Override
protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);

    // Assume we have some Component 'label'
    // 'label' is drawn at 'labelX' and 'labelY'
    // The color is an ARGB value
    // The final boolean renders the drop shadow when true
    graphics.drawString(this.font, this.label, this.labelX, this.labelY, 0xFF404040, false);
}
```

:::note
When submitting the label, you do **not** need to specify the `leftPos` and `topPos` offset. Those have already been translated within the `Matrix3x2fStack` so everything within this method is submitted relative to those coordinates.
:::

## Registering an AbstractContainerScreen

To use an `AbstractContainerScreen` with a menu, it needs to be registered. This can be done by calling `register` within the `RegisterMenuScreensEvent` on the [**mod event bus**][modbus].

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(MY_MENU.get(), MyContainerScreen::new);
}
```

[menus]: menus.md
[network]: ../networking/index.md
[screen]: #the-screen-subtype
[argb]: https://en.wikipedia.org/wiki/RGBA_color_model#ARGB32
[component]: ../resources/client/i18n.md#components
[keymapping]: ../misc/keymappings.md#inside-a-gui
[modbus]: ../concepts/events.md#event-buses
