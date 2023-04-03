package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import dev.latvian.mods.kubejs.KubeJSPlugin;
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
        GTCEuServerEvents.GROUP.register();
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
        event.add("GTSoundEntries", GTSoundEntries.class);
        event.add("GTBlocks", GTBlocks.class);
        event.add("GTMachines", GTMachines.class);
        event.add("GTItems", GTItems.class);
        event.add("GTMaterialBuilder", Material.Builder.class);
        event.add("GTRecipeTypes", GTRecipeTypes.class);
        event.add("TagPrefix", TagPrefix.class);
        event.add("UnificationEntry", UnificationEntry.class);
        event.add("RecipeCapability", RecipeCapability.class);

        event.add("GTValues", GTValues.class);
        event.add("GTMaterialIconSet", MaterialIconSet.class);
        event.add("GTMaterialFlags", MaterialFlags.class);
        event.add("GTToolType", GTToolType.class);
        event.add("RotationState", RotationState.class);
        event.add("FactoryBlockPattern", FactoryBlockPattern.class);
        event.add("MultiblockShapeInfo", MultiblockShapeInfo.class);
        event.add("Predicates", Predicates.class);
        event.add("PartAbility", PartAbility.class);
        event.add("GuiTextures", GuiTextures.class);
        event.add("GTCEu", GTCEu.class);

        // ....TODO add global refs. for convenience, ppl do not need to import the java package themselves.
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        super.registerTypeWrappers(type, typeWrappers);
        typeWrappers.register(GTRecipeType.class, (ctx, o) -> {
            if (o instanceof GTRecipeType recipeType) return recipeType;
            if (o instanceof CharSequence chars) return GTRecipeTypes.get(chars.toString());
            return null;
        });
        typeWrappers.register(Element.class, (ctx, o) -> {
            if (o instanceof Element element) return element;
            if (o instanceof CharSequence chars) return GTElements.get(chars.toString());
            return null;
        });
        typeWrappers.register(Material.class, (ctx, o) -> {
            if (o instanceof Material material) return material;
            if (o instanceof CharSequence chars) return GTMaterials.get(chars.toString());
            return null;
        });
        typeWrappers.register(MachineDefinition.class, (ctx, o) -> {
            if (o instanceof MachineDefinition definition) return definition;
            if (o instanceof CharSequence chars) return GTMachines.get(chars.toString());
            return null;
        });
        typeWrappers.register(TagPrefix.class, (ctx, o) -> {
            if (o instanceof TagPrefix tagPrefix) return tagPrefix;
            if (o instanceof CharSequence chars) return TagPrefix.getPrefix(chars.toString());
            return null;
        });
        typeWrappers.register(UnificationEntry.class, (ctx, o) -> {
            if (o instanceof UnificationEntry entry) return entry;
            if (o instanceof CharSequence chars) {
                var values = chars.toString().split(":");
                if (values.length == 1) {
                    return new UnificationEntry(TagPrefix.getPrefix(values[0]));
                }
                if (values.length >= 2) {
                    return new UnificationEntry(TagPrefix.getPrefix(values[0]), GTMaterials.get(values[1]));
                }
            }
            return null;
        });
        typeWrappers.register(RecipeCapability.class, (ctx, o) -> {
            if (o instanceof RecipeCapability<?> capability) return capability;
            if (o instanceof CharSequence chars) return GTRegistries.RECIPE_CAPABILITIES.get(chars.toString());
            return null;
        });

    }

}
