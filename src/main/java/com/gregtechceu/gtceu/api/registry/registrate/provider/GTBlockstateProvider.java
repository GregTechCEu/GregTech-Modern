package com.gregtechceu.gtceu.api.registry.registrate.provider;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.util.ExtendedBlockModelRotation;
import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;
import com.gregtechceu.gtceu.utils.data.ExistingFileHelper.ResourceType;

import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.client.model.generators.*;

import com.mojang.math.Quadrant;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * GregTech datagen emits blockstates through two paths:
 * <ul>
 * <li><b>Static datagen (this provider).</b> Plain vanilla {@code "variants": {"": {"model": ...}}}
 * JSON for blocks whose state is fully described by a vanilla model — written to
 * {@code src/generated/resources/} via the regular {@code runData} task.</li>
 * <li><b>Runtime dynamic pack.</b> Blocks whose state needs gtceu's custom rotation/active-state
 * behavior are emitted at client load by {@code LegacyCustomBlockStateModel}, written into
 * {@code GTDynamicResourcePack}, with a {@code "type": "gtceu:legacy_model"} tag in the
 * variant JSON. These never appear in {@code src/generated/resources/}.</li>
 * </ul>
 * The mixed schema is intentional. If a {@code "type": "gtceu:legacy_model"} blockstate ever shows
 * up in {@code src/generated/}, it indicates that a runtime emission accidentally ran during
 * datagen — investigate and move it back to {@link com.gregtechceu.gtceu.client.ClientProxy}'s
 * dynamic-pack registration.
 */
public class GTBlockstateProvider extends RegistrateBlockModelGenerator {

    public static final String Z_ROT_PROPERTY_NAME = "gtceu:z";

    public static final ResourceType TEXTURE = new ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
    public static final ResourceType MODEL = new ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");

    private final AbstractRegistrate<?> parent;
    private final BlockModelProvider blockModels;
    private final ItemModelProvider itemModels;

    private static GTBlockstateProvider CURRENT_PROVIDER = null;

    public GTBlockstateProvider(AbstractRegistrate<?> parent, PackOutput output,
                                Consumer<BlockModelDefinitionGenerator> known,
                                ItemModelOutput item,
                                BiConsumer<Identifier, ModelInstance> model,
                                ExistingFileHelper existingFileHelper) {
        super(parent, known, item, model);
        this.parent = parent;
        this.blockModels = new BlockModelProvider(output, parent.getModid(), existingFileHelper) {

            @Override
            protected void registerModels() {}
        };
        this.itemModels = new ItemModelProvider(output, parent.getModid(), existingFileHelper) {

            @Override
            protected void registerModels() {}
        };
    }

    public static GTBlockstateProvider getCurrentProvider() {
        return CURRENT_PROVIDER;
    }

    @Override
    public void run() {
        CURRENT_PROVIDER = this;
        blockModels.clear();
        itemModels.clear();
        parent.genData(ProviderType.BLOCKSTATE, this);
        flushLegacyModels(blockModels);
        flushLegacyModels(itemModels);
        CURRENT_PROVIDER = null;
    }

    private <T extends ModelBuilder<T>> void flushLegacyModels(ModelProvider<T> provider) {
        for (T model : provider.generatedModels.values()) {
            modelOutput.accept(model.getLocation(), model::toJson);
        }
    }

    public BlockModelProvider models() {
        return blockModels;
    }

    public ItemModelProvider itemModels() {
        return itemModels;
    }

    public ExistingFileHelper getExistingFileHelper() {
        return this.models().existingFileHelper;
    }

    public void simpleBlock(Block block, ModelFile model) {
        simpleBlock(block, new ConfiguredModel(model));
    }

    public void simpleBlock(Block block, ConfiguredModel... models) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block, toMultiVariant(models)));
    }

    public void simpleBlockItem(Block block, ModelFile model) {
        itemModels().getBuilder(name(block)).parent(model);
    }

    public void simpleBlockWithItem(Block block, ModelFile model) {
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    public void logBlock(RotatedPillarBlock block) {
        var base = blockTexture(block).sprite();
        axisBlock(block, base, base.withSuffix("_top"));
    }

    public void axisBlock(RotatedPillarBlock block) {
        axisBlock(block, blockTexture(block).sprite());
    }

    public void axisBlock(RotatedPillarBlock block, Identifier baseName) {
        axisBlock(block, baseName.withSuffix("_side"), baseName.withSuffix("_end"));
    }

    public void axisBlock(RotatedPillarBlock block, net.minecraft.client.resources.model.sprite.Material baseName) {
        axisBlock(block, baseName.sprite());
    }

    public void axisBlock(RotatedPillarBlock block, net.minecraft.client.resources.model.sprite.Material side,
                          net.minecraft.client.resources.model.sprite.Material end) {
        axisBlock(block, side.sprite(), end.sprite());
    }

    public void axisBlock(RotatedPillarBlock block, Identifier side, Identifier end) {
        var vertical = models().cubeColumn(name(block), side, end);
        var horizontal = models().cubeColumnHorizontal(name(block) + "_horizontal", side, end);
        blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(block,
                toMultiVariant(new ConfiguredModel(vertical)),
                toMultiVariant(new ConfiguredModel(horizontal))));
    }

    public void stairsBlock(StairBlock block, net.minecraft.client.resources.model.sprite.Material texture) {
        stairsBlock(block, texture.sprite());
    }

    public void stairsBlock(StairBlock block, Identifier texture) {
        stairsBlock(block, texture, texture, texture);
    }

    public void stairsBlock(StairBlock block, Identifier side, Identifier bottom, Identifier top) {
        blockStateOutput.accept(createStairs(block,
                toMultiVariant(new ConfiguredModel(models().stairsInner(name(block) + "_inner", side, bottom, top))),
                toMultiVariant(new ConfiguredModel(models().stairs(name(block), side, bottom, top))),
                toMultiVariant(new ConfiguredModel(models().stairsOuter(name(block) + "_outer", side, bottom, top)))));
    }

    public void slabBlock(SlabBlock block, net.minecraft.client.resources.model.sprite.Material doubleslab,
                          net.minecraft.client.resources.model.sprite.Material texture) {
        slabBlock(block, doubleslab.sprite(), texture.sprite());
    }

    public void slabBlock(SlabBlock block, Identifier doubleslab, Identifier texture) {
        slabBlock(block, doubleslab, texture, texture, texture);
    }

    public void slabBlock(SlabBlock block, Identifier doubleslab, Identifier side, Identifier bottom, Identifier top) {
        blockStateOutput.accept(createSlab(block,
                toMultiVariant(new ConfiguredModel(models().slab(name(block), side, bottom, top))),
                toMultiVariant(new ConfiguredModel(models().slabTop(name(block) + "_top", side, bottom, top))),
                toMultiVariant(
                        new ConfiguredModel(new ModelFile.ExistingModelFile(doubleslab, getExistingFileHelper())))));
    }

    public void buttonBlock(ButtonBlock block, net.minecraft.client.resources.model.sprite.Material texture) {
        buttonBlock(block, texture.sprite());
    }

    public void buttonBlock(ButtonBlock block, Identifier texture) {
        blockStateOutput.accept(createButton(block,
                toMultiVariant(new ConfiguredModel(models().button(name(block), texture))),
                toMultiVariant(new ConfiguredModel(models().buttonPressed(name(block) + "_pressed", texture)))));
    }

    public void pressurePlateBlock(PressurePlateBlock block,
                                   net.minecraft.client.resources.model.sprite.Material texture) {
        pressurePlateBlock(block, texture.sprite());
    }

    public void pressurePlateBlock(PressurePlateBlock block, Identifier texture) {
        blockStateOutput.accept(createPressurePlate(block,
                toMultiVariant(new ConfiguredModel(models().pressurePlate(name(block), texture))),
                toMultiVariant(new ConfiguredModel(models().pressurePlateDown(name(block) + "_down", texture)))));
    }

    public void signBlock(StandingSignBlock signBlock, WallSignBlock wallSignBlock,
                          net.minecraft.client.resources.model.sprite.Material texture) {
        signBlock(signBlock, wallSignBlock, texture.sprite());
    }

    public void signBlock(StandingSignBlock signBlock, WallSignBlock wallSignBlock, Identifier texture) {
        ModelFile sign = models().sign(name(signBlock), texture);
        simpleBlock(signBlock, sign);
        simpleBlock(wallSignBlock, sign);
    }

    public void fenceBlock(FenceBlock block, net.minecraft.client.resources.model.sprite.Material texture) {
        fenceBlock(block, texture.sprite());
    }

    public void fenceBlock(FenceBlock block, Identifier texture) {
        blockStateOutput.accept(createFence(block,
                toMultiVariant(new ConfiguredModel(models().fencePost(name(block) + "_post", texture))),
                toMultiVariant(new ConfiguredModel(models().fenceSide(name(block) + "_side", texture)))));
    }

    public void fenceGateBlock(FenceGateBlock block, net.minecraft.client.resources.model.sprite.Material texture) {
        fenceGateBlock(block, texture.sprite());
    }

    public void fenceGateBlock(FenceGateBlock block, Identifier texture) {
        blockStateOutput.accept(createFenceGate(block,
                toMultiVariant(new ConfiguredModel(models().fenceGate(name(block), texture))),
                toMultiVariant(new ConfiguredModel(models().fenceGateOpen(name(block) + "_open", texture))),
                toMultiVariant(new ConfiguredModel(models().fenceGateWall(name(block) + "_wall", texture))),
                toMultiVariant(new ConfiguredModel(models().fenceGateWallOpen(name(block) + "_wall_open", texture))),
                true));
    }

    public void doorBlock(DoorBlock block, Identifier bottom, Identifier top) {
        blockStateOutput.accept(createDoor(block,
                toMultiVariant(new ConfiguredModel(models().doorBottomLeft(name(block) + "_bottom_left", bottom, top))),
                toMultiVariant(new ConfiguredModel(
                        models().doorBottomLeftOpen(name(block) + "_bottom_left_open", bottom, top))),
                toMultiVariant(
                        new ConfiguredModel(models().doorBottomRight(name(block) + "_bottom_right", bottom, top))),
                toMultiVariant(new ConfiguredModel(
                        models().doorBottomRightOpen(name(block) + "_bottom_right_open", bottom, top))),
                toMultiVariant(new ConfiguredModel(models().doorTopLeft(name(block) + "_top_left", bottom, top))),
                toMultiVariant(
                        new ConfiguredModel(models().doorTopLeftOpen(name(block) + "_top_left_open", bottom, top))),
                toMultiVariant(new ConfiguredModel(models().doorTopRight(name(block) + "_top_right", bottom, top))),
                toMultiVariant(
                        new ConfiguredModel(models().doorTopRightOpen(name(block) + "_top_right_open", bottom, top)))));
    }

    public void trapdoorBlock(TrapDoorBlock block, net.minecraft.client.resources.model.sprite.Material texture,
                              boolean orientable) {
        trapdoorBlock(block, texture.sprite(), orientable);
    }

    public void trapdoorBlock(TrapDoorBlock block, Identifier texture, boolean orientable) {
        ModelFile bottom = orientable ? models().trapdoorOrientableBottom(name(block) + "_bottom", texture) :
                models().trapdoorBottom(name(block) + "_bottom", texture);
        ModelFile top = orientable ? models().trapdoorOrientableTop(name(block) + "_top", texture) :
                models().trapdoorTop(name(block) + "_top", texture);
        ModelFile open = orientable ? models().trapdoorOrientableOpen(name(block) + "_open", texture) :
                models().trapdoorOpen(name(block) + "_open", texture);
        blockStateOutput.accept(orientable ?
                createOrientableTrapdoor(block,
                        toMultiVariant(new ConfiguredModel(bottom)),
                        toMultiVariant(new ConfiguredModel(top)),
                        toMultiVariant(new ConfiguredModel(open))) :
                createTrapdoor(block,
                        toMultiVariant(new ConfiguredModel(bottom)),
                        toMultiVariant(new ConfiguredModel(top)),
                        toMultiVariant(new ConfiguredModel(open))));
    }

    public MultiVariantGenerator multiVariantGenerator(Block block) {
        var multiVariant = MultiVariantGenerator.dispatch(block,
                new MultiVariant(WeightedList.of(new Variant(Identifier.withDefaultNamespace("block/air")))));
        blockStateOutput.accept(multiVariant);
        return multiVariant;
    }

    public MultiVariantGenerator multiVariantGenerator(Block block, Variant baseVariant) {
        var multiVariant = MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(baseVariant)));
        blockStateOutput.accept(multiVariant);
        return multiVariant;
    }

    public MultiPartGenerator multiPartGenerator(Block block) {
        var multiPart = MultiPartGenerator.multiPart(block);
        blockStateOutput.accept(multiPart);
        return multiPart;
    }

    public <T extends BlockModelDefinitionGenerator> T addVanillaGenerator(Block block, T generator) {
        blockStateOutput.accept(generator);
        return generator;
    }

    private MultiVariant toMultiVariant(ConfiguredModel... models) {
        Variant[] variants = new Variant[models.length];
        for (int i = 0; i < models.length; i++) {
            variants[i] = toVariant(models[i]);
        }
        return variants(variants);
    }

    private Variant toVariant(ConfiguredModel model) {
        Variant variant = new Variant(model.model.getLocation());
        variant = applyRotation(variant, model.rotationX, model.rotationY, 0);
        if (model.uvLock) variant = variant.with(VariantMutator.UV_LOCK.withValue(true));
        return variant;
    }

    public Optional<MultiVariantGenerator> getExistingMultiVariantGenerator(Block block) {
        return Optional.empty();
    }

    public Optional<MultiPartGenerator> getExistingMultipartGenerator(Block block) {
        return Optional.empty();
    }

    public static @Nullable PropertyDispatch<VariantMutator> createFacingDispatch(MachineDefinition definition) {
        return createFacingDispatch(definition.getRotationState(), definition.isAllowExtendedFacing());
    }

    public static @Nullable PropertyDispatch<VariantMutator> createFacingDispatch(RotationState rotationState,
                                                                                  boolean allowExtendedFacing) {
        if (rotationState == RotationState.NONE) return null;

        PropertyDispatch<VariantMutator> dispatch;
        if (!allowExtendedFacing) {
            var disp = PropertyDispatch.modify(rotationState.property);
            dispatch = disp.generate(front -> {
                var orientation = ExtendedBlockModelRotation.get(front, Direction.NORTH);
                return createOrientationMutator(orientation);
            });
        } else {
            var disp = PropertyDispatch.modify(rotationState.property, GTBlockStateProperties.UPWARDS_FACING);
            dispatch = disp.generate((front, up) -> {
                var orientation = ExtendedBlockModelRotation.get(front, up);
                return createOrientationMutator(orientation);
            });
        }
        return dispatch;
    }

    public static VariantMutator createOrientationMutator(ExtendedBlockModelRotation orientation) {
        return createRotationMutator(orientation.getAngleX(), orientation.getAngleY(), orientation.getAngleZ());
    }

    public static Variant applyOrientation(Variant variant, ExtendedBlockModelRotation orientation) {
        return applyRotation(variant, orientation.getAngleX(), orientation.getAngleY(), orientation.getAngleZ());
    }

    public static VariantMutator createRotationMutator(int angleX, int angleY, int angleZ) {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);
        angleZ = normalizeAngle(angleZ);

        VariantMutator mutator = variant -> variant;
        if (angleX != 0) mutator = mutator.then(VariantMutator.X_ROT.withValue(rotationByAngle(angleX)));
        if (angleY != 0) mutator = mutator.then(VariantMutator.Y_ROT.withValue(rotationByAngle(angleY)));
        if (angleZ != 0) mutator = mutator.then(VariantMutator.Z_ROT.withValue(rotationByAngle(angleZ)));
        return mutator;
    }

    public static Variant applyRotation(Variant variant, int angleX, int angleY, int angleZ) {
        return variant.with(createRotationMutator(angleX, angleY, angleZ));
    }

    private static int normalizeAngle(int angle) {
        return angle - (angle / 360) * 360;
    }

    private static Quadrant rotationByAngle(int angle) {
        return switch (angle) {
            case 0 -> Quadrant.R0;
            case 90 -> Quadrant.R90;
            case 180 -> Quadrant.R180;
            case 270 -> Quadrant.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }

    private String name(Block block) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}
