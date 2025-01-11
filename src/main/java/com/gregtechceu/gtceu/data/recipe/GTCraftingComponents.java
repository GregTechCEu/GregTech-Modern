package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.event.CraftingComponentModificationEvent;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.CraftingComponentsEventJS;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;

import static com.gregtechceu.gtceu.api.GTValues.*;

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
    public static CraftingComponent GRINDER;
    public static CraftingComponent SAWBLADE;
    public static CraftingComponent DIAMOND;
    public static CraftingComponent PISTON;
    public static CraftingComponent EMITTER;
    public static CraftingComponent CONVEYOR;
    public static CraftingComponent ROBOT_ARM;
    public static CraftingComponent COIL_HEATING;
    public static CraftingComponent COIL_HEATING_DOUBLE;
    public static CraftingComponent COIL_ELECTRIC;
    public static CraftingComponent STICK_MAGNETIC;
    public static CraftingComponent STICK_DISTILLATION;
    public static CraftingComponent FIELD_GENERATOR;
    public static CraftingComponent STICK_ELECTROMAGNETIC;
    public static CraftingComponent STICK_RADIOACTIVE;
    public static CraftingComponent PIPE_REACTOR;
    public static CraftingComponent POWER_COMPONENT;
    public static CraftingComponent VOLTAGE_COIL;
    public static CraftingComponent SPRING;
    public static CraftingComponent CRATE;
    public static CraftingComponent DRUM;
    public static CraftingComponent FRAME;
    public static CraftingComponent SMALL_SPRING_TRANSFORMER;
    public static CraftingComponent SPRING_TRANSFORMER;

    public static void initializeComponents() {
        /*
         * GTCEu must supply values for at least tiers 1 through 8 (through UV)
         */
        CIRCUIT = new CraftingComponent(CustomTags.ULV_CIRCUITS)
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

        BETTER_CIRCUIT = new CraftingComponent(CustomTags.ULV_CIRCUITS)
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

        WIRE_ELECTRIC = new CraftingComponent(new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Gold))
                .add(ULV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Gold))
                .add(LV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Gold))
                .add(MV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Silver))
                .add(HV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Electrum))
                .add(EV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Platinum))
                .add(IV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium))
                .add(LuV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium))
                .add(ZPM, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium))
                .add(UV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium))
                .add(UHV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Osmium));

        WIRE_QUAD = new CraftingComponent(new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Lead))
                .add(ULV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Lead))
                .add(LV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Tungsten))
                .add(LuV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Europium));

        WIRE_OCT = new CraftingComponent(new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Lead))
                .add(ULV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Lead))
                .add(LV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Tungsten))
                .add(LuV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Europium));

        WIRE_HEX = new CraftingComponent(new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Lead))
                .add(ULV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Lead))
                .add(LV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Tungsten))
                .add(LuV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.wireGtHex, GTMaterials.Europium));

        CABLE = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.RedAlloy))
                .add(LV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Platinum))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Europium));

        CABLE_DOUBLE = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.RedAlloy))
                .add(LV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Platinum))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Europium));

        CABLE_QUAD = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.RedAlloy))
                .add(LV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Platinum))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Europium));

        CABLE_OCT = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.RedAlloy))
                .add(LV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Platinum))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Europium));

        CABLE_HEX = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.RedAlloy))
                .add(LV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Platinum))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Europium));

        CABLE_TIER_UP = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold))
                .add(HV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium))
                .add(EV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Platinum))
                .add(IV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate))
                .add(UV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Europium))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Europium));

        CABLE_TIER_UP_DOUBLE = new CraftingComponent(
                new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Gold))
                .add(HV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Aluminium))
                .add(EV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Platinum))
                .add(IV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.NiobiumTitanium))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.VanadiumGallium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.YttriumBariumCuprate))
                .add(UV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Europium))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtDouble, GTMaterials.Europium));

        CABLE_TIER_UP_QUAD = new CraftingComponent(
                new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Gold))
                .add(HV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Aluminium))
                .add(EV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Platinum))
                .add(IV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.NiobiumTitanium))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.VanadiumGallium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.YttriumBariumCuprate))
                .add(UV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Europium))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.Europium));

        CABLE_TIER_UP_OCT = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Gold))
                .add(HV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Aluminium))
                .add(EV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Platinum))
                .add(IV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.NiobiumTitanium))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.VanadiumGallium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.YttriumBariumCuprate))
                .add(UV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Europium))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.Europium));

        CABLE_TIER_UP_HEX = new CraftingComponent(new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Gold))
                .add(HV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Aluminium))
                .add(EV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Platinum))
                .add(IV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.NiobiumTitanium))
                .add(LuV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.VanadiumGallium))
                .add(ZPM, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.YttriumBariumCuprate))
                .add(UV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Europium))
                .add(UHV, new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.Europium));

        HULL = new CraftingComponent(GTMachines.HULL[ULV].asStack())
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

        CASING = new CraftingComponent(GTBlocks.MACHINE_CASING_ULV.asStack())
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

        PIPE_NORMAL = new CraftingComponent(new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze))
                .add(ULV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze))
                .add(LV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze))
                .add(MV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel))
                .add(HV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel))
                .add(EV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Titanium))
                .add(IV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.TungstenSteel))
                .add(LuV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Iridium))
                .add(UV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Naquadah))
                .add(UHV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Naquadah));

        PIPE_LARGE = new CraftingComponent(new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Bronze))
                .add(ULV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Bronze))
                .add(LV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Bronze))
                .add(MV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Steel))
                .add(HV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel))
                .add(EV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Titanium))
                .add(IV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.TungstenSteel))
                .add(LuV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Ultimet))
                .add(UV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Naquadah))
                .add(UHV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Neutronium));

        PIPE_NONUPLE = new CraftingComponent(new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Titanium))
                .add(EV, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Titanium))
                .add(IV, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.TungstenSteel))
                .add(LuV, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Iridium))
                .add(UV, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Naquadah))
                .add(UHV, new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Neutronium));

        /*
         * Glass: Steam-MV
         * Tempered: HV, EV
         * Laminated Glass: IV, LuV
         * Fusion: ZPM, UV, UHV
         */
        GLASS = new CraftingComponent(Tags.Items.GLASS)
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

        PLATE = new CraftingComponent(new UnificationEntry(TagPrefix.plate, GTMaterials.Iron))
                .add(ULV, new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron))
                .add(LV, new UnificationEntry(TagPrefix.plate, GTMaterials.Steel))
                .add(MV, new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium))
                .add(HV, new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel))
                .add(EV, new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium))
                .add(IV, new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel))
                .add(LuV, new UnificationEntry(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium))
                .add(ZPM, new UnificationEntry(TagPrefix.plate, GTMaterials.NaquadahAlloy))
                .add(UV, new UnificationEntry(TagPrefix.plate, GTMaterials.Darmstadtium))
                .add(UHV, new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium));

        HULL_PLATE = new CraftingComponent(new UnificationEntry(TagPrefix.plate, GTMaterials.Wood))
                .add(ULV, new UnificationEntry(TagPrefix.plate, GTMaterials.Wood))
                .add(LV, new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron))
                .add(MV, new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron))
                .add(HV, new UnificationEntry(TagPrefix.plate, GTMaterials.Polyethylene))
                .add(EV, new UnificationEntry(TagPrefix.plate, GTMaterials.Polyethylene))
                .add(IV, new UnificationEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene))
                .add(LuV, new UnificationEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene))
                .add(ZPM, new UnificationEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole))
                .add(UV, new UnificationEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole))
                .add(UHV, new UnificationEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole));

        ROTOR = new CraftingComponent(new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin))
                .add(ULV, new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.rotor, GTMaterials.Bronze))
                .add(HV, new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel))
                .add(EV, new UnificationEntry(TagPrefix.rotor, GTMaterials.StainlessSteel))
                .add(IV, new UnificationEntry(TagPrefix.rotor, GTMaterials.TungstenSteel))
                .add(LuV, new UnificationEntry(TagPrefix.rotor, GTMaterials.RhodiumPlatedPalladium))
                .add(ZPM, new UnificationEntry(TagPrefix.rotor, GTMaterials.NaquadahAlloy))
                .add(UV, new UnificationEntry(TagPrefix.rotor, GTMaterials.Darmstadtium))
                .add(UHV, new UnificationEntry(TagPrefix.rotor, GTMaterials.Darmstadtium));

        GRINDER = new CraftingComponent(new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond))
                .add(ULV, new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond))
                .add(LV, new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond))
                .add(MV, new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond))
                .add(HV, GTItems.COMPONENT_GRINDER_DIAMOND.asStack())
                .add(EV, GTItems.COMPONENT_GRINDER_DIAMOND.asStack())
                .add(IV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(LuV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(ZPM, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(UV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack())
                .add(UHV, GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack());

        SAWBLADE = new CraftingComponent(new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Bronze))
                .add(ULV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Bronze))
                .add(LV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.CobaltBrass))
                .add(MV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.VanadiumSteel))
                .add(HV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.RedSteel))
                .add(EV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Ultimet))
                .add(IV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.TungstenCarbide))
                .add(LuV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.HSSE))
                .add(ZPM, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.NaquadahAlloy))
                .add(UV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Duranium))
                .add(UHV, new UnificationEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Duranium));

        DIAMOND = new CraftingComponent(new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond));

        MOTOR = new CraftingComponent(GTItems.ELECTRIC_MOTOR_LV.asStack())
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

        PUMP = new CraftingComponent(GTItems.ELECTRIC_PUMP_LV.asStack())
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

        PISTON = new CraftingComponent(GTItems.ELECTRIC_PISTON_LV.asStack())
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

        EMITTER = new CraftingComponent(GTItems.EMITTER_LV.asStack())
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

        SENSOR = new CraftingComponent(GTItems.SENSOR_LV.asStack())
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

        CONVEYOR = new CraftingComponent(GTItems.CONVEYOR_MODULE_LV.asStack())
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

        ROBOT_ARM = new CraftingComponent(GTItems.ROBOT_ARM_LV.asStack())
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

        FIELD_GENERATOR = new CraftingComponent(GTItems.FIELD_GENERATOR_LV.asStack())
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

        COIL_HEATING = new CraftingComponent(new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Copper))
                .add(ULV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Copper))
                .add(LV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Cupronickel))
                .add(HV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Kanthal))
                .add(EV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Nichrome))
                .add(IV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.RTMAlloy))
                .add(LuV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.HSSG))
                .add(ZPM, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Naquadah))
                .add(UV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.NaquadahAlloy))
                .add(UHV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Trinium));

        COIL_HEATING_DOUBLE = new CraftingComponent(new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Copper))
                .add(ULV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Copper))
                .add(LV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Cupronickel))
                .add(HV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Kanthal))
                .add(EV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Nichrome))
                .add(IV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.RTMAlloy))
                .add(LuV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.HSSG))
                .add(ZPM, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Naquadah))
                .add(UV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.NaquadahAlloy))
                .add(UHV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Trinium));

        COIL_ELECTRIC = new CraftingComponent(new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Tin))
                .add(ULV, new UnificationEntry(TagPrefix.wireGtSingle, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.wireGtDouble, GTMaterials.Silver))
                .add(EV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Steel))
                .add(IV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Graphene))
                .add(LuV, new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.NiobiumNitride))
                .add(ZPM, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.wireGtOctal, GTMaterials.Europium));

        STICK_MAGNETIC = new CraftingComponent(new UnificationEntry(TagPrefix.rod, GTMaterials.IronMagnetic))
                .add(ULV, new UnificationEntry(TagPrefix.rod, GTMaterials.IronMagnetic))
                .add(LV, new UnificationEntry(TagPrefix.rod, GTMaterials.IronMagnetic))
                .add(MV, new UnificationEntry(TagPrefix.rod, GTMaterials.SteelMagnetic))
                .add(HV, new UnificationEntry(TagPrefix.rod, GTMaterials.SteelMagnetic))
                .add(EV, new UnificationEntry(TagPrefix.rod, GTMaterials.NeodymiumMagnetic))
                .add(IV, new UnificationEntry(TagPrefix.rod, GTMaterials.NeodymiumMagnetic))
                .add(LuV, new UnificationEntry(TagPrefix.rodLong, GTMaterials.NeodymiumMagnetic))
                .add(ZPM, new UnificationEntry(TagPrefix.rodLong, GTMaterials.NeodymiumMagnetic))
                .add(UV, new UnificationEntry(TagPrefix.block, GTMaterials.NeodymiumMagnetic))
                .add(UHV, new UnificationEntry(TagPrefix.block, GTMaterials.SamariumMagnetic));

        STICK_DISTILLATION = new CraftingComponent(new UnificationEntry(TagPrefix.rod, GTMaterials.Blaze))
                .add(ULV, new UnificationEntry(TagPrefix.rod, GTMaterials.Blaze))
                .add(LV, new UnificationEntry(TagPrefix.spring, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.spring, GTMaterials.Cupronickel))
                .add(HV, new UnificationEntry(TagPrefix.spring, GTMaterials.Kanthal))
                .add(EV, new UnificationEntry(TagPrefix.spring, GTMaterials.Nichrome))
                .add(IV, new UnificationEntry(TagPrefix.spring, GTMaterials.RTMAlloy))
                .add(LuV, new UnificationEntry(TagPrefix.spring, GTMaterials.HSSG))
                .add(ZPM, new UnificationEntry(TagPrefix.spring, GTMaterials.Naquadah))
                .add(UV, new UnificationEntry(TagPrefix.spring, GTMaterials.NaquadahAlloy))
                .add(UHV, new UnificationEntry(TagPrefix.spring, GTMaterials.Trinium));

        STICK_ELECTROMAGNETIC = new CraftingComponent(new UnificationEntry(TagPrefix.rod, GTMaterials.Iron))
                .add(ULV, new UnificationEntry(TagPrefix.rod, GTMaterials.Iron))
                .add(LV, new UnificationEntry(TagPrefix.rod, GTMaterials.Iron))
                .add(MV, new UnificationEntry(TagPrefix.rod, GTMaterials.Steel))
                .add(HV, new UnificationEntry(TagPrefix.rod, GTMaterials.Steel))
                .add(EV, new UnificationEntry(TagPrefix.rod, GTMaterials.Neodymium))
                .add(IV, new UnificationEntry(TagPrefix.rod, GTMaterials.VanadiumGallium))
                .add(LuV, new UnificationEntry(TagPrefix.rod, GTMaterials.VanadiumGallium))
                .add(ZPM, new UnificationEntry(TagPrefix.rod, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.rod, GTMaterials.VanadiumGallium))
                .add(UHV, new UnificationEntry(TagPrefix.rod, GTMaterials.VanadiumGallium));

        STICK_RADIOACTIVE = new CraftingComponent(new UnificationEntry(TagPrefix.rod, GTMaterials.Uranium235))
                .add(EV, new UnificationEntry(TagPrefix.rod, GTMaterials.Uranium235))
                .add(IV, new UnificationEntry(TagPrefix.rod, GTMaterials.Plutonium241))
                .add(LuV, new UnificationEntry(TagPrefix.rod, GTMaterials.NaquadahEnriched))
                .add(ZPM, new UnificationEntry(TagPrefix.rod, GTMaterials.Americium))
                .add(UV, new UnificationEntry(TagPrefix.rod, GTMaterials.Tritanium))
                .add(UHV, new UnificationEntry(TagPrefix.rod, GTMaterials.Tritanium));

        PIPE_REACTOR = new CraftingComponent(Tags.Items.GLASS)
                .add(ULV, Tags.Items.GLASS)
                .add(LV, Tags.Items.GLASS)
                .add(MV, Tags.Items.GLASS)
                .add(HV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polyethylene))
                .add(EV, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polyethylene))
                .add(IV, new UnificationEntry(TagPrefix.pipeHugeFluid, GTMaterials.Polyethylene))
                .add(LuV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polytetrafluoroethylene))
                .add(ZPM, new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene))
                .add(UV, new UnificationEntry(TagPrefix.pipeHugeFluid, GTMaterials.Polytetrafluoroethylene))
                .add(UHV, new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polybenzimidazole));

        POWER_COMPONENT = new CraftingComponent(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(MV, GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(HV, GTItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(EV, GTItems.POWER_INTEGRATED_CIRCUIT.asStack())
                .add(IV, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(LuV, GTItems.HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(ZPM, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(UV, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack())
                .add(UHV, GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack());

        VOLTAGE_COIL = new CraftingComponent(GTItems.VOLTAGE_COIL_ULV.asStack())
                .add(ULV, GTItems.VOLTAGE_COIL_ULV.asStack())
                .add(LV, GTItems.VOLTAGE_COIL_LV.asStack())
                .add(MV, GTItems.VOLTAGE_COIL_MV.asStack())
                .add(HV, GTItems.VOLTAGE_COIL_HV.asStack())
                .add(EV, GTItems.VOLTAGE_COIL_EV.asStack())
                .add(IV, GTItems.VOLTAGE_COIL_IV.asStack())
                .add(LuV, GTItems.VOLTAGE_COIL_LuV.asStack())
                .add(ZPM, GTItems.VOLTAGE_COIL_ZPM.asStack())
                .add(UV, GTItems.VOLTAGE_COIL_UV.asStack());

        SPRING = new CraftingComponent(new UnificationEntry(TagPrefix.spring, GTMaterials.Lead))
                .add(ULV, new UnificationEntry(TagPrefix.spring, GTMaterials.Lead))
                .add(LV, new UnificationEntry(TagPrefix.spring, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.spring, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.spring, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.spring, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.spring, GTMaterials.Tungsten))
                .add(LuV, new UnificationEntry(TagPrefix.spring, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.spring, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.spring, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.spring, GTMaterials.Europium));

        CRATE = new CraftingComponent(Tags.Items.CHESTS_WOODEN)
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

        DRUM = new CraftingComponent(Tags.Items.GLASS)
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

        FRAME = new CraftingComponent(new UnificationEntry(TagPrefix.frameGt, GTMaterials.Wood))
                .add(ULV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Wood))
                .add(LV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel))
                .add(MV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Aluminium))
                .add(HV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.StainlessSteel))
                .add(EV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium))
                .add(IV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel))
                .add(LuV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Ruridit))
                .add(ZPM, new UnificationEntry(TagPrefix.frameGt, GTMaterials.Iridium))
                .add(UV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.NaquadahAlloy))
                .add(UHV, new UnificationEntry(TagPrefix.frameGt, GTMaterials.NaquadahAlloy));

        SMALL_SPRING_TRANSFORMER = new CraftingComponent(
                new UnificationEntry(TagPrefix.springSmall, GTMaterials.RedAlloy))
                .add(ULV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.RedAlloy))
                .add(LV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.Tin))
                .add(MV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.Copper))
                .add(HV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.Gold))
                .add(EV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.Aluminium))
                .add(IV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.Platinum))
                .add(LuV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.NiobiumTitanium))
                .add(ZPM, new UnificationEntry(TagPrefix.springSmall, GTMaterials.VanadiumGallium))
                .add(UV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.YttriumBariumCuprate))
                .add(UHV, new UnificationEntry(TagPrefix.springSmall, GTMaterials.Europium));

        SPRING_TRANSFORMER = new CraftingComponent(new UnificationEntry(TagPrefix.spring, GTMaterials.Tin))
                .add(ULV, new UnificationEntry(TagPrefix.spring, GTMaterials.Tin))
                .add(LV, new UnificationEntry(TagPrefix.spring, GTMaterials.Copper))
                .add(MV, new UnificationEntry(TagPrefix.spring, GTMaterials.Gold))
                .add(HV, new UnificationEntry(TagPrefix.spring, GTMaterials.Aluminium))
                .add(EV, new UnificationEntry(TagPrefix.spring, GTMaterials.Platinum))
                .add(IV, new UnificationEntry(TagPrefix.spring, GTMaterials.NiobiumTitanium))
                .add(LuV, new UnificationEntry(TagPrefix.spring, GTMaterials.VanadiumGallium))
                .add(ZPM, new UnificationEntry(TagPrefix.spring, GTMaterials.YttriumBariumCuprate))
                .add(UV, new UnificationEntry(TagPrefix.spring, GTMaterials.Europium))
                .add(UHV, new UnificationEntry(TagPrefix.spring, GTMaterials.Europium));

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
