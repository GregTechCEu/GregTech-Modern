package com.gregtechceu.gtceu.client.renderer.pipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.client.renderer.pipe.util.MaterialModelSupplier;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class PipeModelRedirector implements BakedModel {

    private final boolean ambientOcclusion;
    private final boolean gui3d;

    public final MaterialModelSupplier supplier;
    public final Function<ItemStack, Material> stackMaterialFunction;

    @Getter
    private final ModelResourceLocation loc;

    private final FakeItemOverrides fakeItemOverrideList = new FakeItemOverrides();

    public PipeModelRedirector(ModelResourceLocation loc, MaterialModelSupplier supplier,
                               Function<ItemStack, Material> stackMaterialFunction) {
        this(loc, supplier, stackMaterialFunction, true, true);
    }

    public PipeModelRedirector(ModelResourceLocation loc, MaterialModelSupplier supplier,
                               Function<ItemStack, Material> stackMaterialFunction,
                               boolean ambientOcclusion, boolean gui3d) {
        this.loc = loc;
        this.supplier = supplier;
        this.stackMaterialFunction = stackMaterialFunction;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;

        PipeModelRegistry.MODELS.put(loc, this);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return List.of();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData data,
                                             @Nullable RenderType renderType) {
        Material mat = data.get(AbstractPipeModel.MATERIAL_PROPERTY);
        return supplier.getModel(mat).getQuads(state, side, rand, data, renderType);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return gui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return ModelFactory.getBlockSprite(GTCEu.id("block/cable/wire"));
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return fakeItemOverrideList;
    }

    @FunctionalInterface
    public interface Supplier {

        PipeModelRedirector create(ModelResourceLocation loc, MaterialModelSupplier supplier,
                                   Function<ItemStack, Material> stackMaterialFunction);
    }

    protected class FakeItemOverrides extends ItemOverrides {

        @Nullable
        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level,
                                  @Nullable LivingEntity entity, int seed) {
            if (originalModel instanceof PipeModelRedirector model) {
                PipeItemModel<?> item = model.supplier.getModel(model.stackMaterialFunction.apply(stack))
                        .getItemModel(PipeModelRedirector.this, stack, level, entity);
                if (item != null) return item;
            }
            return originalModel;
        }
    }
}
