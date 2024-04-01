package com.gregtechceu.gtceu.integration;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class GTOreByProduct {

    private static final List<TagPrefix> ORES = new ArrayList<>();

    public static void addOreByProductPrefix(TagPrefix orePrefix) {
        if (!ORES.contains(orePrefix)) {
            ORES.add(orePrefix);
        }
    }

    private static ImmutableList<TagPrefix> IN_PROCESSING_STEPS;

    private static ImmutableList<ItemStack> ALWAYS_MACHINES;

    private final Int2ObjectMap<Content> chances = new Int2ObjectOpenHashMap<>();
    protected final List<List<ItemStack>> itemInputs = new ArrayList<>();
    protected final List<List<ItemStack>> itemOutputs = new ArrayList<>();
    protected final List<List<FluidStack>> fluidInputs = new ArrayList<>();
    private boolean hasDirectSmelt = false;
    private boolean hasChemBath = false;
    private boolean hasSeparator = false;
    private boolean hasSifter = false;
    private int currentSlot;

    public GTOreByProduct(Material material) {
        if (IN_PROCESSING_STEPS == null) {
            IN_PROCESSING_STEPS = ImmutableList.of(
                TagPrefix.crushed,
                TagPrefix.crushedPurified,
                TagPrefix.dustImpure,
                TagPrefix.dustPure,
                TagPrefix.crushedRefined
            );
        }
        if (ALWAYS_MACHINES == null) {
            ALWAYS_MACHINES = ImmutableList.of(
                GTMachines.MACERATOR[GTValues.LV].asStack(),
                GTMachines.MACERATOR[GTValues.LV].asStack(),
                GTMachines.CENTRIFUGE[GTValues.LV].asStack(),
                GTMachines.ORE_WASHER[GTValues.LV].asStack(),
                GTMachines.THERMAL_CENTRIFUGE[GTValues.LV].asStack(),
                GTMachines.MACERATOR[GTValues.LV].asStack(),
                GTMachines.MACERATOR[GTValues.LV].asStack(),
                GTMachines.CENTRIFUGE[GTValues.LV].asStack());
        }
        OreProperty property = material.getProperty(PropertyKey.ORE);
        int oreMultiplier = property.getOreMultiplier();
        int byproductMultiplier = property.getByProductMultiplier();
        currentSlot = 0;
        Material[] byproducts = new Material[] {
            property.getOreByProduct(0, material),
            property.getOreByProduct(1, material),
            property.getOreByProduct(2, material),
            property.getOreByProduct(3, material)
        };

        // "INPUTS"

        Pair<Material, Integer> washedIn = property.getWashedIn();
        List<Material> separatedInto = property.getSeparatedInto();

        List<ItemStack> oreStacks = new ArrayList<>();
        for (TagPrefix prefix : ORES) {
            // get all ores with the relevant oredicts instead of just the first unified ore
            oreStacks.addAll(BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(prefix, material)).stream().map(Holder::value).map(ItemStack::new).toList());
        }
        itemInputs.add(oreStacks);

        // set up machines as inputs
        List<ItemStack> simpleWashers = new ArrayList<>();
        simpleWashers.add(new ItemStack(Items.CAULDRON));
        simpleWashers.add(GTMachines.ORE_WASHER[GTValues.LV].asStack());

        if (!material.hasProperty(PropertyKey.BLAST)) {
            addToInputs(new ItemStack(Blocks.FURNACE));
            hasDirectSmelt = true;
        } else {
            addToInputs(ItemStack.EMPTY);
        }

        for (ItemStack stack : ALWAYS_MACHINES) {
            addToInputs(stack);
        }
        // same amount of lines as a for loop :trol:
        itemInputs.add(simpleWashers);
        itemInputs.add(simpleWashers);
        itemInputs.add(simpleWashers);

        if (washedIn != null && washedIn.getKey() != null) {
            hasChemBath = true;
            addToInputs(GTMachines.CHEMICAL_BATH[GTValues.LV].asStack());
        } else {
            addToInputs(ItemStack.EMPTY);
        }
        if (separatedInto != null && !separatedInto.isEmpty()) {
            hasSeparator = true;
            addToInputs(GTMachines.ELECTROMAGNETIC_SEPARATOR[GTValues.LV].asStack());
        } else {
            addToInputs(ItemStack.EMPTY);
        }
        if (material.hasProperty(PropertyKey.GEM)) {
            hasSifter = true;
            addToInputs(GTMachines.SIFTER[GTValues.LV].asStack());
        } else {
            addToInputs(ItemStack.EMPTY);
        }

        // add prefixes that should count as inputs to input lists (they will not be displayed in actual page)
        for (TagPrefix prefix : IN_PROCESSING_STEPS) {
            List<ItemStack> tempList = new ArrayList<>();
            tempList.add(ChemicalHelper.get(prefix, material));
            itemInputs.add(tempList);
        }

        // total number of inputs added
        currentSlot += 21;

        // BASIC PROCESSING

        // begin lots of logic duplication from OreRecipeHandler
        // direct smelt
        if (hasDirectSmelt) {
            ItemStack smeltingResult;
            Material smeltingMaterial = property.getDirectSmeltResult() == null ? material :
                property.getDirectSmeltResult();
            if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
                smeltingResult = ChemicalHelper.get(TagPrefix.ingot, smeltingMaterial);
            } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
                smeltingResult = ChemicalHelper.get(TagPrefix.gem, smeltingMaterial);
            } else {
                smeltingResult = ChemicalHelper.get(TagPrefix.dust, smeltingMaterial);
            }
            smeltingResult.setCount(smeltingResult.getCount() * oreMultiplier);
            addToOutputs(smeltingResult);
        } else {
            addEmptyOutputs(1);
        }

        // macerate ore -> crushed
        addToOutputs(material, TagPrefix.crushed, 2 * oreMultiplier);
        if (!ChemicalHelper.get(TagPrefix.gem, byproducts[0]).isEmpty()) {
            addToOutputs(byproducts[0], TagPrefix.gem, 1);
        } else {
            addToOutputs(byproducts[0], TagPrefix.dust, 1);
        }
        addChance(1400, 850);

        // macerate crushed -> impure
        addToOutputs(material, TagPrefix.dustImpure, 1);
        addToOutputs(byproducts[0], TagPrefix.dust, byproductMultiplier);
        addChance(1400, 850);

        // centrifuge impure -> dust
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(byproducts[0], TagPrefix.dust, 1);
        addChance(1111, 0);

        // ore wash crushed -> crushed purified
        addToOutputs(material, TagPrefix.crushedPurified, 1);
        addToOutputs(byproducts[0], TagPrefix.dust, 1);
        addChance(3333, 0);
        List<FluidStack> fluidStacks = new ArrayList<>();
        fluidStacks.add(GTMaterials.Water.getFluid(1000));
        fluidStacks.add(GTMaterials.DistilledWater.getFluid(100));
        fluidInputs.add(fluidStacks);

        // TC crushed/crushed purified -> centrifuged
        addToOutputs(material, TagPrefix.crushedRefined, 1);
        addToOutputs(byproducts[1], TagPrefix.dust, byproductMultiplier);
        addChance(3333, 0);

        // macerate centrifuged -> dust
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(byproducts[2], TagPrefix.dust, 1);
        addChance(1400, 850);

        // macerate crushed purified -> purified
        addToOutputs(material, TagPrefix.dustPure, 1);
        addToOutputs(byproducts[1], TagPrefix.dust, 1);
        addChance(1400, 850);

        // centrifuge purified -> dust
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(byproducts[1], TagPrefix.dust, 1);
        addChance(1111, 0);

        // cauldron/simple washer
        addToOutputs(material, TagPrefix.crushed, 1);
        addToOutputs(material, TagPrefix.crushedPurified, 1);
        addToOutputs(material, TagPrefix.dustImpure, 1);
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(material, TagPrefix.dustPure, 1);
        addToOutputs(material, TagPrefix.dust, 1);

        // ADVANCED PROCESSING

        // chem bath
        if (hasChemBath) {
            addToOutputs(material, TagPrefix.crushedPurified, 1);
            addToOutputs(byproducts[3], TagPrefix.dust, byproductMultiplier);
            addChance(7000, 580);
            List<FluidStack> washedFluid = new ArrayList<>();
            washedFluid.add(washedIn.getKey().getFluid(washedIn.getValue()));
            fluidInputs.add(washedFluid);
        } else {
            addEmptyOutputs(2);
            List<FluidStack> washedFluid = new ArrayList<>();
            fluidInputs.add(washedFluid);
        }

        // electromagnetic separator
        if (hasSeparator) {
            TagPrefix prefix = (separatedInto.get(separatedInto.size() - 1).getBlastTemperature() == 0 &&
                separatedInto.get(separatedInto.size() - 1).hasProperty(PropertyKey.INGOT)) ? TagPrefix.nugget :
                TagPrefix.dust;
            ItemStack separatedStack2 = ChemicalHelper.get(prefix, separatedInto.get(separatedInto.size() - 1),
                prefix == TagPrefix.nugget ? 2 : 1);

            addToOutputs(material, TagPrefix.dust, 1);
            addToOutputs(separatedInto.get(0), TagPrefix.dust, 1);
            addChance(1000, 250);
            addToOutputs(separatedStack2);
            addChance(prefix == TagPrefix.dust ? 500 : 2000, prefix == TagPrefix.dust ? 150 : 600);
        } else {
            addEmptyOutputs(3);
        }

        // sifter
        if (hasSifter) {
            boolean highOutput = material.hasFlag(MaterialFlags.HIGH_SIFTER_OUTPUT);
            ItemStack flawedStack = ChemicalHelper.get(TagPrefix.gemFlawed, material);
            ItemStack chippedStack = ChemicalHelper.get(TagPrefix.gemChipped, material);

            addToOutputs(material, TagPrefix.gemExquisite, 1);
            addGemChance(300, 100, 500, 150, highOutput);
            addToOutputs(material, TagPrefix.gemFlawless, 1);
            addGemChance(1000, 150, 1500, 200, highOutput);
            addToOutputs(material, TagPrefix.gem, 1);
            addGemChance(3500, 500, 5000, 1000, highOutput);
            addToOutputs(material, TagPrefix.dustPure, 1);
            addGemChance(5000, 750, 2500, 500, highOutput);

            if (!flawedStack.isEmpty()) {
                addToOutputs(flawedStack);
                addGemChance(2500, 300, 2000, 500, highOutput);
            } else {
                addEmptyOutputs(1);
            }
            if (!chippedStack.isEmpty()) {
                addToOutputs(chippedStack);
                addGemChance(3500, 400, 3000, 350, highOutput);
            } else {
                addEmptyOutputs(1);
            }
        } else {
            addEmptyOutputs(6);
        }
    }

    public List<Component> getTooltip(int slotIndex) {
        List<Component> tooltips = new ArrayList<>();
        if (chances.containsKey(slotIndex)) {
            Content entry = chances.get(slotIndex);
            float chance = entry.chance * 100;
            float boost = entry.tierChanceBoost * 100;
            tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_1", chance));
            tooltips.add(FormattingUtil.formatPercentage2Places("gtceu.gui.content.tier_boost", boost));
        }
        return tooltips;
    }

    public Content getChance(int slot) {
        return chances.get(slot);
    }

    public boolean hasSifter() {
        return hasSifter;
    }

    public boolean hasSeparator() {
        return hasSeparator;
    }

    public boolean hasChemBath() {
        return hasChemBath;
    }

    public boolean hasDirectSmelt() {
        return hasDirectSmelt;
    }

    private void addToOutputs(Material material, TagPrefix prefix, int size) {
        addToOutputs(ChemicalHelper.get(prefix, material, size));
    }

    private void addToOutputs(ItemStack stack) {
        List<ItemStack> tempList = new ArrayList<>();
        tempList.add(stack);
        itemOutputs.add(tempList);
        currentSlot++;
    }

    private void addEmptyOutputs(int amount) {
        for (int i = 0; i < amount; i++) {
            addToOutputs(ItemStack.EMPTY);
        }
    }

    private void addToInputs(ItemStack stack) {
        List<ItemStack> tempList = new ArrayList<>();
        tempList.add(stack);
        itemInputs.add(tempList);
    }

    private void addChance(int base, int tier) {
        // this is solely for the chance overlay and tooltip, neither of which care about the ItemStack
        chances.put(currentSlot - 1, new Content(ItemStack.EMPTY, base / 10000f, tier / 10000f, null, null));
    }

    // make the code less :weary:
    private void addGemChance(int baseLow, int tierLow, int baseHigh, int tierHigh, boolean high) {
        if (high) {
            addChance(baseHigh, tierHigh);
        } else {
            addChance(baseLow, tierLow);
        }
    }
}
