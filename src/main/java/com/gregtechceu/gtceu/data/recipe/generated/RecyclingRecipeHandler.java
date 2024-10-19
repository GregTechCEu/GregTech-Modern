package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public class RecyclingRecipeHandler {

    private static final List<Object> CRUSHING_PREFIXES = Arrays.asList(
            ingot, gem, rod, plate, ring, rodLong, foil, bolt, screw,
            nugget, gearSmall, gear, frameGt, plateDense, spring, springSmall,
            block, wireFine, rotor, lens, turbineBlade, round, plateDouble, dust,
            (Predicate<TagPrefix>) orePrefix -> orePrefix.name().startsWith("toolHead"),
            (Predicate<TagPrefix>) orePrefix -> orePrefix.name().contains("Gem"),
            (Predicate<TagPrefix>) orePrefix -> orePrefix.name().startsWith("cableGt"),
            (Predicate<TagPrefix>) orePrefix -> orePrefix.name().startsWith("wireGt"),
            (Predicate<TagPrefix>) orePrefix -> orePrefix.name().startsWith("pipe"));

    private static final List<TagPrefix> IGNORE_ARC_SMELTING = Arrays.asList(ingot, gem, nugget);

    public static void init(Consumer<FinishedRecipe> provider) {
        // registers universal maceration recipes for specified ore prefixes
        for (TagPrefix orePrefix : TagPrefix.values()) {
            if (CRUSHING_PREFIXES.stream().anyMatch(object -> {
                if (object instanceof TagPrefix)
                    return object == orePrefix;
                else if (object instanceof Predicate)
                    return ((Predicate<TagPrefix>) object).test(orePrefix);
                else return false;
            })) orePrefix.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
        }
    }

    public static void processCrushing(TagPrefix thingPrefix, Material material, DustProperty property,
                                       Consumer<FinishedRecipe> provider) {
        ArrayList<MaterialStack> materialStacks = new ArrayList<>();
        materialStacks.add(new MaterialStack(material, thingPrefix.getMaterialAmount(material)));
        materialStacks.addAll(thingPrefix.secondaryMaterials());
        // only ignore arc smelting for blacklisted prefixes if yielded material is the same as input material
        // if arc smelting gives different material, allow it
        boolean ignoreArcSmelting = IGNORE_ARC_SMELTING.contains(thingPrefix) &&
                !(material.hasProperty(PropertyKey.INGOT) &&
                        material.getProperty(PropertyKey.INGOT).getArcSmeltingInto() != material);
        RecyclingRecipes.registerRecyclingRecipes(provider, ChemicalHelper.get(thingPrefix, material), materialStacks,
                ignoreArcSmelting, thingPrefix);
    }
}
