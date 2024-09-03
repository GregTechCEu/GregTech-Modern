package com.gregtechceu.gtceu.integration.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.rei.multipage.MultiblockInfoDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.oreprocessing.GTOreProcessingDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.orevein.GTBedrockFluidDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.orevein.GTBedrockOreDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.orevein.GTOreVeinDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.recipe.GTRecipeTypeDisplayCategory;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.shedaniel.rei.plugin.common.BuiltinPlugin.SMELTING;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote REIPlugin
 */
@REIPluginClient
public class GTREIPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new MultiblockInfoDisplayCategory());
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.add(new GTOreProcessingDisplayCategory());
        registry.add(new GTOreVeinDisplayCategory());
        registry.add(new GTBedrockFluidDisplayCategory());
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.add(new GTBedrockOreDisplayCategory());
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                    registry.add(new GTRecipeTypeDisplayCategory(gtRecipeType));
                }
            }
        }
        // workstations
        GTRecipeTypeDisplayCategory.registerWorkStations(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingDisplayCategory.registerWorkstations(registry);
        GTOreVeinDisplayCategory.registerWorkstations(registry);
        GTBedrockFluidDisplayCategory.registerWorkstations(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreDisplayCategory.registerWorkstations(registry);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            if (definition != null) {
                registry.addWorkstations(SMELTING, EntryStacks.of(definition.asStack()));
            }
        }
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.STEAM_FURNACE.left().asStack()));
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.STEAM_FURNACE.right().asStack()));
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.STEAM_OVEN.asStack()));
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.MULTI_SMELTER.asStack()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        GTRecipeTypeDisplayCategory.registerDisplays(registry);
        MultiblockInfoDisplayCategory.registerDisplays(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingDisplayCategory.registerDisplays(registry);
        GTOreVeinDisplayCategory.registerDisplays(registry);
        GTBedrockFluidDisplayCategory.registerDisplays(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreDisplayCategory.registerDisplays(registry);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            registry.group(GTCEu.id("tool/" + toolType.name),
                    Component.translatable("gtceu.tool.class." + toolType.name),
                    EntryIngredients.ofItemTag(toolType.itemTags.get(0)));
            // EntryIngredients.ofItemStacks(GTItems.TOOL_ITEMS.column(toolType).values().stream().filter(Objects::nonNull).map(ItemProviderEntry::get).map(IGTTool::get).collect(Collectors.toSet()))
        }

        for (var cell : GTBlocks.MATERIAL_BLOCKS.columnMap().entrySet()) {
            var value = cell.getValue();
            if (value.size() <= 1) continue;

            var material = cell.getKey();
            List<ItemLike> items = new ArrayList<>();
            for (var t : value.entrySet()) {
                var name = t.getKey().name;
                if (Objects.equals(name, TagPrefix.frameGt.name) ||
                        Objects.equals(name, TagPrefix.block.name) ||
                        Objects.equals(name, TagPrefix.rawOreBlock.name))
                    continue;

                items.add(t.getValue());
            }

            var name = material.getName();
            var label = toUpperAllWords(name.replace("_", " "));
            registry.group(GTCEu.id("ore/" + name), Component.translatable("tagprefix.stone", label),
                    EntryIngredients.ofItems(items));
        }
    }

    private static String toUpperAllWords(String text) {
        StringBuilder result = new StringBuilder();
        result.append(text.substring(0, 1).toUpperCase());
        for (int i = 1; i < text.length(); i++) {
            if (" ".equals(text.substring(i - 1, i)))
                result.append(text.substring(i, i + 1).toUpperCase());
            else
                result.append(text.charAt(i));
        }
        return result.toString();
    }
}
