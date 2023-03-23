package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CommonTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;

public class ToolRecipeHandler {

    // todo electric tools
    //public static Map<Integer, ItemEntry<? extends Item>> motorItems = new HashMap<>();
    //public static Map<Integer, Material> baseMaterials = new HashMap<>();
    //public static Map<Integer, List<ItemEntry<? extends Item>>> batteryItems = new HashMap<>();
    //public static Map<Integer, ItemEntry<? extends Item>> powerUnitItems = new HashMap<>();

    public static void init(Consumer<FinishedRecipe> provider) {
        TagPrefix.plate.executeHandler(PropertyKey.TOOL, (tagPrefix, material, property) -> processTool(tagPrefix, material, property, provider));
        //TagPrefix.plate.executeHandler(PropertyKey.TOOL, ToolRecipeHandler::processElectricTool);
        registerCustomToolRecipes(provider);
    }
/*
    public static void initializeGTItems() {
        motorItems.put(GTValues.LV, GTItems.ELECTRIC_MOTOR_LV);
        motorItems.put(GTValues.MV, GTItems.ELECTRIC_MOTOR_MV);
        motorItems.put(GTValues.HV, GTItems.ELECTRIC_MOTOR_HV);
        motorItems.put(GTValues.EV, GTItems.ELECTRIC_MOTOR_EV);
        motorItems.put(GTValues.IV, GTItems.ELECTRIC_MOTOR_IV);

        baseMaterials.put(GTValues.LV, GTMaterials.Steel);
        baseMaterials.put(GTValues.MV, GTMaterials.Aluminium);
        baseMaterials.put(GTValues.HV, GTMaterials.StainlessSteel);
        baseMaterials.put(GTValues.EV, GTMaterials.Titanium);
        baseMaterials.put(GTValues.IV, GTMaterials.TungstenSteel);

        powerUnitItems.put(GTValues.LV, GTItems.POWER_UNIT_LV);
        powerUnitItems.put(GTValues.MV, GTItems.POWER_UNIT_MV);
        powerUnitItems.put(GTValues.HV, GTItems.POWER_UNIT_HV);
        powerUnitItems.put(GTValues.EV, GTItems.POWER_UNIT_EV);
        powerUnitItems.put(GTValues.IV, GTItems.POWER_UNIT_IV);

        batteryItems.put(GTValues.ULV, Collections.singletonList(GTItems.BATTERY_ULV_TANTALUM));
        batteryItems.put(GTValues.LV, ImmutableList.of(GTItems.BATTERY_LV_LITHIUM, GTItems.BATTERY_LV_CADMIUM, GTItems.BATTERY_LV_SODIUM));
        batteryItems.put(GTValues.MV, ImmutableList.of(GTItems.BATTERY_MV_LITHIUM, GTItems.BATTERY_MV_CADMIUM, GTItems.BATTERY_MV_SODIUM));
        batteryItems.put(GTValues.HV, ImmutableList.of(GTItems.BATTERY_HV_LITHIUM, GTItems.BATTERY_HV_CADMIUM, GTItems.BATTERY_HV_SODIUM, GTItems.ENERGIUM_CRYSTAL));
        batteryItems.put(GTValues.EV, ImmutableList.of(GTItems.BATTERY_EV_VANADIUM, GTItems.LAPOTRON_CRYSTAL));
        batteryItems.put(GTValues.IV, ImmutableList.of(GTItems.BATTERY_IV_VANADIUM, GTItems.ENERGY_LAPOTRONIC_ORB));
        batteryItems.put(GTValues.LuV, ImmutableList.of(GTItems.BATTERY_LUV_VANADIUM, GTItems.ENERGY_LAPOTRONIC_ORB_CLUSTER));
        batteryItems.put(GTValues.ZPM, ImmutableList.of(GTItems.BATTERY_ZPM_NAQUADRIA, GTItems.ENERGY_MODULE));
        batteryItems.put(GTValues.UV, ImmutableList.of(GTItems.BATTERY_UV_NAQUADRIA, GTItems.ENERGY_CLUSTER));

        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, ToolItems.DRILL_LV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, ToolItems.DRILL_MV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, ToolItems.DRILL_HV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, ToolItems.DRILL_EV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadDrill, ToolItems.DRILL_IV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadChainsaw, ToolItems.CHAINSAW_LV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWrench, ToolItems.WRENCH_LV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWrench, ToolItems.WRENCH_HV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadWrench, ToolItems.WRENCH_IV);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadBuzzSaw, ToolItems.BUZZSAW);
        //ToolHeadReplaceRecipe.setToolHeadForTool(toolHeadScrewdriver, ToolItems.SCREWDRIVER_LV);

        //ForgeRegistries.RECIPES.register(new ToolHeadReplaceRecipe().setRegistryName(new ResourceLocation(MODID, "replacetoolhead")));
    }

    public static void registerPowerUnitRecipes(Consumer<FinishedRecipe> provider) {

        for (int tier : powerUnitItems.keySet()) {
            List<ItemEntry<? extends Item>> tieredBatteryItems = batteryItems.get(tier);
            for (ItemEntry<? extends Item> batteryItem : tieredBatteryItems) {
                ItemStack batteryStack = batteryItem.getStackForm();
                long maxCharge = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null).getMaxCharge();
                ItemStack powerUnitStack = powerUnitItems.get(tier).getMaxChargeOverrideStack(maxCharge);
                String recipeName = String.format("%s_%s", powerUnitItems.get(tier).unlocalizedName, batteryItem.unlocalizedName);

                ModHandler.addShapedEnergyTransferRecipe(recipeName, powerUnitStack,
                        Ingredient.fromStacks(batteryStack), true, false,
                        "S d", "GMG", "PBP",
                        'M', motorItems.get(tier).getStackForm(),
                        'S', new UnificationEntry(screw, baseMaterials.get(tier)),
                        'P', new UnificationEntry(plate, baseMaterials.get(tier)),
                        'G', new UnificationEntry(gearSmall, baseMaterials.get(tier)),
                        'B', batteryStack);
            }
        }

    }
*/
    private static void processTool(TagPrefix prefix, Material material, ToolProperty property, Consumer<FinishedRecipe> provider) {
        UnificationEntry stick = new UnificationEntry(TagPrefix.stick, GTMaterials.Wood);
        UnificationEntry plate = new UnificationEntry(TagPrefix.plate, material);
        UnificationEntry ingot = new UnificationEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material);

        if (material.hasFlag(GENERATE_PLATE)) {
            //addToolRecipe(provider, material, GTToolType.MINING_HAMMER, true,
            //        "PPf", "PPS", "PPh",
            //        'P', plate,
            //        'S', stick);

            //addToolRecipe(provider, material, GTToolType.SPADE, false,
            //        "fPh", "PSP", " S ",
            //        'P', plate,
            //        'S', stick);

            addToolRecipe(provider, material, GTToolType.SAW, false,
                    "PPS", "fhS",
                    'P', plate,
                    'S', stick);

            //addToolRecipe(provider, material, GTToolType.AXE, false,
            //        "PIh", "PS ", "fS ",
            //        'P', plate,
            //        'I', ingot,
            //        'S', stick);

            //addToolRecipe(provider, material, GTToolType.HOE, false,
            //        "PIh", "fS ", " S ",
            //        'P', plate,
            //        'I', ingot,
            //        'S', stick);

            //addToolRecipe(provider, material, GTToolType.PICKAXE, false,
            //        "PII", "fSh", " S ",
            //        'P', plate,
            //        'I', ingot,
            //        'S', stick);

            addToolRecipe(provider, material, GTToolType.SCYTHE, false,
                    "PPI", "fSh", " S ",
                    'P', plate,
                    'I', ingot,
                    'S', stick);

            //addToolRecipe(provider, material, GTToolType.SHOVEL, false,
            //        "fPh", " S ", " S ",
            //        'P', plate,
            //        'S', stick);

            //addToolRecipe(provider, material, GTToolType.SWORD, false,
            //        " P ", "fPh", " S ",
            //        'P', plate,
            //        'S', stick);

            addToolRecipe(provider, material, GTToolType.HARD_HAMMER, true,
                    "II ", "IIS", "II ",
                    'I', ingot,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.FILE, true,
                    " P ", " P " , " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.KNIFE, false,
                    "fPh", " S ",
                    'P', plate,
                    'S', stick);

            addToolRecipe(provider, material, GTToolType.WRENCH, false,
                    "PhP", " P ", " P ",
                    'P', plate);
        }

        if (material.hasFlag(GENERATE_ROD)) {
            UnificationEntry rod = new UnificationEntry(TagPrefix.stick, material);

            if (material.hasFlag(GENERATE_PLATE)) {
                addToolRecipe(provider, material, GTToolType.BUTCHERY_KNIFE, false,
                        "PPf", "PP ", "Sh ",
                        'P', plate,
                        'S', rod);

                if (material.hasFlag(GENERATE_BOLT_SCREW)) {
                    addToolRecipe(provider, material, GTToolType.WIRE_CUTTER, false,
                            "PfP", "hPd", "STS",
                            'P', plate,
                            'T', new UnificationEntry(TagPrefix.screw, material),
                            'S', rod);
                }
            }

            addToolRecipe(provider, material, GTToolType.SCREWDRIVER, true,
                    " fS", " Sh", "W  ",
                    'S', rod,
                    'W', stick);

            addToolRecipe(provider, material, GTToolType.CROWBAR, true,
                    "hDS", "DSD", "SDf",
                    'S', rod,
                    'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Lapis)); // todo blue dyes
                    //'D', new UnificationEntry(dye, MarkerMaterials.Color.Blue));
        }
    }
/*
    private static void processElectricTool(TagPrefix prefix, Material material, ToolProperty property) {
        final int voltageMultiplier = material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
        TagPrefix toolPrefix;

        if (material.hasFlag(GENERATE_PLATE)) {
            final UnificationEntry plate = new UnificationEntry(TagPrefix.plate, material);
            final UnificationEntry steelPlate = new UnificationEntry(TagPrefix.plate, GTMaterials.Steel);
            final UnificationEntry steelRing = new UnificationEntry(ring, GTMaterials.Steel);

            // drill
            toolPrefix = toolHeadDrill;
            ModHandler.addShapedRecipe(String.format("drill_head_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "XSX", "XSX", "ShS",
                    'X', plate,
                    'S', steelPlate);

            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.DRILL_LV, ToolItems.DRILL_MV, ToolItems.DRILL_HV, ToolItems.DRILL_EV, ToolItems.DRILL_IV});

            // chainsaw
            toolPrefix = toolHeadChainsaw;
            ModHandler.addShapedRecipe(String.format("chainsaw_head_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "SRS", "XhX", "SRS",
                    'X', plate,
                    'S', steelPlate,
                    'R', steelRing);

            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.CHAINSAW_LV});

            // wrench
            toolPrefix = toolHeadWrench;
            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.WRENCH_LV, ToolItems.WRENCH_HV, ToolItems.WRENCH_IV});

            ModHandler.addShapedRecipe(String.format("wrench_head_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "hXW", "XRX", "WXd",
                    'X', plate,
                    'R', steelRing,
                    'W', new UnificationEntry(screw, Materials.Steel));

            // buzzsaw
            toolPrefix = toolHeadBuzzSaw;
            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.BUZZSAW});

            ModHandler.addShapedRecipe(String.format("buzzsaw_blade_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "sXh", "X X", "fXx",
                    'X', plate);

            if (material.hasFlag(GENERATE_GEAR)) {
                LATHE_RECIPES.recipeBuilder()
                        .input(gear, material)
                        .output(toolPrefix, material)
                        .duration((int) material.getMass() * 4)
                        .EUt(8L * voltageMultiplier)
                        .buildAndRegister();
            }
        }

        // screwdriver
        if (material.hasFlag(GENERATE_LONG_ROD)) {
            toolPrefix = toolHeadScrewdriver;
            addElectricToolRecipe(toolPrefix, material, new IGTTool[]{ToolItems.SCREWDRIVER_LV});

            ModHandler.addShapedRecipe(String.format("screwdriver_tip_%s", material),
                    OreDictUnifier.get(toolPrefix, material),
                    "fR", " h",
                    'R', new UnificationEntry(stickLong, material));
        }
    }

    public static void addElectricToolRecipe(OrePrefix toolHead, Material material, IGTTool[] toolItems) {
        for (IGTTool toolItem : toolItems) {
            int tier = toolItem.getElectricTier();
            ItemStack powerUnitStack = powerUnitItems.get(tier).getStackForm();
            IElectricItem powerUnit = powerUnitStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            ItemStack tool = toolItem.get(material, 0, powerUnit.getMaxCharge());
            ModHandler.addShapedEnergyTransferRecipe(String.format("%s_%s", toolItem.getId(), material),
                    tool,
                    Ingredient.fromStacks(powerUnitStack), true, true,
                    "wHd", " U ",
                    'H', new UnificationEntry(toolHead, material),
                    'U', powerUnitStack);
        }
    }
*/
    public static void addToolRecipe(Consumer<FinishedRecipe> provider, @Nonnull Material material, @Nonnull GTToolType tool, boolean mirrored, Object... recipe) {
        ItemStack toolStack = ToolHelper.get(tool, material);
        if (toolStack.isEmpty()) return;
        if (mirrored) { // todo mirrored
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_%s", tool.name, material),
                    toolStack, recipe);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_%s", tool.name, material),
                    toolStack, recipe);
        }
    }

    public static void registerCustomToolRecipes(Consumer<FinishedRecipe> provider) {
        registerFlintToolRecipes(provider);
        registerMortarRecipes(provider);
        registerSoftToolRecipes(provider);
        //registerElectricRecipes(provider);
    }

    private static void registerFlintToolRecipes(Consumer<FinishedRecipe> provider) {
        final UnificationEntry flint = new UnificationEntry(TagPrefix.gem, GTMaterials.Flint);
        final ItemStack stick = new ItemStack(Items.STICK);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.MORTAR, false,
                " I ", "SIS", "SSS",
                'I', flint,
                'S', new ItemStack(Blocks.STONE));

        //addToolRecipe(provider, GTMaterials.Flint, GTToolType.SWORD, false,
        //        "I", "I", "S",
        //        'I', flint,
        //        'S', stick);

        //addToolRecipe(provider, GTMaterials.Flint, GTToolType.PICKAXE, false,
        //        "III", " S ", " S ",
        //        'I', flint,
        //        'S', stick);

        //addToolRecipe(provider, GTMaterials.Flint, GTToolType.SHOVEL, false,
        //        "I", "S", "S",
        //        'I', flint,
        //        'S', stick);

        //addToolRecipe(provider, GTMaterials.Flint, GTToolType.AXE, true,
        //        "II", "IS", " S",
        //        'I', flint,
        //        'S', stick);

        //addToolRecipe(provider, GTMaterials.Flint, GTToolType.HOE, true,
        //        "II", " S", " S",
        //        'I', flint,
        //        'S', stick);

        addToolRecipe(provider, GTMaterials.Flint, GTToolType.KNIFE, false,
                "I", "S",
                'I', flint,
                'S', stick);
    }

    private static void registerMortarRecipes(Consumer<FinishedRecipe> provider) {
        for (Material material : new Material[]{
                GTMaterials.Bronze, GTMaterials.Iron, GTMaterials.Invar, GTMaterials.Steel,
                GTMaterials.DamascusSteel, GTMaterials.CobaltBrass, GTMaterials.WroughtIron }) {

            addToolRecipe(provider, material, GTToolType.MORTAR, false,
                    " I ", "SIS", "SSS",
                    'I', new UnificationEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material),
                    'S', new ItemStack(Blocks.STONE));
        }
    }

    private static void registerSoftToolRecipes(Consumer<FinishedRecipe> provider) {
        final Material[] softMaterials = new Material[]{
                GTMaterials.Wood, GTMaterials.Rubber, GTMaterials.Polyethylene,
                GTMaterials.Polytetrafluoroethylene, GTMaterials.Polybenzimidazole
        };

        final ItemStack stick = new ItemStack(Items.STICK);

        for (int i = 0; i < softMaterials.length; i++) {
            Material material = softMaterials[i];

            if (material == GTMaterials.Wood) {
                // todo allow these 3 to be mirrored
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("soft_mallet_%s", material),
                        ToolHelper.get(GTToolType.SOFT_MALLET, material),
                        "II ", "IIS", "II ",
                        'I', CommonTags.TAG_PLANKS,
                        'S', stick);
            } else {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("soft_mallet_%s", material),
                        ToolHelper.get(GTToolType.SOFT_MALLET, material),
                        "II ", "IIS", "II ",
                        'I', new UnificationEntry(TagPrefix.ingot, material),
                        'S', stick);

                // todo fix plunger
                //ModHandler.addMirroredShapedRecipe(String.format("plunger_%s", material),
                //        ToolHelper.getAndSetToolData(GTToolType.PLUNGER, material, 128 * (i << 1), 1, 4F, 0F),
                //        "xPP", " SP", "S f",
                //        'P', new UnificationEntry(TagPrefix.plate, material),
                //        'S', stick);
            }
        }
    }
/*
    private static void registerElectricRecipes(Consumer<FinishedRecipe> provider) {

        for (MetaValueItem batteryItem : batteryItems.get(LV)) {
            ModHandler.addShapedEnergyTransferRecipe("prospector_lv_" + batteryItem.unlocalizedName, GTItems.PROSPECTOR_LV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_LV.getStackForm(),
                    'P', new UnificationEntry(plate, Materials.Steel),
                    'S', GTItems.SENSOR_LV.getStackForm(),
                    'D', new UnificationEntry(plate, Materials.Glass),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.LV),
                    'B', batteryItem.getStackForm());

            ModHandler.addShapedEnergyTransferRecipe("magnet_lv_" + batteryItem.unlocalizedName, GTItems.ITEM_MAGNET_LV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "MwM", "MBM", "CPC",
                    'M', new UnificationEntry(stick, Materials.SteelMagnetic),
                    'P', new UnificationEntry(plate, Materials.Steel),
                    'C', new UnificationEntry(cableGtSingle, Materials.Tin),
                    'B', batteryItem.getStackForm());
        }

        for (MetaValueItem batteryItem : batteryItems.get(MV)) {
            ModHandler.addShapedEnergyTransferRecipe("tricorder_" + batteryItem.unlocalizedName, GTItems.TRICORDER_SCANNER.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_MV.getStackForm(),
                    'P', new UnificationEntry(plate, Materials.Aluminium),
                    'S', GTItems.SENSOR_MV.getStackForm(),
                    'D', GTItems.COVER_SCREEN.getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                    'B', batteryItem.getStackForm());
        }

        for (MetaValueItem batteryItem : batteryItems.get(HV)) {
            ModHandler.addShapedEnergyTransferRecipe("prospector_hv_" + batteryItem.unlocalizedName, GTItems.PROSPECTOR_HV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_HV.getStackForm(),
                    'P', new UnificationEntry(plate, Materials.StainlessSteel),
                    'S', GTItems.SENSOR_HV.getStackForm(),
                    'D', GTItems.COVER_SCREEN.getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                    'B', batteryItem.getStackForm());

            ModHandler.addShapedEnergyTransferRecipe("magnet_hv_" + batteryItem.unlocalizedName, GTItems.ITEM_MAGNET_HV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "MwM", "MBM", "CPC",
                    'M', new UnificationEntry(stick, Materials.NeodymiumMagnetic),
                    'P', new UnificationEntry(plate, Materials.StainlessSteel),
                    'C', new UnificationEntry(cableGtSingle, Materials.Gold),
                    'B', batteryItem.getStackForm());
        }

        for (ItemEntry<? extends Item> batteryItem : batteryItems.get(LuV)) {
            ModHandler.addShapedEnergyTransferRecipe("prospector_luv_" + batteryItem.unlocalizedName, GTItems.PROSPECTOR_LUV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "EPS", "CDC", "PBP",
                    'E', GTItems.EMITTER_LuV.asStack(),
                    'P', new UnificationEntry(plate, GTMaterials.RhodiumPlatedPalladium),
                    'S', GTItems.SENSOR_LuV.asStack(),
                    'D', GTItems.COVER_SCREEN.asStack(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.LuV),
                    'B', batteryItem.asStack());
        }
    }
 */
}
