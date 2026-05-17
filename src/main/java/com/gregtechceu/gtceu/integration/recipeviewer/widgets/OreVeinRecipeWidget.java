package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class OreVeinRecipeWidget extends ParentWidget<OreVeinRecipeWidget> {

    private final String nameLang;
    private final int weight;
    private final @Nullable String range;
    private final @Nullable Set<ResourceKey<Level>> dimensionFilter;
    public final static int WIDTH = 120;

    private OreVeinRecipeWidget(int width, int height, String nameLang, int weight, @Nullable String range,
                                @Nullable Set<ResourceKey<Level>> dimensionFilter) {
        size(width, height);
        this.nameLang = nameLang;
        this.weight = weight;
        this.range = range;
        this.dimensionFilter = dimensionFilter;
    }

    public OreVeinRecipeWidget(BedrockFluidDefinition fluid) {
        this(WIDTH, 140, getFluidName(fluid), fluid.getWeight(), null, fluid.dimensionFilter);
        drawUI(Flow.row().coverChildren().child(RecipeViewerSlotWidget.create()
                .value(new FluidStack(fluid.getStoredFluid().get(), 1000)).recipeSlotRole(RecipeSlotRole.OUTPUT)));
    }

    public OreVeinRecipeWidget(GTOreDefinition oreDefinition) {
        this(WIDTH, 160, getOreName(oreDefinition), oreDefinition.weight(), range(oreDefinition),
                oreDefinition.dimensionFilter());

        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        List<Integer> chances = oreDefinition.veinGenerator().getAllChances();
        containedOresAsItemStacks.addAll(getRawMaterialList(oreDefinition));

        var slots = Flow.row().coverChildren();
        for (int i = 0; i < containedOresAsItemStacks.size(); i++) {
            RecipeViewerSlotWidget<?> oreSlot = RecipeViewerSlotWidget.create().value(containedOresAsItemStacks.get(i))
                    .recipeSlotRole(RecipeSlotRole.OUTPUT);
            int finalI = i;
            oreSlot.tooltipBuilder(r -> r.add(Text.lang("gtceu.jei.ore_vein_diagram.chance", chances.get(finalI))));
            slots.child(oreSlot);
        }
        drawUI(slots);
    }

    public OreVeinRecipeWidget(BedrockOreDefinition bedrockOre) {
        this(WIDTH, 140, getBedrockOreName(bedrockOre), bedrockOre.weight(), null, bedrockOre.dimensionFilter());

        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        IntList chances = bedrockOre.getAllChances();
        containedOresAsItemStacks.addAll(getRawMaterialList(bedrockOre));

        var slots = Flow.row().coverChildren();
        for (int i = 0; i < containedOresAsItemStacks.size(); i++) {
            RecipeViewerSlotWidget<?> oreSlot = RecipeViewerSlotWidget.create().value(containedOresAsItemStacks.get(i))
                    .recipeSlotRole(RecipeSlotRole.OUTPUT);
            int finalI = i;
            oreSlot.tooltipBuilder(r -> r.add(Text.lang("gtceu.jei.ore_vein_diagram.chance", chances.getInt(finalI))));
            slots.child(oreSlot);
        }
        drawUI(slots);
    }

    private void drawUI(Flow contentsRow) {
        var col = Flow.col().sizeRel(1f)
                .child(Text.lang(nameLang).asWidget().marginBottom(3))
                .child(contentsRow.marginBottom(3))
                .childIf(range != null,
                        () -> Text.lang("gtceu.jei.ore_vein_diagram.spawn_range").asWidget().marginBottom(1))
                .childIf(range != null, () -> Text.str(Objects.requireNonNull(range)).asWidget().marginBottom(3))
                .child(Text.lang("gtceu.jei.ore_vein_diagram.weight", weight).asWidget().marginBottom(3))
                .child(Text.lang("gtceu.jei.ore_vein_diagram.dimensions").asWidget());

        if (this.dimensionFilter != null) {

            Flow row = Flow.row().coverChildren().padding(2);

            for (DimensionMarker dimMarker : getDimensionMarkers(dimensionFilter)) {
                RecipeViewerSlotWidget<?> dimSlot = RecipeViewerSlotWidget.create().value(dimMarker.getIcon())
                        .recipeSlotRole(RecipeSlotRole.CATALYST).background(IDrawable.NONE);
                if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
                    dimSlot.overlay(
                            Text.str("T" + (dimMarker.tier >= DimensionMarker.MAX_TIER ? "?" : dimMarker.tier)));
                }
                row.child(dimSlot);
            }
            col.child(row);
        } else {
            col.child(Text.str("Any").asWidget());
        }
        child(col);
    }

    @SuppressWarnings("all")
    private static String range(GTOreDefinition oreDefinition) {
        HeightProvider height = oreDefinition.range().height;
        int minHeight = 0, maxHeight = 0;
        if (height instanceof UniformHeight uniform) {
            minHeight = uniform.minInclusive.resolveY(null);
            maxHeight = uniform.maxInclusive.resolveY(null);
        }
        return String.format("%d - %d", minHeight, maxHeight);
    }

    public static List<ItemStack> getContainedOresAndBlocks(GTOreDefinition oreDefinition) {
        return oreDefinition.veinGenerator().getAllEntries().stream()
                .flatMap(entry -> entry.map(state -> Stream.of(state.getBlock().asItem().getDefaultInstance()),
                        material -> {
                            Set<ItemStack> ores = new HashSet<>();
                            ores.add(ChemicalHelper.get(TagPrefix.rawOre, material));
                            for (TagPrefix prefix : TagPrefix.ORES.keySet()) {
                                ores.add(ChemicalHelper.get(prefix, material));
                            }
                            return ores.stream();
                        }))
                .toList();
    }

    public static List<ItemStack> getRawMaterialList(GTOreDefinition oreDefinition) {
        return oreDefinition.veinGenerator().getAllEntries().stream()
                .map(entry -> entry.map(state -> state.getBlock().asItem().getDefaultInstance(),
                        material -> ChemicalHelper.get(TagPrefix.rawOre, material)))
                .toList();
    }

    public static List<ItemStack> getRawMaterialList(BedrockOreDefinition bedrockOreDefinition) {
        return bedrockOreDefinition.materials().stream()
                .map(entry -> ChemicalHelper.get(TagPrefix.rawOre, entry.material()))
                .toList();
    }

    public static DimensionMarker[] getDimensionMarkers(Set<ResourceKey<Level>> dimensionFilter) {
        return dimensionFilter.stream()
                .map(ResourceKey::location)
                .map(loc -> GTRegistries.DIMENSION_MARKERS.getOrDefault(loc,
                        new DimensionMarker(DimensionMarker.MAX_TIER, () -> Blocks.BARRIER, loc.toString())))
                .sorted(Comparator.comparingInt(DimensionMarker::getTier))
                .toArray(DimensionMarker[]::new);
    }

    public static String getOreName(GTOreDefinition oreDefinition) {
        ResourceLocation id = ClientProxy.CLIENT_ORE_VEINS.inverse().get(oreDefinition);
        return "gtceu.jei.ore_vein." + id.getPath();
    }

    public static String getFluidName(BedrockFluidDefinition fluid) {
        ResourceLocation id = ClientProxy.CLIENT_FLUID_VEINS.inverse().get(fluid);
        return "gtceu.jei.bedrock_fluid." + id.getPath();
    }

    public static String getBedrockOreName(BedrockOreDefinition oreDefinition) {
        ResourceLocation id = ClientProxy.CLIENT_BEDROCK_ORE_VEINS.inverse().get(oreDefinition);
        return "gtceu.jei.bedrock_ore." + id.getPath();
    }
}
