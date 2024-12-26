package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.SimpleCoilType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
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
    public Block createObject() {
        SimpleCoilType coilType = new SimpleCoilType(this.id.getPath(), temperature, level, energyDiscount, tier,
                material, texture);
        CoilBlock result = new CoilBlock(this.createProperties(), coilType);
        GTCEuAPI.HEATING_COILS.put(coilType, () -> result);
        return result;
    }
}
