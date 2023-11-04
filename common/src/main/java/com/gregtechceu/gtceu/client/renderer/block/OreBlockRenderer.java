package com.gregtechceu.gtceu.client.renderer.block;

import com.google.common.base.Suppliers;
import com.gregtechceu.gtceu.client.model.ItemBakedModel;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.impl.BlockStateRenderer;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote OreBlockRenderer
 */
public class OreBlockRenderer extends BlockStateRenderer {
    private final Supplier<BlockState> stone;
    private final Supplier<ResourceLocation> overlaySupplier;
    private final boolean emissive;

    public OreBlockRenderer(Supplier<BlockState> stone, Supplier<ResourceLocation> overlaySupplier, boolean emissive) {
        this.stone = Suppliers.memoize(stone::get);
        this.overlaySupplier = overlaySupplier;
        this.emissive = emissive;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    @Override
    public BlockInfo getBlockInfo() {
        return new BlockInfo(stone.get());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        super.renderItem(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay, model);
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay,
                new ItemBakedModel() {
                    @Override
                    @Environment(EnvType.CLIENT)
                    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
                        List<BakedQuad> quads = new LinkedList<>();
                        if (direction != null) {
                            quads.add(FaceQuad.bakeFace(direction, ModelFactory.getBlockSprite(overlaySupplier.get()), BlockModelRotation.X0_Y0, 1, emissive ? 15 : 0, true, !emissive));
                        }
                        return quads;
                    }
                });
        IItemRendererProvider.disabled.set(false);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand) {
        List<BakedQuad> quads = new LinkedList<>(super.renderModel(level, pos, state, side, rand));
        if (side != null) {
            quads.add(FaceQuad.bakeFace(side, ModelFactory.getBlockSprite(overlaySupplier.get()), BlockModelRotation.X0_Y0, 1, emissive ? 15 : 0, true, !emissive));
        }
        return quads;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(overlaySupplier.get());
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useAO() {
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = brd.getBlockModel(stone.get());
        return model.useAmbientOcclusion();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = brd.getBlockModel(stone.get());
        return model.usesBlockLight();
    }
}
