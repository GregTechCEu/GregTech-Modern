package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public abstract class MachineBuilder extends BuilderBase<MachineDefinition> {
    public transient String name;
    public transient GTRecipeType recipeType;

    public MachineBuilder(ResourceLocation i) {
        super(i);
        this.name = i.getPath();
        this.recipeType = GTRegistries.RECIPE_TYPES.get(i);
    }

    public MachineBuilder recipeType(GTRecipeType type) {
        this.recipeType = type;
        return this;
    }

    @Override
    public RegistryObjectBuilderTypes<? super MachineDefinition> getRegistryType() {
        return GregTechKubeJSPlugin.MACHINE;
    }
}
