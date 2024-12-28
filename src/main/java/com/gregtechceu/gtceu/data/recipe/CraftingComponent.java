package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.event.CraftingComponentModificationEvent;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.CraftingComponentsEventJS;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class CraftingComponent {

    public static Component CIRCUIT;
    public static Component BETTER_CIRCUIT;
    public static Component PUMP;
    public static Component WIRE_ELECTRIC;
    public static Component WIRE_QUAD;
    public static Component WIRE_OCT;
    public static Component WIRE_HEX;
    public static Component CABLE;
    public static Component CABLE_DOUBLE;
    public static Component CABLE_QUAD;
    public static Component CABLE_OCT;
    public static Component CABLE_HEX;
    public static Component CABLE_TIER_UP;
    public static Component CABLE_TIER_UP_DOUBLE;
    public static Component CABLE_TIER_UP_QUAD;
    public static Component CABLE_TIER_UP_OCT;
    public static Component CABLE_TIER_UP_HEX;
    public static Component CASING;
    public static Component HULL;
    public static Component PIPE_NORMAL;
    public static Component PIPE_LARGE;
    public static Component PIPE_NONUPLE;
    public static Component GLASS;
    public static Component PLATE;
    public static Component HULL_PLATE;
    public static Component MOTOR;
    public static Component ROTOR;
    public static Component SENSOR;
    public static Component GRINDER;
    public static Component SAWBLADE;
    public static Component DIAMOND;
    public static Component PISTON;
    public static Component EMITTER;
    public static Component CONVEYOR;
    public static Component ROBOT_ARM;
    public static Component COIL_HEATING;
    public static Component COIL_HEATING_DOUBLE;
    public static Component COIL_ELECTRIC;
    public static Component STICK_MAGNETIC;
    public static Component STICK_DISTILLATION;
    public static Component FIELD_GENERATOR;
    public static Component STICK_ELECTROMAGNETIC;
    public static Component STICK_RADIOACTIVE;
    public static Component PIPE_REACTOR;
    public static Component POWER_COMPONENT;
    public static Component VOLTAGE_COIL;
    public static Component SPRING;
    public static Component CRATE;
    public static Component DRUM;
    public static Component FRAME;

    public static final Map<BlastProperty.GasTier, FluidIngredient> EBF_GASES = new EnumMap<>(
            BlastProperty.GasTier.class);

    static {
        EBF_GASES.put(BlastProperty.GasTier.LOW, FluidIngredient.of(1000, GTMaterials.Nitrogen.getFluid()));
        EBF_GASES.put(BlastProperty.GasTier.MID, FluidIngredient.of(100, GTMaterials.Helium.getFluid()));
        EBF_GASES.put(BlastProperty.GasTier.HIGH, FluidIngredient.of(50, GTMaterials.Argon.getFluid()));
        EBF_GASES.put(BlastProperty.GasTier.HIGHER, FluidIngredient.of(25, GTMaterials.Neon.getFluid()));
        EBF_GASES.put(BlastProperty.GasTier.HIGHEST, FluidIngredient.of(10, GTMaterials.Krypton.getFluid()));
    }

    public static void initializeComponents() {
        /*
         * GTCEu must supply values for at least tiers 1 through 8 (through UV)
         */
        CIRCUIT = new Component(Stream.of(new Object[][] {

                { 0, CustomTags.ULV_CIRCUITS },
                { 1, CustomTags.LV_CIRCUITS },
                { 2, CustomTags.MV_CIRCUITS },
                { 3, CustomTags.HV_CIRCUITS },
                { 4, CustomTags.EV_CIRCUITS },
                { 5, CustomTags.IV_CIRCUITS },
                { 6, CustomTags.LuV_CIRCUITS },
                { 7, CustomTags.ZPM_CIRCUITS },
                { 8, CustomTags.UV_CIRCUITS },
                { 9, CustomTags.UHV_CIRCUITS },
                { 10, CustomTags.UEV_CIRCUITS },
                { 11, CustomTags.UIV_CIRCUITS },
                { 12, CustomTags.UXV_CIRCUITS },
                { 13, CustomTags.OpV_CIRCUITS },
                { 14, CustomTags.MAX_CIRCUITS },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        BETTER_CIRCUIT = new Component(Stream.of(new Object[][] {

                { 0, CustomTags.LV_CIRCUITS },
                { 1, CustomTags.MV_CIRCUITS },
                { 2, CustomTags.HV_CIRCUITS },
                { 3, CustomTags.EV_CIRCUITS },
                { 4, CustomTags.IV_CIRCUITS },
                { 5, CustomTags.LuV_CIRCUITS },
                { 6, CustomTags.ZPM_CIRCUITS },
                { 7, CustomTags.UV_CIRCUITS },
                { 8, CustomTags.UHV_CIRCUITS },
                { 10, CustomTags.UEV_CIRCUITS },
                { 11, CustomTags.UIV_CIRCUITS },
                { 12, CustomTags.UXV_CIRCUITS },
                { 13, CustomTags.OpV_CIRCUITS },
                { 14, CustomTags.MAX_CIRCUITS },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PUMP = new Component(Stream.of(new Object[][] {

                { 1, GTItems.ELECTRIC_PUMP_LV.asStack() },
                { 2, GTItems.ELECTRIC_PUMP_MV.asStack() },
                { 3, GTItems.ELECTRIC_PUMP_HV.asStack() },
                { 4, GTItems.ELECTRIC_PUMP_EV.asStack() },
                { 5, GTItems.ELECTRIC_PUMP_IV.asStack() },
                { 6, GTItems.ELECTRIC_PUMP_LuV.asStack() },
                { 7, GTItems.ELECTRIC_PUMP_ZPM.asStack() },
                { 8, GTItems.ELECTRIC_PUMP_UV.asStack() },
                { FALLBACK, GTItems.ELECTRIC_PUMP_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            PUMP.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.ELECTRIC_PUMP_UHV.asStack() },
                    { 10, GTItems.ELECTRIC_PUMP_UEV.asStack() },
                    { 11, GTItems.ELECTRIC_PUMP_UIV.asStack() },
                    { 12, GTItems.ELECTRIC_PUMP_UXV.asStack() },
                    { 13, GTItems.ELECTRIC_PUMP_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        WIRE_ELECTRIC = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Gold) },
                { 1, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Gold) },
                { 2, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Silver) },
                { 3, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Electrum) },
                { 4, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Platinum) },
                { 5, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium) },
                { 6, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium) },
                { 7, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium) },
                { 8, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium) },
                { 9, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_QUAD = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Lead) },
                { 1, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Tungsten) },
                { 6, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_OCT = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Lead) },
                { 1, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Tungsten) },
                { 6, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_HEX = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Lead) },
                { 1, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Tungsten) },
                { 6, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.RedAlloy) },
                { 1, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Platinum) },
                { 6, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_DOUBLE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.RedAlloy) },
                { 1, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Platinum) },
                { 6, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_QUAD = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.RedAlloy) },
                { 1, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Platinum) },
                { 6, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_OCT = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.RedAlloy) },
                { 1, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Platinum) },
                { 6, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_HEX = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.RedAlloy) },
                { 1, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Platinum) },
                { 6, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin) },
                { 1, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold) },
                { 3, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium) },
                { 4, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Platinum) },
                { 5, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium) },
                { 6, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium) },
                { 7, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate) },
                { 8, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_DOUBLE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Tin) },
                { 1, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Gold) },
                { 3, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Aluminium) },
                { 4, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Platinum) },
                { 5, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.NiobiumTitanium) },
                { 6, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.VanadiumGallium) },
                { 7, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.YttriumBariumCuprate) },
                { 8, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_QUAD = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Tin) },
                { 1, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Gold) },
                { 3, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Aluminium) },
                { 4, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Platinum) },
                { 5, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.NiobiumTitanium) },
                { 6, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.VanadiumGallium) },
                { 7, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.YttriumBariumCuprate) },
                { 8, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_OCT = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Tin) },
                { 1, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Gold) },
                { 3, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Aluminium) },
                { 4, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Platinum) },
                { 5, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.NiobiumTitanium) },
                { 6, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.VanadiumGallium) },
                { 7, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.YttriumBariumCuprate) },
                { 8, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_HEX = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Tin) },
                { 1, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Gold) },
                { 3, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Aluminium) },
                { 4, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Platinum) },
                { 5, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.NiobiumTitanium) },
                { 6, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.VanadiumGallium) },
                { 7, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.YttriumBariumCuprate) },
                { 8, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Europium) },
                { FALLBACK, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        HULL = new Component(Stream.of(new Object[][] {

                { 0, GTMachines.HULL[0].asStack() },
                { 1, GTMachines.HULL[1].asStack() },
                { 2, GTMachines.HULL[2].asStack() },
                { 3, GTMachines.HULL[3].asStack() },
                { 4, GTMachines.HULL[4].asStack() },
                { 5, GTMachines.HULL[5].asStack() },
                { 6, GTMachines.HULL[6].asStack() },
                { 7, GTMachines.HULL[7].asStack() },
                { 8, GTMachines.HULL[8].asStack() },
                { 9, GTMachines.HULL[9].asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            HULL.appendIngredients(Stream.of(new Object[][] {
                    { 10, GTMachines.HULL[10].asStack() },
                    { 11, GTMachines.HULL[11].asStack() },
                    { 12, GTMachines.HULL[12].asStack() },
                    { 13, GTMachines.HULL[13].asStack() },
                    { 14, GTMachines.HULL[14].asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        CASING = new Component(Stream.of(new Object[][] {

                { 0, GTBlocks.MACHINE_CASING_ULV.asStack() },
                { 1, GTBlocks.MACHINE_CASING_LV.asStack() },
                { 2, GTBlocks.MACHINE_CASING_MV.asStack() },
                { 3, GTBlocks.MACHINE_CASING_HV.asStack() },
                { 4, GTBlocks.MACHINE_CASING_EV.asStack() },
                { 5, GTBlocks.MACHINE_CASING_IV.asStack() },
                { 6, GTBlocks.MACHINE_CASING_LuV.asStack() },
                { 7, GTBlocks.MACHINE_CASING_ZPM.asStack() },
                { 8, GTBlocks.MACHINE_CASING_UV.asStack() },
                { 9, GTBlocks.MACHINE_CASING_UHV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            CASING.appendIngredients(Stream.of(new Object[][] {
                    { 10, GTBlocks.MACHINE_CASING_UEV.asStack() },
                    { 11, GTBlocks.MACHINE_CASING_UIV.asStack() },
                    { 12, GTBlocks.MACHINE_CASING_UXV.asStack() },
                    { 13, GTBlocks.MACHINE_CASING_OpV.asStack() },
                    { 14, GTBlocks.MACHINE_CASING_MAX.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        PIPE_NORMAL = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze) },
                { 1, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze) },
                { 2, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel) },
                { 3, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel) },
                { 4, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Titanium) },
                { 5, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.TungstenSteel) },
                { 6, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Iridium) },
                { 8, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Naquadah) },
                { FALLBACK, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Naquadah) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_LARGE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Bronze) },
                { 1, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Bronze) },
                { 2, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Steel) },
                { 3, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel) },
                { 4, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Titanium) },
                { 5, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.TungstenSteel) },
                { 6, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Ultimet) },
                { 8, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Naquadah) },
                { FALLBACK, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Neutronium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_NONUPLE = new Component(Stream.of(new Object[][] {

                { 4, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Titanium) },
                { 5, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.TungstenSteel) },
                { 6, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Iridium) },
                { 8, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Naquadah) },
                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Neutronium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        /*
         * Glass: Steam-MV
         * Tempered: HV, EV
         * Laminated Glass: IV, LuV
         * Fusion: ZPM, UV
         */
        GLASS = new Component(Stream.of(new Object[][] {

                { GTValues.FALLBACK, Tags.Items.GLASS },
                { ULV, Tags.Items.GLASS },
                { LV, Tags.Items.GLASS },
                { MV, Tags.Items.GLASS },
                { HV, GTBlocks.CASING_TEMPERED_GLASS.asStack() },
                { EV, GTBlocks.CASING_TEMPERED_GLASS.asStack() },
                { IV, GTBlocks.CASING_LAMINATED_GLASS.asStack() },
                { LuV, GTBlocks.CASING_LAMINATED_GLASS.asStack() },
                { ZPM, GTBlocks.FUSION_GLASS.asStack() },
                { UV, GTBlocks.FUSION_GLASS.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PLATE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron) },
                { 1, new UnificationEntry(TagPrefix.plate, GTMaterials.Steel) },
                { 2, new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium) },
                { 3, new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel) },
                { 4, new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium) },
                { 5, new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel) },
                { 6, new UnificationEntry(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium) },
                { 7, new UnificationEntry(TagPrefix.plate, GTMaterials.NaquadahAlloy) },
                { 8, new UnificationEntry(TagPrefix.plate, GTMaterials.Darmstadtium) },
                { 9, new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium) },
                { FALLBACK, new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        HULL_PLATE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.plate, GTMaterials.Wood) },
                { 1, new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron) },
                { 2, new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron) },
                { 3, new UnificationEntry(TagPrefix.plate, GTMaterials.Polyethylene) },
                { 4, new UnificationEntry(TagPrefix.plate, GTMaterials.Polyethylene) },
                { 5, new UnificationEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene) },
                { 6, new UnificationEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene) },
                { 7, new UnificationEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole) },
                { 8, new UnificationEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole) },
                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        MOTOR = new Component(Stream.of(new Object[][] {

                { 1, GTItems.ELECTRIC_MOTOR_LV.asStack() },
                { 2, GTItems.ELECTRIC_MOTOR_MV.asStack() },
                { 3, GTItems.ELECTRIC_MOTOR_HV.asStack() },
                { 4, GTItems.ELECTRIC_MOTOR_EV.asStack() },
                { 5, GTItems.ELECTRIC_MOTOR_IV.asStack() },
                { 6, GTItems.ELECTRIC_MOTOR_LuV.asStack() },
                { 7, GTItems.ELECTRIC_MOTOR_ZPM.asStack() },
                { 8, GTItems.ELECTRIC_MOTOR_UV.asStack() },
                { FALLBACK, GTItems.ELECTRIC_MOTOR_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            MOTOR.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.ELECTRIC_MOTOR_UHV.asStack() },
                    { 10, GTItems.ELECTRIC_MOTOR_UEV.asStack() },
                    { 11, GTItems.ELECTRIC_MOTOR_UIV.asStack() },
                    { 12, GTItems.ELECTRIC_MOTOR_UXV.asStack() },
                    { 13, GTItems.ELECTRIC_MOTOR_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        ROTOR = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin) },
                { 1, new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.rotor, GTMaterials.Bronze) },
                { 3, new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel) },
                { 4, new UnificationEntry(TagPrefix.rotor, GTMaterials.StainlessSteel) },
                { 5, new UnificationEntry(TagPrefix.rotor, GTMaterials.TungstenSteel) },
                { 6, new UnificationEntry(TagPrefix.rotor, GTMaterials.RhodiumPlatedPalladium) },
                { 7, new UnificationEntry(TagPrefix.rotor, GTMaterials.NaquadahAlloy) },
                { 8, new UnificationEntry(TagPrefix.rotor, GTMaterials.Darmstadtium) },
                { FALLBACK, new UnificationEntry(TagPrefix.rotor, GTMaterials.Darmstadtium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        SENSOR = new Component(Stream.of(new Object[][] {

                { 1, GTItems.SENSOR_LV.asStack() },
                { 2, GTItems.SENSOR_MV.asStack() },
                { 3, GTItems.SENSOR_HV.asStack() },
                { 4, GTItems.SENSOR_EV.asStack() },
                { 5, GTItems.SENSOR_IV.asStack() },
                { 6, GTItems.SENSOR_LuV.asStack() },
                { 7, GTItems.SENSOR_ZPM.asStack() },
                { 8, GTItems.SENSOR_UV.asStack() },
                { FALLBACK, GTItems.SENSOR_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            SENSOR.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.SENSOR_UHV.asStack() },
                    { 10, GTItems.SENSOR_UEV.asStack() },
                    { 11, GTItems.SENSOR_UIV.asStack() },
                    { 12, GTItems.SENSOR_UXV.asStack() },
                    { 13, GTItems.SENSOR_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        GRINDER = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond) },
                { 1, new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond) },
                { 2, new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond) },
                { 3, GTItems.COMPONENT_GRINDER_DIAMOND.asStack() },
                { 4, GTItems.COMPONENT_GRINDER_DIAMOND.asStack() },
                { 5, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack() },
                { GTValues.FALLBACK, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        SAWBLADE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Bronze) },
                { 1, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.CobaltBrass) },
                { 2, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.VanadiumSteel) },
                { 3, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.RedSteel) },
                { 4, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Ultimet) },
                { 5, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.TungstenCarbide) },
                { 6, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.HSSE) },
                { 7, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.NaquadahAlloy) },
                { 8, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Duranium) },
                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Duranium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        DIAMOND = new Component(Stream.of(new Object[][] {

                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PISTON = new Component(Stream.of(new Object[][] {

                { 1, GTItems.ELECTRIC_PISTON_LV.asStack() },
                { 2, GTItems.ELECTRIC_PISTON_MV.asStack() },
                { 3, GTItems.ELECTRIC_PISTON_HV.asStack() },
                { 4, GTItems.ELECTRIC_PISTON_EV.asStack() },
                { 5, GTItems.ELECTRIC_PISTON_IV.asStack() },
                { 6, GTItems.ELECTRIC_PISTON_LuV.asStack() },
                { 7, GTItems.ELECTRIC_PISTON_ZPM.asStack() },
                { 8, GTItems.ELECTRIC_PISTON_UV.asStack() },
                { FALLBACK, GTItems.ELECTRIC_PISTON_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            PISTON.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.ELECTRIC_PISTON_UHV.asStack() },
                    { 10, GTItems.ELECTRIC_PISTON_UEV.asStack() },
                    { 11, GTItems.ELECTRIC_PISTON_UIV.asStack() },
                    { 12, GTItems.ELECTRIC_PISTON_UXV.asStack() },
                    { 13, GTItems.ELECTRIC_PISTON_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        EMITTER = new Component(Stream.of(new Object[][] {

                { 1, GTItems.EMITTER_LV.asStack() },
                { 2, GTItems.EMITTER_MV.asStack() },
                { 3, GTItems.EMITTER_HV.asStack() },
                { 4, GTItems.EMITTER_EV.asStack() },
                { 5, GTItems.EMITTER_IV.asStack() },
                { 6, GTItems.EMITTER_LuV.asStack() },
                { 7, GTItems.EMITTER_ZPM.asStack() },
                { 8, GTItems.EMITTER_UV.asStack() },
                { FALLBACK, GTItems.EMITTER_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            EMITTER.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.EMITTER_UHV.asStack() },
                    { 10, GTItems.EMITTER_UEV.asStack() },
                    { 11, GTItems.EMITTER_UIV.asStack() },
                    { 12, GTItems.EMITTER_UXV.asStack() },
                    { 13, GTItems.EMITTER_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        CONVEYOR = new Component(Stream.of(new Object[][] {

                { 1, GTItems.CONVEYOR_MODULE_LV.asStack() },
                { 2, GTItems.CONVEYOR_MODULE_MV.asStack() },
                { 3, GTItems.CONVEYOR_MODULE_HV.asStack() },
                { 4, GTItems.CONVEYOR_MODULE_EV.asStack() },
                { 5, GTItems.CONVEYOR_MODULE_IV.asStack() },
                { 6, GTItems.CONVEYOR_MODULE_LuV.asStack() },
                { 7, GTItems.CONVEYOR_MODULE_ZPM.asStack() },
                { 8, GTItems.CONVEYOR_MODULE_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            CONVEYOR.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.CONVEYOR_MODULE_UHV.asStack() },
                    { 10, GTItems.CONVEYOR_MODULE_UEV.asStack() },
                    { 11, GTItems.CONVEYOR_MODULE_UIV.asStack() },
                    { 12, GTItems.CONVEYOR_MODULE_UXV.asStack() },
                    { 13, GTItems.CONVEYOR_MODULE_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        ROBOT_ARM = new Component(Stream.of(new Object[][] {

                { 1, GTItems.ROBOT_ARM_LV.asStack() },
                { 2, GTItems.ROBOT_ARM_MV.asStack() },
                { 3, GTItems.ROBOT_ARM_HV.asStack() },
                { 4, GTItems.ROBOT_ARM_EV.asStack() },
                { 5, GTItems.ROBOT_ARM_IV.asStack() },
                { 6, GTItems.ROBOT_ARM_LuV.asStack() },
                { 7, GTItems.ROBOT_ARM_ZPM.asStack() },
                { 8, GTItems.ROBOT_ARM_UV.asStack() },
                { FALLBACK, GTItems.ROBOT_ARM_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            ROBOT_ARM.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.ROBOT_ARM_UHV.asStack() },
                    { 10, GTItems.ROBOT_ARM_UEV.asStack() },
                    { 11, GTItems.ROBOT_ARM_UIV.asStack() },
                    { 12, GTItems.ROBOT_ARM_UXV.asStack() },
                    { 13, GTItems.ROBOT_ARM_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        COIL_HEATING = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Copper) },
                { 1, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Cupronickel) },
                { 3, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Kanthal) },
                { 4, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Nichrome) },
                { 5, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.RTMAlloy) },
                { 6, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.HSSG) },
                { 7, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Naquadah) },
                { 8, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.NaquadahAlloy) },
                { FALLBACK, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Trinium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        COIL_HEATING_DOUBLE = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Copper) },
                { 1, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Cupronickel) },
                { 3, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Kanthal) },
                { 4, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Nichrome) },
                { 5, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.RTMAlloy) },
                { 6, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.HSSG) },
                { 7, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Naquadah) },
                { 8, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.NaquadahAlloy) },
                { FALLBACK, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Trinium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        COIL_ELECTRIC = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Tin) },
                { 1, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Silver) },
                { 4, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Steel) },
                { 5, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Graphene) },
                { 6, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.NiobiumNitride) },
                { 7, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.YttriumBariumCuprate) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_MAGNETIC = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.rod, GTMaterials.IronMagnetic) },
                { 1, new UnificationEntry(TagPrefix.rod, GTMaterials.IronMagnetic) },
                { 2, new UnificationEntry(TagPrefix.rod, GTMaterials.SteelMagnetic) },
                { 3, new UnificationEntry(TagPrefix.rod, GTMaterials.SteelMagnetic) },
                { 4, new UnificationEntry(TagPrefix.rod, GTMaterials.NeodymiumMagnetic) },
                { 5, new UnificationEntry(TagPrefix.rod, GTMaterials.NeodymiumMagnetic) },
                { 6, new UnificationEntry(TagPrefix.rodLong, GTMaterials.NeodymiumMagnetic) },
                { 7, new UnificationEntry(TagPrefix.rodLong, GTMaterials.NeodymiumMagnetic) },
                { 8, new UnificationEntry(TagPrefix.block, GTMaterials.NeodymiumMagnetic) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_DISTILLATION = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.rod, GTMaterials.Blaze) },
                { 1, new UnificationEntry(TagPrefix.spring, GTMaterials.Copper) },
                { 2, new UnificationEntry(TagPrefix.spring, GTMaterials.Cupronickel) },
                { 3, new UnificationEntry(TagPrefix.spring, GTMaterials.Kanthal) },
                { 4, new UnificationEntry(TagPrefix.spring, GTMaterials.Nichrome) },
                { 5, new UnificationEntry(TagPrefix.spring, GTMaterials.RTMAlloy) },
                { 6, new UnificationEntry(TagPrefix.spring, GTMaterials.HSSG) },
                { 7, new UnificationEntry(TagPrefix.spring, GTMaterials.Naquadah) },
                { 8, new UnificationEntry(TagPrefix.spring, GTMaterials.NaquadahAlloy) },
                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.rod, GTMaterials.Blaze) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        FIELD_GENERATOR = new Component(Stream.of(new Object[][] {

                { 1, GTItems.FIELD_GENERATOR_LV.asStack() },
                { 2, GTItems.FIELD_GENERATOR_MV.asStack() },
                { 3, GTItems.FIELD_GENERATOR_HV.asStack() },
                { 4, GTItems.FIELD_GENERATOR_EV.asStack() },
                { 5, GTItems.FIELD_GENERATOR_IV.asStack() },
                { 6, GTItems.FIELD_GENERATOR_LuV.asStack() },
                { 7, GTItems.FIELD_GENERATOR_ZPM.asStack() },
                { 8, GTItems.FIELD_GENERATOR_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        if (GTCEuAPI.isHighTier()) {
            FIELD_GENERATOR.appendIngredients(Stream.of(new Object[][] {
                    { 9, GTItems.FIELD_GENERATOR_UHV.asStack() },
                    { 10, GTItems.FIELD_GENERATOR_UEV.asStack() },
                    { 11, GTItems.FIELD_GENERATOR_UIV.asStack() },
                    { 12, GTItems.FIELD_GENERATOR_UXV.asStack() },
                    { 13, GTItems.FIELD_GENERATOR_OpV.asStack() },
            }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
        }

        STICK_ELECTROMAGNETIC = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.rod, GTMaterials.Iron) },
                { 1, new UnificationEntry(TagPrefix.rod, GTMaterials.Iron) },
                { 2, new UnificationEntry(TagPrefix.rod, GTMaterials.Steel) },
                { 3, new UnificationEntry(TagPrefix.rod, GTMaterials.Steel) },
                { 4, new UnificationEntry(TagPrefix.rod, GTMaterials.Neodymium) },
                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.rod, GTMaterials.VanadiumGallium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_RADIOACTIVE = new Component(Stream.of(new Object[][] {

                { 4, new UnificationEntry(TagPrefix.rod, GTMaterials.Uranium235) },
                { 5, new UnificationEntry(TagPrefix.rod, GTMaterials.Plutonium241) },
                { 6, new UnificationEntry(TagPrefix.rod, GTMaterials.NaquadahEnriched) },
                { 7, new UnificationEntry(TagPrefix.rod, GTMaterials.Americium) },
                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.rod, GTMaterials.Tritanium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_REACTOR = new Component(Stream.of(new Object[][] {

                { 0, new ItemStack(Blocks.GLASS, 1) },
                { 1, new ItemStack(Blocks.GLASS, 1) },
                { 2, new ItemStack(Blocks.GLASS, 1) },
                { 3, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polyethylene) },
                { 4, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polyethylene) },
                { 5, new UnificationEntry(TagPrefix.pipeHugeFluid, GTMaterials.Polyethylene) },
                { 6, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polytetrafluoroethylene) },
                { 7, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene) },
                { 8, new UnificationEntry(TagPrefix.pipeHugeFluid, GTMaterials.Polytetrafluoroethylene) },
                { GTValues.FALLBACK, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polyethylene) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        POWER_COMPONENT = new Component(Stream.of(new Object[][] {

                { 2, GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack() },
                { 3, GTItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack() },
                { 4, GTItems.POWER_INTEGRATED_CIRCUIT.asStack() },
                { 5, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack() },
                { 6, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack() },
                { 7, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack() },
                { 8, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack() },
                { 9, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack() },
                { GTValues.FALLBACK, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        VOLTAGE_COIL = new Component(Stream.of(new Object[][] {

                { 0, GTItems.VOLTAGE_COIL_ULV.asStack() },
                { 1, GTItems.VOLTAGE_COIL_LV.asStack() },
                { 2, GTItems.VOLTAGE_COIL_MV.asStack() },
                { 3, GTItems.VOLTAGE_COIL_HV.asStack() },
                { 4, GTItems.VOLTAGE_COIL_EV.asStack() },
                { 5, GTItems.VOLTAGE_COIL_IV.asStack() },
                { 6, GTItems.VOLTAGE_COIL_LuV.asStack() },
                { 7, GTItems.VOLTAGE_COIL_ZPM.asStack() },
                { 8, GTItems.VOLTAGE_COIL_UV.asStack() },
                { GTValues.FALLBACK, GTItems.VOLTAGE_COIL_UV.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        SPRING = new Component(Stream.of(new Object[][] {

                { 0, new UnificationEntry(TagPrefix.spring, GTMaterials.Lead) },
                { 1, new UnificationEntry(TagPrefix.spring, GTMaterials.Tin) },
                { 2, new UnificationEntry(TagPrefix.spring, GTMaterials.Copper) },
                { 3, new UnificationEntry(TagPrefix.spring, GTMaterials.Gold) },
                { 4, new UnificationEntry(TagPrefix.spring, GTMaterials.Aluminium) },
                { 5, new UnificationEntry(TagPrefix.spring, GTMaterials.Tungsten) },
                { 6, new UnificationEntry(TagPrefix.spring, GTMaterials.NiobiumTitanium) },
                { 7, new UnificationEntry(TagPrefix.spring, GTMaterials.VanadiumGallium) },
                { 8, new UnificationEntry(TagPrefix.spring, GTMaterials.YttriumBariumCuprate) },
                { 9, new UnificationEntry(TagPrefix.spring, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CRATE = new Component(Stream.of(new Object[][] {
                { 0, new ItemStack(Blocks.CHEST) },
                { 1, GTMachines.WOODEN_CRATE.asStack() },
                { 2, GTMachines.BRONZE_CRATE.asStack() },
                { 3, GTMachines.STEEL_CRATE.asStack() },
                { 4, GTMachines.ALUMINIUM_CRATE.asStack() },
                { 5, GTMachines.STAINLESS_STEEL_CRATE.asStack() },
                { 6, GTMachines.TITANIUM_CRATE.asStack() },
                { 7, GTMachines.TUNGSTENSTEEL_CRATE.asStack() },
                { 8, GTMachines.SUPER_CHEST[1].asStack() },
                { FALLBACK, GTMachines.SUPER_CHEST[1].asStack() },
        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        DRUM = new Component(Stream.of(new Object[][] {
                { 0, new ItemStack(Blocks.GLASS) },
                { 1, GTMachines.WOODEN_DRUM.asStack() },
                { 2, GTMachines.BRONZE_DRUM.asStack() },
                { 3, GTMachines.STEEL_DRUM.asStack() },
                { 4, GTMachines.ALUMINIUM_DRUM.asStack() },
                { 5, GTMachines.STAINLESS_STEEL_DRUM.asStack() },
                { 6, GTMachines.TITANIUM_DRUM.asStack() },
                { 7, GTMachines.TUNGSTENSTEEL_DRUM.asStack() },
                { 8, GTMachines.SUPER_TANK[1].asStack() },
                { FALLBACK, GTMachines.SUPER_TANK[1].asStack() },
        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        FRAME = new Component(Stream.of(new Object[][] {
                { 0, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Wood) },
                { 1, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel) },
                { 2, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Aluminium) },
                { 3, new UnificationEntry(TagPrefix.frameGt, GTMaterials.StainlessSteel) },
                { 4, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium) },
                { 5, new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel) },
                { 6, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Ruridit) },
                { 7, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Iridium) },
                { 8, new UnificationEntry(TagPrefix.frameGt, GTMaterials.NaquadahAlloy) },
                { FALLBACK, new UnificationEntry(TagPrefix.frameGt, GTMaterials.NaquadahAlloy) },
        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        MinecraftForge.EVENT_BUS.post(new CraftingComponentModificationEvent());
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.craftingComponentModification();
        }
    }

    public static class Component {

        private final Map<Integer, Object> ingredients;

        public Component(Map<Integer, Object> craftingComponents) {
            ingredients = craftingComponents;
        }

        public Object getIngredient(int tier) {
            Object ingredient = ingredients.get(tier);
            return ingredient == null ? ingredients.get(GTValues.FALLBACK) : ingredient;
        }

        /**
         * appendIngredients will add onto the default GTCEu map of Crafting Components with the
         * ingredients that are passed into the method. If an Entry is passed in that overlaps
         * with a default entry, the passed entry will override the default GTCEu entry.
         * <p>
         * An entry with the Key of "-1" will be the "fallback" value if no entry exists for the
         * queried key. Any default value will be removed if ingredients are appended
         * via this method.
         *
         * @param newIngredients Map of <tier, ingredient> to append to the component type.
         */
        @SuppressWarnings("unused")
        public void appendIngredients(Map<Integer, Object> newIngredients) {
            ingredients.remove(GTValues.FALLBACK);
            newIngredients.forEach((key, value) -> ingredients.merge(key, value, (v1, v2) -> v2));
        }
    }

    private static final class KJSCallWrapper {

        private static void craftingComponentModification() {
            GTCEuStartupEvents.CRAFTING_COMPONENTS.post(new CraftingComponentsEventJS());
        }
    }
}
