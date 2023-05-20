package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.client.model.ItemBakedModel;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.client.bakedpipeline.FaceQuad;
import com.gregtechceu.gtlib.client.model.ModelFactory;
import com.gregtechceu.gtlib.client.renderer.IItemRendererProvider;
import com.gregtechceu.gtlib.client.renderer.impl.BlockStateRenderer;
import com.gregtechceu.gtlib.utils.BlockInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

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
    private ResourceLocation overlay;
    private final boolean emissive;

    public OreBlockRenderer(Supplier<BlockState> stone, ResourceLocation overlay, boolean emissive) {
        this.stone = stone;
        this.overlay = overlay;
        this.emissive = emissive;
        if (GTLib.isClient()) {
            registerEvent();
        }
    }

    public void setOverlayTexture(ResourceLocation newOverlay) {
        this.overlay = newOverlay;
    }

    @Override
    public BlockInfo getBlockInfo() {
        return new BlockInfo(stone.get());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderItem(ItemStack stack, ItemTransforms.TransformType transformType, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        super.renderItem(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay, model);
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay,
                (ItemBakedModel) (state, direction, random) -> {
                    List<BakedQuad> quads = new LinkedList<>();
                    if (direction != null) {
                        quads.add(FaceQuad.bakeFace(direction, ModelFactory.getBlockSprite(overlay), BlockModelRotation.X0_Y0, 1, emissive ? 15 : 0, true, !emissive));
                    }
                    return quads;
                });
        IItemRendererProvider.disabled.set(false);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand) {
        List<BakedQuad> quads = new LinkedList<>(super.renderModel(level, pos, state, side, rand));
        if (side != null) {
            quads.add(FaceQuad.bakeFace(side, ModelFactory.getBlockSprite(overlay), BlockModelRotation.X0_Y0, 1, emissive ? 15 : 0, true, !emissive));
        }
        return quads;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(overlay);
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
