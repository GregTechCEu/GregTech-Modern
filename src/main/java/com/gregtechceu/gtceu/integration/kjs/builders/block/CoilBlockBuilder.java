package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.block.SimpleCoilType;
import com.gregtechceu.gtceu.api.chemical.material.Material;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.kjs.builders.RendererBlockItemBuilder;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.BlockItemBuilder;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class CoilBlockBuilder extends BlockBuilder {
    @Setter
    public transient int temperature = 0, level = 0, energyDiscount = 1, tier = 0;
    @NotNull
    public transient Supplier<@Nullable Material> material = () -> null;
    @Setter
    public transient ResourceLocation texture = new ResourceLocation("missingno");

    public CoilBlockBuilder(ResourceLocation i) {
        super(i);
    }

    public CoilBlockBuilder coilMaterial(@NotNull Supplier<@Nullable Material> material) {
        this.material = material;
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
