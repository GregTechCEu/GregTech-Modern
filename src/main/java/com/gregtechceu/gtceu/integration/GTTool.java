package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public class GTTool {

    private int currentSlot;
    protected NonNullList<ItemStack> items = NonNullList.create();

    public GTTool(Material material) {
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        var tools = property.getTypes();
        currentSlot = 1;

        for(int i = 0; i < 81; i++) {
            items.add(ItemStack.EMPTY);
        }

        if(property.hasType(GTToolType.PICKAXE)) {
            addToItems(1, material, GTToolType.PICKAXE);
        }

        if(property.hasType(GTToolType.SHOVEL)) {
            addToItems(2, material, GTToolType.SHOVEL);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.AXE)) {
            addToItems(3, material, GTToolType.AXE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SWORD)) {
            addToItems(4, material, GTToolType.SWORD);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.HOE)) {
            addToItems(5, material, GTToolType.HOE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.BUTCHERY_KNIFE)) {
            addToItems(6, material, GTToolType.BUTCHERY_KNIFE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.BUZZSAW)) {
            addToItems(7, material, GTToolType.BUZZSAW);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.CHAINSAW_LV)) {
            addToItems(8, material, GTToolType.CHAINSAW_LV);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.CROWBAR)) {
            addToItems(9, material, GTToolType.CROWBAR);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.DRILL_LV)) {
            addToItems(10, material, GTToolType.DRILL_LV);
            /*if(property.hasType(GTToolType.DRILL_MV)) addToItems(material, GTToolType.DRILL_MV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.DRILL_HV)) addToItems(material, GTToolType.DRILL_HV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.DRILL_EV)) addToItems(material, GTToolType.DRILL_EV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.DRILL_IV)) addToItems(material, GTToolType.DRILL_IV);
            else addEmptyItem(1);*/
        }

        if(property.hasType(GTToolType.FILE)) {
            addToItems(15, material, GTToolType.FILE);
        }

        if(property.hasType(GTToolType.HARD_HAMMER)) {
            addToItems(16, material, GTToolType.HARD_HAMMER);
        }

        if(property.hasType(GTToolType.KNIFE)) {
            addToItems(17, material, GTToolType.KNIFE);
        }

        if(property.hasType(GTToolType.MINING_HAMMER)) {
            addToItems(18, material, GTToolType.MINING_HAMMER);
        }

        if(property.hasType(GTToolType.MORTAR)) {
            addToItems(19, material, GTToolType.MORTAR);
        }

        if(property.hasType(GTToolType.PLUNGER)) {
            addToItems(20, material, GTToolType.PLUNGER);
        }

        if(property.hasType(GTToolType.SAW)) {
            addToItems(21, material, GTToolType.SAW);
        }

        if(property.hasType(GTToolType.SCREWDRIVER)) {
            addToItems(22, material, GTToolType.SCREWDRIVER);
            if(property.hasType(GTToolType.SCREWDRIVER_LV)) addToItems(23, material, GTToolType.SCREWDRIVER_LV);
        }

        if(property.hasType(GTToolType.SCYTHE)) {
            addToItems(24, material, GTToolType.SCYTHE);
        }

        if(property.hasType(GTToolType.SHEARS)) {
            addToItems(25, material, GTToolType.SHEARS);
        }

        if(property.hasType(GTToolType.SOFT_MALLET)) {
            addToItems(26, material, GTToolType.SOFT_MALLET);
        }

        if(property.hasType(GTToolType.SPADE)) {
            addToItems(27, material, GTToolType.SPADE);
        }

        if(property.hasType(GTToolType.WIRE_CUTTER)) {
            addToItems(28, material, GTToolType.WIRE_CUTTER);
            if(property.hasType(GTToolType.WIRE_CUTTER_LV)) addToItems(29, material, GTToolType.WIRE_CUTTER_LV);
            /*if(property.hasType(GTToolType.WIRE_CUTTER_HV)) addToItems(material, GTToolType.WIRE_CUTTER_HV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.WIRE_CUTTER_IV)) addToItems(material, GTToolType.WIRE_CUTTER_IV);
            else addEmptyItem(1);*/
        }

        if(property.hasType(GTToolType.WRENCH)) {
            addToItems(32, material, GTToolType.WRENCH);
            if(property.hasType(GTToolType.WRENCH_LV)) addToItems(33, material, GTToolType.WRENCH_LV);
            /*if(property.hasType(GTToolType.WRENCH_HV)) addToItems(material, GTToolType.WRENCH_HV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.WRENCH_IV)) addToItems(material, GTToolType.WRENCH_IV);
            else addEmptyItem(1);*/
        }
    }

    private void addToItems(Material material, GTToolType type) {
        addToItems(ToolHelper.get(type, material));
    }

    private void addToItems(int index, Material material, GTToolType type) {
        addToItems(index, ToolHelper.get(type, material));
    }

    private void addToItems(int index, ItemStack stack) {
        items.add(index, stack);
        currentSlot++;
    }

    private void addToItems(ItemStack stack) {
        items.add(stack);
        currentSlot++;
    }

    private void addEmptyItem(int amount) {
        for(int i = 0; i < amount; i++) {
            addToItems(ItemStack.EMPTY);
        }
    }

    public void getTooltip(int slotIndex, List<Component> tooltips) {

    }
}
