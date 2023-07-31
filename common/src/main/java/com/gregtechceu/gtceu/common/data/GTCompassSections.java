package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;

/**
 * @author KilaBash
 * @date 2023/7/31
 * @implNote GTCompassSections
 */
public class GTCompassSections {
    public final static CompassSection INTRODUCTION = CompassSection.create("introduction")
            .icon(() -> GuiTextures.GREGTECH_LOGO)
            .background(() -> GuiTextures.DISPLAY)
            .priority(0)
            .register();

    public final static CompassSection GENERATIONS = CompassSection.create("generation")
            .icon(() -> new ItemStackTexture(GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.ore, GTMaterials.Uranium235).asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .register();

    public final static CompassSection COVERS = CompassSection.create("covers")
            .icon(() -> new ItemStackTexture(GTItems.ITEM_FILTER.asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .register();

    public final static CompassSection TOOLS = CompassSection.create("tools")
            .icon(() -> new ItemStackTexture(GTItems.TOOL_ITEMS.get(GTMaterials.Iron.getToolTier(), GTToolType.WRENCH).asStack()))
            .background(() -> GuiTextures.DISPLAY)
            .register();

    public static void init() {

    }
}
