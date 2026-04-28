---
sidebar_position: 1
---
# Features

A rendering feature defines a set of objects not baked into the level geometry, such as entities, text, and particles. These objects typically have a dynamic position, so things like falling or held blocks and items also fall into this category. The purpose of the feature renderer is then to better batch and order the objects being rendered to the screen. The feature renderer is broken into two phases: the submission phase, where all features are collected; and the rendering phases, where the collected features are rendered.

## Submitting Features

Feature submission is typically handled by the underlying subsystem responsible for those objects: [`EntityRenderer` for entities][entities], [`BlockEntityRenderer` for block entities][blockentities], [`ParticleGroupRenderState` for particles][particles], etc. Each provides their own `submit` method, usually taking in some general render state of the object. The necessary elements are then submitted through the `SubmitNodeCollector` and stored in a `SubmitNodeCollection` tree map for rendering.

The following methods are made available through the collector, in the order they are ultimately rendered:

| Method                 | Description                                                                                                |
|:----------------------:|:-----------------------------------------------------------------------------------------------------------|
| `submitShadow`         | A number of black ovals for the desired radius, location, and opacity.                                     |
| `submitNameTag`        | Text, transparency sorted.                                                                                 |
| `submitText`           | Text.                                                                                                      |
| `submitFlame`          | The flame overlay applied to entities.                                                                     |
| `submitLeash`          | A 24-segment plane.                                                                                        |
| `submitModel`          | `Model`s with render states, transparency sorted.                                                          |
| `submitModelPart`      | `ModelPart`s.                                                                                              |
| `submitBlock`          | A `BlockState` with baked lighting.                                                                        |
| `submitMovingBlock`    | A `BlockState` with dynamic lighting.                                                                      |
| `submitBlockModel`     | A `BlockStateModel` with baked lighting.                                                                   |
| `submitItem`           | A deconstructed `ItemStackRenderState`.                                                                    |
| `submitCustomGeometry` | An arbitrary method that defines the vertices uploaded to the buffer of the given `RenderType`.            |
| `submitParticleGroup`  | A renderer for caching and writing a batch of particle quads.                                              |

:::warning
Each of the elements submitted to the collector should be considered immutable after the method is invoked. Some elements like the `PoseStack` are snapshotted at that time to prevent any further mutations.
:::

Technically, all of the methods listed above are part of the superinterface `OrderedSubmitNodeCollector`. This is because the collector can group the features into 'orders', which represent a single pass of the renderer. By default, all features are rendered on order 0, meaning they will be drawn based on the rendering order defined below. Orders with a smaller number will be rendered first while orders with a large number will be rendered after. `SubmitNodeCollector#order` can be used to specify the order of which the element is drawn:

```java
// Assume we have some SubmitNodeCollector collector

// This will be rendered on order 0.
collector.submitModel(...);

// This will be rendered before the model.
collector.order(-1).submitBlock(...);

// This will be rendered after the model.
collector.order(1).submitParticleGroup(...);
```

## Rendering Features

Feature rendering is handled through the `FeatureRenderDispatcher` via `renderAllFeatures`, which renders the submitted objects within the `SubmitNodeStorage` holding the `SubmitNodeCollection` tree map. The dispatcher contains a list of renderer classes which are responsible for rendering a given type of submitted objects. For a given 'order', the features are rendered in the following order:

- Shadows
- Opaque models followed by sorted transparent models
- Model parts
- Entity flame overlays
- Sorted transparent name tags followed by opaque name tags
- Text
- Leashes
- Items
- Moving blocks, blocks, and then block models
- Custom geometry
- Particles

Once the features have finished rendering, the `SubmitNodeStorage` is cleared for its next use. The feature renderer may be called multiple times per frame, as not only is it used for the level, but also for the held item and [picture-in-picture gui renderer][gui]. Note that in those cases, `renderAllFeatures` is followed by `MultiBufferSource.BufferSource#endBatch` to the build the mesh and draw it to the buffer.

[blockentities]: ../blockentities/ber.md#blockentityrenderer
[entities]: ../entities/renderer.md#entity-renderers
[gui]: screens.md#picture-in-picture
[particles]: #TODO
