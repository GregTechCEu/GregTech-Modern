package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleCoilType implements ICoilType, StringRepresentable {

    @Getter
    private final String name;
    // electric blast furnace properties
    @Getter
    private final int coilTemperature;
    // multi smelter properties
    @Getter
    private final int level;
    @Getter
    private final int tier;
    @Getter
    private final int energyDiscount;
    @NotNull
    private final Supplier<Material> material;
    @Getter
    private final ResourceLocation texture;

    public SimpleCoilType(String name, int coilTemperature, int level, int energyDiscount, int tier,
                          @NotNull Supplier<Material> material, ResourceLocation texture) {
        this.name = name;
        this.coilTemperature = coilTemperature;
        this.level = level;
        this.energyDiscount = energyDiscount;
        this.tier = tier;
        this.material = GTMemoizer.memoize(material);
        this.texture = texture;
    }

    @NotNull
    @Override
    public String toString() {
        return getName();
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name;
    }

    @Override
    public Material getMaterial() {
        return material.get();
    }
}
