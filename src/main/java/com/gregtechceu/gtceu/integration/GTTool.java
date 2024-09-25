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
        currentSlot = 0;

        if(property.hasType(GTToolType.PICKAXE)) {
            addToItems(material, GTToolType.PICKAXE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SHOVEL)) {
            addToItems(material, GTToolType.SHOVEL);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.AXE)) {
            addToItems(material, GTToolType.AXE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SWORD)) {
            addToItems(material, GTToolType.SWORD);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.HOE)) {
            addToItems(material, GTToolType.HOE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.BUTCHERY_KNIFE)) {
            addToItems(material, GTToolType.BUTCHERY_KNIFE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.BUZZSAW)) {
            addToItems(material, GTToolType.BUZZSAW);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.CHAINSAW_LV)) {
            addToItems(material, GTToolType.CHAINSAW_LV);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.CROWBAR)) {
            addToItems(material, GTToolType.CROWBAR);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.DRILL_LV)) {
            addToItems(material, GTToolType.DRILL_LV);
            if(property.hasType(GTToolType.DRILL_MV)) addToItems(material, GTToolType.DRILL_MV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.DRILL_HV)) addToItems(material, GTToolType.DRILL_HV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.DRILL_EV)) addToItems(material, GTToolType.DRILL_EV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.DRILL_IV)) addToItems(material, GTToolType.DRILL_IV);
            else addEmptyItem(1);
        }
        else addEmptyItem(5);

        if(property.hasType(GTToolType.FILE)) {
            addToItems(material, GTToolType.FILE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.HARD_HAMMER)) {
            addToItems(material, GTToolType.HARD_HAMMER);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.KNIFE)) {
            addToItems(material, GTToolType.KNIFE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.MINING_HAMMER)) {
            addToItems(material, GTToolType.MINING_HAMMER);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.MORTAR)) {
            addToItems(material, GTToolType.MORTAR);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.PLUNGER)) {
            addToItems(material, GTToolType.PLUNGER);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SAW)) {
            addToItems(material, GTToolType.SAW);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SCREWDRIVER)) {
            addToItems(material, GTToolType.SCREWDRIVER);
            if(property.hasType(GTToolType.SCREWDRIVER_LV)) addToItems(material, GTToolType.SCREWDRIVER_LV);
            else addEmptyItem(1);
        }
        else addEmptyItem(2);

        if(property.hasType(GTToolType.SCYTHE)) {
            addToItems(material, GTToolType.SCYTHE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SHEARS)) {
            addToItems(material, GTToolType.SHEARS);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SOFT_MALLET)) {
            addToItems(material, GTToolType.SOFT_MALLET);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.SPADE)) {
            addToItems(material, GTToolType.SPADE);
        }
        else addEmptyItem(1);

        if(property.hasType(GTToolType.WIRE_CUTTER)) {
            addToItems(material, GTToolType.WIRE_CUTTER);
            if(property.hasType(GTToolType.WIRE_CUTTER_LV)) addToItems(material, GTToolType.WIRE_CUTTER_LV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.WIRE_CUTTER_HV)) addToItems(material, GTToolType.WIRE_CUTTER_HV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.WIRE_CUTTER_IV)) addToItems(material, GTToolType.WIRE_CUTTER_IV);
            else addEmptyItem(1);
        }
        else addEmptyItem(4);

        if(property.hasType(GTToolType.WRENCH)) {
            addToItems(material, GTToolType.WRENCH);
            if(property.hasType(GTToolType.WRENCH_LV)) addToItems(material, GTToolType.WRENCH_LV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.WRENCH_HV)) addToItems(material, GTToolType.WRENCH_HV);
            else addEmptyItem(1);
            if(property.hasType(GTToolType.WRENCH_IV)) addToItems(material, GTToolType.WRENCH_IV);
            else addEmptyItem(1);
        }
        else addEmptyItem(4);
    }

    private void addToItems(Material material, GTToolType type) {
        addToItems(ToolHelper.get(type, material));
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
