---
title: Central Monitor & Placeholder System
---

### Custom monitor modules
If you want to add a monitor module, simply attach a component that implements `IMonitorModuleItem` to your `ComponentItem`.
Modules can have a custom UI, can be ticked (in a placeholder or not) and, most importantly, rendered.
??? example "Example of a custom module in Java"
    ```java
    public class ExampleModuleBehaviour implements IMonitorModuleItem {
        @Override
        public String getType() {
            // can be any string, this is currently only used for CC: Tweaked compat
            return "example";
        }

        @Override
        public void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
            // this is only called on the logical server
            // put all of your module's logic here instead of in getRenderer(stack)
            // can also be left completely empty (like in the image module)
        }

        @Override
        public void tickInPlaceholder(ItemStack stack, PlaceholderContext context) {
            // this is also only called on the logical server, but only when a placeholder accesses this module and wants to render it
            // this *isn't* called on each tick
            // you can even put the same code here as in the tick() method, like the text module does
        }

        @Override
        public IMonitorRenderer getRenderer(ItemStack stack) {
            // this is only called on the logical client
            // should return a new instance of the renderer for this module (not null)
            // for examples of renderer code look in the GTCEu Modern github:
            // https://github.com/GregTechCEu/GregTech-Modern/tree/1.20.1/src/main/java/com/gregtechceu/gtceu/client/renderer
            return new MonitorTextRenderer(MultiLineComponent.of("this text is displayed on the monitor"), 1.0);
        }

        @Override
        public Widget createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
            // should create the UI for your module and return it
            // if the module doesn't need a UI just return new WidgetGroup()
            return new WidgetGroup();
        }
    }
    ```

!!! info "For info on the placeholder system itself, see [the gameplay wiki page](../../Gameplay/Central-Monitor.md)"

### Adding custom placeholders

Placeholders can be added by calling `PlaceholderHandler.addPlaceholder(...)` at any point during runtime (preferably at mod init time).
They can take any number of arguments in the form of a `List<MultiLineComponent>`. They also take an instance of `PlaceholderContext` and
must return a `MultiLineComponent`. Placeholders can also render literally anything, not only text, using `MultiLineComponent.addRenderer()`,
`GraphicsComponent` and an `IPlaceholderRenderer` (that has to be registered separately using `PlaceholderHandler.addRenderer(...)`)

??? example "Example of a `sum` placeholder in Java"
    ```java
    public class Example {
        // you should call this function at mod initialization
        public static void addPlaceholders() {
            int priority = 1; // by default the priority of all placeholders is 0 (you don't have to specify it)
            PlaceholderHandler.addPlaceholder(new Placeholder("sum", priority) {
                @Override
                public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) throws PlaceholderException {
                    PlaceholderUtils.checkArgs(args, 2); // check that there are exactly 2 arguments
                    double a = PlaceholderUtils.toDouble(args.get(0));
                    double b = PlaceholderUtils.toDouble(args.get(1));
                    return MultiLineComponent.literal(a + b);
                }
            });
            // you can call addPlaceholder as many times as you need
            // if you want to override an existing placeholder, simply add a new one with the same name and a higher or equal priority
        }
    }
    ```

!!! tip "Placeholder exceptions"
    Any runtime exception that occurs while processing a placeholder will be caught and even displayed to the player.
    Instead of relying on runtime exceptions though, you should throw any subclass of `PlaceholderException`, for example
    `InvalidNumberException` or `MissingItemException`. All the `PlaceholderUtils` methods throw these, so you should use them
    instead of calling `parseDouble` yourself, for example.

!!! note "Placeholder data"
    If your placeholder needs to save any data specific to the placeholder caller, you can use `getData(ctx)` at any point in
    a placeholder. It will return a `CompoundTag` that is automatically saved, and you're free to modify it in whatever way you want.

### Placeholder graphics

You may have noticed, that some placeholders output graphics instead of text, for example `rect` or `quad`.
To achieve that you have to write your own class that implements `IPlaceholderRenderer`, or use an existing one.
They work similarly to normal renderers, except you can pass a `CompoundTag` into them from your placeholder.
To register one, call `PlaceholderHandler.addRenderer("put_id_here", new YourRendererClassHere())`.
After that, you can reference it from any placeholder by calling `output.addGraphics(new GraphicsComponent(x, y, "put_id_here", renderData)`
on the object that your placeholder will return. `renderData` is the same `CompoundTag` that will be passed into your renderer as an argument.
This is done to avoid calling rendering code on the server side, as all placeholders are processed server-side only. A neat side effect of that
is that all players will (almost always) see the same thing on the monitor.

!!! warning "Graphics do not work on the Computer Monitor Cover"

### Placeholder parsing

You may want to add something that needs to parse a string containing placeholders. To achieve that, you can use
`PlaceholderHandler.processPlaceholders(string, context)`. You can also use `PlaceholderHandler.placeholderExists(name)`
to check if a placeholder exists, or `PlaceholderHandler.getAllPlaceholderNames()` to get all placeholders.
To get a `PlaceholderContext`, you just have to call its constructor (it takes in basic parameters like `Level`, `BlockPos`, etc., most of which can be `null`).