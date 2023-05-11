package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public abstract class MachineBuilder extends BuilderBase<MachineDefinition> {
    public transient String name;
    public transient GTRecipeType recipeType;

    public MachineBuilder(ResourceLocation i, Object... args) {
        super(new ResourceLocation(GTCEu.MOD_ID, i.getPath()));
        this.name = i.getPath();
        this.recipeType = null /*GTRecipeTypes.get(args[0].toString())*/;
    }

    public MachineBuilder recipeType(GTRecipeType type) {
        this.recipeType = type;
        return this;
    }
}
