package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtlib.utils.GTLibItemGroup;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTCreativeModeTabs
 */
@SuppressWarnings("Convert2MethodRef")
public class GTCreativeModeTabs {
    public static GTLibItemGroup MATERIAL_FLUID = new GTLibItemGroup(GTCEu.MOD_ID, "material_fluid", () -> GTItems.FLUID_CELL.asStack());
    public static GTLibItemGroup MATERIAL_ITEM = new GTLibItemGroup(GTCEu.MOD_ID, "material_item", () -> ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Aluminium));
    public static GTLibItemGroup MATERIAL_BLOCK = new GTLibItemGroup(GTCEu.MOD_ID, "material_block", () -> ChemicalHelper.get(TagPrefix.block, GTMaterials.Gold));
    public static GTLibItemGroup MATERIAL_PIPE = new GTLibItemGroup(GTCEu.MOD_ID, "material_pipe", () -> ChemicalHelper.get(Insulation.WIRE_DOUBLE.getTagPrefix(), GTMaterials.Copper));
    public static GTLibItemGroup DECORATION = new GTLibItemGroup(GTCEu.MOD_ID, "decoration", () -> GTBlocks.COIL_CUPRONICKEL.asStack());
    public static GTLibItemGroup TOOL = new GTLibItemGroup(GTCEu.MOD_ID, "tool", () -> ToolHelper.get(GTToolType.WRENCH, GTMaterials.Steel));
    public static GTLibItemGroup MACHINE = new GTLibItemGroup(GTCEu.MOD_ID, "machine", () -> GTMachines.ELECTROLYZER[0].asStack());
    public static GTLibItemGroup ITEM = new GTLibItemGroup(GTCEu.MOD_ID, "item", () -> GTItems.COIN_GOLD_ANCIENT.asStack());

}
