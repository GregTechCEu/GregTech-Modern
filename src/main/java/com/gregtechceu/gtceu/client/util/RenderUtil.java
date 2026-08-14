package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
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
import net.minecraftforge.registries.ForgeRegistries;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.BiFunction;

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

    public static int getFluidLight(Fluid fluid, BlockPos pos) {
        if (Minecraft.getInstance().level == null) return 0;
        return LevelRenderer.getLightColor(Minecraft.getInstance().level, fluid.defaultFluidState().createLegacyBlock(),
                pos);
    }

    public static void vertex(Matrix4f pose, VertexConsumer vertexConsumer, float x, float y, float z,
                              int r, int g, int b, int a, float u, float v, int overlayCoords, int lightOverlay,
                              float v0, float v1, float v2) {
        /*
         * For future reference:
         * The order of the vertex calls is important.
         * Change it, and it'll break and complain that you didn't fill all elements (even though you did).
         */
        vertexConsumer
                .vertex(pose, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlayCoords)
                .uv2(lightOverlay)
                .normal(v0, v1, v2)
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
        var contents = new ArrayList<FluidIngredient>();
        contents.addAll(recipe.outputs.getOrDefault(FluidRecipeCapability.CAP, List.of()));
        contents.addAll(recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, List.of()));
        if (contents.isEmpty()) {
            return null;
        }

        var fluidContent = contents.stream()
                .filter(ingredient -> ingredient.getFluids().length > 0)
                .findAny();
        if (fluidContent.isEmpty()) {
            return null;
        }
        var ingredient = fluidContent.get();

        var stacks = ingredient.getFluids();
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

        Collection<GTRecipeDefinition> recipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
        if (recipes == null || recipes.isEmpty()) return false;

        for (var recipe : recipes) {
            // check item outputs first
            var itemOutputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
            if (!itemOutputs.isEmpty()) {
                ItemStack[] items = itemOutputs.get(0).getItems();
                if (items.length > 0) {
                    ItemStack output = items[0];
                    if (!output.isEmpty() && !GTUtil.isSameItemSameTags(output, stack)) {
                        originalMethod.call(entity, level, output, x, y, seed, z);
                        return true;
                    }
                }
            }
            // if there are no item outputs, try to find a fluid output
            var fluidOutputs = recipe.getOutputContents(FluidRecipeCapability.CAP);
            if (!fluidOutputs.isEmpty()) {
                FluidStack[] fluids = fluidOutputs.get(0).getFluids();
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

    public static void renderItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, float width,
                                  float height) {
        renderItem(guiGraphics, null, null, itemStack, x, y, 0, width, height, 0);
    }

    /**
     * Enhanced {@link GuiGraphics#renderItem(LivingEntity, Level, ItemStack, int, int, int, int)} where you can assign
     * the width and height of the rendered item.
     */
    public static void renderItem(GuiGraphics guiGraphics, @Nullable LivingEntity livingEntity, @Nullable Level level,
                                  ItemStack itemStack, int x, int y, int seed, float width, float height,
                                  int guiOffset) {
        if (itemStack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        BakedModel bakedModel = mc.getItemRenderer().getModel(itemStack, level, livingEntity, seed);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(
                (float) x + (width / 2),
                (float) y + (height / 2),
                (float) (/* 150 */ 250 + (bakedModel.isGui3d() ? guiOffset : 0)));

        try {
            pose.mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
            pose.scale(width, height, 16.0F);
            boolean flag = !bakedModel.usesBlockLight();
            if (flag) {
                Lighting.setupForFlatItems();
            }
            mc.getItemRenderer().render(
                    itemStack,
                    ItemDisplayContext.GUI,
                    false,
                    pose,
                    guiGraphics.bufferSource(),
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    bakedModel);
            guiGraphics.flush();
            if (flag) {
                Lighting.setupFor3DItems();
            }
        } catch (Throwable e) {
            CrashReport crash = CrashReport.forThrowable(e, "Rendering item");
            CrashReportCategory category = crash.addCategory("Item being rendered");
            category.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
            category.setDetail(
                    "Registry Name",
                    () -> String.valueOf(ForgeRegistries.ITEMS.getKey(itemStack.getItem())));
            category.setDetail("Item Damage", () -> String.valueOf(itemStack.getDamageValue()));
            category.setDetail("Item NBT", () -> String.valueOf(itemStack.getTag()));
            category.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
            throw new ReportedException(crash);
        }

        pose.popPose();
    }
}
