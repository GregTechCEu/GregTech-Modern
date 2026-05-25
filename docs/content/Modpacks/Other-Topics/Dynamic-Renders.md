---
title: Dynamic Renders
---

Dynamic renders allow you to attach custom block entity rendering logic to a machine. They run client-side every frame and can read live machine state to animate or display information in the world.

### How it works

A `DynamicRender` is a typed renderer that is attached to a machine model. You create a class that extends `DynamicRender<T, S>`, where `T` is the machine type it reads from and `S` is the renderer class itself. The renderer is then registered in `ClientProxy`, and attached to a machine definition via `.model([base model].andThen(b -> b.addDynamicRenderer(...)))`.

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
    `render(...)` runs on the render thread, so any machine state you read must be sent to clients, for exakple with `@SyncToClient` . Using server-only fields will silently read stale or default values. `RecipeLogic.status` and `RecipeLogic.isActive` are both `@SyncToClient`, which is why `recipeLogic.isWorking()` works here. If you add fields to a custom machine and want to use them in a renderer, annotate them accordingly. See the [sync annotations reference](../../Development/Data-Sync-System/Annotations.md) for details.

!!! warning "Renders are global"
    Only one instance of the render class exists. Make sure to not store any machine- or instance-specific data in the class. There is also no lifecycle management for BEs going off-screen or being destroyed, so by putting the data in the render class you would have a bunch of stale values and memory leaks.

### Registering the type

Register your renderer type in `ClientProxy`:

```java title="ClientProxy.java"
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        super();
        init();
    }

    public static void init() {
        initializeDynamicRenders();
    }

    public static void initializeDynamicRenders() {
        DynamicRenderManager.register(GTCEu.id("item_above_controller"), ItemAboveControllerRender.TYPE);
    }
}
```


### Attaching to a machine

Use `.model([base model].andThen(b -> b.addDynamicRenderer(new ...())))` when defining the machine. If the machine previously used the `.workableCasingModel(...)` shorthand, expand it into `.model(createWorkableCasingMachineModel(...).andThen(...))`:

```java
public static final MultiblockMachineDefinition MY_MACHINE = REGISTRATE
    .multiblock("my_machine", WorkableElectricMultiblockMachine::new)
    // ... recipe type, pattern, etc. ...
    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
    .model(createWorkableCasingMachineModel(
            GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
            GTCEu.id("block/multiblock/large_chemical_reactor"))
            .andThen(b -> b.addDynamicRenderer(new ItemAboveControllerRender())))
    .hasBER(true)
    .register();
```

### Optional overrides

| Method | Default | Purpose                                            |
|---|---|----------------------------------------------------|
| `shouldRender(machine, cameraPos)` | within view distance | Skip rendering based on distance or state          |
| `shouldRenderOffScreen(machine)` | `false` | Keep rendering even when the machine is off-screen |
| `getRenderBoundingBox(machine)` | 3×2×3 around controller | Off-screen culling                                 |
| `getViewDistance()` | 64 | Distance cutoff for `shouldRender`                 |
