package com.gregtechceu.gtceu.client.renderer.block;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.client.data.MultiplyComposite;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.utils.ResourceHelper;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote MaterialBlockRenderer
 */
public class MaterialBlockRenderer {
    private static final Table<MaterialIconType, MaterialIconSet, MaterialBlockRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    public static MaterialBlockRenderer getOrCreate(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new MaterialBlockRenderer(block, type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }

    public static void reinitModels() {
        for (MaterialBlockRenderer model : MODELS.values()) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            ResourceLocation blockTexture = GTDynamicResourcePack.getTextureLocation("block", blockId);
            GTDynamicResourcePack.addBlockState(blockId,
                    BlockModelGenerators.createSimpleBlock(model.block, ModelTemplates.CUBE_ALL.create(model.block, TextureMapping.cube(blockTexture), GTDynamicResourcePack::addBlockModel)));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()), new DelegatedModel(ModelLocationUtils.getModelLocation(model.block)));

            try {
                if (!(model.block instanceof MaterialBlock materialBlock)) continue;
                Resource file = Minecraft.getInstance().getResourceManager().getResource(GTDynamicResourcePack.getTextureLocation(null, model.type.getBlockTexturePath(model.iconSet, true))).orElse(null);
                if (file == null) continue;

                NativeImage image = NativeImage.read(file.open());
                for (int x = 0; x < image.getWidth(); ++x) {
                    for (int y = 0; y < image.getHeight(); ++y) {
                        image.blendPixel(x, y, GradientUtil.argbToABGR(materialBlock.material.getMaterialARGB()));
                    }
                }
                GTDynamicResourcePack.addBlockTexture(blockTexture, image.asByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Block block;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    protected MaterialBlockRenderer(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        this.block = block;
        this.type = type;
        this.iconSet = iconSet;
    }
}
