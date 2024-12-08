package com.gregtechceu.gtceu.integration.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.rei.circuit.GTProgrammedCircuitCategory;
import com.gregtechceu.gtceu.integration.rei.multipage.MultiblockInfoDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.oreprocessing.GTOreProcessingDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.orevein.GTBedrockFluidDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.orevein.GTBedrockOreDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.orevein.GTOreVeinDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.recipe.GTRecipeREICategory;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote REIPlugin
 */
@REIPluginClient
public class GTREIPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        // Categories
        registry.add(new MultiblockInfoDisplayCategory());
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.add(new GTOreProcessingDisplayCategory());
        registry.add(new GTOreVeinDisplayCategory());
        registry.add(new GTBedrockFluidDisplayCategory());
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.add(new GTBedrockOreDisplayCategory());
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (Platform.isDevEnv() || category.isXEIVisible()) {
                registry.add(new GTRecipeREICategory(category));
            }
        }
        registry.add(new GTProgrammedCircuitCategory());

        // Workstations
        GTRecipeREICategory.registerWorkStations(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingDisplayCategory.registerWorkstations(registry);
        GTOreVeinDisplayCategory.registerWorkstations(registry);
        GTBedrockFluidDisplayCategory.registerWorkstations(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreDisplayCategory.registerWorkstations(registry);
        registry.addWorkstations(GTRecipeREICategory.CATEGORIES.apply(GTRecipeTypes.CHEMICAL_RECIPES.getCategory()),
                EntryStacks.of(GTMachines.LARGE_CHEMICAL_REACTOR.asStack()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        GTRecipeREICategory.registerDisplays(registry);
        MultiblockInfoDisplayCategory.registerDisplays(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingDisplayCategory.registerDisplays(registry);
        GTOreVeinDisplayCategory.registerDisplays(registry);
        GTBedrockFluidDisplayCategory.registerDisplays(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreDisplayCategory.registerDisplays(registry);
        registry.add(new GTProgrammedCircuitCategory.GTProgrammedCircuitDisplay());
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

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        registry.registerNbt(GTItems.PROGRAMMED_CIRCUIT.asItem());
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
