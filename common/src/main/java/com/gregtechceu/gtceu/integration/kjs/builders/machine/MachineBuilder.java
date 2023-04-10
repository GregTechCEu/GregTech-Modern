package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public abstract class MachineBuilder extends BuilderBase<Block> {
    public transient String name;
    public transient String recipeType;

    public MachineBuilder(ResourceLocation i) {
        super(new ResourceLocation(GTCEu.MOD_ID, i.getPath()));
        this.name = i.getPath();
        this.recipeType = null;

        //this.dummyBuilder = true;
    }

    public MachineBuilder recipeType(String type) {
        this.recipeType = type;
        return this;
    }

    @Override
    public RegistryObjectBuilderTypes<? super Block> getRegistryType() {
        return RegistryObjectBuilderTypes.BLOCK;
    }
}
