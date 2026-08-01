package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class GTLiquidBlock extends LiquidBlock {

    private final Material material;
    private final String langKey;

    public GTLiquidBlock(Supplier<? extends FlowingFluid> fluidSupplier, Properties properties, Material material,
                         String langKey) {
        super(fluidSupplier, properties);
        this.material = material;
        this.langKey = langKey;
    }

    @Override
    public String getDescriptionId() {
        return langKey;
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable(this.langKey, material.getLocalizedName());
    }
}
