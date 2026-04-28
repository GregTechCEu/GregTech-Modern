# BlockEntityRenderer

A `BlockEntityRenderer` or `BER` is used to render blocks in a way that cannot be represented with a static baked model (JSON, OBJ, B3D, others). A block entity renderer requires the block to have a `BlockEntity`.

## Creating a BER

To create a BER, create a class that inherits from `BlockEntityRenderer`. It takes a generic argument specifying the block's `BlockEntity` class. The generic argument is used in the BER's `render` method.

Only one BER exists for a given `BlockEntityType`. Therefore, values that are specific to a single instance in the level should be stored in the block entity being passed to the renderer rather than in the BER itself. For example, an integer that increments every frame, if stored in the BER, will increment every frame for every block entity of this type in the level.

### `render`

This method is called every frame in order to render the block entity. 

#### Parameters
- `blockEntity`: This is the instance of the block entity being rendered.
- `partialTick`: The amount of time, in fractions of a tick, that has passed since the last full tick.
- `poseStack`: A stack holding four-dimensional matrix entries offset to the current position of the block entity.
- `bufferSource`: A rendering buffer able to access a vertex consumer.
- `combinedLight`: An integer of the current light value on the block entity.
- `combinedOverlay`: An integer set to the current overlay of the block entity, usually `OverlayTexture#NO_OVERLAY` or 655,360.

## Registering a BER

In order to register a BER, you must subscribe to the EntityRenderersEvent.RegisterRenderers event on the mod event bus and call `#registerBlockEntityRenderer`.
