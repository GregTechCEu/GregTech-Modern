---
title: Dynamic Renders
---

Dynamic renders allow you to attach custom block entity rendering logic to a machine. They run client-side every frame and can read live machine state to animate or display information in the world.

### How it works

A `DynamicRender` is a typed renderer that is attached to a machine model. You create a class that extends `DynamicRender<T, S>`, where `T` is the machine type it reads from and `S` is the renderer class itself. The renderer is registered during mod initialisation, and attached to a machine definition via `.model([base model].andThen(b -> b.addDynamicRenderer(...)))`.

### Creating a renderer

Each renderer needs three things:

1. A `Codec` for serialization (use `Codec.unit(MyRender::new)` if it has no configuration)
2. A `DynamicRenderType` that wraps the codec
3. A `render(...)` method that does the actual rendering

```java title="ItemAboveControllerRender.java"
public class ItemAboveControllerRender
        extends DynamicRender<WorkableElectricMultiblockMachine, ItemAboveControllerRender> {

    // spotless:off
    public static final Codec<ItemAboveControllerRender> CODEC = Codec.unit(ItemAboveControllerRender::new);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, ItemAboveControllerRender> TYPE =
            new DynamicRenderType<>(ItemAboveControllerRender.CODEC);
    // spotless:on

    public ItemAboveControllerRender() {}

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, ItemAboveControllerRender> getType() {
        return TYPE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(WorkableElectricMultiblockMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        // recipeLogic.isWorking() is safe here because RecipeLogic.status is @SyncToClient
        ItemStack displayStack = machine.recipeLogic.isWorking()
                ? new ItemStack(Items.GREEN_WOOL)
                : new ItemStack(Items.RED_WOOL);

        poseStack.pushPose();
        poseStack.translate(0.5, 2.5, 0.5); // center of block, 2 blocks up
        float totalTick = machine.getLevel().getGameTime() + partialTick;
        poseStack.mulPose(new Quaternionf().rotateY(totalTick * Mth.TWO_PI / 60));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                displayStack, ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, machine.getLevel(), 0);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine machine) {
        BlockPos pos = machine.getBlockPos();
        return new AABB(pos.offset(-1, 0, -1), pos.offset(2, 4, 2));
    }
}
```

!!! warning "Server-side data must be synced"
    `render(...)` runs on the render thread, so any machine state you read must be sent to clients, for example with `@SyncToClient` . Using server-only fields will silently read stale or default values. `RecipeLogic.status` and `RecipeLogic.isActive` are both `@SyncToClient`, which is why `recipeLogic.isWorking()` works here. If you add fields to a custom machine and want to use them in a renderer, annotate them accordingly. See the [sync annotations reference](../../Development/Data-Sync-System/Annotations.md) for details.

!!! warning "Renders are global"
    Only one instance of the render class exists. Make sure to not store any machine- or instance-specific data in the class. There is also no lifecycle management for BEs going off-screen or being destroyed, so by putting the data in the render class you would have a bunch of stale values and memory leaks.

### Registering the type

Register your renderer type in your mod's main java file:

```java title="ExampleMod.java"
@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    
    public ExampleMod() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ExampleMod::initializeDynamicRenders);
    }
    
    public static void initializeDynamicRenders() {
        DynamicRenderManager.register(ExampleMod.id("item_above_controller"), ItemAboveControllerRender.TYPE);
    }
}
```

### Attaching to a machine

Use `.model([base model].andThen(b -> b.addDynamicRenderer(() -> new ...())))` when defining the machine. If the machine previously used the `.workableCasingModel(...)` shorthand, expand it into `.model(createWorkableCasingMachineModel(...).andThen(...))`:

```java
public static final MultiblockMachineDefinition MY_MACHINE = REGISTRATE
    .multiblock("my_machine", WorkableElectricMultiblockMachine::new)
    // ... recipe type, pattern, etc. ...
    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
    .model(createWorkableCasingMachineModel(
            GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
            GTCEu.id("block/multiblock/large_chemical_reactor"))
            .andThen(b -> b.addDynamicRenderer(() -> new ItemAboveControllerRender())))
    .hasBER(true)
    .register();
```

### Optional overrides

- **`shouldRender(machine, cameraPos)`** — Controls whether the renderer runs at all for a given frame. By default it returns `true` as long as the camera is within the view distance returned by `getViewDistance()`. You can override this to add additional conditions, such as skipping the render when the machine is not working.

- **`shouldRenderOffScreen(machine)`** — Determines whether the renderer continues to run when the controller block is outside the camera frustum. Defaults to `false`, meaning the renderer is culled along with the block when the block moves out of view.

- **`getRenderBoundingBox(machine)`** — Defines the bounding box used for off-screen culling when `shouldRenderOffScreen` is `true`. Defaults to a 3×2×3 box centered on the controller. Override this to return a box that tightly wraps your actual rendered content; a box that is too small will cause the render to disappear while still partially visible, and a box that is too large will prevent culling and waste resources.

- **`getViewDistance()`** — Sets the maximum distance in blocks at which the renderer will run, used by the default `shouldRender` implementation. Defaults to `64`. Lower this for expensive renders that do not need to be visible far away, or raise it if the render needs to be legible from a long distance.
