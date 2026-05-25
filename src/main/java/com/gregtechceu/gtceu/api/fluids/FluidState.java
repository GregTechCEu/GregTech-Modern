package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum FluidState {

    LIQUID("tooltip.gtceu.fluid_state.liquid", CustomTags.LIQUID_FLUIDS),
    GAS("tooltip.gtceu.fluid_state.gas", Tags.Fluids.GASEOUS),
    PLASMA("tooltip.gtceu.fluid_state.plasma", CustomTags.PLASMA_FLUIDS),
    ;

    @Getter
    private final String translationKey;
    @Getter
    private final TagKey<Fluid> tagKey;

    FluidState(@NotNull String translationKey, @NotNull TagKey<Fluid> tagKey) {
        this.translationKey = translationKey;
        this.tagKey = tagKey;
    }
}
