package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.LampBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("Convert2MethodRef")
public class GTCreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> MATERIAL_FLUID = REGISTRATE.defaultCreativeTab("material_fluid",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_fluid", REGISTRATE))
                    .icon(() -> GTItems.FLUID_CELL.asStack())
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("material_fluid"),
                            GTCEu.NAME + " Material Fluid Containers"))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MATERIAL_ITEM = REGISTRATE.defaultCreativeTab("material_item",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_item", REGISTRATE))
                    .icon(() -> ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Aluminium))
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("material_item"), GTCEu.NAME + " Material Items"))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MATERIAL_BLOCK = REGISTRATE.defaultCreativeTab("material_block",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_block", REGISTRATE))
                    .icon(() -> ChemicalHelper.get(TagPrefix.block, GTMaterials.Gold))
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("material_block"), GTCEu.NAME + " Material Blocks"))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MATERIAL_PIPE = REGISTRATE.defaultCreativeTab("material_pipe",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_pipe", REGISTRATE))
                    .icon(() -> ChemicalHelper.get(Insulation.WIRE_DOUBLE.getTagPrefix(), GTMaterials.Copper))
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("material_pipe"), GTCEu.NAME + " Material Pipes"))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> DECORATION = REGISTRATE.defaultCreativeTab("decoration",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("decoration", REGISTRATE))
                    .icon(() -> GTBlocks.COIL_CUPRONICKEL.asStack())
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("decoration"), GTCEu.NAME + " Decoration Blocks"))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> TOOL = REGISTRATE.defaultCreativeTab("tool",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("tool", REGISTRATE))
                    .icon(() -> ToolHelper.get(GTToolType.WRENCH, GTMaterials.Steel))
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("tool"), GTCEu.NAME + " Tools"))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MACHINE = REGISTRATE.defaultCreativeTab("machine",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("machine", REGISTRATE))
                    .icon(() -> GTMachines.ELECTROLYZER[GTValues.LV].asStack())
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("machine"), GTCEu.NAME + " Machines"))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> ITEM = REGISTRATE.defaultCreativeTab("item",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("item", REGISTRATE))
                    .icon(() -> GTItems.BASIC_TAPE.asStack())
                    .title(REGISTRATE.addLang("itemGroup", GTCEu.id("item"), GTCEu.NAME + " Items"))
                    .build())
            .register();

    public static void init() {}

    public static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {

        public final String name;
        public final GTRegistrate registrate;

        public RegistrateDisplayItemsGenerator(String name, GTRegistrate registrate) {
            this.name = name;
            this.registrate = registrate;
        }

        @Override
        public void accept(@NotNull CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                           @NotNull CreativeModeTab.Output output) {
            var tab = registrate.get(name, Registries.CREATIVE_MODE_TAB);
            for (var entry : registrate.getAll(Registries.BLOCK)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get().asItem();
                if (item == Items.AIR)
                    continue;
                if (item instanceof IComponentItem componentItem) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    componentItem.fillItemCategory(tab.get(), list);
                    list.forEach(output::accept);
                } else if (item instanceof IGTTool tool) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    tool.definition$fillItemCategory(tab.get(), list);
                    list.forEach(output::accept);
                } else if (item instanceof LampBlockItem lamp) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    lamp.fillItemCategory(tab.get(), list);
                    list.forEach(output::accept);
                } else {
                    output.accept(item);
                }
            }
            for (var entry : registrate.getAll(Registries.ITEM)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (item instanceof IComponentItem componentItem) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    componentItem.fillItemCategory(tab.get(), list);
                    list.forEach(output::accept);
                } else if (item instanceof IGTTool tool) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    tool.definition$fillItemCategory(tab.get(), list);
                    list.forEach(output::accept);
                } else {
                    output.accept(item);
                }
            }
        }
    }
}
