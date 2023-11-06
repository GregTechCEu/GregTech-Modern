package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;

import java.util.Locale;

/**
 * @author KilaBash
 * @date 2023/7/31
 * @implNote GTCompassSections
 */
public class GTCompassSections {

    private static int priority = 0;

    public final static CompassSection INTRODUCTION = CompassSection.create("introduction")
            .icon(() -> GuiTextures.GREGTECH_LOGO)
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection GENERATIONS = CompassSection.create("generation")
            .icon(() -> new ItemStackTexture(GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.ore, GTMaterials.Silver).asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection COVERS = CompassSection.create("covers")
            .icon(() -> new ItemStackTexture(GTItems.ITEM_FILTER.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection TOOLS = CompassSection.create("tools")
            .icon(() -> new ItemStackTexture(GTItems.TOOL_ITEMS.get(GTMaterials.Iron.getToolTier(), GTToolType.WRENCH).asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection MATERIALS = CompassSection.create("materials")
            .icon(() -> new ItemStackTexture(GTItems.MATERIAL_ITEMS.get(TagPrefix.gear, GTMaterials.Steel).asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection ITEMS = CompassSection.create("items")
            .icon(() -> new ItemStackTexture(GTItems.SHAPE_MOLD_INGOT.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection MISC = CompassSection.create("misc")
            .icon(() -> new ItemStackTexture(GTItems.COIN_GOLD_ANCIENT.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection CIRCUITS = CompassSection.create("circuits")
            .icon(() -> new ItemStackTexture(GTItems.ELECTRONIC_CIRCUIT_LV.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection COMPONENTS = CompassSection.create("components")
            .icon(() -> new ItemStackTexture(GTItems.ELECTRIC_MOTOR_LV.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection BATTERIES = CompassSection.create("batteries")
            .icon(() -> new ItemStackTexture(GTItems.BATTERY_HV_SODIUM.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection BLOCKS = CompassSection.create("blocks")
            .icon(() -> new ItemStackTexture(GTBlocks.COIL_CUPRONICKEL.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection MACHINES = CompassSection.create("machines")
            .icon(() -> new ItemStackTexture(GTMachines.CHEMICAL_REACTOR[GTValues.LV].asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection PARTS = CompassSection.create("parts")
            .icon(() -> new ItemStackTexture(GTMachines.MAINTENANCE_HATCH.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();

    public final static CompassSection MULTIBLOCK = CompassSection.create("multiblock")
            .icon(() -> new ItemStackTexture(GTMachines.ELECTRIC_BLAST_FURNACE.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .priority(priority++)
            .register();
    public final static CompassSection STEAM = CompassSection.create("steam")
            .icon(() -> new ItemStackTexture(GTMachines.STEAM_SOLID_BOILER.left().asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .lang("Steam Age")
            .priority(priority++)
            .register();

    public final static CompassSection[] TIER = new CompassSection[10];

    static {
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            TIER[i] = CompassSection.create(GTValues.VN[i].toLowerCase(Locale.ROOT))
                    .icon(() -> new ItemStackTexture(GTMachines.HULL[finalI].asStack()))
                    .background(() -> GuiTextures.DISPLAY)
                    .lang(GTValues.VNF[i] + " Voltage")
                    .priority(priority++)
                    .register();
        }
    }
    public static void init() {

    }
}
