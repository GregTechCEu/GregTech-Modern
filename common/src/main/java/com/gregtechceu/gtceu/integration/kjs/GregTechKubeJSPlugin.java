package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GregTechKubeJSPlugin
 */
public class GregTechKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerEvents() {
        super.registerEvents();
        GTCEuStartupEvents.GROUP.register();
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        super.registerClasses(type, filter);
        // allow user to access all gtceu classes by importing them.
        filter.allow("com.gregtechceu.gtceu");
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("GTRegistries", GTRegistries.class);
        event.add("GTMaterials", GTMaterials.class);
        event.add("GTElements", GTElements.class);
        event.add("GTMaterialBuilder", Material.Builder.class);
        event.add("GTRecipeTypes", GTRecipeTypes.class);
        event.add("GTMaterialIconSet", MaterialIconSet.class);
        event.add("GTMaterialFlags", MaterialFlags.class);
        event.add("GTToolType", GTToolType.class);
        // ....TODO add global refs. for convenience, ppl do not need to import the java package themselves.
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        super.registerTypeWrappers(type, typeWrappers);
        // TODO probably it should be moved to ldlib
        typeWrappers.register(FluidStack.class, (ctx, o) -> {
            var fluidStack = FluidStackJS.of(o).getFluidStack();
            return FluidStack.create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
        });
    }

}
