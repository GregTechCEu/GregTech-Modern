package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.GrowingPlantBlockAccessor;
import com.gregtechceu.gtceu.core.mixins.IntegerPropertyAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GrowingPlantRender extends DynamicRender<IRecipeLogicMachine, GrowingPlantRender> {

    // spotless:off
    @SuppressWarnings("deprecation")
    public static final Codec<GrowingPlantRender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.VECTOR3F.listOf().fieldOf("offsets").forGetter(GrowingPlantRender::getOffsets),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("growing_block").forGetter(GrowingPlantRender::getGrowingBlock),
            GrowthMode.CODEC.optionalFieldOf("growth_mode").forGetter(GrowingPlantRender::getGrowthMode)
    ).apply(instance, GrowingPlantRender::new));
    public static final DynamicRenderType<IRecipeLogicMachine, GrowingPlantRender> TYPE = new DynamicRenderType<>(GrowingPlantRender.CODEC);
    // spotless:on

    private static final float EPSILON = 1e-25f;

    @Getter
    private final List<Vector3f> offsets;
    @Getter
    private final Optional<Block> growingBlock;
    @Getter
    private final Optional<GrowthMode> growthMode;

    public GrowingPlantRender(List<Vector3f> offsets) {
        this(offsets, Optional.empty(), Optional.empty());
    }

    public GrowingPlantRender(List<Vector3f> offsets, Optional<Block> growingBlock, Optional<GrowthMode> growthMode) {
        this.offsets = offsets;
        this.growingBlock = growingBlock;
        this.growthMode = growthMode;
    }

    @Override
    public DynamicRenderType<IRecipeLogicMachine, GrowingPlantRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public AABB getRenderBoundingBox(IRecipeLogicMachine machine) {
        final BlockPos pos = machine.self().getPos();

        List<BlockPos> positions = new ArrayList<>();
        Collections.addAll(positions, pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
        for (Vector3f offset : this.offsets) {
            positions.add(BlockPos.containing(offset.x(), offset.y(), offset.z()));
        }

        return BoundingBox.encapsulatingPositions(positions).map(AABB::of)
                .orElseGet(() -> super.getRenderBoundingBox(machine));
    }

    @Override
    public void render(IRecipeLogicMachine rlm, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderGrowingPlants) return;
        if (!rlm.isActive()) return;
        final RecipeLogic recipeLogic = rlm.getRecipeLogic();

        Optional<Block> currentBlock = this.growingBlock
                .or(() -> Optional.ofNullable(recipeLogic.getLastRecipe()).flatMap(this::findGrowing));
        if (currentBlock.isEmpty()) return;
        Block growing = currentBlock.get();
        BlockState state = growing.defaultBlockState();

        double progress = recipeLogic.getProgressPercent();
        GrowthMode mode = this.growthMode.orElseGet(() -> getGrowthModeForBlock(growing));

        // a couple of special case replacements in case of small mistakes in manual configuration
        if (this.growthMode.isPresent() && !mode.predicate().test(growing)) {
            if (mode == GrowthMode.GROWING_PLANT && GrowthMode.DOUBLE_TRANSLATE.predicate.test(growing)) {
                mode = GrowthMode.DOUBLE_TRANSLATE;
            }
            if (mode == GrowthMode.AGE_4 && GrowthMode.PICKLES.predicate().test(growing)) {
                // special case the pickles property to work if using age_4
                mode = GrowthMode.PICKLES;
            } else {
                mode = GrowthMode.SCALE;
            }
        }

        MetaMachine machine = rlm.self();
        Level level = machine.getLevel();
        assert level != null;
        BlockPos machinePos = machine.getPos();

        var statesToDraw = mode.renderFunction().configureState(level, state, progress);

        for (Vector3fc offset : this.getOffsets()) {
            poseStack.pushPose();

            Vector3f rotated = new Vector3f(offset);
            rotated.rotateX(-Mth.HALF_PI);
            machine.getFrontFacing().getRotation().transform(rotated);
            poseStack.translate(rotated.x(), rotated.y() + EPSILON, rotated.z());

            BlockPos pos = machinePos.offset(BlockPos.containing(rotated.x(), rotated.y(), rotated.z()));
            for (StateWithOffset toDraw : statesToDraw) {
                poseStack.pushPose();
                Vector3fc translation = toDraw.offset;
                poseStack.translate(translation.x(), translation.y(), translation.z());

                mode.renderFunction().renderGrowingBlock(level, pos, rotated, toDraw.state,
                        progress, bufferSource, poseStack);

                poseStack.popPose();
            }

            poseStack.popPose();
        }
    }

    public void drawBlocks(BlockAndTintGetter level, BlockPos machinePos, Direction frontFacing,
                           double progress, GrowthMode mode, BlockState state,
                           PoseStack poseStack, MultiBufferSource bufferSource) {
        for (final Vector3fc offset : getOffsets()) {
            poseStack.pushPose();

            Vector3f rotated = new Vector3f(offset);
            rotated.rotateX(-Mth.HALF_PI);
            frontFacing.getRotation().transform(rotated);
            poseStack.translate(rotated.x(), rotated.y(), rotated.z());

            poseStack.translate(0, 1 + EPSILON, 0);
            poseStack.translate(0.0, (progress * 2) % (1 + EPSILON) - 1, 0.0);
            if (mode == GrowthMode.GROWING_PLANT && state.getBlock() instanceof GrowingPlantBlock gp) {
                poseStack.last().pose().rotateAround(
                        ((GrowingPlantBlockAccessor) gp).gtceu$getGrowthDirection().getRotation(), 0.5f, 0.5f, 0.5f);
            }
            RenderUtil.drawBlock(level, machinePos, state, bufferSource, poseStack);

            poseStack.popPose();
        }
    }

    protected Optional<Block> findGrowing(GTRecipe recipe) {
        return RECIPE_BLOCK_CACHE.apply(recipe);
    }

    private static final Function<GTRecipe, Optional<Block>> RECIPE_BLOCK_CACHE = GTMemoizer
            .memoizeFunctionWeakIdent(recipe -> {
                List<Content> allItemContents = new ArrayList<>();
                allItemContents.addAll(recipe.getInputContents(ItemRecipeCapability.CAP));
                allItemContents.addAll(recipe.getTickInputContents(ItemRecipeCapability.CAP));
                allItemContents.addAll(recipe.getOutputContents(ItemRecipeCapability.CAP));
                allItemContents.addAll(recipe.getTickOutputContents(ItemRecipeCapability.CAP));
                return allItemContents.stream()
                        .map(Content::getContent).map(ItemRecipeCapability.CAP::of)
                        .map(Ingredient::getItems).flatMap(Arrays::stream)
                        .map(ItemStack::getItem)
                        .filter(BlockItem.class::isInstance)
                        .findFirst()
                        .map(BlockItem.class::cast)
                        .map(BlockItem::getBlock);
            });

    protected GrowthMode getGrowthModeForBlock(Block block) {
        if (block instanceof GrowingPlantBlock) {
            return GrowthMode.GROWING_PLANT;
        }
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) ||
                state.hasProperty(BlockStateProperties.HALF) ||
                state.is(CustomTags.TALL_PLANTS)) {
            return GrowthMode.DOUBLE_TRANSLATE;
        } else if (state.is(BlockTags.FLOWERS)) {
            return GrowthMode.TRANSLATE;
        }

        IntegerProperty ageProp = findAgeProperty(state.getProperties());
        if (ageProp != null) {
            GrowthMode mode = GrowthMode.MODE_BY_PROPERTY.get(ageProp);
            if (mode != null) return mode;
        }
        // default to SCALE
        return GrowthMode.SCALE;
    }

    public static @Nullable IntegerProperty findAgeProperty(Collection<Property<?>> properties) {
        for (Property<?> prop : properties) {
            if ((prop.getName().equals("age") || prop.getName().equals("pickles")) &&
                    prop instanceof IntegerProperty intProp) {
                return intProp;
            }
        }
        return null;
    }

    public record GrowthMode(String name,
                             Predicate<Block> predicate,
                             RenderFunction renderFunction) {

        public static final Map<String, GrowthMode> VALUES = new HashMap<>();
        public static final Map<IntegerProperty, GrowthMode> MODE_BY_PROPERTY = new HashMap<>();

        public static final GrowthMode NONE = new GrowthMode("none", RenderFunction.NO_OP);
        public static final GrowthMode SCALE = new GrowthMode("scale", RenderFunction.SCALE);
        public static final GrowthMode TRANSLATE = new GrowthMode("translate", RenderFunction.TRANSLATE);

        public static final GrowthMode DOUBLE_TRANSLATE = new GrowthMode("double_translate",
                RenderFunction.DOUBLE_BLOCK);
        public static final GrowthMode GROWING_PLANT = new GrowthMode("growing_plant",
                block -> block instanceof GrowingPlantBlock, RenderFunction.GROWING_PLANT);

        // all the different age properties. not going to add extras, though.
        public static final GrowthMode AGE_1 = ofIntegerProperty("age_1", BlockStateProperties.AGE_1);
        public static final GrowthMode AGE_2 = ofIntegerProperty("age_2", BlockStateProperties.AGE_2);
        public static final GrowthMode AGE_3 = ofIntegerProperty("age_3", BlockStateProperties.AGE_3);
        public static final GrowthMode AGE_4 = ofIntegerProperty("age_4", BlockStateProperties.AGE_4);
        public static final GrowthMode AGE_5 = ofIntegerProperty("age_5", BlockStateProperties.AGE_5);
        public static final GrowthMode AGE_7 = ofIntegerProperty("age_7", BlockStateProperties.AGE_7);
        public static final GrowthMode AGE_15 = ofIntegerProperty("age_15", BlockStateProperties.AGE_15);
        public static final GrowthMode AGE_25 = ofIntegerProperty("age_25", BlockStateProperties.AGE_25);

        public static final GrowthMode PICKLES = ofIntegerProperty("pickles", BlockStateProperties.PICKLES, 0, 4);

        private static final Codec<GrowthMode> CODEC = Codec.STRING.comapFlatMap(name -> {
            GrowthMode mode = VALUES.get(name);
            if (mode != null) {
                return DataResult.success(mode);
            } else {
                // default to SCALE in case of an error
                return DataResult.error(() -> "Could not find growth mode named " + name, SCALE);
            }
        }, GrowthMode::name);

        public GrowthMode {
            VALUES.put(name, this);
        }

        public GrowthMode(String name, RenderFunction renderFunction) {
            this(name, block -> true, renderFunction);
        }

        public static GrowthMode ofIntegerProperty(String name, IntegerProperty property) {
            IntegerPropertyAccessor accessor = (IntegerPropertyAccessor) property;
            final int min = accessor.gtceu$getMin();
            final int max = accessor.gtceu$getMax();
            return ofIntegerProperty(name, property, min, max);
        }

        public static GrowthMode ofIntegerProperty(String name, IntegerProperty property, int min, int max) {
            GrowthMode mode = new GrowthMode(name,
                    block -> block.getStateDefinition().getProperties().contains(property),
                    RenderFunction.byIntegerProperty(property, min, max));
            MODE_BY_PROPERTY.put(property, mode);
            return mode;
        }
    }

    @FunctionalInterface
    public interface RenderFunction {

        void renderGrowingBlock(BlockAndTintGetter level, BlockPos pos, Vector3f offset, BlockState state,
                                double progress, MultiBufferSource bufferSource, PoseStack poseStack);

        default Collection<StateWithOffset> configureState(BlockAndTintGetter level, BlockState state,
                                                           double progress) {
            return Collections.singleton(new StateWithOffset(state));
        }

        RenderFunction NO_OP = (level, pos, offset, state, progress, bufferSource, poseStack) -> {};

        RenderFunction SCALE = (level, pos, offset, state, progress, bufferSource, poseStack) -> {
            poseStack.last().pose().scaleAround((float) progress, 0.5f, 0.0f, 0.5f);
            poseStack.last().normal().scale((float) progress);

            RenderUtil.drawBlock(level, pos, state, bufferSource, poseStack);
        };

        RenderFunction.ConfigureOnly TRANSLATE = (level, state, progress) -> {
            Vector3fc translation = new Vector3f(0, (float) (progress - 1), 0);
            return Collections.singleton(new StateWithOffset(state, translation));
        };

        RenderFunction.ConfigureOnly DOUBLE_BLOCK = (level, state, progress) -> {
            Vector3fc translation = new Vector3f(0, (float) (progress * 2 - 1), 0);

            if (progress > 0.5) {
                Vector3fc bottomTranslation = new Vector3f(translation.x(), translation.y() - 1, translation.z());

                BlockState topState = state;
                if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                    topState = topState.trySetValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                } else if (state.hasProperty(BlockStateProperties.HALF)) {
                    topState = topState.trySetValue(BlockStateProperties.HALF, Half.TOP);
                }

                return Arrays.asList(new StateWithOffset(state, bottomTranslation),
                        new StateWithOffset(topState, translation));
            } else {
                if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                    state = state.trySetValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                } else if (state.hasProperty(BlockStateProperties.HALF)) {
                    state = state.trySetValue(BlockStateProperties.HALF, Half.TOP);
                }

                return Collections.singleton(new StateWithOffset(state, translation));
            }
        };

        RenderFunction GROWING_PLANT = new RenderFunction() {

            @Override
            public void renderGrowingBlock(BlockAndTintGetter level, BlockPos pos, Vector3f offset, BlockState state,
                                           double progress, MultiBufferSource bufferSource, PoseStack poseStack) {
                GrowingPlantBlockAccessor accessor = (GrowingPlantBlockAccessor) state.getBlock();
                poseStack.rotateAround(accessor.gtceu$getGrowthDirection().getRotation(), 0.5f, 0.5f, 0.5f);

                RenderUtil.drawBlock(level, pos, state, bufferSource, poseStack);
            }

            @Override
            public Collection<StateWithOffset> configureState(BlockAndTintGetter level, BlockState state,
                                                              double progress) {
                GrowingPlantBlockAccessor accessor = (GrowingPlantBlockAccessor) state.getBlock();

                Vector3fc translation = new Vector3f(0, (float) (progress * 2 - 1), 0);

                if (progress < 0.5) {
                    BlockState headState = accessor.gtceu$getHeadBlock().defaultBlockState();
                    IntegerProperty ageProp = findAgeProperty(headState.getProperties());
                    if (ageProp != null) {
                        IntegerPropertyAccessor prop = (IntegerPropertyAccessor) ageProp;
                        int minValue = prop.gtceu$getMin();
                        int maxValue = prop.gtceu$getMax();

                        int stage = GTMath.lerpInt(progress, minValue, maxValue + 1);
                        headState = headState.trySetValue(ageProp, Math.min(stage, maxValue));
                    }

                    if (progress >= 0.25 && headState.hasProperty(BlockStateProperties.BERRIES)) {
                        headState = headState.trySetValue(CaveVines.BERRIES, true);
                    }

                    return Collections.singleton(new StateWithOffset(headState, translation));
                } else {
                    BlockState headState = accessor.gtceu$getHeadBlock().defaultBlockState();
                    IntegerProperty ageProp = findAgeProperty(headState.getProperties());
                    if (ageProp != null) {
                        headState = headState.trySetValue(ageProp, ((IntegerPropertyAccessor) ageProp).gtceu$getMax());
                    }
                    if (headState.hasProperty(BlockStateProperties.BERRIES)) {
                        headState = headState.trySetValue(CaveVines.BERRIES, true);
                    }

                    BlockState bodyState = accessor.gtceu$getBodyBlock().defaultBlockState();
                    if (progress >= 0.75 && bodyState.hasProperty(BlockStateProperties.BERRIES)) {
                        bodyState = bodyState.trySetValue(CaveVines.BERRIES, true);
                    }
                    Vector3fc bodyTranslation = new Vector3f(translation.x(), translation.y() - 1, translation.z());

                    return Arrays.asList(new StateWithOffset(bodyState, bodyTranslation),
                            new StateWithOffset(headState, translation));
                }
            }
        };

        TriFunction<IntegerProperty, Integer, Integer, ConfigureOnly> PROPERTY_FUNCTION_CACHE = GTMemoizer
                .memoize((property, min, max) -> {
                    IntegerPropertyAccessor accessor = (IntegerPropertyAccessor) property;
                    final int minValue = accessor.gtceu$getMin();
                    final int maxValue = accessor.gtceu$getMax();
                    return (level, state, progress) -> {
                        int growthStage = GTMath.lerpInt(progress, min, max + 1);
                        if (growthStage < minValue) {
                            return Collections.emptySet();
                        }
                        state = state.trySetValue(property, Math.min(growthStage, maxValue));

                        return List.of(new StateWithOffset(state));
                    };
                });

        static RenderFunction.ConfigureOnly byIntegerProperty(IntegerProperty property, int min, int max) {
            return PROPERTY_FUNCTION_CACHE.apply(property, min, max);
        }

        @FunctionalInterface
        interface ConfigureOnly extends RenderFunction {

            @Override
            default void renderGrowingBlock(BlockAndTintGetter level, BlockPos pos, Vector3f offset, BlockState state,
                                            double progress, MultiBufferSource bufferSource, PoseStack poseStack) {
                RenderUtil.drawBlock(level, pos, state, bufferSource, poseStack);
            }

            @Override
            Collection<StateWithOffset> configureState(BlockAndTintGetter level, BlockState state, double progress);
        }
    }

    public record StateWithOffset(BlockState state, Vector3fc offset) {

        private static final Vector3fc ZERO_VECTOR = new Vector3f();

        public StateWithOffset(BlockState state) {
            this(state, ZERO_VECTOR);
        }
    }
}
