# BlockEntityRenderer

A `BlockEntityRenderer`, often abbreviated as BER, is used to render [blocks][block] in a way that cannot be represented with a [static baked model][model] (JSON, OBJ, others). For example, this could be used to dynamically render container contents of a chest-like block. A block entity renderer requires the block to have a [`BlockEntity`][blockentity], even if the block does not store any data otherwise.

To create a BER, create a class that inherits from `BlockEntityRenderer`. It takes a generic argument specifying the block's `BlockEntity` class, which is used as a parameter type in the BER's `render` method.

```java
// Assumes the existence of MyBlockEntity as a subclass of BlockEntity.
public class MyBlockEntityRenderer implements BlockEntityRenderer<MyBlockEntity> {
    // Add the constructor parameter for the lambda below. You may also use it to get some context
    // to be stored in local fields, such as the entity renderer dispatcher, if needed.
    public MyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    // This method is called every frame in order to render the block entity. Parameters are:
    // - blockEntity:   The block entity instance being rendered. Uses the generic type passed to the super interface.
    // - partialTick:   The amount of time, in fractions of a tick (0.0 to 1.0), that has passed since the last tick.
    // - poseStack:     The pose stack to render to.
    // - bufferSource:  The buffer source to get vertex buffers from.
    // - packedLight:   The light value of the block entity.
    // - packedOverlay: The current overlay value of the block entity, usually OverlayTexture.NO_OVERLAY.
    @Override
    public void render(MyBlockEntity blockEntity, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Do the rendering here.
    }
}
```

Only one BER may exist for a given `BlockEntityType<?>`. Therefore, values that are specific to a single block entity instance should be stored in that block entity instance, rather than the BER itself.

When you have created your BER, you must also register it to `EntityRenderersEvent.RegisterRenderers`, an [event] fired on the [mod event bus][eventbus]:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(
            // The block entity type to register the renderer for.
            MyBlockEntities.MY_BLOCK_ENTITY.get(),
            // A function of BlockEntityRendererProvider.Context to BlockEntityRenderer.
            MyBlockEntityRenderer::new
    );
}
```

In the event that you do not need the BER provider context in your BER, you can also remove the constructor:

```java
public class MyBlockEntityRenderer implements BlockEntityRenderer<MyBlockEntity> {
    @Override
    public void render( /* ... */ ) { /* ... */ }
}

// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(MyBlockEntities.MY_BLOCK_ENTITY.get(),
            // Pass the context to an empty (default) constructor call
            context -> new MyBlockEntityRenderer()
    );
}
```

## `BlockEntityWithoutLevelRenderer`

`BlockEntityWithoutLevelRenderer`, colloquially known as BEWLR, is an adaptation of the regular `BlockEntityRenderer` for special [item] rendering (hence "without level", as items do not have level context). Its overall purpose is the same: do special rendering for cases where static models aren't enough.

To add a BEWLR, create a class that extends `BlockEntityWithoutLevelRenderer` and overrides `#renderByItem`. It also requires some additional constructor setup:

```java
public class MyBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    // We need some boilerplate in the constructor, telling the superclass where to find the central block entity and entity renderers.
    public MyBlockEntityWithoutLevelRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
    
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Do the rendering here.
    }
}
```

Keep in mind that, like with BERs, there is only one instance of your BEWLR. Stack-specific properties should therefore be stored in the stack, not the BEWLR.

Unlike BERs, we do not register BEWLRs directly. Instead, we register an instance of `IClientItemExtensions` to the `RegisterClientExtensionsEvent`. `IClientItemExtensions` is an interface that allows us to specify a number of rendering-related behaviors on items, such as (but not limited to) a BEWLR. As such, our implementation of that interface could look like so:

```java
public class MyClientItemExtensions implements IClientItemExtensions {
    // Cache our BEWLR in a field.
    private final MyBlockEntityWithoutLevelRenderer myBEWLR = new MyBlockEntityWithoutLevelRenderer();

    // Return our BEWLR here.
    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return myBEWLR;
    }
}
```

And then, we can register our `IClientItemExtensions` to the event:

```java
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
    event.registerItem(
            // The only instance of our IClientItemExtensions, and as such, the only instance of our BEWLR.
            new MyClientItemExtensions(),
            // A vararg list of items that use this BEWLR.
            MyItems.ITEM_1, MyItems.ITEM_2
    );
}
```

:::info
`IClientItemExtensions` are generally expected to be treated as singletons. Do not construct them outside `RegisterClientExtensionsEvent`!
:::

[block]: ../blocks/index.md
[blockentity]: index.md
[event]: ../concepts/events.md#registering-an-event-handler
[eventbus]: ../concepts/events.md#event-buses
[item]: ../items/index.md
[model]: ../resources/client/models/index.md
