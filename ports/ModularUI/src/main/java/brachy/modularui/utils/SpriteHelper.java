package brachy.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpriteHelper {
    public static TextureAtlasSprite getSpriteOfBlockState(BlockState blockState, @Nullable Direction facing) {
        return getBestTexture(blockModel(blockState), blockState, facing);
    }

    public static List<BakedQuad> getQuadsOfBlockState(BlockState blockState, @Nullable Direction facing) {
        return getQuads(blockModel(blockState), facing);
    }

    private static BlockStateModel blockModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    // This context-free helper has no level or block position. Callers needing model data must collect
    // parts themselves with BlockStateModel.collectParts(level, pos, state, random, parts).
    @SuppressWarnings("deprecation")
    private static List<BakedQuad> getQuads(BlockStateModel model, @Nullable Direction facing) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42), parts);
        return parts.stream().flatMap(part -> part.getQuads(facing).stream()).toList();
    }

    public static TextureAtlasSprite getBestTexture(BlockStateModel model, @Nullable BlockState blockState,
                                                     @Nullable Direction facing) {
        List<BakedQuad> quads = getQuads(model, facing);
        return quads.isEmpty() ? missingSprite() : quads.getFirst().materialInfo().sprite();
    }

    public static TextureAtlasSprite getSpriteOfItem(ItemStack item) {
        CollectedItemState state = resolveItem(item);
        List<BakedQuad> quads = state.quads();
        if (!quads.isEmpty()) return quads.getFirst().materialInfo().sprite();
        var particle = state.pickParticleMaterial(RandomSource.create(42));
        return particle == null ? missingSprite() : particle.sprite();
    }

    public static List<BakedQuad> getQuadsOfItem(ItemStack item) {
        return resolveItem(item).quads();
    }

    private static CollectedItemState resolveItem(ItemStack item) {
        Minecraft minecraft = Minecraft.getInstance();
        CollectedItemState state = new CollectedItemState();
        minecraft.getItemModelResolver().updateForTopItem(state, item, ItemDisplayContext.GUI,
                minecraft.level, minecraft.player, 0);
        return state;
    }

    private static TextureAtlasSprite missingSprite() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(MissingTextureAtlasSprite.getLocation());
    }

    /** Collects every model layer, rather than assuming an item has one baked model. */
    private static final class CollectedItemState extends ItemStackRenderState {
        private final List<LayerRenderState> collectedLayers = new ArrayList<>();

        @Override
        public LayerRenderState newLayer() {
            LayerRenderState layer = super.newLayer();
            collectedLayers.add(layer);
            return layer;
        }

        @Override
        public void clear() {
            super.clear();
            collectedLayers.clear();
        }

        List<BakedQuad> quads() {
            return collectedLayers.stream().flatMap(layer -> layer.prepareQuadList().stream()).toList();
        }
    }
}
