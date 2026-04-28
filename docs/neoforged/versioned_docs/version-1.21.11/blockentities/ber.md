# BlockEntityRenderer

A `BlockEntityRenderer`, often abbreviated as BER, is used to 'render' [blocks][block] in a way that cannot be represented with a [static baked model][model] (JSON, OBJ, others). For example, this could be used to dynamically render container contents of a chest-like block. A block entity renderer requires the block to have a [`BlockEntity`][blockentity], even if the block does not store any data otherwise.


BERs directly implements the `BlockEntityRenderer`, which submits its [features] for rendering:

```java
// The generic type in the superinterface should be set to what block entity
// you are trying to render, along with its extracted render state. More on this below.
public class MyBlockEntityRenderer implements BlockEntityRenderer<MyBlockEntity, MyBlockEntityRenderState> {

    public MyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Get whatever is necessary from the context
    }

    // Tell the renderer how to create a new render state.
    @Override
    public MyBlockEntityRenderState createRenderState() {
        return new MyBlockEntityRenderState();
    }

    // Update the render state by copying the needed values from the passed block entity
    // to the passed render state.
    // The block entity and render state are the generic types passed to the renderer
    @Override
    public void extractRenderState(MyBlockEntity blockEntity, MyBlockEntityRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        // Always call super or `BlockEntityRenderState#extractBase`
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        // Extract and store any additional values in the state here.
        renderState.value = blockEntity.getValue();
    }

    // Actually submit the features of the block entity to render.
    // The first parameter matches the render state's generic type.
    @Override
    public void submit(MyBlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        // Submit using the collector here.
    }
}
```

Now that we have our BER, we also need to register and connect it to its owning block entity. This is done in [`EntityRenderersEvent.RegisterRenderers`][event] like so:

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

:::note

In the event that you do not need the provider context in your BER, you can also remove the constructor:

```java
public class MyBlockEntityRenderer implements BlockEntityRenderer<MyBlockEntity, MyBlockEntityRenderState> {
    
    // ...
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

:::

## Block Entity Render States

As mentioned in the above example, block entity render states are used to extract the values used for rendering from the actual block entity's values. They are functionally mutable data storage objects extended from `BlockEntityRenderState`:

```java
public class MyBlockEntityRenderState extends BlockEntityRenderState {
    public boolean value;
}
```

The values should then be populated from the `BlockEntity` subclass within `BlockEntityRenderer#extractRenderState`.

## Item Block Rendering

As not all block entities with renderers can be represented by static item models, a special renderer can be created to more dynamically control the process. This is done using [`SpecialModelRenderer`s][special]. In these cases, both a special model renderer must be created to submit the desired [features], and a corresponding registered special block model renderer for scenarios when the block itself is being submitted for rendering rather than an item variant (e.g., enderman carrying a block).

Please refer to the [client item documentation][special] for more information.

[block]: ../blocks/index.md
[blockentity]: index.md
[event]: ../concepts/events.md#registering-an-event-handler
[features]: ../rendering/feature.md
[item]: ../items/index.md
[model]: ../resources/client/models/index.md
[special]: ../resources/client/models/items.md#special-models
