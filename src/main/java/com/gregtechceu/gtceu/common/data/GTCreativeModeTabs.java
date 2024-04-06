package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;

import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import com.lowdragmc.lowdraglib.utils.LDLItemGroup;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTCreativeModeTabs
 */
@SuppressWarnings("Convert2MethodRef")
public class GTCreativeModeTabs {
    public static LDLItemGroup MATERIAL_FLUID = new LDLItemGroup(GTCEu.MOD_ID, "material_fluid", () -> GTItems.FLUID_CELL.asStack());
    public static LDLItemGroup MATERIAL_ITEM = new LDLItemGroup(GTCEu.MOD_ID, "material_item", () -> ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Aluminium));
    public static LDLItemGroup MATERIAL_BLOCK = new LDLItemGroup(GTCEu.MOD_ID, "material_block", () -> ChemicalHelper.get(TagPrefix.block, GTMaterials.Gold));
    public static LDLItemGroup MATERIAL_PIPE = new LDLItemGroup(GTCEu.MOD_ID, "material_pipe", () -> ChemicalHelper.get(Insulation.WIRE_DOUBLE.getTagPrefix(), GTMaterials.Copper));
    public static LDLItemGroup DECORATION = new LDLItemGroup(GTCEu.MOD_ID, "decoration", () -> GTBlocks.COIL_CUPRONICKEL.asStack());
    public static LDLItemGroup TOOL = new LDLItemGroup(GTCEu.MOD_ID, "tool", () -> ToolHelper.get(GTToolType.WRENCH, GTMaterials.Steel));
    public static LDLItemGroup MACHINE = new LDLItemGroup(GTCEu.MOD_ID, "machine", () -> GTMachines.ELECTROLYZER[GTValues.LV].asStack());
    public static LDLItemGroup ITEM = new LDLItemGroup(GTCEu.MOD_ID, "item", () -> GTItems.COIN_GOLD_ANCIENT.asStack());

}
