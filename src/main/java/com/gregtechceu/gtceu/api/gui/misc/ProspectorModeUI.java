package com.gregtechceu.gtceu.api.gui.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;

final class ProspectorModeUI {

    private ProspectorModeUI() {}

    static Object oreIcon(String name) {
        if (name.startsWith("material_")) {
            var mat = GTMaterials.get(name.substring(9));
            if (!mat.isNull()) {
                var list = new ArrayList<ItemStack>();
                for (TagPrefix oreTag : TagPrefix.ORES.keySet()) {
                    for (var block : ChemicalHelper.getBlocks(new MaterialEntry(oreTag, mat))) {
                        list.add(new ItemStack(block));
                    }
                }
                return new ItemStackTexture(list.toArray(ItemStack[]::new)).scale(0.8f);
            }
        }
        return new ItemStackTexture(new ItemStack(BuiltInRegistries.BLOCK.getValue(Identifier.parse(name))))
                .scale(0.8f);
    }

    static Object fluidIcon(ProspectorMode.FluidInfo item) {
        return new ItemStackTexture(item.fluid().getBucket());
    }

    static Object bedrockOreIcon(ProspectorMode.OreInfo item) {
        var material = item.material();
        ItemStack stack = GTUtil.getFirstNonEmpty(
                ChemicalHelper.get(TagPrefix.get(ConfigHolder.INSTANCE.machines.bedrockOreDropTagPrefix), material),
                ChemicalHelper.get(TagPrefix.crushed, material),
                ChemicalHelper.get(TagPrefix.gem, material),
                ChemicalHelper.get(TagPrefix.ore, material),
                ChemicalHelper.get(TagPrefix.dust, material));
        return new ItemStackTexture(stack).scale(0.8f);
    }

    static void drawFluidGrid(GuiGraphics graphics, ProspectorMode.FluidInfo[] items, int x, int y, int width,
                              int height) {
        if (items.length > 0) {
            var item = items[0];
            double progress = item.left() * 1.0 / Math.max(Math.min(item.left(), 100), 1);
            float drawnU = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnU(progress);
            float drawnV = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnV(progress);
            float drawnWidth = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnWidth(progress);
            float drawnHeight = (float) ProgressTexture.FillDirection.DOWN_TO_UP.getDrawnHeight(progress);
            DrawerHelper.drawFluidForGui(graphics, new FluidStack(item.fluid(), item.left()),
                    (int) (x + drawnU * width), (int) (y + drawnV * height), ((int) (width * drawnWidth)),
                    ((int) (height * drawnHeight)));
        }
    }
}
