package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.crushed;
import static com.gregtechceu.gtceu.common.data.GTRecipeCategories.MACERATOR_RECYCLING;
import static com.gregtechceu.gtceu.common.data.GTRecipeCategories.ORE_CRUSHING;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;

public enum MaceratorLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var recipeHandlers = holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        for (var handler : recipeHandlers) {
            for (var content : handler.getContents()) {
                if (!(content instanceof ItemStack stack)) continue;
                if (stack.isEmpty()) continue;
                var recipe = search(stack);
                if (recipe != null) return recipe;
            }
        }
        return null;
    }

    public @Nullable GTRecipe search(ItemStack stack) {
        if (stack.getItem() instanceof MaterialBlockItem blockItem) {
            var oreTag = blockItem.tagPrefix;
            var mat = blockItem.material;

            var oreProperty = mat.getProperty(PropertyKey.ORE);

            return buildOreRecipe("macerate_", stack, mat, oreTag, oreProperty);
        }

        var turbineBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
        if (turbineBehaviour != null) {
            float durability = 1.f - (float) turbineBehaviour.getPartDamage(stack) /
                    (float) turbineBehaviour.getPartMaxDurability(stack);
            return applyDurabilityRecipe("rotor_decomp", stack, turbineBehaviour.getPartMaterial(stack),
                    (float) (turbineBlade.materialAmount() * 8) / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        }

        if (stack.getItem() instanceof IGTTool tool && !tool.isElectric()) {
            float durability = (float) (tool.getTotalMaxDurability(stack) - stack.getDamageValue() + 1) /
                    (tool.getTotalMaxDurability(stack) + 1);
            return applyDurabilityRecipe("tool_decomp", stack, tool.getMaterial(),
                    (float) (tool.getToolType().materialAmount / GTValues.M), durability,
                    GTValues.VH[GTValues.LV], 2);
        }

        return null;
    }

    public @Nullable GTRecipe applyDurabilityRecipe(String id, ItemStack inputStack, @NotNull Material mat,
                                                    float fullAmount, float durability, long voltage,
                                                    int durationFactor) {
        float outputAmount = (durability * fullAmount);
        int dustAmount = (int) outputAmount;
        int leftover = (int) ((outputAmount - (float) dustAmount) * 36.f);
        TagPrefix tag = leftover % 4 >= leftover % 9 ? dustSmall : dustTiny;
        int leftAmount = leftover % 4 >= leftover % 9 ? leftover / 9 : leftover / 4;

        if (dustAmount == 0 && leftAmount == 0) return null;

        var builder = MACERATOR_RECIPES.recipeBuilder(id + "/" + mat.getName())
                .inputItems(inputStack)
                .EUt(voltage)
                .duration((int) (mat.getMass() * outputAmount) * durationFactor);

        if (dustAmount > 0) {
            builder.outputItems(dust, mat, dustAmount);
        }
        if (leftAmount > 0) {
            builder.outputItems(tag, mat, leftAmount);
        }

        return builder.buildRawRecipe();
    }

    public static @Nullable GTRecipe buildOreRecipe(String id, ItemStack inputStack, Material material,
                                             TagPrefix stoneTypePrefix, OreProperty property) {
        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = ChemicalHelper.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) {
            byproductStack = ChemicalHelper.get(dust, byproductMaterial);
        }

        Material smeltingMaterial = property.getDirectSmeltResult().isNull() ? material :
                property.getDirectSmeltResult();
        ItemStack ingotStack;
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }

        int oreTypeMultiplier = TagPrefix.ORES.get(stoneTypePrefix).isDoubleDrops() ? 2 : 1;
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * oreTypeMultiplier);

        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        //crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        String prefixString = stoneTypePrefix == ore ? "" : stoneTypePrefix.name + "_";

        if(crushedStack.isEmpty()) return null;
        int crushedAmount = property.getOreMultiplier() * 2 * oreTypeMultiplier;
        var recipe = MACERATOR_RECIPES.recipeBuilder(id + prefixString + material.getName() + "_ore_to_crushed_ore")
                .inputItems(inputStack.copyWithCount(1))
                .outputItems(crushedStack.copyWithCount(crushedAmount))
                .chancedOutput(byproductStack, 1400, 0)
                .EUt(2)
                .duration(400)
                .category(GTRecipeCategories.ORE_CRUSHING);

        for (MaterialStack secondaryMaterial : stoneTypePrefix.secondaryMaterials()) {
            if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                recipe.chancedOutput(dustStack, 6700, 0);
            }
        }

        return recipe.buildRawRecipe();
    }

    @Override
    public void buildRepresentativeRecipes() {
        ItemStack stack = GTItems.TURBINE_ROTOR.asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.rotor"));
        GTRecipe rotorRecipe;
        GTRecipe pickaxeRecipe;
        float durability = 0.75f;
        var turbineBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
        assert turbineBehaviour != null : "Default Turbine Stack doesn't have Turbine Behaviour";
        turbineBehaviour.setPartMaterial(stack, GTMaterials.Iron);
        turbineBehaviour.setPartDamage(stack, 8928);

        rotorRecipe = applyDurabilityRecipe("rotor_decomp", stack, turbineBehaviour.getPartMaterial(stack),
                (float) (turbineBlade.materialAmount() * 8) / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        assert rotorRecipe != null : "Default Turbine Decomp recipe couldn't be generated";
        rotorRecipe.setId(rotorRecipe.getId().withPrefix("/"));

        // noinspection DataFlowIssue
        stack = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron, GTToolType.PICKAXE).asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.tool"));
        stack.setDamageValue(79);
        pickaxeRecipe = applyDurabilityRecipe("tool_decomp", stack, GTMaterials.Iron,
                (float) (GTToolType.PICKAXE.materialAmount / GTValues.M), durability,
                GTValues.VH[GTValues.LV], 2);

        assert pickaxeRecipe != null : "Default Tool Decomp recipe couldn't be generated";
        pickaxeRecipe.setId(pickaxeRecipe.getId().withPrefix("/"));
        MACERATOR_RECYCLING.addRecipe(pickaxeRecipe);
        MACERATOR_RECYCLING.addRecipe(rotorRecipe);

        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            for(var stoneType : ORES.keySet()) {
                if (!material.shouldGenerateRecipesFor(stoneType)) {
                    continue;
                }
                if (!material.hasProperty(PropertyKey.ORE)) {
                    continue;
                }
                OreProperty prop = material.getProperty(PropertyKey.ORE);

                ItemStack oreStack = ChemicalHelper.get(stoneType, material);
                var oreRecipe = buildOreRecipe("ore_", oreStack, material, stoneType, prop);
                assert oreRecipe != null : "Ore recipe couldn't be generated";
                oreRecipe.setId(oreRecipe.getId().withPrefix("/"));
                ORE_CRUSHING.addRecipe(oreRecipe);
            }
        }
    }
}
