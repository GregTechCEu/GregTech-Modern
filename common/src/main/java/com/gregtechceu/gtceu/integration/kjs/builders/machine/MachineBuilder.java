package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.CommonProperties;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public abstract class MachineBuilder extends BlockBuilder {
    public transient String name;
    public transient GTRecipeType recipeType;

    public MachineBuilder(ResourceLocation i) {
        super(new ResourceLocation(GTCEu.MOD_ID, i.getPath()));
        this.name = i.getPath();
        this.recipeType = GTRegistries.RECIPE_TYPES.get(i);
    }

    public MachineBuilder recipeType(GTRecipeType type) {
        this.recipeType = type;
        return this;
    }

}
