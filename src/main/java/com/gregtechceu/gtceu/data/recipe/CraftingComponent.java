package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
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

                { 0, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Gold) },
                { 1, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Gold) },
                { 2, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Silver) },
                { 3, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Electrum) },
                { 4, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Platinum) },
                { 5, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Osmium) },
                { 6, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Osmium) },
                { 7, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Osmium) },
                { 8, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Osmium) },
                { 9, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Osmium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_QUAD = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Lead) },
                { 1, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Tungsten) },
                { 6, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_OCT = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Lead) },
                { 1, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Tungsten) },
                { 6, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        WIRE_HEX = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Lead) },
                { 1, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Tungsten) },
                { 6, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.WIRE_GT_HEX, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.RedAlloy) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Platinum) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_DOUBLE = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.RedAlloy) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Platinum) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_QUAD = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.RedAlloy) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Platinum) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_OCT = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.RedAlloy) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Platinum) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_HEX = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.RedAlloy) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Platinum) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Tin) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Gold) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Aluminium) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Platinum) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.NiobiumTitanium) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.VanadiumGallium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.YttriumBariumCuprate) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_SINGLE, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_DOUBLE = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Tin) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Gold) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Aluminium) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Platinum) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.NiobiumTitanium) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.VanadiumGallium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.YttriumBariumCuprate) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_DOUBLE, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_QUAD = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Tin) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Gold) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Aluminium) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Platinum) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.NiobiumTitanium) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.VanadiumGallium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.YttriumBariumCuprate) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_QUADRUPLE, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_OCT = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Tin) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Gold) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Aluminium) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Platinum) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.NiobiumTitanium) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.VanadiumGallium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.YttriumBariumCuprate) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_OCTAL, GTMaterials.Europium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        CABLE_TIER_UP_HEX = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Tin) },
                { 1, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Gold) },
                { 3, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Aluminium) },
                { 4, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Platinum) },
                { 5, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.NiobiumTitanium) },
                { 6, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.VanadiumGallium) },
                { 7, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.YttriumBariumCuprate) },
                { 8, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Europium) },
                { FALLBACK, new MaterialEntry(TagPrefix.CABLE_GT_HEX, GTMaterials.Europium) },

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

                { 0, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Bronze) },
                { 1, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Bronze) },
                { 2, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Steel) },
                { 3, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.StainlessSteel) },
                { 4, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Titanium) },
                { 5, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.TungstenSteel) },
                { 6, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Iridium) },
                { 8, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Naquadah) },
                { FALLBACK, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Naquadah) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_LARGE = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Bronze) },
                { 1, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Bronze) },
                { 2, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Steel) },
                { 3, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.StainlessSteel) },
                { 4, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Titanium) },
                { 5, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.TungstenSteel) },
                { 6, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Ultimet) },
                { 8, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Naquadah) },
                { FALLBACK, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Neutronium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_NONUPLE = new Component(Stream.of(new Object[][] {

                { 4, new MaterialEntry(TagPrefix.PIPE_NONUPLE_FLUID, GTMaterials.Titanium) },
                { 5, new MaterialEntry(TagPrefix.PIPE_NONUPLE_FLUID, GTMaterials.TungstenSteel) },
                { 6, new MaterialEntry(TagPrefix.PIPE_NONUPLE_FLUID, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.PIPE_NONUPLE_FLUID, GTMaterials.Iridium) },
                { 8, new MaterialEntry(TagPrefix.PIPE_NONUPLE_FLUID, GTMaterials.Naquadah) },
                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.PIPE_NONUPLE_FLUID, GTMaterials.Neutronium) },

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

                { 0, new MaterialEntry(TagPrefix.PLATE, GTMaterials.WroughtIron) },
                { 1, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Steel) },
                { 2, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Aluminium) },
                { 3, new MaterialEntry(TagPrefix.PLATE, GTMaterials.StainlessSteel) },
                { 4, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Titanium) },
                { 5, new MaterialEntry(TagPrefix.PLATE, GTMaterials.TungstenSteel) },
                { 6, new MaterialEntry(TagPrefix.PLATE, GTMaterials.RhodiumPlatedPalladium) },
                { 7, new MaterialEntry(TagPrefix.PLATE, GTMaterials.NaquadahAlloy) },
                { 8, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Darmstadtium) },
                { 9, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Neutronium) },
                { FALLBACK, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Neutronium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        HULL_PLATE = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Wood) },
                { 1, new MaterialEntry(TagPrefix.PLATE, GTMaterials.WroughtIron) },
                { 2, new MaterialEntry(TagPrefix.PLATE, GTMaterials.WroughtIron) },
                { 3, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Polyethylene) },
                { 4, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Polyethylene) },
                { 5, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Polytetrafluoroethylene) },
                { 6, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Polytetrafluoroethylene) },
                { 7, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Polybenzimidazole) },
                { 8, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Polybenzimidazole) },
                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.PLATE, GTMaterials.Polybenzimidazole) },

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

                { 0, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.Tin) },
                { 1, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.Bronze) },
                { 3, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.Steel) },
                { 4, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.StainlessSteel) },
                { 5, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.TungstenSteel) },
                { 6, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.RhodiumPlatedPalladium) },
                { 7, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.NaquadahAlloy) },
                { 8, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.Darmstadtium) },
                { FALLBACK, new MaterialEntry(TagPrefix.ROTOR, GTMaterials.Darmstadtium) },

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

                { 0, new MaterialEntry(TagPrefix.GEM, GTMaterials.Diamond) },
                { 1, new MaterialEntry(TagPrefix.GEM, GTMaterials.Diamond) },
                { 2, new MaterialEntry(TagPrefix.GEM, GTMaterials.Diamond) },
                { 3, GTItems.COMPONENT_GRINDER_DIAMOND.asStack() },
                { 4, GTItems.COMPONENT_GRINDER_DIAMOND.asStack() },
                { 5, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack() },
                { GTValues.FALLBACK, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack() },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        SAWBLADE = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.Bronze) },
                { 1, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.CobaltBrass) },
                { 2, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.VanadiumSteel) },
                { 3, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.RedSteel) },
                { 4, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.Ultimet) },
                { 5, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.TungstenCarbide) },
                { 6, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.HSSE) },
                { 7, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.NaquadahAlloy) },
                { 8, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.Duranium) },
                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.TOOL_HEAD_BUZZ_SAW, GTMaterials.Duranium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        DIAMOND = new Component(Stream.of(new Object[][] {

                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.GEM, GTMaterials.Diamond) },

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

                { 0, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Copper) },
                { 1, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Cupronickel) },
                { 3, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Kanthal) },
                { 4, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Nichrome) },
                { 5, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.RTMAlloy) },
                { 6, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.HSSG) },
                { 7, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Naquadah) },
                { 8, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.NaquadahAlloy) },
                { FALLBACK, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Trinium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        COIL_HEATING_DOUBLE = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Copper) },
                { 1, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Cupronickel) },
                { 3, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Kanthal) },
                { 4, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Nichrome) },
                { 5, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.RTMAlloy) },
                { 6, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.HSSG) },
                { 7, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Naquadah) },
                { 8, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.NaquadahAlloy) },
                { FALLBACK, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Trinium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        COIL_ELECTRIC = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.WIRE_GT_SINGLE, GTMaterials.Tin) },
                { 1, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.WIRE_GT_DOUBLE, GTMaterials.Silver) },
                { 4, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Steel) },
                { 5, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.Graphene) },
                { 6, new MaterialEntry(TagPrefix.WIRE_GT_QUADRUPLE, GTMaterials.NiobiumNitride) },
                { 7, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.WIRE_GT_OCTAL, GTMaterials.YttriumBariumCuprate) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_MAGNETIC = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.ROD, GTMaterials.IronMagnetic) },
                { 1, new MaterialEntry(TagPrefix.ROD, GTMaterials.IronMagnetic) },
                { 2, new MaterialEntry(TagPrefix.ROD, GTMaterials.SteelMagnetic) },
                { 3, new MaterialEntry(TagPrefix.ROD, GTMaterials.SteelMagnetic) },
                { 4, new MaterialEntry(TagPrefix.ROD, GTMaterials.NeodymiumMagnetic) },
                { 5, new MaterialEntry(TagPrefix.ROD, GTMaterials.NeodymiumMagnetic) },
                { 6, new MaterialEntry(TagPrefix.ROD_LONG, GTMaterials.NeodymiumMagnetic) },
                { 7, new MaterialEntry(TagPrefix.ROD_LONG, GTMaterials.NeodymiumMagnetic) },
                { 8, new MaterialEntry(TagPrefix.BLOCK, GTMaterials.NeodymiumMagnetic) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_DISTILLATION = new Component(Stream.of(new Object[][] {

                { 0, new MaterialEntry(TagPrefix.ROD, GTMaterials.Blaze) },
                { 1, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Copper) },
                { 2, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Cupronickel) },
                { 3, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Kanthal) },
                { 4, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Nichrome) },
                { 5, new MaterialEntry(TagPrefix.SPRING, GTMaterials.RTMAlloy) },
                { 6, new MaterialEntry(TagPrefix.SPRING, GTMaterials.HSSG) },
                { 7, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Naquadah) },
                { 8, new MaterialEntry(TagPrefix.SPRING, GTMaterials.NaquadahAlloy) },
                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.ROD, GTMaterials.Blaze) },

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

                { 0, new MaterialEntry(TagPrefix.ROD, GTMaterials.Iron) },
                { 1, new MaterialEntry(TagPrefix.ROD, GTMaterials.Iron) },
                { 2, new MaterialEntry(TagPrefix.ROD, GTMaterials.Steel) },
                { 3, new MaterialEntry(TagPrefix.ROD, GTMaterials.Steel) },
                { 4, new MaterialEntry(TagPrefix.ROD, GTMaterials.Neodymium) },
                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.ROD, GTMaterials.VanadiumGallium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        STICK_RADIOACTIVE = new Component(Stream.of(new Object[][] {

                { 4, new MaterialEntry(TagPrefix.ROD, GTMaterials.Uranium235) },
                { 5, new MaterialEntry(TagPrefix.ROD, GTMaterials.Plutonium241) },
                { 6, new MaterialEntry(TagPrefix.ROD, GTMaterials.NaquadahEnriched) },
                { 7, new MaterialEntry(TagPrefix.ROD, GTMaterials.Americium) },
                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.ROD, GTMaterials.Tritanium) },

        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));

        PIPE_REACTOR = new Component(Stream.of(new Object[][] {

                { 0, new ItemStack(Blocks.GLASS, 1) },
                { 1, new ItemStack(Blocks.GLASS, 1) },
                { 2, new ItemStack(Blocks.GLASS, 1) },
                { 3, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Polyethylene) },
                { 4, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Polyethylene) },
                { 5, new MaterialEntry(TagPrefix.PIPE_HUGE_FLUID, GTMaterials.Polyethylene) },
                { 6, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Polytetrafluoroethylene) },
                { 7, new MaterialEntry(TagPrefix.PIPE_LARGE_FLUID, GTMaterials.Polytetrafluoroethylene) },
                { 8, new MaterialEntry(TagPrefix.PIPE_HUGE_FLUID, GTMaterials.Polytetrafluoroethylene) },
                { GTValues.FALLBACK, new MaterialEntry(TagPrefix.PIPE_NORMAL_FLUID, GTMaterials.Polyethylene) },

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

                { 0, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Lead) },
                { 1, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Tin) },
                { 2, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Copper) },
                { 3, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Gold) },
                { 4, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Aluminium) },
                { 5, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Tungsten) },
                { 6, new MaterialEntry(TagPrefix.SPRING, GTMaterials.NiobiumTitanium) },
                { 7, new MaterialEntry(TagPrefix.SPRING, GTMaterials.VanadiumGallium) },
                { 8, new MaterialEntry(TagPrefix.SPRING, GTMaterials.YttriumBariumCuprate) },
                { 9, new MaterialEntry(TagPrefix.SPRING, GTMaterials.Europium) },

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
                { 0, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.Wood) },
                { 1, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.Steel) },
                { 2, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.Aluminium) },
                { 3, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.StainlessSteel) },
                { 4, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.Titanium) },
                { 5, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.TungstenSteel) },
                { 6, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.Ruridit) },
                { 7, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.Iridium) },
                { 8, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.NaquadahAlloy) },
                { FALLBACK, new MaterialEntry(TagPrefix.FRAME_GT, GTMaterials.NaquadahAlloy) },
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
