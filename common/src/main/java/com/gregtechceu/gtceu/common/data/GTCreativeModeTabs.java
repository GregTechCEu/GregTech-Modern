package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;

import javax.annotation.Nonnull;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTCreativeModeTabs
 */
@SuppressWarnings("Convert2MethodRef")
public class GTCreativeModeTabs {
    public static RegistryEntry<CreativeModeTab> MATERIAL_FLUID = REGISTRATE.defaultCreativeTab("material_fluid",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_fluid"))
                    .icon(() -> GTItems.FLUID_CELL.asStack())
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MATERIAL_ITEM = REGISTRATE.defaultCreativeTab("material_item",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_item"))
                    .icon(() -> ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Aluminium))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MATERIAL_BLOCK = REGISTRATE.defaultCreativeTab("material_block",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_block"))
                    .icon(() -> ChemicalHelper.get(TagPrefix.block, GTMaterials.Gold))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MATERIAL_PIPE = REGISTRATE.defaultCreativeTab("material_pipe",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_pipe"))
                    .icon(() -> ChemicalHelper.get(Insulation.WIRE_DOUBLE.getTagPrefix(), GTMaterials.Copper))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> DECORATION = REGISTRATE.defaultCreativeTab("decoration",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("decoration"))
                    .icon(() -> GTBlocks.COIL_CUPRONICKEL.asStack())
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> TOOL = REGISTRATE.defaultCreativeTab("tool",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("tool"))
                    .icon(() -> ToolHelper.get(GTToolType.WRENCH, GTMaterials.Steel))
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> MACHINE = REGISTRATE.defaultCreativeTab("machine",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("machine"))
                    .icon(() -> GTMachines.ELECTROLYZER[GTValues.LV].asStack())
                    .build())
            .register();
    public static RegistryEntry<CreativeModeTab> ITEM = REGISTRATE.defaultCreativeTab("item",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("item"))
                    .icon(() -> GTItems.COIN_GOLD_ANCIENT.asStack())
                    .build())
            .register();

    public static void init() {

    }

    public static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {

        public final String name;

        public RegistrateDisplayItemsGenerator(String name) {
            this.name = name;
        }

        @Override
        public void accept(@Nonnull CreativeModeTab.ItemDisplayParameters itemDisplayParameters, @Nonnull CreativeModeTab.Output output) {
            var tab = REGISTRATE.get(name, Registries.CREATIVE_MODE_TAB);
            for (var entry : REGISTRATE.getAll(Registries.BLOCK)) {
                if (!REGISTRATE.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get().asItem();
                if (item == Items.AIR)
                    continue;
                if (item instanceof ComponentItem componentItem) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    componentItem.fillItemCategory(tab.get(), list);
                    list.forEach(output::accept);
                } else {
                    output.accept(item);
                }
            }
            for (var entry : REGISTRATE.getAll(Registries.ITEM)) {
                if (!REGISTRATE.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (item instanceof ComponentItem componentItem) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    componentItem.fillItemCategory(tab.get(), list);
                    list.forEach(output::accept);
                } else {
                    output.accept(item);
                }
            }
        }
    }

}
