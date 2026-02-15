package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.SimpleCoilType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockRenderType;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class CoilBlockBuilder extends ActiveBlockBuilder {

    @Setter
    public transient int temperature = 0, level = 0, energyDiscount = 1, tier = 0;
    @Setter
    public transient Supplier<Material> material = () -> GTMaterials.NULL;
    @Setter
    public transient String texture = "minecraft:missingno";

    public CoilBlockBuilder(ResourceLocation i) {
        super(GTResourceLocation.implicitAsGtceu(i));
        property(GTBlockStateProperties.ACTIVE);
        renderType(BlockRenderType.CUTOUT_MIPPED);
        noValidSpawns(true);
        type = Type.CUSTOM;
    }

    @HideFromJS
    @Override
    public ActiveBlockBuilder simple(String base) {
        return this;
    }

    @HideFromJS
    @Override
    public ActiveBlockBuilder bloom(String base) {
        return this;
    }

    @HideFromJS
    @Override
    public ActiveBlockBuilder firebox(String bottom, String side, String top) {
        return this;
    }

    @Override
    protected void generateBlockModels(KubeAssetGenerator generator) {
        if (modelGenerator != null) {
            superGenerateBlockModels(generator);
            return;
        }
        generator.blockModel(id, m -> {
            m.parent(KubeAssetGenerator.CUBE_ALL_BLOCK_MODEL);
            m.texture("all", texture);
        });
        generator.blockModel(id.withPath(ActiveBlockBuilder.ACTIVE), m -> {
            m.parent(ActiveBlockBuilder.CUBE_2_LAYER_ALL_MODEL);
            m.texture("bot_all", texture);
            m.texture("top_all", ActiveBlockBuilder.BLOOM.apply(texture));
        });
    }

    @Override
    public Block createObject() {
        SimpleCoilType coilType = new SimpleCoilType(this.id.getPath(),
                temperature, level, energyDiscount, tier,
                material, ResourceLocation.parse(texture));
        CoilBlock result = new CoilBlock(this.createProperties(), coilType);
        GTCEuAPI.HEATING_COILS.put(coilType, () -> result);
        return result;
    }
}
