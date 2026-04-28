package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class BlockStateProvider implements DataProvider {

    protected final Map<Block, IGeneratedBlockState> registeredBlocks = new LinkedHashMap<>();

    private final PackOutput output;
    private final String modid;
    private final BlockModelProvider blockModels;
    private final ItemModelProvider itemModels;

    public BlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        this.output = output;
        this.modid = modid;
        this.blockModels = new BlockModelProvider(output, modid, exFileHelper) {

            @Override
            protected void registerModels() {}

            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return CompletableFuture.allOf();
            }
        };
        this.itemModels = new ItemModelProvider(output, modid, exFileHelper) {

            @Override
            protected void registerModels() {}

            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return CompletableFuture.allOf();
            }
        };
    }

    protected abstract void registerStatesAndModels();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        models().clear();
        itemModels().clear();
        registeredBlocks.clear();
        registerStatesAndModels();

        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(models().generateAll(cache));
        futures.add(itemModels().generateAll(cache));
        for (Map.Entry<Block, IGeneratedBlockState> entry : registeredBlocks.entrySet()) {
            futures.add(saveBlockState(cache, entry.getValue().toJson(), entry.getKey()));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public VariantBlockStateBuilder getVariantBuilder(Block block) {
        if (registeredBlocks.containsKey(block)) {
            IGeneratedBlockState old = registeredBlocks.get(block);
            Preconditions.checkState(old instanceof VariantBlockStateBuilder);
            return (VariantBlockStateBuilder) old;
        }
        VariantBlockStateBuilder builder = new VariantBlockStateBuilder(block);
        registeredBlocks.put(block, builder);
        return builder;
    }

    public MultiPartBlockStateBuilder getMultipartBuilder(Block block) {
        if (registeredBlocks.containsKey(block)) {
            IGeneratedBlockState old = registeredBlocks.get(block);
            Preconditions.checkState(old instanceof MultiPartBlockStateBuilder);
            return (MultiPartBlockStateBuilder) old;
        }
        MultiPartBlockStateBuilder builder = new MultiPartBlockStateBuilder(block);
        registeredBlocks.put(block, builder);
        return builder;
    }

    public BlockModelProvider models() {
        return blockModels;
    }

    public ItemModelProvider itemModels() {
        return itemModels;
    }

    public Identifier modLoc(String name) {
        return Identifier.fromNamespaceAndPath(modid, name);
    }

    public Identifier mcLoc(String name) {
        return name.contains(":") ? Identifier.parse(name) : Identifier.withDefaultNamespace(name);
    }

    protected Identifier key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public String name(Block block) {
        return key(block).getPath();
    }

    public Identifier blockTexture(Block block) {
        Identifier name = key(block);
        return Identifier.fromNamespaceAndPath(name.getNamespace(), "block/" + name.getPath());
    }

    private Identifier extend(Identifier id, String suffix) {
        return id.withSuffix(suffix);
    }

    public ModelFile cubeAll(Block block) {
        return models().cubeAll(name(block), blockTexture(block));
    }

    public void simpleBlock(Block block) {
        simpleBlock(block, cubeAll(block));
    }

    public void simpleBlock(Block block, Function<ModelFile, ConfiguredModel[]> expander) {
        simpleBlock(block, expander.apply(cubeAll(block)));
    }

    public void simpleBlock(Block block, ModelFile model) {
        simpleBlock(block, new ConfiguredModel(model));
    }

    public void simpleBlock(Block block, ConfiguredModel... models) {
        getVariantBuilder(block).partialState().setModels(models);
    }

    public void simpleBlockItem(Block block, ModelFile model) {
        itemModels().getBuilder(key(block).getPath()).parent(model);
    }

    public void simpleBlockWithItem(Block block, ModelFile model) {
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    public void logBlock(RotatedPillarBlock block) {
        axisBlock(block, blockTexture(block), extend(blockTexture(block), "_top"));
    }

    public void axisBlock(RotatedPillarBlock block) {
        axisBlock(block, blockTexture(block));
    }

    public void axisBlock(RotatedPillarBlock block, Identifier baseName) {
        axisBlock(block, extend(baseName, "_side"), extend(baseName, "_end"));
    }

    public void axisBlock(RotatedPillarBlock block, Identifier side, Identifier end) {
        axisBlock(block,
                models().cubeColumn(name(block), side, end),
                models().cubeColumnHorizontal(name(block) + "_horizontal", side, end));
    }

    public void axisBlock(RotatedPillarBlock block, ModelFile vertical, ModelFile horizontal) {
        getVariantBuilder(block)
                .partialState().with(RotatedPillarBlock.AXIS, Axis.Y)
                .modelForState().modelFile(vertical).addModel()
                .partialState().with(RotatedPillarBlock.AXIS, Axis.Z)
                .modelForState().modelFile(horizontal).rotationX(90).addModel()
                .partialState().with(RotatedPillarBlock.AXIS, Axis.X)
                .modelForState().modelFile(horizontal).rotationX(90).rotationY(90).addModel();
    }

    public void stairsBlock(StairBlock block, Identifier texture) {
        stairsBlock(block, texture, texture, texture);
    }

    public void stairsBlock(StairBlock block, Identifier side, Identifier bottom, Identifier top) {
        stairsBlockInternal(block, key(block).toString(), side, bottom, top);
    }

    private void stairsBlockInternal(StairBlock block, String baseName, Identifier side, Identifier bottom,
                                     Identifier top) {
        ModelFile stairs = models().stairs(baseName, side, bottom, top);
        ModelFile stairsInner = models().stairsInner(baseName + "_inner", side, bottom, top);
        ModelFile stairsOuter = models().stairsOuter(baseName + "_outer", side, bottom, top);
        getVariantBuilder(block).forAllStates(state -> {
            StairsShape shape = state.getValue(StairBlock.SHAPE);
            Direction facing = state.getValue(StairBlock.FACING);
            Half half = state.getValue(StairBlock.HALF);
            int yRot = (int) facing.toYRot();
            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) yRot += 270;
            if (shape != StairsShape.STRAIGHT && half == Half.TOP) yRot += 90;
            yRot %= 360;
            boolean uvLock = yRot != 0 || half == Half.TOP;
            return ConfiguredModel.builder()
                    .modelFile(shape == StairsShape.STRAIGHT ? stairs :
                            shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? stairsInner :
                                    stairsOuter)
                    .rotationX(half == Half.BOTTOM ? 0 : 180)
                    .rotationY(yRot)
                    .uvLock(uvLock)
                    .build();
        });
    }

    public void slabBlock(SlabBlock block, Identifier doubleslab, Identifier texture) {
        slabBlock(block, doubleslab, texture, texture, texture);
    }

    public void slabBlock(SlabBlock block, Identifier doubleslab, Identifier side, Identifier bottom, Identifier top) {
        ModelFile slab = models().slab(name(block), side, bottom, top);
        ModelFile slabTop = models().slabTop(name(block) + "_top", side, bottom, top);
        getVariantBuilder(block).forAllStates(state -> switch (state.getValue(SlabBlock.TYPE)) {
            case BOTTOM -> ConfiguredModel.builder().modelFile(slab).build();
            case TOP -> ConfiguredModel.builder().modelFile(slabTop).build();
            case DOUBLE -> ConfiguredModel.builder()
                    .modelFile(new ModelFile.ExistingModelFile(doubleslab, models().existingFileHelper))
                    .build();
        });
    }

    public void buttonBlock(ButtonBlock block, Identifier texture) {
        ModelFile button = models().button(name(block), texture);
        ModelFile buttonPressed = models().buttonPressed(name(block) + "_pressed", texture);
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(ButtonBlock.FACING);
            AttachFace face = state.getValue(ButtonBlock.FACE);
            boolean powered = state.getValue(ButtonBlock.POWERED);
            int xRot = face == AttachFace.CEILING ? 180 : face == AttachFace.WALL ? 90 : 0;
            int yRot = ((int) facing.toYRot() + (face == AttachFace.CEILING ? 180 : 0)) % 360;
            return ConfiguredModel.builder()
                    .modelFile(powered ? buttonPressed : button)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .uvLock(face == AttachFace.WALL)
                    .build();
        });
        itemModels().getBuilder(name(block)).parent(models().buttonInventory(name(block) + "_inventory", texture));
    }

    public void pressurePlateBlock(PressurePlateBlock block, Identifier texture) {
        ModelFile up = models().pressurePlate(name(block), texture);
        ModelFile down = models().pressurePlateDown(name(block) + "_down", texture);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(PressurePlateBlock.POWERED) ? down : up)
                .build());
    }

    public void signBlock(StandingSignBlock signBlock, WallSignBlock wallSignBlock, Identifier texture) {
        ModelFile sign = models().sign(name(signBlock), texture);
        simpleBlock(signBlock, sign);
        simpleBlock(wallSignBlock, sign);
    }

    public void fenceBlock(FenceBlock block, Identifier texture) {
        ModelFile post = models().fencePost(name(block) + "_post", texture);
        ModelFile side = models().fenceSide(name(block) + "_side", texture);
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        builder.part().modelFile(post).addModel();
        fenceSide(builder, side, FenceBlock.NORTH, Direction.NORTH, 0);
        fenceSide(builder, side, FenceBlock.EAST, Direction.EAST, 90);
        fenceSide(builder, side, FenceBlock.SOUTH, Direction.SOUTH, 180);
        fenceSide(builder, side, FenceBlock.WEST, Direction.WEST, 270);
        itemModels().getBuilder(name(block)).parent(models().fenceInventory(name(block) + "_inventory", texture));
    }

    private void fenceSide(MultiPartBlockStateBuilder builder, ModelFile model, BooleanProperty prop,
                           Direction direction, int yRot) {
        builder.part().modelFile(model).rotationY(yRot).uvLock(true).addModel().condition(prop, true);
    }

    public void fenceGateBlock(FenceGateBlock block, Identifier texture) {
        ModelFile gate = models().fenceGate(name(block), texture);
        ModelFile gateOpen = models().fenceGateOpen(name(block) + "_open", texture);
        ModelFile gateWall = models().fenceGateWall(name(block) + "_wall", texture);
        ModelFile gateWallOpen = models().fenceGateWallOpen(name(block) + "_wall_open", texture);
        getVariantBuilder(block).forAllStates(state -> {
            boolean inWall = state.getValue(FenceGateBlock.IN_WALL);
            boolean open = state.getValue(FenceGateBlock.OPEN);
            ModelFile model = inWall ? (open ? gateWallOpen : gateWall) : (open ? gateOpen : gate);
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY((int) state.getValue(FenceGateBlock.FACING).toYRot())
                    .uvLock(true)
                    .build();
        });
    }

    public void doorBlock(DoorBlock block, Identifier bottom, Identifier top) {
        ModelFile bottomLeft = models().doorBottomLeft(name(block) + "_bottom_left", bottom, top);
        ModelFile bottomLeftOpen = models().doorBottomLeftOpen(name(block) + "_bottom_left_open", bottom, top);
        ModelFile bottomRight = models().doorBottomRight(name(block) + "_bottom_right", bottom, top);
        ModelFile bottomRightOpen = models().doorBottomRightOpen(name(block) + "_bottom_right_open", bottom, top);
        ModelFile topLeft = models().doorTopLeft(name(block) + "_top_left", bottom, top);
        ModelFile topLeftOpen = models().doorTopLeftOpen(name(block) + "_top_left_open", bottom, top);
        ModelFile topRight = models().doorTopRight(name(block) + "_top_right", bottom, top);
        ModelFile topRightOpen = models().doorTopRightOpen(name(block) + "_top_right_open", bottom, top);
        getVariantBuilder(block).forAllStates(state -> {
            boolean open = state.getValue(DoorBlock.OPEN);
            boolean right = state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;
            boolean lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
            Direction facing = state.getValue(DoorBlock.FACING);
            int yRot = ((int) facing.toYRot() + 90) % 360;
            if (open) yRot = (yRot + (right ? 90 : 270)) % 360;
            ModelFile model = lower ? (right ? (open ? bottomRightOpen : bottomRight) :
                    (open ? bottomLeftOpen : bottomLeft)) :
                    (right ? (open ? topRightOpen : topRight) : (open ? topLeftOpen : topLeft));
            return ConfiguredModel.builder().modelFile(model).rotationY(yRot).build();
        });
    }

    public void trapdoorBlock(TrapDoorBlock block, Identifier texture, boolean orientable) {
        ModelFile bottom = orientable ? models().trapdoorOrientableBottom(name(block) + "_bottom", texture) :
                models().trapdoorBottom(name(block) + "_bottom", texture);
        ModelFile top = orientable ? models().trapdoorOrientableTop(name(block) + "_top", texture) :
                models().trapdoorTop(name(block) + "_top", texture);
        ModelFile open = orientable ? models().trapdoorOrientableOpen(name(block) + "_open", texture) :
                models().trapdoorOpen(name(block) + "_open", texture);
        getVariantBuilder(block).forAllStates(state -> {
            boolean isOpen = state.getValue(TrapDoorBlock.OPEN);
            Half half = state.getValue(TrapDoorBlock.HALF);
            Direction facing = state.getValue(TrapDoorBlock.FACING);
            int xRot = isOpen ? 90 : half == Half.TOP ? 180 : 0;
            int yRot = ((int) facing.toYRot() + (isOpen && half == Half.TOP ? 180 : 0)) % 360;
            return ConfiguredModel.builder()
                    .modelFile(isOpen ? open : half == Half.TOP ? top : bottom)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });
    }

    private CompletableFuture<?> saveBlockState(CachedOutput cache, JsonObject stateJson, Block owner) {
        var path = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates").json(key(owner));
        return DataProvider.saveStable(cache, stateJson, path);
    }

    public static class ConfiguredModelList {

        private final List<ConfiguredModel> models;

        public ConfiguredModelList(ConfiguredModel... models) {
            this.models = Lists.newArrayList(models);
        }

        public ConfiguredModelList(List<ConfiguredModel> models) {
            this.models = Lists.newArrayList(models);
        }

        public List<ConfiguredModel> models() {
            return models;
        }

        public ConfiguredModelList append(ConfiguredModel... models) {
            this.models.addAll(Arrays.asList(models));
            return this;
        }

        public JsonElement toJSON() {
            if (models.size() == 1) return models.getFirst().toJSON(false);
            JsonArray array = new JsonArray();
            for (ConfiguredModel model : models) array.add(model.toJSON(true));
            return array;
        }
    }
}
