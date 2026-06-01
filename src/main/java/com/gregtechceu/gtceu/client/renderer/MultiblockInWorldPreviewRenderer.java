package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternAisle;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateBlocks;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateFluidTag;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateFluids;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateStates;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.lowdragmc.lowdraglib.client.scene.forge.WorldSceneRendererImpl;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.world.level.block.RenderShape.INVISIBLE;

@OnlyIn(Dist.CLIENT)
public class MultiblockInWorldPreviewRenderer {

    private enum CacheState {
        UNUSED,
        COMPILING,
        COMPILED
    }

    @Getter(lazy = true)
    private final static VertexBuffer[] BUFFERS = initBuffers();
    @Nullable
    private static TrackedDummyWorld LEVEL = null;
    @Nullable
    private static Thread THREAD = null;
    @Nullable
    private static Set<BlockPos> BLOCK_ENTITIES;
    private final static AtomicInteger LEFT_TICK = new AtomicInteger(-1);

    /**
     * It will be cached by lombok#@Getter(lazy=true)
     */
    private static VertexBuffer[] initBuffers() {
        List<RenderType> layers = RenderType.chunkBufferLayers();
        var buffers = new VertexBuffer[layers.size()];
        for (int j = 0; j < layers.size(); ++j) {
            buffers[j] = new VertexBuffer(VertexBuffer.Usage.STATIC);
        }
        return buffers;
    }

    private final static AtomicReference<CacheState> CACHE_STATE = new AtomicReference<>(CacheState.UNUSED);

    @Nullable
    private static BlockPos LAST_POS = null;
    private static int LAST_LAYER = -1;

    public static void cleanPreview() {
        CACHE_STATE.set(CacheState.UNUSED);
        LEVEL = null;
        BLOCK_ENTITIES = null;
        LEFT_TICK.set(-1);
        LAST_POS = null;
        LAST_LAYER = -1;
    }

    public static void removePreview(BlockPos pos) {
        if (LAST_POS != null && LAST_POS.equals(pos)) {
            cleanPreview();
        }
    }

    /**
     * Show the multiblock preview in the world by the given pos, side, and shape info.
     *
     * @param pos        the pos of the controller
     * @param controller the controller
     * @param duration   the duration of the preview. in ticks.
     */
    public static void showPreview(BlockPos pos, MultiblockControllerMachine controller,
                                   int duration) {
        if (!controller.getDefinition().isRenderWorldPreview()) return;
        Direction front = controller.getFrontFacing();
        Direction up = controller.getUpwardsFacing();

        IBlockPattern pattern = controller.getSubstructurePattern(MultiblockControllerMachine.DEFAULT_STRUCTURE);

        // MultiblockShapeInfo shapeInfo = controller.getDefinition().getMatchingShapes().get(0);

        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        MultiblockControllerMachine controllerBase = null;
        LEVEL = new TrackedDummyWorld();

        // BlockInfo[][][] blocks = pattern.

        // var blocks = shapeInfo.getBlocks();
        // BlockPos controllerPatternPos = controller.getSubstructure(MultiblockControllerMachine.DEFAULT_STRUCTURE)
        // .getOffset().toBlockPos();
        BlockPos controllerPos = controller.getBlockPos();
        BlockPos patternControllerPos = controller.getSubstructurePattern(MultiblockControllerMachine.DEFAULT_STRUCTURE)
                .getOffset().toBlockPos();
        var maxY = 0;

        if (LAST_POS != null && LAST_POS.equals(pos)) {
            LAST_LAYER++;
            if (LAST_LAYER >= maxY) {
                LAST_LAYER = -1;
            }
        } else {
            LAST_LAYER = -1;
        }
        LAST_POS = pos;

        if (pattern instanceof BlockPattern blockPattern) {
            var predicateMapping = blockPattern.getPredicates();
            int[] dims = blockPattern.getDimensions();
            for (int x = 0; x < dims[0]; x++) {
                PatternAisle aisle = blockPattern.getAisles()[x];
                for (int y = 0; y < dims[1]; y++) {
                    var string = aisle.getPattern()[y];
                    for (int z = 0; z < dims[2]; z++) {
                        var predList = predicateMapping.get(string.charAt(z));
                        var pred = predList.predicateList.get(0);
                        BlockState blockState = Blocks.AIR.defaultBlockState();
                        if (pred instanceof PredicateBlocks blockPred) {
                            blockState = blockPred.blocks[0].defaultBlockState();
                        } else if (pred instanceof PredicateStates statePred) {
                            blockState = statePred.states[0];
                        }
                        if (pred instanceof PredicateFluids || pred instanceof PredicateFluidTag) continue;

                        BlockPos offset = RelativeDirection.offsetPos(controllerPos, front, up, false, y, x, z);

                        blockMap.put(offset, BlockInfo.fromBlockState(blockState));
                    }
                }
            }
        }

        /*
         * for (int x = 0; x < pattern.; x++) {
         * BlockInfo[][] aisle = blocks[x];
         * for (int y = 0; y < aisle.length; y++) {
         * BlockInfo[] column = aisle[y];
         * if (LAST_LAYER != -1 && LAST_LAYER != y) {
         * continue;
         * }
         * for (int z = 0; z < column.length; z++) {
         * var blockState = column[z].getBlockState();
         * //var offset = new BlockPos(x, y, z).subtract(patternControllerPos);
         * BlockPos offset = RelativeDirection.offsetPos(controllerPos, front, up, false, y, x, z);
         * 
         * if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
         * var rotationState = machineBlock.getRotationState();
         * if (rotationState != RotationState.NONE) {
         * var face = blockState.getValue(rotationState.property);
         * if (face.getAxis() != Direction.Axis.Y) {
         * face = switch (front) {
         * case NORTH, UP, DOWN -> front;
         * case SOUTH -> face.getOpposite();
         * case WEST -> face.getCounterClockWise();
         * case EAST -> face.getClockWise();
         * };
         * }
         * if (rotationState.test(face)) {
         * blockState = blockState.setValue(rotationState.property, face);
         * }
         * }
         * }
         * 
         * if (column[z].getBlockEntity(offset) instanceof MultiblockControllerMachine cont) {
         * cont.self().setLevel(LEVEL);
         * controllerBase = cont;
         * } else {
         * blockMap.put(offset, BlockInfo.fromBlockState(blockState));
         * }
         * }
         * }
         * }
         */

        // LEVEL.addBlocks(blockMap);
        // if (controllerBase != null) {
        // LEVEL.setInnerBlockEntity(controllerBase.self());
        // }

        // prepareBuffers(LEVEL, blockMap.keySet(), duration);
    }

    public static void onClientTick() {
        if (LEFT_TICK.get() > 0) {
            if (LEFT_TICK.decrementAndGet() <= 0) {
                cleanPreview();
            }
        }
    }

    public static void renderInWorldPreview(PoseStack poseStack, Camera camera, float partialTicks) {
        if (CACHE_STATE.get() == CacheState.COMPILED && LEVEL != null) {
            poseStack.pushPose();
            Vec3 projectedView = camera.getPosition();
            poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            for (int i = 0; i < RenderType.chunkBufferLayers().size(); i++) {
                VertexBuffer vertexbuffer = getBUFFERS()[i];
                // some of stupid mod doesn't check if the buffer is invalid
                if (vertexbuffer.isInvalid() || vertexbuffer.getFormat() == null) continue;
                var layer = RenderType.chunkBufferLayers().get(i);

                // render TESR before translucent
                if (layer == RenderType.translucent() && BLOCK_ENTITIES != null) { // render tesr before translucent
                    var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
                    for (BlockPos pos : BLOCK_ENTITIES) {
                        BlockEntity tile = LEVEL.getBlockEntity(pos);
                        if (tile != null) {
                            poseStack.pushPose();
                            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                            BlockEntityRenderer<BlockEntity> ber = Minecraft.getInstance()
                                    .getBlockEntityRenderDispatcher().getRenderer(tile);
                            if (ber != null) {
                                if (tile.hasLevel() && tile.getType().isValid(tile.getBlockState())) {
                                    ber.render(tile, partialTicks, poseStack, buffers, LightTexture.FULL_BRIGHT,
                                            OverlayTexture.NO_OVERLAY);
                                }
                            }
                            poseStack.popPose();
                        }
                    }
                    buffers.endBatch();
                }

                // render cache vbo
                layer.setupRenderState();
                poseStack.pushPose();
                ShaderInstance shaderInstance = RenderSystem.getShader();

                for (int j = 0; j < 12; ++j) {
                    int k = RenderSystem.getShaderTexture(j);
                    shaderInstance.setSampler("Sampler" + j, k);
                }

                // setup shader uniform
                if (shaderInstance.MODEL_VIEW_MATRIX != null) {
                    shaderInstance.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
                }

                if (shaderInstance.PROJECTION_MATRIX != null) {
                    shaderInstance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
                }

                if (shaderInstance.COLOR_MODULATOR != null) {
                    shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
                }

                if (shaderInstance.FOG_START != null) {
                    shaderInstance.FOG_START.set(Float.MAX_VALUE);
                }

                if (shaderInstance.FOG_END != null) {
                    shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
                }

                if (shaderInstance.FOG_COLOR != null) {
                    shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
                }

                if (shaderInstance.FOG_SHAPE != null) {
                    shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
                }

                if (shaderInstance.TEXTURE_MATRIX != null) {
                    shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
                }

                if (shaderInstance.GAME_TIME != null) {
                    shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
                }

                RenderSystem.setupShaderLights(shaderInstance);
                shaderInstance.apply();

                RenderSystem.setShaderColor(1, 1, 1, 1);
                if (layer == RenderType.translucent()) { // SOLID
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    RenderSystem.depthMask(false);
                } else { // TRANSLUCENT
                    RenderSystem.enableDepthTest();
                    RenderSystem.disableBlend();
                    RenderSystem.depthMask(true);
                }

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                vertexbuffer.bind();
                vertexbuffer.draw();

                poseStack.popPose();

                shaderInstance.clear();
                VertexBuffer.unbind();
                layer.clearRenderState();
            }
            poseStack.popPose();
        }
    }

    private static void prepareBuffers(TrackedDummyWorld level, Collection<BlockPos> renderedBlocks, int duration) {
        if (THREAD != null) {
            THREAD.interrupt();
        }
        CACHE_STATE.set(CacheState.COMPILING);
        // call it to init the buffers
        getBUFFERS();
        THREAD = new Thread(() -> {
            var dispatcher = Minecraft.getInstance().getBlockRenderer();
            ModelBlockRenderer.enableCaching();
            PoseStack poseStack = new PoseStack();
            for (int i = 0; i < RenderType.chunkBufferLayers().size(); i++) {
                if (Thread.interrupted())
                    return;
                var layer = RenderType.chunkBufferLayers().get(i);
                var buffer = new BufferBuilder(layer.bufferSize());
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                renderBlocks(level, poseStack, dispatcher, layer, new WorldSceneRenderer.VertexConsumerWrapper(buffer),
                        renderedBlocks);
                var builder = buffer.end();
                var vertexBuffer = getBUFFERS()[i];
                Runnable toUpload = () -> {
                    if (!vertexBuffer.isInvalid()) {
                        vertexBuffer.bind();
                        vertexBuffer.upload(builder);
                        VertexBuffer.unbind();
                    }
                };
                CompletableFuture.runAsync(toUpload, runnable -> {
                    RenderSystem.recordRenderCall(runnable::run);
                });

            }
            ModelBlockRenderer.clearCache();

            // record all BlockEntities having TESR.
            Set<BlockPos> poses = new HashSet<>();
            for (BlockPos pos : renderedBlocks) {
                if (Thread.interrupted())
                    return;
                BlockEntity tile = level.getBlockEntity(pos);
                if (tile != null) {
                    if (Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile) != null) {
                        poses.add(pos);
                    }
                }
            }

            if (Thread.interrupted())
                return;
            BLOCK_ENTITIES = poses;
            CACHE_STATE.set(CacheState.COMPILED);
            THREAD = null;
            LEFT_TICK.set(duration);
        });
        THREAD.start();
    }

    private static void renderBlocks(TrackedDummyWorld level, PoseStack poseStack, BlockRenderDispatcher dispatcher,
                                     RenderType layer, WorldSceneRenderer.VertexConsumerWrapper wrapperBuffer,
                                     Collection<BlockPos> renderedBlocks) {
        for (BlockPos pos : renderedBlocks) {
            BlockState state = level.getBlockState(pos);
            FluidState fluidState = state.getFluidState();
            Block block = state.getBlock();
            BlockEntity be = level.getBlockEntity(pos);

            if (block == Blocks.AIR) continue;

            // render blocks
            if (state.getRenderShape() != INVISIBLE && ItemBlockRenderTypes.getRenderLayers(state).contains(layer)) {
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.8f, 0.8f, 0.8f);
                poseStack.translate(-0.5, -0.5, -0.5);

                level.setRenderFilter(p -> p.equals(pos));
                WorldSceneRendererImpl.renderBlocksForge(dispatcher, state, pos, level, poseStack, wrapperBuffer,
                        GTValues.RNG, layer);
                level.setRenderFilter(p -> true);
                poseStack.popPose();
            }

            // render fluids
            if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == layer) {
                wrapperBuffer.addOffset((pos.getX() - (pos.getX() & 15)), (pos.getY() - (pos.getY() & 15)),
                        (pos.getZ() - (pos.getZ() & 15)));
                dispatcher.renderLiquid(pos, level, wrapperBuffer, state, fluidState);
            }

            wrapperBuffer.clerOffset();
            wrapperBuffer.clearColor();
        }
    }
}
