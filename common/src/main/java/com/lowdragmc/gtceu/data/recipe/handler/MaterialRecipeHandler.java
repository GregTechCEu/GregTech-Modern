package com.lowdragmc.gtceu.data.recipe.handler;

import com.lowdragmc.gtceu.api.data.chemical.ChemicalHelper;
import com.lowdragmc.gtceu.api.data.chemical.material.MarkerMaterials;
import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.gtceu.api.data.chemical.material.properties.*;
import com.lowdragmc.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.lowdragmc.gtceu.api.tag.TagPrefix;
import com.lowdragmc.gtceu.common.libs.GTItems;
import com.lowdragmc.gtceu.common.libs.GTMaterials;
import com.lowdragmc.gtceu.common.libs.GTRecipeTypes;
import com.lowdragmc.gtceu.config.ConfigHolder;
import com.lowdragmc.gtceu.data.recipe.CraftingComponent;
import com.lowdragmc.gtceu.data.recipe.VanillaRecipeHelper;
import com.lowdragmc.gtceu.utils.FormattingUtil;
import com.lowdragmc.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.lowdragmc.gtceu.api.GTValues.*;
import static com.lowdragmc.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;

public class MaterialRecipeHandler {

    private static final List<TagPrefix> GEM_ORDER = ConfigHolder.recipes.generateLowQualityGems ? Arrays.asList(
            TagPrefix.gemChipped, TagPrefix.gemFlawed, TagPrefix.gem, TagPrefix.gemFlawless, TagPrefix.gemExquisite) :
            Arrays.asList(TagPrefix.gem, TagPrefix.gemFlawless, TagPrefix.gemExquisite);

    public static void init(Consumer<FinishedRecipe> provider) {
        TagPrefix.ingot.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processIngot(tagPrefix, material, property, provider));
        TagPrefix.nugget.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processNugget(tagPrefix, material, property, provider));

        TagPrefix.block.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processBlock(tagPrefix, material, property, provider));
        TagPrefix.frameGt.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processFrame(tagPrefix, material, property, provider));

        TagPrefix.dust.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processDust(tagPrefix, material, property, provider));
        TagPrefix.dustSmall.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processSmallDust(tagPrefix, material, property, provider));
        TagPrefix.dustTiny.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processTinyDust(tagPrefix, material, property, provider));

        for (TagPrefix orePrefix : GEM_ORDER) {
            orePrefix.executeHandler(PropertyKey.GEM, (tagPrefix, material, property) -> processGemConversion(tagPrefix, material, property, provider));
        }
    }

    public static void processDust(TagPrefix dustPrefix, Material mat, DustProperty property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_".formatted(FormattingUtil.toLowerCaseUnder(dustPrefix.name), mat.getName().toLowerCase());
        var dustTag = ChemicalHelper.getTag(dustPrefix, mat);
        if (mat.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = ChemicalHelper.get(TagPrefix.gem, mat);
            ItemStack smallDarkAshStack = ChemicalHelper.get(TagPrefix.dustSmall, GTMaterials.DarkAsh);

            if (mat.hasFlag(CRYSTALLIZABLE)) {
                GTRecipeTypes.AUTOCLAVE_RECIPES.recipeBuilder(id + "gem_crystal_0")
                        .inputItems(dustTag)
                        .inputFluids(GTMaterials.Water.getFluid(250))
                        .chancedOutput(gemStack, 7000, 1000)
                        .duration(1200).EUt(24)
                        .save(provider);

                GTRecipeTypes.AUTOCLAVE_RECIPES.recipeBuilder(id + "gem_crystal_1")
                        .inputItems(dustTag)
                        .inputFluids(GTMaterials.DistilledWater.getFluid(50))
                        .outputItems(gemStack)
                        .duration(600).EUt(24)
                        .save(provider);
            }

            if (!mat.hasFlag(EXPLOSIVE) && !mat.hasFlag(FLAMMABLE)) {
                GTRecipeTypes.IMPLOSION_RECIPES.recipeBuilder(id + "gem_gem_0")
                        .inputItems(dustTag, 4)
                        .outputItems(GTUtil.copyAmount(3, gemStack), smallDarkAshStack)
                        .explosivesAmount(2)
                        .save(provider);

                //TODO
//                GTRecipeTypes.IMPLOSION_RECIPES.recipeBuilder(id + "gem_gem_1")
//                        .inputItems(dustTag, 4)
//                        .outputItems(GTUtil.copyAmount(3, gemStack), smallDarkAshStack)
//                        .explosivesType(GTItems.DYNAMITE.asStack())
//                        .save(provider);
            }

        } else if (mat.hasProperty(PropertyKey.INGOT)) {
            if (!mat.hasAnyOfFlags(FLAMMABLE, NO_SMELTING)) {

                boolean hasHotIngot = ingotHot.doGenerateItem(mat);
                ItemStack ingotStack = ChemicalHelper.get(hasHotIngot ? ingotHot : TagPrefix.ingot, mat);
                int blastTemp = mat.getBlastTemperature();

                if (blastTemp <= 0) {
                    // smelting magnetic dusts is handled elsewhere
                    if (!mat.hasFlag(IS_MAGNETIC)) {
                        // do not register inputs by ore dict here. Let other mods register their own dust -> ingots
                        VanillaRecipeHelper.addSmeltingRecipe(provider, id + "ingot", ChemicalHelper.getTag(dustPrefix, mat), ingotStack);
                    }
                } else {
                    IngotProperty ingotProperty = mat.getProperty(PropertyKey.INGOT);
                    BlastProperty blastProperty = mat.getProperty(PropertyKey.BLAST);

                    processEBFRecipe(mat, blastProperty, ingotStack, provider);

                    if (ingotProperty.getMagneticMaterial() != null) {
                        processEBFRecipe(ingotProperty.getMagneticMaterial(), blastProperty, ingotStack, provider);
                    }
                }
            }
        } else {
            if (mat.hasFlag(GENERATE_PLATE) && !mat.hasFlag(EXCLUDE_PLATE_COMPRESSOR_RECIPE)) {
                GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder(id + "plate")
                        .inputItems(dustTag)
                        .outputItems(ChemicalHelper.get(TagPrefix.plate, mat))
                        .save(provider);
            }
        }
    }

    private static void processEBFRecipe(Material material, BlastProperty property, ItemStack output, Consumer<FinishedRecipe> provider) {
        int blastTemp = property.getBlastTemperature();
        BlastProperty.GasTier gasTier = property.getGasTier();
        int duration = property.getDurationOverride();
        if (duration <= 0) {
            duration = Math.max(1, (int) (material.getMass() * blastTemp / 50L));
        }
        int EUt = property.getEUtOverride();
        if (EUt <= 0) EUt = VA[MV];

        var id = "%s_%s_ebf_".formatted(material.getName(), output.getItem().getDescriptionId());
        var blastBuilder = GTRecipeTypes.BLAST_RECIPES.recipeBuilder(id + 0)
                .inputItems(dust, material)
                .outputItems(output)
                .blastFurnaceTemp(blastTemp)
                .EUt(EUt);

        if (gasTier != null) {
            FluidStack gas = CraftingComponent.EBF_GASES.get(gasTier).copy();

            blastBuilder.copy(id + 1)
                    .circuitMeta(1)
                    .duration(duration)
                    .save(provider);

            blastBuilder.copy(id + 2)
                    .circuitMeta(2)
                    .inputFluids(gas)
                    .duration((int) (duration * 0.67))
                    .save(provider);
        } else {
            blastBuilder.duration(duration);
            if (material == GTMaterials.Silicon) {
                blastBuilder.circuitMeta(1);
            }
            blastBuilder.save(provider);
        }

        // Add Vacuum Freezer recipe if required.
        if (ingotHot.doGenerateItem(material)) {
            if(blastTemp < 5000) {
                GTRecipeTypes.VACUUM_RECIPES.recipeBuilder(id + "hot_ingot")
                        .inputItems(ingotHot, material)
                        .outputItems(ingot, material)
                        .duration((int) material.getMass() * 3)
                        .save(provider);
            } else {
                GTRecipeTypes.VACUUM_RECIPES.recipeBuilder(id + "hot_ingot")
                        .inputItems(ingotHot, material)
                        .inputFluids(GTMaterials.LiquidHelium.getFluid(500))
                        .outputItems(ingot, material)
                        .outputFluids(GTMaterials.Helium.getFluid(250))
                        .duration((int) material.getMass() * 3)
                        .save(provider);
            }
        }
    }

    public static void processSmallDust(TagPrefix orePrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_".formatted(FormattingUtil.toLowerCaseUnder(orePrefix.name), material.getName().toLowerCase());
        ItemStack smallDustStack = ChemicalHelper.get(orePrefix, material);
        ItemStack dustStack = ChemicalHelper.get(TagPrefix.dust, material);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_dust_disassembling_%s", material),
                GTUtil.copyAmount(4, smallDustStack), " X", "  ", 'X', new UnificationEntry(TagPrefix.dust, material));
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_dust_assembling_%s", material),
                dustStack, "XX", "XX", 'X', new UnificationEntry(orePrefix, material));

        GTRecipeTypes.PACKER_RECIPES.recipeBuilder(id + "small_dust_4").inputItems(orePrefix, material, 4)
                .circuitMeta(1)
                .outputItems(dustStack)
                .save(provider);

        GTRecipeTypes.PACKER_RECIPES.recipeBuilder(id + "small_dust_1").inputItems(TagPrefix.dust, material)
                .circuitMeta(2)
                .outputItems(GTUtil.copyAmount(4, smallDustStack))
                .save(provider);
    }

    public static void processTinyDust(TagPrefix orePrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_".formatted(FormattingUtil.toLowerCaseUnder(orePrefix.name), material.getName().toLowerCase());
        ItemStack tinyDustStack = ChemicalHelper.get(orePrefix, material);
        ItemStack dustStack = ChemicalHelper.get(TagPrefix.dust, material);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("tiny_dust_disassembling_%s", material),
                GTUtil.copyAmount(9, tinyDustStack), "X ", "  ", 'X', new UnificationEntry(TagPrefix.dust, material));
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("tiny_dust_assembling_%s", material),
                dustStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));

        GTRecipeTypes.PACKER_RECIPES.recipeBuilder(id + "tiny_dust_9").inputItems(orePrefix, material, 9)
                .circuitMeta(1)
                .outputItems(dustStack)
                .save(provider);

        GTRecipeTypes.PACKER_RECIPES.recipeBuilder(id + "tiny_dust_1").inputItems(TagPrefix.dust, material)
                .circuitMeta(1)
                .outputItems(GTUtil.copyAmount(9, tinyDustStack))
                .save(provider);
    }

    public static void processIngot(TagPrefix ingotPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_".formatted(FormattingUtil.toLowerCaseUnder(ingotPrefix.name), material.getName().toLowerCase());
        if (material.hasFlag(MORTAR_GRINDABLE)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("mortar_grind_%s", material),
                    ChemicalHelper.get(TagPrefix.dust, material), "X", "m", 'X', new UnificationEntry(ingotPrefix, material));
        }

        if (material.hasFlag(GENERATE_ROD)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_%s", material),
                    ChemicalHelper.get(TagPrefix.stick, material),
                    "f ", " X",
                    'X', new UnificationEntry(ingotPrefix, material));
            if (!material.hasFlag(NO_WORKING)) {
                GTRecipeTypes.EXTRUDER_RECIPES.recipeBuilder(id + "extruder_rod")
                        .inputItems(ingotPrefix, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_ROD.asStack())
                        .outputItems(ChemicalHelper.get(TagPrefix.stick, material, 2))
                        .duration((int) material.getMass() * 2)
                        .EUt(6L * getVoltageMultiplier(material))
                        .save(provider);
            }
        }

        if (material.hasFluid()) {
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES.recipeBuilder(id + "mold_ingot")
                    .notConsumable(GTItems.SHAPE_MOLD_INGOT.asStack())
                    .inputFluids(material.getFluid(L))
                    .outputItems(ChemicalHelper.get(ingotPrefix, material))
                    .duration(20).EUt(VA[ULV])
                    .save(provider);
        }

        if (material.hasFlag(NO_SMASHING)) {
            GTRecipeTypes.EXTRUDER_RECIPES.recipeBuilder(id + "extruder_ingot")
                    .inputItems(TagPrefix.dust, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_INGOT.asStack())
                    .outputItems(ChemicalHelper.get(TagPrefix.ingot, material))
                    .duration(10)
                    .EUt(4L * getVoltageMultiplier(material))
                    .save(provider);
        }

        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(id + "mold_nugget")
                .EUt(VA[ULV]).duration((int) material.getMass())
                .inputItems(ingot, material)
                .notConsumable(GTItems.SHAPE_MOLD_NUGGET.asStack())
                .outputItems(nugget, material, 9)
                .save(provider);

        if (!ChemicalHelper.get(block, material).isEmpty()) {
            GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(id + "mold_ingot").EUt(VA[ULV]).duration((int) material.getMass() * 9)
                    .inputItems(block, material)
                    .notConsumable(GTItems.SHAPE_MOLD_INGOT.asStack())
                    .outputItems(ingot, material, 9)
                    .save(provider);

            GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder(id + "block").EUt(2).duration(300)
                    .inputItems(ingot, material, (int) (block.getMaterialAmount(material) / M))
                    .outputItems(block, material)
                    .save(provider);
        }

        if (material.hasFlag(GENERATE_PLATE) && !material.hasFlag(NO_WORKING)) {

            if (!material.hasFlag(NO_SMASHING)) {
                ItemStack plateStack = ChemicalHelper.get(TagPrefix.plate, material);
                if (!plateStack.isEmpty()) {
                    GTRecipeTypes.BENDER_RECIPES.recipeBuilder("%s_%s_plate".formatted(ingotPrefix.name, material.getName()))
                            .circuitMeta(1)
                            .inputItems(ingotPrefix, material)
                            .outputItems(plateStack)
                            .EUt(24).duration((int) (material.getMass()))
                            .save(provider);

                    GTRecipeTypes.FORGE_HAMMER_RECIPES.recipeBuilder(id + "plate")
                            .inputItems(ingotPrefix, material, 3)
                            .outputItems(GTUtil.copyAmount(2, plateStack))
                            .EUt(16).duration((int) material.getMass())
                            .save(provider);

                    VanillaRecipeHelper.addShapedRecipe(provider, String.format("plate_%s", material),
                            plateStack, "h", "I", "I", 'I', new UnificationEntry(ingotPrefix, material));
                }
            }

            int voltageMultiplier = getVoltageMultiplier(material);
            if (!ChemicalHelper.get(plate, material).isEmpty()) {
                GTRecipeTypes.EXTRUDER_RECIPES.recipeBuilder(id + "extruder_plate")
                        .inputItems(ingotPrefix, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_PLATE.asStack())
                        .outputItems(ChemicalHelper.get(TagPrefix.plate, material))
                        .duration((int) material.getMass())
                        .EUt(8L * voltageMultiplier)
                        .save(provider);

                if (material.hasFlag(NO_SMASHING)) {
                    GTRecipeTypes.EXTRUDER_RECIPES.recipeBuilder(id + "dust_extruder_plate")
                            .inputItems(dust, material)
                            .notConsumable(GTItems.SHAPE_EXTRUDER_PLATE.asStack())
                            .outputItems(ChemicalHelper.get(TagPrefix.plate, material))
                            .duration((int) material.getMass())
                            .EUt(8L * voltageMultiplier)
                            .save(provider);
                }
            }
        }

    }

    public static void processGemConversion(TagPrefix gemPrefix, Material material, GemProperty property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_".formatted(FormattingUtil.toLowerCaseUnder(gemPrefix.name), material.getName().toLowerCase());
        long materialAmount = gemPrefix.getMaterialAmount(material);
        ItemStack crushedStack = ChemicalHelper.getDust(material, materialAmount);

        if (material.hasFlag(MORTAR_GRINDABLE)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("gem_to_dust_%s_%s", material, gemPrefix), crushedStack,
                    "X", "m", 'X', new UnificationEntry(gemPrefix, material));
        }

        TagPrefix prevPrefix = GTUtil.getItem(GEM_ORDER, GEM_ORDER.indexOf(gemPrefix) - 1, null);
        ItemStack prevStack = prevPrefix == null ? ItemStack.EMPTY : ChemicalHelper.get(prevPrefix, material);
        ItemStack gemStack = ChemicalHelper.get(gemPrefix, material);
        if (!prevStack.isEmpty() && !gemStack.isEmpty()) {
            VanillaRecipeHelper.addShapelessRecipe(provider, String.format("gem_to_gem_%s_%s", prevPrefix, material), prevStack,
                    "h", new UnificationEntry(gemPrefix, material));

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder(id + "gem")
                    .inputItems(gemPrefix, material)
                    .outputItems(prevStack)
                    .duration(20)
                    .EUt(16)
                    .save(provider);

            GTRecipeTypes.LASER_ENGRAVER_RECIPES.recipeBuilder(id + "gem")
                    .inputItems(prevStack)
                    .chance(0)
                    .inputItems(craftingLens, MarkerMaterials.Color.White)
                    .chance(1)
                    .outputItems(gemPrefix, material)
                    .duration(300)
                    .EUt(240)
                    .save(provider);
        }
    }

    public static void processNugget(TagPrefix orePrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack nuggetStack = ChemicalHelper.get(orePrefix, material);
        String id = "%s_%s_".formatted(orePrefix.name.toLowerCase(), material.getName().toLowerCase());
        if (material.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = ChemicalHelper.get(TagPrefix.ingot, material);

            if (!ConfigHolder.recipes.disableManualCompression) {
                VanillaRecipeHelper.addShapelessRecipe(provider, String.format("nugget_disassembling_%s", material),
                        GTUtil.copyAmount(9, nuggetStack), new UnificationEntry(TagPrefix.ingot, material));
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("nugget_assembling_%s", material),
                        ingotStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));
            }

            GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder(id + "ingot")
                    .inputItems(nugget, material, 9)
                    .outputItems(ingot, material)
                    .EUt(2).duration(300).save(provider);

            GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(id + "mold_ingot").EUt(VA[ULV]).duration((int) material.getMass())
                    .inputItems(nugget, material, 9)
                    .notConsumable(GTItems.SHAPE_MOLD_INGOT.asStack())
                    .outputItems(ingot, material)
                    .save(provider);

            if (material.hasFluid()) {
                GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES.recipeBuilder(id + "mold_nugget")
                        .notConsumable(GTItems.SHAPE_MOLD_NUGGET.asStack())
                        .inputFluids(material.getFluid(L))
                        .outputItems(ChemicalHelper.get(orePrefix, material, 9))
                        .duration((int) material.getMass())
                        .EUt(VA[ULV])
                        .save(provider);
            }
        } else if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = ChemicalHelper.get(TagPrefix.gem, material);

            if (!ConfigHolder.recipes.disableManualCompression) {
                VanillaRecipeHelper.addShapelessRecipe(provider, String.format("nugget_disassembling_%s", material),
                        GTUtil.copyAmount(9, nuggetStack), new UnificationEntry(TagPrefix.gem, material));
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("nugget_assembling_%s", material),
                        gemStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));
            }
        }
    }

    public static void processFrame(TagPrefix framePrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s".formatted(FormattingUtil.toLowerCaseUnder(framePrefix.name), material.getName().toLowerCase());
        if (material.hasFlag(GENERATE_FRAME)) {
            boolean isWoodenFrame = material == GTMaterials.Wood || material == GTMaterials.TreatedWood;
            VanillaRecipeHelper.addShapedRecipe(provider, id,
                    ChemicalHelper.get(framePrefix, material, 2),
                    "SSS", isWoodenFrame ? "SsS" : "SwS", "SSS",
                    'S', new UnificationEntry(TagPrefix.stick, material));

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(id)
                    .inputItems(TagPrefix.stick, material, 4)
                    .circuitMeta(4)
                    .outputItems(ChemicalHelper.get(framePrefix, material, 1))
                    .EUt(VA[ULV]).duration(64)
                    .save(provider);
        }
    }

    public static void processBlock(TagPrefix blockPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_".formatted(FormattingUtil.toLowerCaseUnder(blockPrefix.name), material.getName().toLowerCase());
        ItemStack blockStack = ChemicalHelper.get(blockPrefix, material);
        if (blockStack.isEmpty()) return;
        long materialAmount = blockPrefix.getMaterialAmount(material);
        if (material.hasFluid()) {
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES.recipeBuilder(id + "mold_block")
                    .notConsumable(GTItems.SHAPE_MOLD_BLOCK.asStack())
                    .inputFluids(material.getFluid((int) (materialAmount * L / M)))
                    .outputItems(blockStack)
                    .duration((int) material.getMass()).EUt(VA[ULV])
                    .save(provider);
        }

        if (material.hasFlag(GENERATE_PLATE)) {
            ItemStack plateStack = ChemicalHelper.get(TagPrefix.plate, material);
            if (!plateStack.isEmpty()) {
                GTRecipeTypes.CUTTER_RECIPES.recipeBuilder(id + "plate")
                        .inputItems(blockPrefix, material)
                        .outputItems(GTUtil.copyAmount((int) (materialAmount / M), plateStack))
                        .duration((int) (material.getMass() * 8L)).EUt(VA[LV])
                        .save(provider);
            }
        }

        UnificationEntry blockEntry;
        if (material.hasProperty(PropertyKey.GEM)) {
            blockEntry = new UnificationEntry(TagPrefix.gem, material);
        } else if (material.hasProperty(PropertyKey.INGOT)) {
            blockEntry = new UnificationEntry(TagPrefix.ingot, material);
        } else {
            blockEntry = new UnificationEntry(TagPrefix.dust, material);
        }

        ArrayList<Object> result = new ArrayList<>();
        for (int index = 0; index < materialAmount / M; index++) {
            result.add(blockEntry);
        }

        //do not allow hand crafting or uncrafting, extruding or alloy smelting of blacklisted blocks
        if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_RECIPES)) {

            //do not allow hand crafting or uncrafting of blacklisted blocks
            if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES) && !ConfigHolder.recipes.disableManualCompression) {
                VanillaRecipeHelper.addShapelessRecipe(provider, String.format("block_compress_%s", material), blockStack, result.toArray());

                VanillaRecipeHelper.addShapelessRecipe(provider, String.format("block_decompress_%s", material),
                        GTUtil.copyAmount((int) (materialAmount / M), ChemicalHelper.get(blockEntry.tagPrefix, blockEntry.material)),
                        new UnificationEntry(blockPrefix, material));
            }

            if (material.hasProperty(PropertyKey.INGOT)) {
                int voltageMultiplier = getVoltageMultiplier(material);
                GTRecipeTypes.EXTRUDER_RECIPES.recipeBuilder(id + "extruder_block")
                        .inputItems(TagPrefix.ingot, material, (int) (materialAmount / M))
                        .notConsumable(GTItems.SHAPE_EXTRUDER_BLOCK.asStack())
                        .outputItems(blockStack)
                        .duration(10).EUt(8L * voltageMultiplier)
                        .save(provider);

                GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(id + "mold_block")
                        .inputItems(TagPrefix.ingot, material, (int) (materialAmount / M))
                        .notConsumable(GTItems.SHAPE_MOLD_BLOCK.asStack())
                        .outputItems(blockStack)
                        .duration(5).EUt(4L * voltageMultiplier)
                        .save(provider);
            } else if (material.hasProperty(PropertyKey.GEM)) {
                GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder(id + "gem")
                        .inputItems(gem, material, (int) (block.getMaterialAmount(material) / M))
                        .outputItems(block, material)
                        .duration(300).EUt(2).save(provider);

                GTRecipeTypes.FORGE_HAMMER_RECIPES.recipeBuilder(id + "gem")
                        .inputItems(block, material)
                        .outputItems(gem, material, (int) (block.getMaterialAmount(material) / M))
                        .duration(100).EUt(24).save(provider);
            }
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
