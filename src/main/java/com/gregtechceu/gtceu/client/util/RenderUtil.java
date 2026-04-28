package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {

    private static @Nullable ModelBlockRenderer blockModelRenderer;

    public enum FluidTextureType {

        STILL,
        FLOWING,
        OVERLAY;

        private static final Identifier WATER_STILL = Identifier.withDefaultNamespace("block/water_still");

        public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions) {
            return atlasSprite(WATER_STILL);
        }

        public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions, FluidStack fluidStack) {
            return map(fluidStack);
        }

        public TextureAtlasSprite map(Fluid fluid) {
            return map(fluid.defaultFluidState(), FluidStack.EMPTY);
        }

        public TextureAtlasSprite map(FluidStack fluidStack) {
            if (fluidStack.isEmpty()) {
                return atlasSprite(WATER_STILL);
            }
            return map(fluidStack.getFluid().defaultFluidState(), fluidStack);
        }

        private TextureAtlasSprite map(net.minecraft.world.level.material.FluidState state, FluidStack stack) {
            FluidModel model = fluidModel(state);
            return switch (this) {
                case STILL -> model.stillMaterial().sprite();
                case FLOWING -> model.flowingMaterial().sprite();
                case OVERLAY -> model.overlayMaterial() == null ? model.stillMaterial().sprite() :
                        model.overlayMaterial().sprite();
            };
        }

        private static TextureAtlasSprite atlasSprite(Identifier texture) {
            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS)
                    .getSprite(texture);
        }
    }

    public static Vec3 vec3(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static Vector3f vec3f(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    // spotless:off
    private static final Map<Direction, Vector3fc[]> DIRECTION_POSITION_MAP = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.UP, new Vector3fc[] { vec3f(0, 1, 1), vec3f(1, 1, 1), vec3f(1, 1, 0), vec3f(0, 1, 0) });
        map.put(Direction.DOWN, new Vector3fc[] { vec3f(1, 0, 1), vec3f(0, 0, 1), vec3f(0, 0, 0), vec3f(1, 0, 0) });
        map.put(Direction.SOUTH, new Vector3fc[] { vec3f(1, 1, 0), vec3f(1, 0, 0), vec3f(0, 0, 0), vec3f(0, 1, 0) });
        map.put(Direction.NORTH, new Vector3fc[] { vec3f(0, 1, 1), vec3f(0, 0, 1), vec3f(1, 0, 1), vec3f(1, 1, 1) });
        map.put(Direction.EAST, new Vector3fc[] { vec3f(0, 1, 0), vec3f(0, 0, 0), vec3f(0, 0, 1), vec3f(0, 1, 1) });
        map.put(Direction.WEST, new Vector3fc[] { vec3f(1, 1, 1), vec3f(1, 0, 1), vec3f(1, 0, 0), vec3f(1, 1, 0) });
    });
    // spotless:on

    public static Vector3fc[] getVertices(Direction direction) {
        return DIRECTION_POSITION_MAP.get(direction);
    }

    // spotless:off
    private static final Map<Direction, Vector3fc> DIRECTION_NORMAL_MAP = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.UP, vec3f(0, 1, 0));
        map.put(Direction.DOWN, vec3f(0, 1, 0));
        map.put(Direction.SOUTH, vec3f(0, 0, 1));
        map.put(Direction.NORTH, vec3f(0, 0, 1));
        map.put(Direction.EAST, vec3f(1, 0, 0));
        map.put(Direction.WEST, vec3f(1, 0, 0));
    });
    // spotless:on

    public static Vector3fc getNormal(Direction direction) {
        return DIRECTION_NORMAL_MAP.get(direction);
    }

    public static int getFluidLight(Fluid fluid, BlockPos pos) {
        if (Minecraft.getInstance().level == null) return 0;
        return LevelRenderer.getLightCoords(Minecraft.getInstance().level, pos);
    }

    public static FluidModel fluidModel(Fluid fluid) {
        return fluidModel(fluid.defaultFluidState());
    }

    public static FluidModel fluidModel(net.minecraft.world.level.material.FluidState state) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
    }

    public static int getFluidTint(Fluid fluid) {
        var state = fluid.defaultFluidState();
        var tintSource = fluidModel(state).fluidTintSource();
        return tintSource == null ? -1 : tintSource.color(state);
    }

    public static int getFluidTint(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) return -1;
        var tintSource = fluidModel(fluidStack.getFluid()).fluidTintSource();
        return tintSource == null ? -1 : tintSource.colorAsStack(fluidStack);
    }

    public static void vertex(Matrix4f pose, VertexConsumer vertexConsumer,
                              float x, float y, float z,
                              int r, int g, int b, int a,
                              float u, float v, int overlayCoords, int lightOverlay,
                              float v0, float v1, float v2) {
        /*
         * For future reference:
         * The order of the vertex calls is important.
         * Change it, and it'll break and complain that you didn't fill all elements (even though you did).
         */
        vertexConsumer
                .addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlayCoords)
                .setLight(lightOverlay)
                .setNormal(v0, v1, v2);
    }

    public static Vector3f transformVertex(Vector3fc vertex, Direction direction,
                                           float offsetX, float offsetY, float offsetZ) {
        float addX = offsetX, addY = offsetY, addZ = offsetZ;
        switch (direction) {
            case DOWN -> addY = -addY;
            case SOUTH -> addZ = -addZ;
            case EAST -> addX = -addX;
        }

        return new Vector3f(vertex).add(addX, addY, addZ);
    }

    public static @Nullable Fluid getRecipeFluidToRender(GTRecipe recipe) {
        if (recipe == null) {
            return null;
        }
        var contents = new ObjectArrayList<Content>();
        var empty = new ArrayList<Content>();
        contents.addAll(recipe.outputs.getOrDefault(FluidRecipeCapability.CAP, empty));
        contents.addAll(recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, empty));
        if (contents.isEmpty()) {
            return null;
        }

        var fluidContent = contents.stream()
                .filter(content -> content.content instanceof SizedFluidIngredient ingredient &&
                        !ingredient.ingredient().fluids().isEmpty())
                .findAny();
        if (fluidContent.isEmpty()) {
            return null;
        }
        var ingredient = (SizedFluidIngredient) fluidContent.get().content;

        var fluids = ingredient.ingredient().fluids();
        if (fluids.isEmpty()) {
            return null;
        }

        return fluids.getFirst().value();
    }

    public static void moveToFace(PoseStack poseStack, Vector3fc pos, Direction face) {
        moveToFace(poseStack, pos.x(), pos.y(), pos.z(), face);
    }

    public static void moveToFace(PoseStack poseStack, float x, float y, float z, Direction face) {
        poseStack.translate(Math.fma(face.getStepX(), 0.5f, x),
                Math.fma(face.getStepY(), 0.5f, y),
                Math.fma(face.getStepZ(), 0.5f, z));
    }

    public static void drawBlock(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                 MultiBufferSource bufferSource, PoseStack poseStack) {
        RenderShape renderShape = state.getRenderShape();
        if (renderShape != RenderShape.MODEL) {
            return;
        }

        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        BlockQuadOutput output = (x, y, z, quad, quadInstance) -> {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            bufferSource.getBuffer(quad.materialInfo().itemRenderType()).putBakedQuad(poseStack.last(), quad,
                    quadInstance);
            poseStack.popPose();
        };
        blockModelRenderer().tesselateBlock(output, 0, 0, 0, level, pos, state, model, state.getSeed(pos));
    }

    private static ModelBlockRenderer blockModelRenderer() {
        if (blockModelRenderer == null) {
            blockModelRenderer = new ModelBlockRenderer(true, true, Minecraft.getInstance().getBlockColors());
        }
        return blockModelRenderer;
    }

    private static void renderBlockItem(ItemStack stack, BlockAndTintGetter level, MultiBufferSource bufferSource,
                                        PoseStack poseStack, int seed) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft minecraft = Minecraft.getInstance();
        renderState.clear();
        minecraft.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.NONE,
                level instanceof Level realLevel ? realLevel : minecraft.level, null, seed);

        SubmitNodeStorage submitStorage = new SubmitNodeStorage();
        renderState.submit(poseStack, submitStorage, LevelRenderer.getLightCoords(level, BlockPos.ZERO),
                OverlayTexture.NO_OVERLAY, 0);
        for (SubmitNodeCollection submits : submitStorage.getSubmitsPerOrder().values()) {
            for (SubmitNodeStorage.ItemSubmit submit : submits.getItemSubmits()) {
                renderSubmittedItem(bufferSource, submit);
            }
        }
    }

    private static void renderSubmittedItem(MultiBufferSource bufferSource, SubmitNodeStorage.ItemSubmit submit) {
        QuadInstance quadInstance = new QuadInstance();
        quadInstance.setLightCoords(submit.lightCoords());
        quadInstance.setOverlayCoords(submit.overlayCoords());

        for (BakedQuad quad : submit.quads()) {
            var material = quad.materialInfo();
            var renderType = material.itemRenderType();
            quadInstance.setColor(getLayerColorSafe(submit.tintLayers(), material));

            if (submit.foilType() != ItemStackRenderState.FoilType.NONE) {
                ItemFeatureRenderer.getFoilBuffer(bufferSource, renderType, true, true)
                        .putBakedQuad(submit.pose(), quad, quadInstance);
            }

            bufferSource.getBuffer(renderType).putBakedQuad(submit.pose(), quad, quadInstance);
        }
    }

    private static int getLayerColorSafe(int[] tintLayers, BakedQuad.MaterialInfo material) {
        return material.isTinted() && material.tintIndex() >= 0 && material.tintIndex() < tintLayers.length ?
                tintLayers[material.tintIndex()] : -1;
    }

    /**
     * Rotate the current coordinate system, so it is on the face of the given block side.
     * This can be used to render on the given face as if it was a 2D canvas,
     * where x+ is facing right and y+ is facing up.
     */
    public static void rotateToFace(PoseStack poseStack, Direction face, Direction spin) {
        float rotationAngle = Mth.HALF_PI * switch (face) {
            case UP, WEST -> 1;
            case DOWN, EAST -> -1;
            case SOUTH -> 2;
            case NORTH -> 0;
        };
        Quaternionf rotation = new Quaternionf();
        if (face.getAxis() == Direction.Axis.Y) {
            poseStack.scale(1.0f, -1.0f, 1.0f);
            rotation.rotateX(rotationAngle);
        } else {
            poseStack.scale(-1.0f, -1.0f, -1.0f);
            rotation.rotateY(rotationAngle);
        }
        rotation.rotateZ(getSpinAngle(spin, face));

        poseStack.mulPose(rotation);
    }

    private static float getSpinAngle(Direction spin, Direction face) {
        if (spin.getAxis() == Direction.Axis.Z && face == Direction.DOWN) {
            spin = spin.getOpposite();
        }
        return GTMatrixUtils.upwardFacingAngle(spin);
    }

    public static boolean renderResearchItemContent(GuiGraphicsExtractor graphics, Operation<Void> originalMethod,
                                                    @Nullable LivingEntity entity, @Nullable Level level,
                                                    ItemStack stack, int x, int y, int z, int seed) {
        if (!Minecraft.getInstance().hasShiftDown()) return false;

        ResearchManager.ResearchItem researchData = stack.get(GTDataComponents.RESEARCH_ITEM);
        if (researchData == null) return false;

        Collection<GTRecipe> recipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
        if (recipes == null || recipes.isEmpty()) return false;

        for (var recipe : recipes) {
            // check item outputs first
            List<Content> outputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!outputs.isEmpty()) {
                var ingredient = ItemRecipeCapability.CAP.of(outputs.getFirst().content);
                ItemStack[] items = ingredient.ingredient().items()
                        .map(holder -> new ItemStack(holder, ingredient.count()))
                        .toArray(ItemStack[]::new);
                if (items.length > 0) {
                    ItemStack output = items[0];
                    if (!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, stack)) {
                        originalMethod.call(entity, level, output, x, y, seed, z);
                        return true;
                    }
                }
            }
            // if there are no item outputs, try to find a fluid output
            outputs = recipe.getOutputContents(FluidRecipeCapability.CAP);
            if (!outputs.isEmpty()) {
                var ingredient = FluidRecipeCapability.CAP.of(outputs.getFirst().content);
                FluidStack[] fluids = ingredient.ingredient().fluids().stream()
                        .map(holder -> new FluidStack(holder, ingredient.amount()))
                        .toArray(FluidStack[]::new);
                if (fluids.length != 0) {
                    FluidStack output = fluids[0];
                    if (!output.isEmpty()) {
                        var texture = RenderUtil.FluidTextureType.STILL.map(output);
                        int color = RenderUtil.getFluidTint(output);

                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, 16, 16, color);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
