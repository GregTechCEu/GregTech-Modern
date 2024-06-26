package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.material.Fluid;

import lombok.Getter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Arbor
 * @implNote GTOreVeinWidget
 */
@Getter
public class GTOreVeinWidget extends WidgetGroup {

    private final String name;
    private final int weight;
    private final String range;
    private final Set<ResourceKey<Level>> dimensionFilter;
    public final static int width = 120;

    public GTOreVeinWidget(GTOreDefinition oreDefinition) {
        super(0, 0, width, 160);
        this.name = getOreName(oreDefinition);
        this.weight = oreDefinition.weight();
        this.dimensionFilter = oreDefinition.dimensionFilter();
        this.range = range(oreDefinition);
        setClientSideWidget();
        setupBaseGui(oreDefinition);
        setupText(oreDefinition);
    }

    public GTOreVeinWidget(BedrockFluidDefinition fluid) {
        super(0, 0, width, 140);
        this.name = getFluidName(fluid);
        this.weight = fluid.getWeight();
        this.dimensionFilter = fluid.getDimensionFilter();
        this.range = "NULL";
        setClientSideWidget();
        setupBaseGui(fluid);
        setupText(fluid);
    }

    @SuppressWarnings("all")
    private String range(GTOreDefinition oreDefinition) {
        HeightProvider height = oreDefinition.range().height;
        int minHeight = 0, maxHeight = 0;
        if (height instanceof UniformHeight uniform) {
            minHeight = uniform.minInclusive.resolveY(null);
            maxHeight = uniform.maxInclusive.resolveY(null);
        }
        return String.format("%d - %d", minHeight, maxHeight);
    }

    private void setupBaseGui(GTOreDefinition oreDefinition) {
        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        List<Integer> chances = oreDefinition.veinGenerator().getAllChances();
        containedOresAsItemStacks.addAll(getRawMaterialList(oreDefinition));
        int n = containedOresAsItemStacks.size();
        int x = (width - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            SlotWidget oreSlot = new SlotWidget(new ItemStackTransfer(containedOresAsItemStacks), i, x, 18, false,
                    false);
            int finalI = i;
            oreSlot.setOnAddedTooltips((stack, tooltips) -> tooltips.add(Component
                    .nullToEmpty(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.chance", chances.get(finalI)))));
            oreSlot.setIngredientIO(IngredientIO.OUTPUT);
            addWidget(oreSlot);
            x += 18;
        }
    }

    private void setupBaseGui(BedrockFluidDefinition fluid) {
        Fluid storedFluid = fluid.getStoredFluid().get();
        TankWidget fluidSlot = new TankWidget(
                new FluidStorage(FluidStack.create(storedFluid, 1000)), 51, 18, false, false);
        fluidSlot.setIngredientIO(IngredientIO.OUTPUT);
        addWidget(fluidSlot);
    }

    private void setupText(GTOreDefinition ignored) {
        addWidget(new ImageWidget(5, 0, width - 10, 16,
                new TextTexture("gtceu.jei.ore_vein." + name).setType(TextTexture.TextType.LEFT_ROLL)
                        .setWidth(width - 10)));
        addWidget(new LabelWidget(5, 40,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.spawn_range")));
        addWidget(new LabelWidget(5, 50, range));

        addWidget(new LabelWidget(5, 60,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight)));
        addWidget(new LabelWidget(5, 70,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions")));
        setupDimensionMarker(80);
    }

    private void setupText(BedrockFluidDefinition ignored) {
        addWidget(new ImageWidget(5, 0, width - 10, 16,
                new TextTexture("gtceu.jei.bedrock_fluid." + name).setType(TextTexture.TextType.LEFT_ROLL)
                        .setWidth(width - 10)));
        addWidget(new LabelWidget(5, 40,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight)));
        addWidget(new LabelWidget(5, 50,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions")));
        setupDimensionMarker(60);
    }

    private void setupDimensionMarker(int yPosition) {
        if (this.dimensionFilter != null) {
            int interval = 2;
            int rowSlots = (width - 10 + interval) / (16 + interval);

            DimensionMarker[] dimMarkers = dimensionFilter.stream()
                    .map(ResourceKey::location)
                    .map(loc -> GTRegistries.DIMENSION_MARKERS.getOrDefault(loc,
                            new DimensionMarker(DimensionMarker.MAX_TIER, () -> Blocks.BARRIER, loc.toString())))
                    .sorted(Comparator.comparingInt(DimensionMarker::getTier))
                    .toArray(DimensionMarker[]::new);
            var transfer = new ItemStackTransfer(dimMarkers.length);
            for (int i = 0; i < dimMarkers.length; i++) {
                var dimMarker = dimMarkers[i];
                var markerItem = dimMarker.getMarker();
                int row = Math.floorDiv(i, rowSlots);
                SlotWidget dimSlot = new SlotWidget(transfer, i,
                        5 + (16 + interval) * (i - row * rowSlots),
                        yPosition + 18 * row,
                        false, false).setIngredientIO(IngredientIO.INPUT);
                transfer.setStackInSlot(i, markerItem);
                if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
                    dimSlot.setOverlay(
                            new TextTexture("T" + (dimMarker.tier >= DimensionMarker.MAX_TIER ? "?" : dimMarker.tier))
                                    .scale(0.75F)
                                    .transform(-3F, 5F));
                }
                addWidget(dimSlot.setBackgroundTexture(IGuiTexture.EMPTY));
            }
        } else {
            addWidget(new LabelWidget(5, yPosition, "Any"));
        }
    }

    public static List<ItemStack> getContainedOresAndBlocks(GTOreDefinition oreDefinition) {
        return oreDefinition.veinGenerator().getAllEntries().stream()
                .flatMap(entry -> entry.getKey().map(state -> Stream.of(state.getBlock().asItem().getDefaultInstance()),
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
                .map(entry -> entry.getKey().map(state -> state.getBlock().asItem().getDefaultInstance(),
                        material -> ChemicalHelper.get(TagPrefix.rawOre, material)))
                .toList();
    }

    public String getOreName(GTOreDefinition oreDefinition) {
        ResourceLocation id = GTRegistries.ORE_VEINS.getKey(oreDefinition);
        return id.getPath();
    }

    public String getFluidName(BedrockFluidDefinition fluid) {
        ResourceLocation id = GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(fluid);
        return id.getPath();
    }
}
