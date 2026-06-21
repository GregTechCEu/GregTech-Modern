package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.core.mixins.client.GuiGraphicsAccessor;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.*;
import java.util.function.BiFunction;

import static net.minecraft.util.FastColor.ARGB32.*;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {

    public enum FluidTextureType {

        STILL((fluidTypeExtensions, fluidStack) -> {
            if (!fluidStack.isEmpty()) return fluidTypeExtensions.getStillTexture(fluidStack);
            else return fluidTypeExtensions.getStillTexture();
        }),
        FLOWING((fluidTypeExtensions, fluidStack) -> {
            if (!fluidStack.isEmpty()) return fluidTypeExtensions.getFlowingTexture(fluidStack);
            else return fluidTypeExtensions.getFlowingTexture();
        }),
        OVERLAY((fluidTypeExtensions, fluidStack) -> {
            if (!fluidStack.isEmpty()) return fluidTypeExtensions.getOverlayTexture(fluidStack);
            else return fluidTypeExtensions.getOverlayTexture();
        });

        private static final ResourceLocation WATER_STILL = new ResourceLocation("minecraft", "block/water_still");

        private final BiFunction<IClientFluidTypeExtensions, FluidStack, ResourceLocation> mapper;

        FluidTextureType(BiFunction<IClientFluidTypeExtensions, FluidStack, ResourceLocation> mapper) {
            this.mapper = mapper;
        }

        public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions) {
            return map(fluidTypeExtensions, FluidStack.EMPTY);
        }

        public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions, FluidStack fluidStack) {
            ResourceLocation texture = mapper.apply(fluidTypeExtensions, fluidStack);
            if (texture == null) texture = STILL.mapper.apply(fluidTypeExtensions, fluidStack);
            if (texture == null) texture = WATER_STILL;

            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
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

    public static int getFluidLight(Fluid fluid, BlockPos pos, @Nullable BlockAndTintGetter level) {
        if (level == null) level = Minecraft.getInstance().level;
        if (level == null) return 0;

        return LevelRenderer.getLightColor(level, fluid.defaultFluidState().createLegacyBlock(), pos);
    }

    public static void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                              Vector3fc pos, Vector3fc normal, float u, float v,
                              int argb, int packedOverlay, int packedLight) {
        /*
         * For future reference:
         * The order of the vertex calls is important.
         * Change it, and it'll break and complain that you didn't fill all elements (even though you did).
         */
        vertexConsumer.gtceu$vertex(pose.pose(), pos)
                .color(argb)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .gtceu$normal(pose.normal(), normal)
                .endVertex();
    }

    public static void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                              float x, float y, float z,
                              int r, int g, int b, int a, float u, float v,
                              int packedOverlay, int packedLight,
                              float v0, float v1, float v2) {
        /*
         * For future reference:
         * The order of the vertex calls is important.
         * Change it, and it'll break and complain that you didn't fill all elements (even though you did).
         */
        vertexConsumer.vertex(pose.pose(), x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(pose.normal(), v0, v1, v2)
                .endVertex();
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
                .filter(content -> content.content instanceof FluidIngredient ingredient && !ingredient.isEmpty())
                .findAny();
        if (fluidContent.isEmpty()) {
            return null;
        }
        var ingredient = (FluidIngredient) fluidContent.get().content;

        var stacks = ingredient.getStacks();
        if (stacks.length == 0) {
            return null;
        }

        Fluid fluid = null;
        for (int i = 0; i < stacks.length && fluid == null; i++) {
            if (!stacks[i].isEmpty()) {
                fluid = stacks[i].getFluid();
            }
        }

        return fluid;
    }

    /**
     * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
     * type and coordinates as the boundaries.
     *
     * @param y2         the y-coordinate of the second corner of the rectangle.
     * @param x2         the x-coordinate of the second corner of the rectangle.
     * @param y1         the y-coordinate of the first corner of the rectangle.
     * @param x1         the x-coordinate of the first corner of the rectangle.
     * @param renderType the render type to use.
     * @param z          the z-level of the rectangle.
     * @param colorTo    the ending color of the gradient.
     * @param colorFrom  the starting color of the gradient.
     */
    public static void fillHorizontalGradient(GuiGraphics graphics, RenderType renderType, int x1, int y1, int x2,
                                              int y2, int colorFrom, int colorTo, int z) {
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(renderType);
        fillHorizontalGradient(graphics, vertexconsumer, x1, y1, x2, y2, z, colorFrom, colorTo);
        ((GuiGraphicsAccessor) graphics).callFlushIfUnmanaged();
    }

    /**
     * The core `fillGradient` method.
     * <p>
     * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
     * type and coordinates as the boundaries.
     *
     * @param consumer  the {@linkplain VertexConsumer} object for drawing the vertices on screen.
     * @param x1        the x-coordinate of the first corner of the rectangle.
     * @param y1        the y-coordinate of the first corner of the rectangle.
     * @param x2        the x-coordinate of the second corner of the rectangle.
     * @param y2        the y-coordinate of the second corner of the rectangle.
     * @param z         the z-level of the rectangle.
     * @param colorFrom the starting color of the gradient.
     * @param colorTo   the ending color of the gradient.
     */
    private static void fillHorizontalGradient(GuiGraphics graphics, VertexConsumer consumer,
                                               float x1, float y1, float x2, float y2, float z,
                                               int colorFrom, int colorTo) {
        int a1 = alpha(colorFrom), r1 = red(colorFrom), g1 = green(colorFrom), b1 = blue(colorFrom);
        int a2 = alpha(colorTo), r2 = red(colorTo), g2 = green(colorTo), b2 = blue(colorTo);

        Matrix4f pose = graphics.pose().last().pose();
        consumer.vertex(pose, x1, y1, z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(pose, x1, y2, z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(pose, x2, y2, z).color(r2, g2, b2, a2).endVertex();
        consumer.vertex(pose, x2, y1, z).color(r2, g2, b2, a2).endVertex();
    }

    /**
     * Converts an (A)RGB integer color into an array of floats, for use in GL calls
     *
     * @return float[]{R, G, B, A}
     */
    public static float[] floats(int argb) {
        return new float[] {
                (float) (argb >> 16 & 255) / 255.0F,
                (float) (argb >> 8 & 255) / 255.0F,
                (float) (argb & 255) / 255.0F,
                (float) (argb >> 24 & 255) / 255.0F
        };
    }

    public static int interpolateColor(int color1, int color2, float blend) {
        int a1 = color1 >> 24 & 255;
        int r1 = color1 >> 16 & 255;
        int g1 = color1 >> 8 & 255;
        int b1 = color1 & 255;

        int a2 = color2 >> 24 & 255;
        int r2 = color2 >> 16 & 255;
        int g2 = color2 >> 8 & 255;
        int b2 = color2 & 255;

        int a = (int) (a1 * (1 - blend) + a2 * blend);
        int r = (int) (r1 * (1 - blend) + r2 * blend);
        int g = (int) (g1 * (1 - blend) + g2 * blend);
        int b = (int) (b1 * (1 - blend) + b2 * blend);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void moveToFace(PoseStack poseStack, Vector3fc pos, Direction face) {
        moveToFace(poseStack, pos.x(), pos.y(), pos.z(), face);
    }

    public static void moveToFace(PoseStack poseStack, float x, float y, float z, Direction face) {
        poseStack.translate(Math.fma(face.getStepX(), 0.5f, x),
                Math.fma(face.getStepY(), 0.5f, y),
                Math.fma(face.getStepZ(), 0.5f, z));
    }

    public static BakedModel getModelForState(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    public static void drawBlock(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                 MultiBufferSource bufferSource, PoseStack poseStack) {
        int packedLight = LevelRenderer.getLightColor(level, state, pos);

        RenderShape renderShape = state.getRenderShape();
        if (renderShape == RenderShape.INVISIBLE) {
            return;
        } else if (renderShape == RenderShape.ENTITYBLOCK_ANIMATED) {
            // if it's a block entity, use the BEWLR to render it instead of the empty block model
            ItemStack stack = new ItemStack(state.getBlock());
            IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemDisplayContext.NONE,
                    poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            return;
        }

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = blockRenderer.getBlockModel(state);
        ModelData modelData = model.getModelData(level, pos, state, ModelData.EMPTY);

        int blockColor = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
        float r = (float) (blockColor >> 16 & 0xFF) / 255.0F;
        float g = (float) (blockColor >> 8 & 0xFF) / 255.0F;
        float b = (float) (blockColor & 0xFF) / 255.0F;

        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42), modelData)) {
            blockRenderer.getModelRenderer().renderModel(poseStack.last(),
                    bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
                    state, model, r, g, b,
                    packedLight, OverlayTexture.NO_OVERLAY,
                    modelData, renderType);
        }
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

    public static boolean renderResearchItemContent(GuiGraphics graphics, Operation<Void> originalMethod,
                                                    @Nullable LivingEntity entity, @Nullable Level level,
                                                    ItemStack stack, int x, int y, int z, int seed) {
        if (!Screen.hasShiftDown()) return false;

        ResearchManager.ResearchItem researchData = ResearchManager.readResearchId(stack);
        if (researchData == null) return false;

        Collection<GTRecipe> recipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
        if (recipes == null || recipes.isEmpty()) return false;

        for (var recipe : recipes) {
            // check item outputs first
            List<Content> outputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!outputs.isEmpty()) {
                ItemStack[] items = ItemRecipeCapability.CAP.of(outputs.get(0).content).getItems();
                if (items.length > 0) {
                    ItemStack output = items[0];
                    if (!output.isEmpty() && !GTUtil.isSameItemSameTags(output, stack)) {
                        originalMethod.call(entity, level, output, x, y, seed, z);
                        return true;
                    }
                }
            }
            // if there are no item outputs, try to find a fluid output
            outputs = recipe.getOutputContents(FluidRecipeCapability.CAP);
            if (!outputs.isEmpty()) {
                FluidStack[] fluids = FluidRecipeCapability.CAP.of(outputs.get(0).content).getStacks();
                if (fluids.length != 0) {
                    FluidStack output = fluids[0];
                    if (!output.isEmpty()) {
                        var clientExt = IClientFluidTypeExtensions.of(output.getFluid());
                        var texture = RenderUtil.FluidTextureType.STILL.map(clientExt, output);
                        int color = clientExt.getTintColor(output);

                        DrawerHelper.drawFluidTexture(graphics, x, y, texture, 0, 0, z, color);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
