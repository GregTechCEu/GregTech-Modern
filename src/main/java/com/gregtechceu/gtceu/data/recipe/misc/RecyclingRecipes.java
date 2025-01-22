package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.GTValues.L;
import static com.gregtechceu.gtceu.api.GTValues.M;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;

public class RecyclingRecipes {

    // TODO - Fix recipe order with some things (noticed Hermetic Casings)
    // TODO - Figure out solution to LuV+ components
    // TODO - (to remember) Do NOT calculate any material component lists for circuits, they are simply totally lost
    // TODO - Work on durations and EUt's

    public static void init(Consumer<FinishedRecipe> provider) {
        for (Entry<ItemStack, ItemMaterialInfo> entry : ChemicalHelper.getAllItemInfos()) {
            ItemStack itemStack = entry.getKey();
            ItemMaterialInfo materialInfo = entry.getValue();
            ArrayList<MaterialStack> materialStacks = new ArrayList<>(materialInfo.getMaterials());
            registerRecyclingRecipes(provider, itemStack, materialStacks, false, null);
        }
    }

    public static void registerRecyclingRecipes(Consumer<FinishedRecipe> provider, ItemStack input,
                                                List<MaterialStack> components, boolean ignoreArcSmelting,
                                                @Nullable TagPrefix prefix) {
        // Gather the valid Materials for use in recycling recipes.
        // - Filter out Materials that cannot create a Dust
        // - Filter out Materials that do not equate to at least 1 Nugget worth of Material.
        // - Sort Materials on a descending material amount
        List<MaterialStack> materials = components.stream()
                .filter(stack -> stack.material().hasProperty(PropertyKey.DUST))
                .filter(stack -> stack.amount() >= M / 9)
                .sorted(Comparator.comparingLong(ms -> -ms.amount()))
                .toList();

        // Exit if no Materials matching the above requirements exist.
        if (materials.isEmpty()) return;

        // Calculate the voltage multiplier based on if a Material has a Blast Property
        int voltageMultiplier = calculateVoltageMultiplier(components);

        if (prefix != TagPrefix.dust) {
            registerMaceratorRecycling(provider, input, components, voltageMultiplier);
        }
        if (prefix != null) {
            registerExtractorRecycling(provider, input, components, voltageMultiplier, prefix);
        }
        if (ignoreArcSmelting) return;

        if (components.size() == 1) {
            Material m = components.get(0).material();

            // skip non-ingot materials
            if (!m.hasProperty(PropertyKey.INGOT)) {
                return;
            }

            // Skip Ingot -> Ingot Arc Recipes
            if (ChemicalHelper.getPrefix(input.getItem()) == TagPrefix.ingot &&
                    m.getProperty(PropertyKey.INGOT).getArcSmeltingInto() == m) {
                return;
            }

            // Prevent Magnetic dust -> Regular Ingot Arc Furnacing, avoiding the EBF recipe
            // "I will rework magnetic materials soon" - DStrand1
            if (prefix == TagPrefix.dust && m.hasFlag(IS_MAGNETIC)) {
                return;
            }
        }
        registerArcRecycling(provider, input, components, prefix);
    }

    private static void registerMaceratorRecycling(Consumer<FinishedRecipe> provider, ItemStack input,
                                                   List<MaterialStack> materials, int multiplier) {
        // Finalize the output list.
        List<ItemStack> outputs = finalizeOutputs(
                materials,
                GTRecipeTypes.MACERATOR_RECIPES.getMaxOutputs(ItemRecipeCapability.CAP),
                ChemicalHelper::getDust);

        UnificationEntry entry = ChemicalHelper.getUnificationEntry(input.getItem());
        TagKey<Item> inputTag = null;
        if (entry != null && entry.tagPrefix.unificationEnabled()) {
            inputTag = ChemicalHelper.getTag(entry.tagPrefix, entry.material);
        }

        // Exit if no valid Materials exist for this recycling Recipe.
        if (outputs.size() == 0) return;

        // Build the final Recipe.
        ResourceLocation itemPath = BuiltInRegistries.ITEM.getKey(input.getItem());
        GTRecipeBuilder builder = GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder("macerate_" + itemPath.getPath())
                .outputItems(outputs.toArray(ItemStack[]::new))
                .duration(calculateDuration(outputs))
                .EUt(2L * multiplier);

        if (inputTag == null) {
            builder.inputItems(input.copy());
        } else {
            builder.inputItems(inputTag);
        }

        boolean recycle = true;
        if (entry != null && entry.tagPrefix == TagPrefix.ingot) {
            recycle = false;
        }

        if (recycle) {
            builder.category(GTRecipeCategories.MACERATOR_RECYCLING);
        }

        builder.save(provider);
    }

    private static void registerExtractorRecycling(Consumer<FinishedRecipe> provider, ItemStack input,
                                                   List<MaterialStack> materials, int multiplier,
                                                   @Nullable TagPrefix prefix) {
        UnificationEntry entry = ChemicalHelper.getUnificationEntry(input.getItem());
        TagKey<Item> inputTag = null;
        if (entry != null) {
            inputTag = ChemicalHelper.getTag(entry.tagPrefix, entry.material);
        }

        // Handle simple materials separately
        if (prefix != null && prefix.secondaryMaterials().isEmpty()) {
            MaterialStack ms = ChemicalHelper.getMaterial(input);
            if (ms == null || ms.material() == null) {
                return;
            }
            Material m = ms.material();
            if (m.hasProperty(PropertyKey.INGOT) && m.getProperty(PropertyKey.INGOT).getMacerateInto() != m) {
                m = m.getProperty(PropertyKey.INGOT).getMacerateInto();
            }
            if (!m.hasProperty(PropertyKey.FLUID) || m.getFluid() == null) {
                return;
            }
            if (prefix == TagPrefix.dust && m.hasProperty(PropertyKey.BLAST)) {
                return;
            }

            ResourceLocation itemPath = BuiltInRegistries.ITEM.getKey(input.getItem());
            GTRecipeBuilder builder = GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("extract_" + itemPath.getPath())
                    .outputFluids(m.getFluid((int) (ms.amount() * L / M)))
                    .duration((int) Math.max(1, ms.amount() * ms.material().getMass() / M))
                    .EUt((long) GTValues.VA[GTValues.LV] * multiplier)
                    .category(GTRecipeCategories.EXTRACTOR_RECYCLING);
            if (inputTag == null) {
                builder.inputItems(input.copy());
            } else {
                builder.inputItems(inputTag);
            }
            builder.save(provider);

            return;
        }

        // Find the first Material which can create a Fluid.
        // If no Material in the list can create a Fluid, return.
        MaterialStack fluidMs = materials.stream()
                .filter(ms -> ms.material().hasProperty(PropertyKey.FLUID) && ms.material().getFluid() != null)
                .findFirst().orElse(null);
        if (fluidMs == null) return;

        // Find the next MaterialStack, which will be the Item output.
        // This can sometimes be before the Fluid output in the list, so we have to
        // assume it can be anywhere in the list.
        MaterialStack itemMs = materials.stream().filter(ms -> !ms.material().equals(fluidMs.material())).findFirst()
                .orElse(null);

        // Calculate the duration based off of those two possible outputs.
        // - Sum the two Material amounts together (if both exist)
        // - Divide the sum by M
        long duration = fluidMs.amount() * fluidMs.material().getMass();
        if (itemMs != null) duration += (itemMs.amount() * itemMs.material().getMass());
        duration = Math.max(1L, duration / M);

        // Build the final Recipe.
        ResourceLocation itemPath = BuiltInRegistries.ITEM.getKey(input.getItem());
        GTRecipeBuilder extractorBuilder = GTRecipeTypes.EXTRACTOR_RECIPES
                .recipeBuilder("extract_" + itemPath.getPath())
                .outputFluids(fluidMs.material().getFluid((int) (fluidMs.amount() * L / M)))
                .duration((int) duration)
                .EUt((long) GTValues.VA[GTValues.LV] * multiplier)
                .category(GTRecipeCategories.EXTRACTOR_RECYCLING);

        if (inputTag == null) {
            extractorBuilder.inputItems(input.copy());
        } else {
            extractorBuilder.inputItems(inputTag);
        }

        // Null check the Item before adding it to the Builder.
        // - Try to output an Ingot, otherwise output a Dust.
        if (itemMs != null) {
            ItemStack outputStack = ChemicalHelper.getIngotOrDust(itemMs);
            if (!outputStack.isEmpty()) extractorBuilder.outputItems(outputStack);
            // TagPrefix outputPrefix = itemMs.material().hasProperty(PropertyKey.INGOT) ? TagPrefix.ingot :
            // TagPrefix.dust;
            // extractorBuilder.outputItems(outputPrefix, itemMs.material(), (int) (itemMs.amount() / M));
        }

        extractorBuilder.save(provider);
    }

    private static void registerArcRecycling(Consumer<FinishedRecipe> provider, ItemStack input,
                                             List<MaterialStack> materials, @Nullable TagPrefix prefix) {
        UnificationEntry entry = ChemicalHelper.getUnificationEntry(input.getItem());
        TagKey<Item> inputTag = null;
        if (entry != null) {
            inputTag = ChemicalHelper.getTag(entry.tagPrefix, entry.material);
        }

        // Block dusts from being arc'd instead of EBF'd
        MaterialStack ms = ChemicalHelper.getMaterial(input);
        if (prefix == TagPrefix.dust && ms != null && ms.material().hasProperty(PropertyKey.BLAST)) {
            return;
        } else if (prefix == TagPrefix.block) {
            if (ms != null && !ms.material().hasProperty(PropertyKey.GEM)) {
                ItemStack output = ChemicalHelper.get(TagPrefix.ingot,
                        ms.material().getProperty(PropertyKey.INGOT).getArcSmeltingInto(),
                        (int) (TagPrefix.block.getMaterialAmount(ms.material()) / GTValues.M));
                ResourceLocation itemPath = BuiltInRegistries.ITEM.getKey(input.getItem());
                GTRecipeBuilder builder = GTRecipeTypes.ARC_FURNACE_RECIPES.recipeBuilder("arc_" + itemPath.getPath())
                        .outputItems(output)
                        .duration(calculateDuration(Collections.singletonList(output)))
                        .EUt(GTValues.VA[GTValues.LV]);
                if (inputTag == null) {
                    builder.inputItems(input.copy());
                } else {
                    builder.inputItems(inputTag);
                }

                if (ms.material().hasFlag(IS_MAGNETIC) ||
                        ms.material() == ms.material().getProperty(PropertyKey.INGOT).getArcSmeltingInto()) {
                    builder.category(GTRecipeCategories.ARC_FURNACE_RECYCLING);
                }
                builder.save(provider);
            }
            return;
        }

        // Filter down the materials list.
        // - Map to the Arc Smelting result as defined below
        // - Combine any MaterialStacks that have the same Material
        materials = combineStacks(materials.stream()
                .map(RecyclingRecipes::getArcSmeltingResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        // Finalize the output List
        List<ItemStack> outputs = finalizeOutputs(
                materials,
                GTRecipeTypes.ARC_FURNACE_RECIPES.getMaxOutputs(ItemRecipeCapability.CAP),
                RecyclingRecipes::getArcIngotOrDust);

        // Exit if no valid outputs exist for this recycling Recipe.
        if (outputs.size() == 0) return;

        // Build the final Recipe.
        ResourceLocation itemPath = BuiltInRegistries.ITEM.getKey(input.getItem());
        GTRecipeBuilder builder = GTRecipeTypes.ARC_FURNACE_RECIPES.recipeBuilder("arc_" + itemPath.getPath())
                .outputItems(outputs.toArray(ItemStack[]::new))
                .duration(calculateDuration(outputs))
                .EUt(GTValues.VA[GTValues.LV]);
        if (inputTag == null) {
            builder.inputItems(input.copy());
        } else {
            builder.inputItems(inputTag);
        }

        if (needsRecyclingCategory(prefix, ms, outputs)) {
            builder.category(GTRecipeCategories.ARC_FURNACE_RECYCLING);
        }

        builder.save(provider);
    }

    private static boolean needsRecyclingCategory(@Nullable TagPrefix prefix, @Nullable MaterialStack inputStack,
                                                  @NotNull List<ItemStack> outputs) {
        if (prefix == TagPrefix.nugget || prefix == TagPrefix.ingot || prefix == TagPrefix.block) {
            if (outputs.size() == 1) {
                UnificationEntry entry = ChemicalHelper.getUnificationEntry(outputs.get(0).getItem());
                if (entry != null && inputStack != null) {
                    Material mat = inputStack.material();
                    if (!mat.hasFlag(IS_MAGNETIC) && mat.hasProperty(PropertyKey.INGOT)) {
                        return mat.getProperty(PropertyKey.INGOT).getArcSmeltingInto() != entry.material;
                    }
                }
            }
        }
        return true;
    }

    private static MaterialStack getArcSmeltingResult(MaterialStack materialStack) {
        Material material = materialStack.material();
        long amount = materialStack.amount();

        if (material.hasFlag(EXPLOSIVE)) {
            return new MaterialStack(GTMaterials.Ash, amount / 16);
        }

        // If the Material is Flammable, return Ash
        if (material.hasFlag(FLAMMABLE)) {
            return new MaterialStack(GTMaterials.Ash, amount / 8);
        }

        // Else if the Material is a Gem, process its output (see below)
        if (material.hasProperty(PropertyKey.GEM)) {
            return getGemArcSmeltResult(materialStack);
        }

        // Else if the Material has NO_SMELTING, return nothing
        if (material.hasFlag(NO_SMELTING)) {
            return null;
        }

        // Else if the Material is an Ingot, return the Arc Smelting
        // result if it exists, otherwise return the Material itself.
        if (material.hasProperty(PropertyKey.INGOT)) {
            Material arcSmelt = material.getProperty(PropertyKey.INGOT).getArcSmeltingInto();
            if (arcSmelt != null) {
                return new MaterialStack(arcSmelt, amount);
            }
        }
        return materialStack;
    }

    private static ItemStack getArcIngotOrDust(@NotNull MaterialStack stack) {
        if (stack.material() == GTMaterials.Carbon) {
            return ChemicalHelper.getDust(stack);
        }
        return ChemicalHelper.getIngotOrDust(stack);
    }

    private static MaterialStack getGemArcSmeltResult(MaterialStack materialStack) {
        Material material = materialStack.material();
        long amount = materialStack.amount();

        // If the Gem Material has Oxygen in it, return Ash
        if (material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material() == GTMaterials.Oxygen)) {
            return new MaterialStack(GTMaterials.Ash, amount / 8);
        }

        // Else if the Gem Material has Carbon in it, return Carbon
        if (material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material() == GTMaterials.Carbon)) {
            return new MaterialStack(GTMaterials.Carbon, amount / 8);
        }

        // Else return Dark Ash
        return new MaterialStack(GTMaterials.DarkAsh, amount / 8);
    }

    private static int calculateVoltageMultiplier(List<MaterialStack> materials) {
        // Gather the highest blast temperature of any material in the list
        int highestTemp = 0;
        for (MaterialStack ms : materials) {
            Material m = ms.material();
            if (m.hasProperty(PropertyKey.BLAST)) {
                BlastProperty prop = m.getProperty(PropertyKey.BLAST);
                if (prop.getBlastTemperature() > highestTemp) {
                    highestTemp = prop.getBlastTemperature();
                }
            } else if (m.hasFlag(IS_MAGNETIC) && m.hasProperty(PropertyKey.INGOT) &&
                    m.getProperty(PropertyKey.INGOT).getSmeltingInto().hasProperty(PropertyKey.BLAST)) {
                        BlastProperty prop = m.getProperty(PropertyKey.INGOT).getSmeltingInto()
                                .getProperty(PropertyKey.BLAST);
                        if (prop.getBlastTemperature() > highestTemp) {
                            highestTemp = prop.getBlastTemperature();
                        }
                    }
        }

        // No blast temperature in the list means no multiplier
        if (highestTemp == 0) return 1;

        // If less then 2000K, multiplier of 4
        if (highestTemp < 2000) return 4; // todo make this a better value?

        // If above 2000K, multiplier of 16
        return 16;
    }

    /**
     * This method calculates the duration for a recycling method. It:
     * - Sums the amount of material times the mass of the material for the List
     * - Divides that by M
     */
    private static int calculateDuration(List<ItemStack> materials) {
        long duration = 0;
        for (ItemStack is : materials) {
            MaterialStack ms = ChemicalHelper.getMaterial(is);
            if (ms != null) duration += ms.amount() * ms.material().getMass() * is.getCount();
        }
        return (int) Math.max(1L, duration / M);
    }

    /**
     * Combines any matching Materials in the List into one MaterialStack
     */
    private static List<MaterialStack> combineStacks(List<MaterialStack> rawList) {
        // Combine any stacks in the List that have the same Item.
        Map<Material, Long> materialStacksExploded = new HashMap<>();
        for (MaterialStack ms : rawList) {
            long amount = materialStacksExploded.getOrDefault(ms.material(), 0L);
            materialStacksExploded.put(ms.material(), ms.amount() + amount);
        }
        return materialStacksExploded.entrySet().stream()
                .map(e -> new MaterialStack(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static List<ItemStack> finalizeOutputs(List<MaterialStack> materials, int maxOutputs,
                                                   Function<MaterialStack, ItemStack> toItemStackMapper) {
        // Map of ItemStack, Long to properly sort by the true material amount for outputs
        List<Tuple<ItemStack, MaterialStack>> outputs = new ArrayList<>();

        for (MaterialStack ms : materials) {
            ms = new MaterialStack(ms.material().hasFlag(IS_MAGNETIC) ?
                    ms.material().getProperty(PropertyKey.INGOT).getMacerateInto() : ms.material(), ms.amount());
            ItemStack stack = toItemStackMapper.apply(ms);
            if (stack == ItemStack.EMPTY) continue;
            if (stack.getCount() > 64) {
                UnificationEntry entry = ChemicalHelper.getUnificationEntry(stack.getItem());
                if (entry != null) { // should always be true
                    TagPrefix prefix = entry.tagPrefix;

                    // These are the highest forms that a Material can have (for Ingot and Dust, respectively),
                    // so simply split the stacks and continue.
                    if (prefix == TagPrefix.block || prefix == TagPrefix.dust) {
                        splitStacks(outputs, stack, entry);
                    } else {
                        // Attempt to split and to shrink the stack, and choose the option that creates the
                        // "larger" single stack, in terms of raw material amount.
                        List<Tuple<ItemStack, MaterialStack>> split = new ArrayList<>();
                        List<Tuple<ItemStack, MaterialStack>> shrink = new ArrayList<>();
                        splitStacks(split, stack, entry);
                        shrinkStacks(shrink, stack, entry);

                        if (split.get(0).getB().amount() > shrink.get(0).getB().amount()) {
                            outputs.addAll(split);
                        } else outputs.addAll(shrink);
                    }
                }
            } else outputs.add(new Tuple<>(stack, ms));
        }

        // Sort the List by total material amount descending.
        outputs.sort(Comparator.comparingLong(e -> -e.getB().amount()));

        // Sort "duplicate" outputs to the end.
        // For example, if there are blocks of Steel and nuggets of Steel, and the nuggets
        // are preventing some other output from occupying one of the final slots of the machine,
        // cut the nuggets out to favor the newer item instead of having 2 slots occupied by Steel.
        //
        // There is probably a better way to do this.
        Map<MaterialStack, ItemStack> temp = new HashMap<>();
        for (Tuple<ItemStack, MaterialStack> t : outputs) {
            boolean isInMap = false;
            for (MaterialStack ms : temp.keySet()) {
                if (ms.material() == t.getB().material()) {
                    isInMap = true;
                    break;
                }
            }
            if (!isInMap) temp.put(t.getB(), t.getA());
        }
        temp.putAll(outputs.stream()
                .filter(t -> !temp.containsKey(t.getB()))
                .collect(Collectors.toMap(Tuple::getB, Tuple::getA)));

        // Filter Ash to the very end of the list, after all others
        List<ItemStack> ashStacks = temp.entrySet().stream()
                .filter(e -> isAshMaterial(e.getKey()))
                .sorted(Comparator.comparingLong(e -> -e.getKey().amount()))
                .map(Entry::getValue)
                .collect(Collectors.toList());

        List<ItemStack> returnValues = temp.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> -e.getKey().amount()))
                .filter(e -> !isAshMaterial(e.getKey()))
                .limit(maxOutputs)
                .map(Entry::getValue)
                .collect(Collectors.toList());

        for (int i = 0; i < ashStacks.size() && returnValues.size() < maxOutputs; i++) {
            returnValues.add(ashStacks.get(i));
        }
        return returnValues;
    }

    private static void splitStacks(List<Tuple<ItemStack, MaterialStack>> list, ItemStack originalStack,
                                    UnificationEntry entry) {
        int amount = originalStack.getCount();
        while (amount > 64) {
            list.add(new Tuple<>(GTUtil.copyAmount(64, originalStack),
                    new MaterialStack(entry.material, entry.tagPrefix.getMaterialAmount(entry.material) * 64)));
            amount -= 64;
        }
        list.add(new Tuple<>(GTUtil.copyAmount(amount, originalStack),
                new MaterialStack(entry.material, entry.tagPrefix.getMaterialAmount(entry.material) * amount)));
    }

    private static final List<TagPrefix> DUST_ORDER = ImmutableList.of(TagPrefix.dust, TagPrefix.dustSmall,
            TagPrefix.dustTiny);
    private static final List<TagPrefix> INGOT_ORDER = ImmutableList.of(TagPrefix.block, TagPrefix.ingot,
            TagPrefix.nugget);

    private static void shrinkStacks(List<Tuple<ItemStack, MaterialStack>> list, ItemStack originalStack,
                                     UnificationEntry entry) {
        Material material = entry.material;
        long materialAmount = originalStack.getCount() * entry.tagPrefix.getMaterialAmount(material);

        // noinspection ConstantConditions
        final List<TagPrefix> chosenList = material.hasProperty(PropertyKey.INGOT) ? INGOT_ORDER : DUST_ORDER;

        // Break materialAmount into a maximal stack
        Map<TagPrefix, MaterialStack> tempList = new HashMap<>();
        for (TagPrefix prefix : chosenList) {

            // Current prefix too large to "compact" into
            if (materialAmount / prefix.getMaterialAmount(material) == 0) continue;

            long newAmount = materialAmount / prefix.getMaterialAmount(material);
            tempList.put(prefix, new MaterialStack(material, newAmount * prefix.getMaterialAmount(material)));
            materialAmount = materialAmount % prefix.getMaterialAmount(material);
        }

        // Split the "highest level" stack (either Blocks or Dusts) if needed, as it is
        // the only stack that could possibly be above 64.
        if (tempList.containsKey(chosenList.get(0))) {
            TagPrefix prefix = chosenList.get(0);
            MaterialStack ms = tempList.get(prefix);
            splitStacks(list,
                    ChemicalHelper.get(chosenList.get(0), ms.material(),
                            (int) (ms.amount() / prefix.getMaterialAmount(material))),
                    new UnificationEntry(prefix, material));
        }

        TagPrefix mediumPrefix = chosenList.get(1); // dustSmall or ingot
        TagPrefix smallestPrefix = chosenList.get(2); // dustTiny or nugget
        MaterialStack mediumMS = tempList.get(mediumPrefix); // dustSmall or ingot
        MaterialStack smallestMS = tempList.get(smallestPrefix); // dustTiny or nugget

        // Try to compact the two "lower form" prefixes into one stack, if it doesn't exceed stack size
        if (mediumMS != null && smallestMS != null) {
            long singleStackAmount = mediumMS.amount() + smallestMS.amount();
            if (singleStackAmount / smallestPrefix.getMaterialAmount(material) <= 64) {
                list.add(new Tuple<>(
                        ChemicalHelper.get(smallestPrefix, material,
                                (int) (singleStackAmount / smallestPrefix.getMaterialAmount(material))),
                        new MaterialStack(material, singleStackAmount)));
                return;
            }
        }

        // Otherwise simply add the stacks to the List if they exist
        if (mediumMS != null) list.add(new Tuple<>(
                ChemicalHelper.get(mediumPrefix, material,
                        (int) (mediumMS.amount() / mediumPrefix.getMaterialAmount(material))),
                new MaterialStack(material, mediumMS.amount())));

        if (smallestMS != null) list.add(new Tuple<>(
                ChemicalHelper.get(smallestPrefix, material,
                        (int) (smallestMS.amount() / smallestPrefix.getMaterialAmount(material))),
                new MaterialStack(material, smallestMS.amount())));
    }

    private static boolean isAshMaterial(MaterialStack ms) {
        return ms.material() == GTMaterials.Ash || ms.material() == GTMaterials.DarkAsh ||
                ms.material() == GTMaterials.Carbon;
    }
}
