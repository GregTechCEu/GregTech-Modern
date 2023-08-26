package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.block.SimpleCoilType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.BlockItemBuilder;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class CoilBlockBuilder extends BlockBuilder {
    public transient int temperature = 0, level = 0, energyDiscount = 1, tier = 0;
    public transient Material material = GTMaterials.Air;
    public transient ResourceLocation texture = MissingTextureAtlasSprite.getLocation();

    public CoilBlockBuilder(ResourceLocation i) {
        super(i);
    }

    public CoilBlockBuilder temperature(int temperature) {
        this.temperature = temperature;
        return this;
    }

    public CoilBlockBuilder level(int level) {
        this.level = level;
        return this;
    }

    public CoilBlockBuilder energyDiscount(int energyDiscount) {
        this.energyDiscount = energyDiscount;
        return this;
    }

    public CoilBlockBuilder tier(int tier) {
        this.tier = tier;
        return this;
    }

    public CoilBlockBuilder coilMaterial(Material material) {
        this.material = material;
        return this;
    }

    public CoilBlockBuilder texture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {

    }

    @Override
    protected BlockItemBuilder getOrCreateItemBuilder() {
        return itemBuilder == null ? (itemBuilder = new RendererBlockItemBuilder(id)) : itemBuilder;
    }

    @Override
    public Block createObject() {
        SimpleCoilType coilType = new SimpleCoilType(this.id.getPath(), temperature, level, energyDiscount, tier, material, texture);
        CoilBlock result = new CoilBlock(this.createProperties(), coilType);
        GTBlocks.ALL_COILS.put(coilType, () -> result);
        return result;
    }
}
