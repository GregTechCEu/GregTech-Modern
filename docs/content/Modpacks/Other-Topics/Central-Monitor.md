---
title: Central Monitor & Placeholder System
---

!!! info "But can it run Doom?"
    **YES.** <h6>_If anyone is crazy enough to program Doom in the placeholder language._</h6>

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
            // this is only run on the logical server side
            // put all of your module's logic here instead of in getRenderer(stack)
            // can also be left completely empty (like in the image module)
        }

        @Override
        public void tickInPlaceholder(ItemStack stack, PlaceholderContext context) {
            // this is also only run on the logical server side
            // but this is only called when a placeholder accesses this module (and wants to render it)
            // this is not called on each tick
            // you can even put the same code here as in the tick() method (like the text module)
        }

        @Override
        public IMonitorRenderer getRenderer(ItemStack stack) {
            // this is only called on the logical client side
            // should return a new instance of the renderer for this module (not null)
            // for examples of renderer code look in the GTCEu Modern github (src/main/java/com/gregtechceu/gtceu/client/renderer)
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

### Placeholders
Placeholders can be used by players in the monitor text module, or in the computer monitor cover (though a bit more limited).
For example, a player may write something like this in a text module:
```
Hello on day {calc {tick} / 20000}!
Current energy buffer: {formatInt {energy}}/{formatInt {energyCapacity}} EU\
{if {cmp {energy} < 5000000} {color red "\nLOW ENERGY!"}}
Here's some random stuff:
{repeat 5 {repeat {random 2 10} {block}}
```
And something like this would be displayed:
```
Hello on day 420!
Current energy buffer: 4.2M/6.9M EU
LOW ENERGY!
Here's some random stuff:
███████
██
█████
████
██████████
```
This system is turing-complete (i.e. if the player really wanted to play Doom on the Central Monitor, they could).<br>
All placeholders work on strings (or, more specifically, `Component`s to allow text formatting), so when you write `{calc {calc 2 + 4} * 3}`,
first `{calc 2 + 4}` will be evaluated into `6`, then it will be converted to a string and back to an int, and then it will be passed into the second placeholder
to evaluate `{calc 6 * 3}` into `18`, which will be turned into a string again. That also allows for things like `{calc 3 + 1}2`, which will evaluate into `42`,
since outside of placeholders text is simply concatenated. Placeholder arguments are separated by spaces, which may be a bit annoying, when wanting to pass a string
with a space into a placeholder, for example `{if 1 string with spaces}`, which will cause an error. In these cases, double quotes can be used: `{if 1 "string with spaces"}`
will work perfectly fine. There are placeholders that need reference items, to achieve that, there are 8 slots in the text module's UI on the left.
Items can be inserted/extracted from these slots automatically using the `ender` placeholder by interacting with Ender Item Links.<br>

!!! tip "The full list of placeholders with explanations on what they do and usage examples can be found in-game in the text module or computer monitor UI on the left."

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