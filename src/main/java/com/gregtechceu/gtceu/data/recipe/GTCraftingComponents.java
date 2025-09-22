package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.event.CraftingComponentModificationEvent;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.CraftingComponentsEventJS;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class GTCraftingComponents {

    public static CraftingComponent CIRCUIT;
    public static CraftingComponent BETTER_CIRCUIT;
    public static CraftingComponent PUMP;
    public static CraftingComponent WIRE_ELECTRIC;
    public static CraftingComponent WIRE_QUAD;
    public static CraftingComponent WIRE_OCT;
    public static CraftingComponent WIRE_HEX;
    public static CraftingComponent CABLE;
    public static CraftingComponent CABLE_DOUBLE;
    public static CraftingComponent CABLE_QUAD;
    public static CraftingComponent CABLE_OCT;
    public static CraftingComponent CABLE_HEX;
    public static CraftingComponent CABLE_TIER_UP;
    public static CraftingComponent CABLE_TIER_UP_DOUBLE;
    public static CraftingComponent CABLE_TIER_UP_QUAD;
    public static CraftingComponent CABLE_TIER_UP_OCT;
    public static CraftingComponent CABLE_TIER_UP_HEX;
    public static CraftingComponent CASING;
    public static CraftingComponent HULL;
    public static CraftingComponent PIPE_NORMAL;
    public static CraftingComponent PIPE_LARGE;
    public static CraftingComponent PIPE_NONUPLE;
    public static CraftingComponent GLASS;
    public static CraftingComponent PLATE;
    public static CraftingComponent HULL_PLATE;
    public static CraftingComponent MOTOR;
    public static CraftingComponent ROTOR;
    public static CraftingComponent SENSOR;
    public static CraftingComponent SENSOR_EMITTER_GEM;
    public static CraftingComponent GRINDER;
    public static CraftingComponent SAWBLADE;
    public static CraftingComponent PISTON;
    public static CraftingComponent EMITTER;
    public static CraftingComponent CONVEYOR;
    public static CraftingComponent ROBOT_ARM;
    public static CraftingComponent COIL_HEATING;
    public static CraftingComponent COIL_HEATING_DOUBLE;
    public static CraftingComponent COIL_ELECTRIC;
    public static CraftingComponent ROD_MAGNETIC;
    public static CraftingComponent ROD_DISTILLATION;
    public static CraftingComponent FIELD_GENERATOR;
    public static CraftingComponent ROD_ELECTROMAGNETIC;
    public static CraftingComponent ROD_RADIOACTIVE;
    public static CraftingComponent PIPE_REACTOR;
    public static CraftingComponent POWER_COMPONENT;
    public static CraftingComponent VOLTAGE_COIL;
    public static CraftingComponent SPRING;
    public static CraftingComponent CRATE;
    public static CraftingComponent DRUM;
    public static CraftingComponent FRAME;
    public static CraftingComponent SMALL_SPRING_TRANSFORMER;
    public static CraftingComponent SPRING_TRANSFORMER;

    public static void init() {
        CraftingComponent.ALL_COMPONENTS.clear();

        /*
         * GTCEu must supply values for at least tiers 1 through 8 (through UV)
         */
        CIRCUIT = CraftingComponent.of("circuit", CustomTags.ULV_CIRCUITS)
                .add(ULV, CustomTags.ULV_CIRCUITS)
                .add(LV, CustomTags.LV_CIRCUITS)
                .add(MV, CustomTags.MV_CIRCUITS)
                .add(HV, CustomTags.HV_CIRCUITS)
                .add(EV, CustomTags.EV_CIRCUITS)
                .add(IV, CustomTags.IV_CIRCUITS)
                .add(LuV, CustomTags.LuV_CIRCUITS)
                .add(ZPM, CustomTags.ZPM_CIRCUITS)
                .add(UV, CustomTags.UV_CIRCUITS)
                .add(UHV, CustomTags.UHV_CIRCUITS)
                .add(UEV, CustomTags.UEV_CIRCUITS)
                .add(UIV, CustomTags.UIV_CIRCUITS)
                .add(UXV, CustomTags.UXV_CIRCUITS)
                .add(OpV, CustomTags.OpV_CIRCUITS)
                .add(MAX, CustomTags.MAX_CIRCUITS);

        BETTER_CIRCUIT = CraftingComponent.of("better_circuit", CustomTags.ULV_CIRCUITS)
                .add(ULV, CustomTags.LV_CIRCUITS)
                .add(LV, CustomTags.MV_CIRCUITS)
                .add(MV, CustomTags.HV_CIRCUITS)
                .add(HV, CustomTags.EV_CIRCUITS)
                .add(EV, CustomTags.IV_CIRCUITS)
                .add(IV, CustomTags.LuV_CIRCUITS)
                .add(LuV, CustomTags.ZPM_CIRCUITS)
                .add(ZPM, CustomTags.UV_CIRCUITS)
                .add(UV, CustomTags.UHV_CIRCUITS)
                .add(UHV, CustomTags.UEV_CIRCUITS)
                .add(UEV, CustomTags.UIV_CIRCUITS)
                .add(UIV, CustomTags.UXV_CIRCUITS)
                .add(UXV, CustomTags.OpV_CIRCUITS)
                .add(OpV, CustomTags.MAX_CIRCUITS)
                .add(MAX, CustomTags.MAX_CIRCUITS);

        WIRE_ELECTRIC = CraftingComponent.of("wire_single", wireGtSingle, Gold)
                .add(ULV, wireGtSingle, Gold)
                .add(LV, wireGtSingle, Gold)
                .add(MV, wireGtSingle, Silver)
                .add(HV, wireGtSingle, Electrum)
                .add(EV, wireGtSingle, Platinum)
                .add(IV, wireGtSingle, Osmium)
                .add(LuV, wireGtSingle, Osmium)
                .add(ZPM, wireGtSingle, Osmium)
                .add(UV, wireGtSingle, Osmium)
                .add(UHV, wireGtSingle, Osmium);

        WIRE_QUAD = CraftingComponent.of("wire_quad", wireGtQuadruple, Lead)
                .add(ULV, wireGtQuadruple, Lead)
                .add(LV, wireGtQuadruple, Tin)
                .add(MV, wireGtQuadruple, Copper)
                .add(HV, wireGtQuadruple, Gold)
                .add(EV, wireGtQuadruple, Aluminium)
                .add(IV, wireGtQuadruple, Tungsten)
                .add(LuV, wireGtQuadruple, NiobiumTitanium)
                .add(ZPM, wireGtQuadruple, VanadiumGallium)
                .add(UV, wireGtQuadruple, YttriumBariumCuprate)
                .add(UHV, wireGtQuadruple, Europium);

        WIRE_OCT = CraftingComponent.of("wire_oct", wireGtOctal, Lead)
                .add(ULV, wireGtOctal, Lead)
                .add(LV, wireGtOctal, Tin)
                .add(MV, wireGtOctal, Copper)
                .add(HV, wireGtOctal, Gold)
                .add(EV, wireGtOctal, Aluminium)
                .add(IV, wireGtOctal, Tungsten)
                .add(LuV, wireGtOctal, NiobiumTitanium)
                .add(ZPM, wireGtOctal, VanadiumGallium)
                .add(UV, wireGtOctal, YttriumBariumCuprate)
                .add(UHV, wireGtOctal, Europium);

        WIRE_HEX = CraftingComponent.of("wire_hex", wireGtHex, Lead)
                .add(ULV, wireGtHex, Lead)
                .add(LV, wireGtHex, Tin)
                .add(MV, wireGtHex, Copper)
                .add(HV, wireGtHex, Gold)
                .add(EV, wireGtHex, Aluminium)
                .add(IV, wireGtHex, Tungsten)
                .add(LuV, wireGtHex, NiobiumTitanium)
                .add(ZPM, wireGtHex, VanadiumGallium)
                .add(UV, wireGtHex, YttriumBariumCuprate)
                .add(UHV, wireGtHex, Europium);

        CABLE = CraftingComponent.of("cable_single", cableGtSingle, RedAlloy)
                .add(ULV, cableGtSingle, RedAlloy)
                .add(LV, cableGtSingle, Tin)
                .add(MV, cableGtSingle, Copper)
                .add(HV, cableGtSingle, Gold)
                .add(EV, cableGtSingle, Aluminium)
                .add(IV, cableGtSingle, Platinum)
                .add(LuV, cableGtSingle, NiobiumTitanium)
                .add(ZPM, cableGtSingle, VanadiumGallium)
                .add(UV, cableGtSingle, YttriumBariumCuprate)
                .add(UHV, cableGtSingle, Europium);

        CABLE_DOUBLE = CraftingComponent.of("cable_double", cableGtDouble, RedAlloy)
                .add(ULV, cableGtDouble, RedAlloy)
                .add(LV, cableGtDouble, Tin)
                .add(MV, cableGtDouble, Copper)
                .add(HV, cableGtDouble, Gold)
                .add(EV, cableGtDouble, Aluminium)
                .add(IV, cableGtDouble, Platinum)
                .add(LuV, cableGtDouble, NiobiumTitanium)
                .add(ZPM, cableGtDouble, VanadiumGallium)
                .add(UV, cableGtDouble, YttriumBariumCuprate)
                .add(UHV, cableGtDouble, Europium);

        CABLE_QUAD = CraftingComponent.of("cable_quad", cableGtQuadruple, RedAlloy)
                .add(ULV, cableGtQuadruple, RedAlloy)
                .add(LV, cableGtQuadruple, Tin)
                .add(MV, cableGtQuadruple, Copper)
                .add(HV, cableGtQuadruple, Gold)
                .add(EV, cableGtQuadruple, Aluminium)
                .add(IV, cableGtQuadruple, Platinum)
                .add(LuV, cableGtQuadruple, NiobiumTitanium)
                .add(ZPM, cableGtQuadruple, VanadiumGallium)
                .add(UV, cableGtQuadruple, YttriumBariumCuprate)
                .add(UHV, cableGtQuadruple, Europium);

        CABLE_OCT = CraftingComponent.of("cable_oct", cableGtOctal, RedAlloy)
                .add(ULV, cableGtOctal, RedAlloy)
                .add(LV, cableGtOctal, Tin)
                .add(MV, cableGtOctal, Copper)
                .add(HV, cableGtOctal, Gold)
                .add(EV, cableGtOctal, Aluminium)
                .add(IV, cableGtOctal, Platinum)
                .add(LuV, cableGtOctal, NiobiumTitanium)
                .add(ZPM, cableGtOctal, VanadiumGallium)
                .add(UV, cableGtOctal, YttriumBariumCuprate)
                .add(UHV, cableGtOctal, Europium);

        CABLE_HEX = CraftingComponent.of("cable_hex", cableGtHex, RedAlloy)
                .add(ULV, cableGtHex, RedAlloy)
                .add(LV, cableGtHex, Tin)
                .add(MV, cableGtHex, Copper)
                .add(HV, cableGtHex, Gold)
                .add(EV, cableGtHex, Aluminium)
                .add(IV, cableGtHex, Platinum)
                .add(LuV, cableGtHex, NiobiumTitanium)
                .add(ZPM, cableGtHex, VanadiumGallium)
                .add(UV, cableGtHex, YttriumBariumCuprate)
                .add(UHV, cableGtHex, Europium);

        CABLE_TIER_UP = CraftingComponent.of("cable_tier_up_single", cableGtSingle, RedAlloy)
                .add(ULV, cableGtSingle, Tin)
                .add(LV, cableGtSingle, Copper)
                .add(MV, cableGtSingle, Gold)
                .add(HV, cableGtSingle, Aluminium)
                .add(EV, cableGtSingle, Platinum)
                .add(IV, cableGtSingle, NiobiumTitanium)
                .add(LuV, cableGtSingle, VanadiumGallium)
                .add(ZPM, cableGtSingle, YttriumBariumCuprate)
                .add(UV, cableGtSingle, Europium)
                .add(UHV, cableGtSingle, Europium);

        CABLE_TIER_UP_DOUBLE = CraftingComponent.of("cable_tier_up_double", cableGtDouble, RedAlloy)
                .add(ULV, cableGtDouble, Tin)
                .add(LV, cableGtDouble, Copper)
                .add(MV, cableGtDouble, Gold)
                .add(HV, cableGtDouble, Aluminium)
                .add(EV, cableGtDouble, Platinum)
                .add(IV, cableGtDouble, NiobiumTitanium)
                .add(LuV, cableGtDouble, VanadiumGallium)
                .add(ZPM, cableGtDouble, YttriumBariumCuprate)
                .add(UV, cableGtDouble, Europium)
                .add(UHV, cableGtDouble, Europium);

        CABLE_TIER_UP_QUAD = CraftingComponent.of("cable_tier_up_quad", cableGtQuadruple, RedAlloy)
                .add(ULV, cableGtQuadruple, Tin)
                .add(LV, cableGtQuadruple, Copper)
                .add(MV, cableGtQuadruple, Gold)
                .add(HV, cableGtQuadruple, Aluminium)
                .add(EV, cableGtQuadruple, Platinum)
                .add(IV, cableGtQuadruple, NiobiumTitanium)
                .add(LuV, cableGtQuadruple, VanadiumGallium)
                .add(ZPM, cableGtQuadruple, YttriumBariumCuprate)
                .add(UV, cableGtQuadruple, Europium)
                .add(UHV, cableGtQuadruple, Europium);

        CABLE_TIER_UP_OCT = CraftingComponent.of("cable_tier_up_oct", cableGtOctal, RedAlloy)
                .add(ULV, cableGtOctal, Tin)
                .add(LV, cableGtOctal, Copper)
                .add(MV, cableGtOctal, Gold)
                .add(HV, cableGtOctal, Aluminium)
                .add(EV, cableGtOctal, Platinum)
                .add(IV, cableGtOctal, NiobiumTitanium)
                .add(LuV, cableGtOctal, VanadiumGallium)
                .add(ZPM, cableGtOctal, YttriumBariumCuprate)
                .add(UV, cableGtOctal, Europium)
                .add(UHV, cableGtOctal, Europium);

        CABLE_TIER_UP_HEX = CraftingComponent.of("cable_tier_up_hex", cableGtHex, RedAlloy)
                .add(ULV, cableGtHex, Tin)
                .add(LV, cableGtHex, Copper)
                .add(MV, cableGtHex, Gold)
                .add(HV, cableGtHex, Aluminium)
                .add(EV, cableGtHex, Platinum)
                .add(IV, cableGtHex, NiobiumTitanium)
                .add(LuV, cableGtHex, VanadiumGallium)
                .add(ZPM, cableGtHex, YttriumBariumCuprate)
                .add(UV, cableGtHex, Europium)
                .add(UHV, cableGtHex, Europium);

        HULL = CraftingComponent.of("hull", GTMachines.HULL[ULV].asStack())
                .add(ULV, GTMachines.HULL[ULV].asStack())
                .add(LV, GTMachines.HULL[LV].asStack())
                .add(MV, GTMachines.HULL[MV].asStack())
                .add(HV, GTMachines.HULL[HV].asStack())
                .add(EV, GTMachines.HULL[EV].asStack())
                .add(IV, GTMachines.HULL[IV].asStack())
                .add(LuV, GTMachines.HULL[LuV].asStack())
                .add(ZPM, GTMachines.HULL[ZPM].asStack())
                .add(UV, GTMachines.HULL[UV].asStack())
                .add(UHV, GTMachines.HULL[UHV].asStack());
        if (GTCEuAPI.isHighTier()) {
            HULL.add(UEV, GTMachines.HULL[UEV].asStack())
                    .add(UIV, GTMachines.HULL[UIV].asStack())
                    .add(UXV, GTMachines.HULL[UXV].asStack())
                    .add(OpV, GTMachines.HULL[OpV].asStack())
                    .add(MAX, GTMachines.HULL[MAX].asStack());
        }

        CASING = CraftingComponent.of("casing", GTBlocks.MACHINE_CASING_ULV.asStack())
                .add(ULV, GTBlocks.MACHINE_CASING_ULV.asStack())
                .add(LV, GTBlocks.MACHINE_CASING_LV.asStack())
                .add(MV, GTBlocks.MACHINE_CASING_MV.asStack())
                .add(HV, GTBlocks.MACHINE_CASING_HV.asStack())
                .add(EV, GTBlocks.MACHINE_CASING_EV.asStack())
                .add(IV, GTBlocks.MACHINE_CASING_IV.asStack())
                .add(LuV, GTBlocks.MACHINE_CASING_LuV.asStack())
                .add(ZPM, GTBlocks.MACHINE_CASING_ZPM.asStack())
                .add(UV, GTBlocks.MACHINE_CASING_UV.asStack())
                .add(UHV, GTBlocks.MACHINE_CASING_UHV.asStack());
        if (GTCEuAPI.isHighTier()) {
            CASING.add(UEV, GTBlocks.MACHINE_CASING_UEV.asStack())
                    .add(UIV, GTBlocks.MACHINE_CASING_UIV.asStack())
                    .add(UXV, GTBlocks.MACHINE_CASING_UXV.asStack())
                    .add(OpV, GTBlocks.MACHINE_CASING_OpV.asStack())
                    .add(MAX, GTBlocks.MACHINE_CASING_MAX.asStack());
        }

        PIPE_NORMAL = CraftingComponent.of("normal_pipe", pipeNormalFluid, Bronze)
                .add(ULV, pipeNormalFluid, Bronze)
                .add(LV, pipeNormalFluid, Bronze)
                .add(MV, pipeNormalFluid, Steel)
                .add(HV, pipeNormalFluid, StainlessSteel)
                .add(EV, pipeNormalFluid, Titanium)
                .add(IV, pipeNormalFluid, TungstenSteel)
                .add(LuV, pipeNormalFluid, NiobiumTitanium)
                .add(ZPM, pipeNormalFluid, Iridium)
                .add(UV, pipeNormalFluid, Naquadah)
                .add(UHV, pipeNormalFluid, Naquadah);

        PIPE_LARGE = CraftingComponent.of("large_pipe", pipeLargeFluid, Bronze)
                .add(ULV, pipeLargeFluid, Bronze)
                .add(LV, pipeLargeFluid, Bronze)
                .add(MV, pipeLargeFluid, Steel)
                .add(HV, pipeLargeFluid, StainlessSteel)
                .add(EV, pipeLargeFluid, Titanium)
                .add(IV, pipeLargeFluid, TungstenSteel)
                .add(LuV, pipeLargeFluid, NiobiumTitanium)
                .add(ZPM, pipeLargeFluid, Ultimet)
                .add(UV, pipeLargeFluid, Naquadah)
                .add(UHV, pipeLargeFluid, Neutronium);

        PIPE_NONUPLE = CraftingComponent.of("nonuple_pipe", pipeNonupleFluid, Titanium)
                .add(EV, pipeNonupleFluid, Titanium)
                .add(IV, pipeNonupleFluid, TungstenSteel)
                .add(LuV, pipeNonupleFluid, NiobiumTitanium)
                .add(ZPM, pipeNonupleFluid, Iridium)
                .add(UV, pipeNonupleFluid, Naquadah)
                .add(UHV, pipeNonupleFluid, Neutronium);

        /*
         * Glass: Steam-MV
         * Tempered: HV, EV
         * Laminated Glass: IV, LuV
         * Fusion: ZPM, UV, UHV
         */
        GLASS = CraftingComponent.of("glass", Tags.Items.GLASS)
                .add(ULV, Tags.Items.GLASS)
                .add(LV, Tags.Items.GLASS)
                .add(MV, Tags.Items.GLASS)
                .add(HV, GTBlocks.CASING_TEMPERED_GLASS.asStack())
                .add(EV, GTBlocks.CASING_TEMPERED_GLASS.asStack())
                .add(IV, GTBlocks.CASING_LAMINATED_GLASS.asStack())
                .add(LuV, GTBlocks.CASING_LAMINATED_GLASS.asStack())
                .add(ZPM, GTBlocks.FUSION_GLASS.asStack())
                .add(UV, GTBlocks.FUSION_GLASS.asStack())
                .add(UHV, GTBlocks.FUSION_GLASS.asStack());

        PLATE = CraftingComponent.of("plate", plate, Iron)
                .add(ULV, plate, WroughtIron)
                .add(LV, plate, Steel)
                .add(MV, plate, Aluminium)
                .add(HV, plate, StainlessSteel)
                .add(EV, plate, Titanium)
                .add(IV, plate, TungstenSteel)
                .add(LuV, plate, RhodiumPlatedPalladium)
                .add(ZPM, plate, NaquadahAlloy)
                .add(UV, plate, Darmstadtium)
                .add(UHV, plate, Neutronium);

        HULL_PLATE = CraftingComponent.of("hull_plate", plate, Wood)
                .add(ULV, plate, Wood)
                .add(LV, plate, WroughtIron)
                .add(MV, plate, WroughtIron)
                .add(HV, plate, Polyethylene)
                .add(EV, plate, Polyethylene)
                .add(IV, plate, Polytetrafluoroethylene)
                .add(LuV, plate, Polytetrafluoroethylene)
                .add(ZPM, plate, Polybenzimidazole)
                .add(UV, plate, Polybenzimidazole)
                .add(UHV, plate, Polybenzimidazole);

        ROTOR = CraftingComponent.of("rotor", rotor, Tin)
                .add(ULV, rotor, Tin)
                .add(LV, rotor, Tin)
                .add(MV, rotor, Bronze)
                .add(HV, rotor, Steel)
                .add(EV, rotor, StainlessSteel)
                .add(IV, rotor, TungstenSteel)
                .add(LuV, rotor, RhodiumPlatedPalladium)
                .add(ZPM, rotor, NaquadahAlloy)
                .add(UV, rotor, Darmstadtium)
                .add(UHV, rotor, Darmstadtium);

        GRINDER = CraftingComponent.of("grinder", gem, Diamond)
                .add(ULV, gem, Diamond)
                .add(LV, gem, Diamond)
                .add(MV, gem, Diamond)
                .add(HV, GTItems.COMPONENT_GRINDER_DIAMOND.asStack())
                .add(EV, GTItems.COMPONENT_GRINDER_DIAMOND.asStack())
                .add(IV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(LuV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(ZPM, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(UV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(UHV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack());

        SAWBLADE = CraftingComponent.of("sawblade", toolHeadBuzzSaw, Bronze)
                .add(ULV, toolHeadBuzzSaw, Bronze)
                .add(LV, toolHeadBuzzSaw, CobaltBrass)
                .add(MV, toolHeadBuzzSaw, VanadiumSteel)
                .add(HV, toolHeadBuzzSaw, RedSteel)
                .add(EV, toolHeadBuzzSaw, Ultimet)
                .add(IV, toolHeadBuzzSaw, TungstenCarbide)
                .add(LuV, toolHeadBuzzSaw, HSSE)
                .add(ZPM, toolHeadBuzzSaw, NaquadahAlloy)
                .add(UV, toolHeadBuzzSaw, Duranium)
                .add(UHV, toolHeadBuzzSaw, Duranium);

        MOTOR = CraftingComponent.of("motor", GTItems.ELECTRIC_MOTOR_LV.asStack())
                .add(LV, GTItems.ELECTRIC_MOTOR_LV.asStack())
                .add(MV, GTItems.ELECTRIC_MOTOR_MV.asStack())
                .add(HV, GTItems.ELECTRIC_MOTOR_HV.asStack())
                .add(EV, GTItems.ELECTRIC_MOTOR_EV.asStack())
                .add(IV, GTItems.ELECTRIC_MOTOR_IV.asStack())
                .add(LuV, GTItems.ELECTRIC_MOTOR_LuV.asStack())
                .add(ZPM, GTItems.ELECTRIC_MOTOR_ZPM.asStack())
                .add(UV, GTItems.ELECTRIC_MOTOR_UV.asStack());
        if (GTCEuAPI.isHighTier()) {
            MOTOR.add(UHV, GTItems.ELECTRIC_MOTOR_UHV.asStack())
                    .add(UEV, GTItems.ELECTRIC_MOTOR_UEV.asStack())
                    .add(UIV, GTItems.ELECTRIC_MOTOR_UIV.asStack())
                    .add(UXV, GTItems.ELECTRIC_MOTOR_UXV.asStack())
                    .add(OpV, GTItems.ELECTRIC_MOTOR_OpV.asStack());
        }

        PUMP = CraftingComponent.of("pump", GTItems.ELECTRIC_PUMP_LV.asStack())
                .add(LV, GTItems.ELECTRIC_PUMP_LV.asStack())
                .add(MV, GTItems.ELECTRIC_PUMP_MV.asStack())
                .add(HV, GTItems.ELECTRIC_PUMP_HV.asStack())
                .add(EV, GTItems.ELECTRIC_PUMP_EV.asStack())
                .add(IV, GTItems.ELECTRIC_PUMP_IV.asStack())
                .add(LuV, GTItems.ELECTRIC_PUMP_LuV.asStack())
                .add(ZPM, GTItems.ELECTRIC_PUMP_ZPM.asStack())
                .add(UV, GTItems.ELECTRIC_PUMP_UV.asStack());
        if (GTCEuAPI.isHighTier()) {
            PUMP.add(UHV, GTItems.ELECTRIC_PUMP_UHV.asStack())
                    .add(UEV, GTItems.ELECTRIC_PUMP_UEV.asStack())
                    .add(UIV, GTItems.ELECTRIC_PUMP_UIV.asStack())
                    .add(UXV, GTItems.ELECTRIC_PUMP_UXV.asStack())
                    .add(OpV, GTItems.ELECTRIC_PUMP_OpV.asStack());
        }

        PISTON = CraftingComponent.of("piston", GTItems.ELECTRIC_PISTON_LV.asStack())
                .add(LV, GTItems.ELECTRIC_PISTON_LV.asStack())
                .add(MV, GTItems.ELECTRIC_PISTON_MV.asStack())
                .add(HV, GTItems.ELECTRIC_PISTON_HV.asStack())
                .add(EV, GTItems.ELECTRIC_PISTON_EV.asStack())
                .add(IV, GTItems.ELECTRIC_PISTON_IV.asStack())
                .add(LuV, GTItems.ELECTRIC_PISTON_LuV.asStack())
                .add(ZPM, GTItems.ELECTRIC_PISTON_ZPM.asStack())
                .add(UV, GTItems.ELECTRIC_PISTON_UV.asStack());
        if (GTCEuAPI.isHighTier()) {
            PISTON.add(UHV, GTItems.ELECTRIC_PISTON_UHV.asStack())
                    .add(UEV, GTItems.ELECTRIC_PISTON_UEV.asStack())
                    .add(UIV, GTItems.ELECTRIC_PISTON_UIV.asStack())
                    .add(UXV, GTItems.ELECTRIC_PISTON_UXV.asStack())
                    .add(OpV, GTItems.ELECTRIC_PISTON_OpV.asStack());
        }

        EMITTER = CraftingComponent.of("emitter", GTItems.EMITTER_LV.asStack())
                .add(LV, GTItems.EMITTER_LV.asStack())
                .add(MV, GTItems.EMITTER_MV.asStack())
                .add(HV, GTItems.EMITTER_HV.asStack())
                .add(EV, GTItems.EMITTER_EV.asStack())
                .add(IV, GTItems.EMITTER_IV.asStack())
                .add(LuV, GTItems.EMITTER_LuV.asStack())
                .add(ZPM, GTItems.EMITTER_ZPM.asStack())
                .add(UV, GTItems.EMITTER_UV.asStack());

        if (GTCEuAPI.isHighTier()) {
            EMITTER.add(UHV, GTItems.EMITTER_UHV.asStack())
                    .add(UEV, GTItems.EMITTER_UEV.asStack())
                    .add(UIV, GTItems.EMITTER_UIV.asStack())
                    .add(UXV, GTItems.EMITTER_UXV.asStack())
                    .add(OpV, GTItems.EMITTER_OpV.asStack());
        }

        SENSOR = CraftingComponent.of("sensor", GTItems.SENSOR_LV.asStack())
                .add(LV, GTItems.SENSOR_LV.asStack())
                .add(MV, GTItems.SENSOR_MV.asStack())
                .add(HV, GTItems.SENSOR_HV.asStack())
                .add(EV, GTItems.SENSOR_EV.asStack())
                .add(IV, GTItems.SENSOR_IV.asStack())
                .add(LuV, GTItems.SENSOR_LuV.asStack())
                .add(ZPM, GTItems.SENSOR_ZPM.asStack())
                .add(UV, GTItems.SENSOR_UV.asStack());
        if (GTCEuAPI.isHighTier()) {
            SENSOR.add(UHV, GTItems.SENSOR_UHV.asStack())
                    .add(UEV, GTItems.SENSOR_UEV.asStack())
                    .add(UIV, GTItems.SENSOR_UIV.asStack())
                    .add(UXV, GTItems.SENSOR_UXV.asStack())
                    .add(OpV, GTItems.SENSOR_OpV.asStack());
        }

        SENSOR_EMITTER_GEM = CraftingComponent.of("sensor_emitter_gem", gem, Quartzite)
                .add(LV, gem, Quartzite)
                .add(MV, gemFlawless, Emerald)
                .add(HV, gem, EnderEye)
                .add(EV, GTItems.QUANTUM_EYE.asStack())
                .add(IV, GTItems.QUANTUM_STAR.asStack())
                .add(LuV, GTItems.QUANTUM_STAR.asStack())
                .add(ZPM, GTItems.QUANTUM_STAR.asStack())
                .add(UV, GTItems.GRAVI_STAR.asStack());

        CONVEYOR = CraftingComponent.of("conveyor", GTItems.CONVEYOR_MODULE_LV.asStack())
                .add(LV, GTItems.CONVEYOR_MODULE_LV.asStack())
                .add(MV, GTItems.CONVEYOR_MODULE_MV.asStack())
                .add(HV, GTItems.CONVEYOR_MODULE_HV.asStack())
                .add(EV, GTItems.CONVEYOR_MODULE_EV.asStack())
                .add(IV, GTItems.CONVEYOR_MODULE_IV.asStack())
                .add(LuV, GTItems.CONVEYOR_MODULE_LuV.asStack())
                .add(ZPM, GTItems.CONVEYOR_MODULE_ZPM.asStack())
                .add(UV, GTItems.CONVEYOR_MODULE_UV.asStack());
        if (GTCEuAPI.isHighTier()) {
            CONVEYOR.add(UHV, GTItems.CONVEYOR_MODULE_UHV.asStack())
                    .add(UEV, GTItems.CONVEYOR_MODULE_UEV.asStack())
                    .add(UIV, GTItems.CONVEYOR_MODULE_UIV.asStack())
                    .add(UXV, GTItems.CONVEYOR_MODULE_UXV.asStack())
                    .add(OpV, GTItems.CONVEYOR_MODULE_OpV.asStack());
        }

        ROBOT_ARM = CraftingComponent.of("robot_arm", GTItems.ROBOT_ARM_LV.asStack())
                .add(LV, GTItems.ROBOT_ARM_LV.asStack())
                .add(MV, GTItems.ROBOT_ARM_MV.asStack())
                .add(HV, GTItems.ROBOT_ARM_HV.asStack())
                .add(EV, GTItems.ROBOT_ARM_EV.asStack())
                .add(IV, GTItems.ROBOT_ARM_IV.asStack())
                .add(LuV, GTItems.ROBOT_ARM_LuV.asStack())
                .add(ZPM, GTItems.ROBOT_ARM_ZPM.asStack())
                .add(UV, GTItems.ROBOT_ARM_UV.asStack());
        if (GTCEuAPI.isHighTier()) {
            ROBOT_ARM.add(UHV, GTItems.ROBOT_ARM_UHV.asStack())
                    .add(UEV, GTItems.ROBOT_ARM_UEV.asStack())
                    .add(UIV, GTItems.ROBOT_ARM_UIV.asStack())
                    .add(UXV, GTItems.ROBOT_ARM_UXV.asStack())
                    .add(OpV, GTItems.ROBOT_ARM_OpV.asStack());
        }

        FIELD_GENERATOR = CraftingComponent.of("field_generator", GTItems.FIELD_GENERATOR_LV.asStack())
                .add(LV, GTItems.FIELD_GENERATOR_LV.asStack())
                .add(MV, GTItems.FIELD_GENERATOR_MV.asStack())
                .add(HV, GTItems.FIELD_GENERATOR_HV.asStack())
                .add(EV, GTItems.FIELD_GENERATOR_EV.asStack())
                .add(IV, GTItems.FIELD_GENERATOR_IV.asStack())
                .add(LuV, GTItems.FIELD_GENERATOR_LuV.asStack())
                .add(ZPM, GTItems.FIELD_GENERATOR_ZPM.asStack())
                .add(UV, GTItems.FIELD_GENERATOR_UV.asStack());
        if (GTCEuAPI.isHighTier()) {
            FIELD_GENERATOR.add(UHV, GTItems.FIELD_GENERATOR_UHV.asStack())
                    .add(UEV, GTItems.FIELD_GENERATOR_UEV.asStack())
                    .add(UIV, GTItems.FIELD_GENERATOR_UIV.asStack())
                    .add(UXV, GTItems.FIELD_GENERATOR_UXV.asStack())
                    .add(OpV, GTItems.FIELD_GENERATOR_OpV.asStack());
        }

        COIL_HEATING = CraftingComponent.of("coil_heating", wireGtDouble, Copper)
                .add(ULV, wireGtDouble, Copper)
                .add(LV, wireGtDouble, Copper)
                .add(MV, wireGtDouble, Cupronickel)
                .add(HV, wireGtDouble, Kanthal)
                .add(EV, wireGtDouble, Nichrome)
                .add(IV, wireGtDouble, RTMAlloy)
                .add(LuV, wireGtDouble, HSSG)
                .add(ZPM, wireGtDouble, Naquadah)
                .add(UV, wireGtDouble, NaquadahAlloy)
                .add(UHV, wireGtDouble, Trinium);

        COIL_HEATING_DOUBLE = CraftingComponent.of("coil_heating_double", wireGtQuadruple, Copper)
                .add(ULV, wireGtQuadruple, Copper)
                .add(LV, wireGtQuadruple, Copper)
                .add(MV, wireGtQuadruple, Cupronickel)
                .add(HV, wireGtQuadruple, Kanthal)
                .add(EV, wireGtQuadruple, Nichrome)
                .add(IV, wireGtQuadruple, RTMAlloy)
                .add(LuV, wireGtQuadruple, HSSG)
                .add(ZPM, wireGtQuadruple, Naquadah)
                .add(UV, wireGtQuadruple, NaquadahAlloy)
                .add(UHV, wireGtQuadruple, Trinium);

        COIL_ELECTRIC = CraftingComponent.of("coil_electric", wireGtSingle, Tin)
                .add(ULV, wireGtSingle, Tin)
                .add(LV, wireGtDouble, Tin)
                .add(MV, wireGtDouble, Copper)
                .add(HV, wireGtDouble, Silver)
                .add(EV, wireGtQuadruple, Steel)
                .add(IV, wireGtQuadruple, Graphene)
                .add(LuV, wireGtQuadruple, NiobiumNitride)
                .add(ZPM, wireGtOctal, VanadiumGallium)
                .add(UV, wireGtOctal, YttriumBariumCuprate)
                .add(UHV, wireGtOctal, Europium);

        ROD_MAGNETIC = CraftingComponent.of("rod_magnetic", rod, IronMagnetic)
                .add(ULV, rod, IronMagnetic)
                .add(LV, rod, IronMagnetic)
                .add(MV, rod, SteelMagnetic)
                .add(HV, rod, SteelMagnetic)
                .add(EV, rod, NeodymiumMagnetic)
                .add(IV, rod, NeodymiumMagnetic)
                .add(LuV, rodLong, NeodymiumMagnetic)
                .add(ZPM, rodLong, NeodymiumMagnetic)
                .add(UV, block, NeodymiumMagnetic)
                .add(UHV, block, SamariumMagnetic);

        ROD_DISTILLATION = CraftingComponent.of("rod_distillation", rod, Blaze)
                .add(ULV, rod, Blaze)
                .add(LV, spring, Copper)
                .add(MV, spring, Cupronickel)
                .add(HV, spring, Kanthal)
                .add(EV, spring, Nichrome)
                .add(IV, spring, RTMAlloy)
                .add(LuV, spring, HSSG)
                .add(ZPM, spring, Naquadah)
                .add(UV, spring, NaquadahAlloy)
                .add(UHV, spring, Trinium);

        ROD_ELECTROMAGNETIC = CraftingComponent.of("rod_electromagnetic", rod, Iron)
                .add(ULV, rod, Iron)
                .add(LV, rod, Iron)
                .add(MV, rod, Steel)
                .add(HV, rod, Steel)
                .add(EV, rod, Neodymium)
                .add(IV, rod, VanadiumGallium)
                .add(LuV, rod, VanadiumGallium)
                .add(ZPM, rod, VanadiumGallium)
                .add(UV, rod, VanadiumGallium)
                .add(UHV, rod, VanadiumGallium);

        ROD_RADIOACTIVE = CraftingComponent.of("rod_radioactive", rod, Uranium235)
                .add(EV, rod, Uranium235)
                .add(IV, rod, Plutonium241)
                .add(LuV, rod, NaquadahEnriched)
                .add(ZPM, rod, Americium)
                .add(UV, rod, Tritanium)
                .add(UHV, rod, Tritanium);

        PIPE_REACTOR = CraftingComponent.of("pipe_reactor", Tags.Items.GLASS)
                .add(ULV, Tags.Items.GLASS)
                .add(LV, Tags.Items.GLASS)
                .add(MV, Tags.Items.GLASS)
                .add(HV, pipeNormalFluid, Polyethylene)
                .add(EV, pipeLargeFluid, Polyethylene)
                .add(IV, pipeHugeFluid, Polyethylene)
                .add(LuV, pipeNormalFluid, Polytetrafluoroethylene)
                .add(ZPM, pipeLargeFluid, Polytetrafluoroethylene)
                .add(UV, pipeHugeFluid, Polytetrafluoroethylene)
                .add(UHV, pipeNormalFluid, Polybenzimidazole);

        POWER_COMPONENT = CraftingComponent.of("power_component", GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(MV, GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(HV, GTItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(EV, GTItems.POWER_INTEGRATED_CIRCUIT.asStack())
                .add(IV, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(LuV, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(ZPM, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(UV, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(UHV, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack());

        VOLTAGE_COIL = CraftingComponent.of("voltage_coil", GTItems.VOLTAGE_COIL_ULV.asStack())
                .add(ULV, GTItems.VOLTAGE_COIL_ULV.asStack())
                .add(LV, GTItems.VOLTAGE_COIL_LV.asStack())
                .add(MV, GTItems.VOLTAGE_COIL_MV.asStack())
                .add(HV, GTItems.VOLTAGE_COIL_HV.asStack())
                .add(EV, GTItems.VOLTAGE_COIL_EV.asStack())
                .add(IV, GTItems.VOLTAGE_COIL_IV.asStack())
                .add(LuV, GTItems.VOLTAGE_COIL_LuV.asStack())
                .add(ZPM, GTItems.VOLTAGE_COIL_ZPM.asStack())
                .add(UV, GTItems.VOLTAGE_COIL_UV.asStack());

        SPRING = CraftingComponent.of("spring", spring, Lead)
                .add(ULV, spring, Lead)
                .add(LV, spring, Tin)
                .add(MV, spring, Copper)
                .add(HV, spring, Gold)
                .add(EV, spring, Aluminium)
                .add(IV, spring, Tungsten)
                .add(LuV, spring, NiobiumTitanium)
                .add(ZPM, spring, VanadiumGallium)
                .add(UV, spring, YttriumBariumCuprate)
                .add(UHV, spring, Europium);

        CRATE = CraftingComponent.of("crate", Tags.Items.CHESTS_WOODEN)
                .add(ULV, Tags.Items.CHESTS_WOODEN)
                .add(LV, GTMachines.WOODEN_CRATE.asStack())
                .add(MV, GTMachines.BRONZE_CRATE.asStack())
                .add(HV, GTMachines.STEEL_CRATE.asStack())
                .add(EV, GTMachines.ALUMINIUM_CRATE.asStack())
                .add(IV, GTMachines.STAINLESS_STEEL_CRATE.asStack())
                .add(LuV, GTMachines.TITANIUM_CRATE.asStack())
                .add(ZPM, GTMachines.TUNGSTENSTEEL_CRATE.asStack())
                .add(UV, GTMachines.SUPER_CHEST[1].asStack())
                .add(UHV, GTMachines.SUPER_CHEST[2].asStack());

        DRUM = CraftingComponent.of("drum", Tags.Items.GLASS)
                .add(ULV, Tags.Items.GLASS)
                .add(LV, GTMachines.WOODEN_DRUM.asStack())
                .add(MV, GTMachines.BRONZE_DRUM.asStack())
                .add(HV, GTMachines.STEEL_DRUM.asStack())
                .add(EV, GTMachines.ALUMINIUM_DRUM.asStack())
                .add(IV, GTMachines.STAINLESS_STEEL_DRUM.asStack())
                .add(LuV, GTMachines.TITANIUM_DRUM.asStack())
                .add(ZPM, GTMachines.TUNGSTENSTEEL_DRUM.asStack())
                .add(UV, GTMachines.SUPER_TANK[1].asStack())
                .add(UHV, GTMachines.SUPER_TANK[2].asStack());

        FRAME = CraftingComponent.of("frame", frameGt, Wood)
                .add(ULV, frameGt, Wood)
                .add(LV, frameGt, Steel)
                .add(MV, frameGt, Aluminium)
                .add(HV, frameGt, StainlessSteel)
                .add(EV, frameGt, Titanium)
                .add(IV, frameGt, TungstenSteel)
                .add(LuV, frameGt, Ruridit)
                .add(ZPM, frameGt, Iridium)
                .add(UV, frameGt, NaquadahAlloy)
                .add(UHV, frameGt, NaquadahAlloy);

        SMALL_SPRING_TRANSFORMER = CraftingComponent.of("small_spring_transformer", springSmall, RedAlloy)
                .add(ULV, springSmall, RedAlloy)
                .add(LV, springSmall, Tin)
                .add(MV, springSmall, Copper)
                .add(HV, springSmall, Gold)
                .add(EV, springSmall, Aluminium)
                .add(IV, springSmall, Platinum)
                .add(LuV, springSmall, NiobiumTitanium)
                .add(ZPM, springSmall, VanadiumGallium)
                .add(UV, springSmall, YttriumBariumCuprate)
                .add(UHV, springSmall, Europium);

        SPRING_TRANSFORMER = CraftingComponent.of("spring_transformer", spring, Tin)
                .add(ULV, spring, Tin)
                .add(LV, spring, Copper)
                .add(MV, spring, Gold)
                .add(HV, spring, Aluminium)
                .add(EV, spring, Platinum)
                .add(IV, spring, NiobiumTitanium)
                .add(LuV, spring, VanadiumGallium)
                .add(ZPM, spring, YttriumBariumCuprate)
                .add(UV, spring, Europium)
                .add(UHV, spring, Europium);

        MinecraftForge.EVENT_BUS.post(new CraftingComponentModificationEvent());
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.craftingComponentModification();
        }
    }

    private static final class KJSCallWrapper {

        private static void craftingComponentModification() {
            GTCEuStartupEvents.CRAFTING_COMPONENTS.post(new CraftingComponentsEventJS());
        }
    }
}
