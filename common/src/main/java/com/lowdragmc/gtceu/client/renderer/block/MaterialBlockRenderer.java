package com.lowdragmc.gtceu.client.renderer.block;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.lowdragmc.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.lowdragmc.gtceu.core.mixins.BlockModelAccessor;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote MaterialBlockRenderer
 */
public class MaterialBlockRenderer extends IModelRenderer {
    private static final Table<MaterialIconType, MaterialIconSet, MaterialBlockRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    public static MaterialBlockRenderer getOrCreate(MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new MaterialBlockRenderer(type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }

    private final ResourceLocation blockTexture;

    protected MaterialBlockRenderer(MaterialIconType type, MaterialIconSet iconSet) {
        super(GTCEu.id("block/tinted_cube_all"));
        this.blockTexture = type.getBlockTexturePath(iconSet);
    }

    @Nullable
    @Override
    protected BakedModel getBlockBakedModel(BlockPos pos, BlockAndTintGetter blockAccess) {
        return super.getBlockBakedModel(pos, blockAccess);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected UnbakedModel getModel() {
        var model = super.getModel();
        if (model instanceof BlockModelAccessor blockModelAccessor) {
            blockModelAccessor.getTextureMap().put("all", ModelFactory.parseBlockTextureLocationOrReference(blockTexture.toString()));
        }
        return model;
    }

    @Override
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(blockTexture);
        }
    }

    @Override
    public boolean useAO() {
        return true;
    }
}
