package com.gregtechceu.gtceu.integration.xei.widgets;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.api.worldgen.DimensionMarker;
import com.gregtechceu.gtceu.api.worldgen.OreVeinDefinition;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;

import java.util.*;
import java.util.stream.Stream;

@Getter
public class GTOreVeinWidget extends WidgetGroup {

    private final String translationKey;
    private final int weight;
    private final String range;
    private final Set<ResourceKey<Level>> dimensionFilter;
    public final static int width = 120;

    public GTOreVeinWidget(Holder<OreVeinDefinition> ore) {
        super(0, 0, width, 160);
        this.translationKey = getOreName(ore);
        this.weight = ore.value().weight();
        this.dimensionFilter = ore.value().dimensionFilter();
        this.range = range(ore.value());
        setClientSideWidget();
        setupBaseGui(ore.value());
        setupText(ore.value());
    }

    public GTOreVeinWidget(Holder<BedrockFluidDefinition> fluid, Object marker) {
        super(0, 0, width, 140);
        this.translationKey = getFluidName(fluid);
        this.weight = fluid.value().getWeight();
        this.dimensionFilter = fluid.value().getDimensionFilter();
        this.range = "NULL";
        setClientSideWidget();
        setupBaseGui(fluid.value());
        setupText(fluid.value());
    }

    public GTOreVeinWidget(Holder<BedrockOreDefinition> bedrockOre, Void marker) {
        super(0, 0, width, 140);
        this.translationKey = getBedrockOreName(bedrockOre);
        this.weight = bedrockOre.value().weight();
        this.dimensionFilter = bedrockOre.value().dimensionFilter();
        this.range = "NULL";
        setClientSideWidget();
        setupBaseGui(bedrockOre.value());
        setupText(bedrockOre.value());
    }

    @SuppressWarnings("all")
    private String range(OreVeinDefinition oreDefinition) {
        HeightProvider height = oreDefinition.heightRange().height;
        int minHeight = 0, maxHeight = 0;
        if (height instanceof UniformHeight uniform) {
            minHeight = uniform.minInclusive.resolveY(null);
            maxHeight = uniform.maxInclusive.resolveY(null);
        }
        return String.format("%d - %d", minHeight, maxHeight);
    }

    private void setupBaseGui(OreVeinDefinition oreDefinition) {
        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        List<Integer> chances = oreDefinition.veinGenerator().getAllChances();
        containedOresAsItemStacks.addAll(getRawMaterialList(oreDefinition));
        int n = containedOresAsItemStacks.size();
        int x = (width - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            SlotWidget oreSlot = new SlotWidget(new CustomItemStackHandler(containedOresAsItemStacks), i, x, 18, false,
                    false);
            int finalIndex = i;
            oreSlot.setOnAddedTooltips((stack, tooltips) -> tooltips.add(Component
                    .nullToEmpty(
                            LocalizationUtils.format("gtceu.jei.ore_vein_diagram.chance", chances.get(finalIndex)))));
            oreSlot.setIngredientIO(IngredientIO.OUTPUT);
            addWidget(oreSlot);
            x += 18;
        }
    }

    private void setupBaseGui(BedrockFluidDefinition fluid) {
        Fluid storedFluid = fluid.getStoredFluid();
        TankWidget fluidSlot = new TankWidget(
                new CustomFluidTank(new FluidStack(storedFluid, 1000)), 51, 18, false, false);
        fluidSlot.setIngredientIO(IngredientIO.OUTPUT);
        addWidget(fluidSlot);
    }

    private void setupBaseGui(BedrockOreDefinition bedrockOreDefinition) {
        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        List<Integer> chances = bedrockOreDefinition.getAllChances();
        containedOresAsItemStacks.addAll(getRawMaterialList(bedrockOreDefinition));
        int n = containedOresAsItemStacks.size();
        int x = (width - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            SlotWidget oreSlot = new SlotWidget(new CustomItemStackHandler(containedOresAsItemStacks), i, x, 18, false,
                    false);
            int finalIndex = i;
            oreSlot.setOnAddedTooltips((stack, tooltips) -> tooltips.add(Component
                    .nullToEmpty(
                            LocalizationUtils.format("gtceu.jei.ore_vein_diagram.chance", chances.get(finalIndex)))));
            oreSlot.setIngredientIO(IngredientIO.OUTPUT);
            addWidget(oreSlot);
            x += 18;
        }
    }

    private void setupText(OreVeinDefinition ignored) {
        addWidget(new ImageWidget(5, 0, width - 10, 16,
                new TextTexture(translationKey).setType(TextTexture.TextType.LEFT_ROLL)
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
                new TextTexture(translationKey).setType(TextTexture.TextType.LEFT_ROLL)
                        .setWidth(width - 10)));
        addWidget(new LabelWidget(5, 40,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight)));
        addWidget(new LabelWidget(5, 50,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions")));
        setupDimensionMarker(60);
    }

    private void setupText(BedrockOreDefinition ignored) {
        addWidget(new ImageWidget(5, 0, width - 10, 16,
                new TextTexture(translationKey).setType(TextTexture.TextType.LEFT_ROLL)
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
                    .map(loc -> GTRegistries.DIMENSION_MARKERS.getOptional(loc)
                            .orElse(new DimensionMarker(DimensionMarker.MAX_TIER, () -> Blocks.BARRIER,
                                    loc.toString())))
                    .sorted(Comparator.comparingInt(DimensionMarker::getTier))
                    .toArray(DimensionMarker[]::new);
            var handler = new CustomItemStackHandler(dimMarkers.length);
            for (int i = 0; i < dimMarkers.length; i++) {
                var dimMarker = dimMarkers[i];
                var icon = dimMarker.getIcon();
                int row = Math.floorDiv(i, rowSlots);
                SlotWidget dimSlot = new SlotWidget(handler, i,
                        5 + (16 + interval) * (i - row * rowSlots),
                        yPosition + 18 * row,
                        false, false).setIngredientIO(IngredientIO.CATALYST);
                handler.setStackInSlot(i, icon);
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

    public static List<ItemStack> getContainedOresAndBlocks(OreVeinDefinition oreDefinition) {
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

    public static List<ItemStack> getRawMaterialList(OreVeinDefinition oreDefinition) {
        return oreDefinition.veinGenerator().getAllEntries().stream()
                .map(entry -> entry.getKey().map(state -> state.getBlock().asItem().getDefaultInstance(),
                        material -> ChemicalHelper.get(TagPrefix.rawOre, material)))
                .toList();
    }

    public static List<ItemStack> getRawMaterialList(BedrockOreDefinition bedrockOreDefinition) {
        return bedrockOreDefinition.materials().stream()
                .map(entry -> ChemicalHelper.get(TagPrefix.rawOre, entry.getFirst()))
                .toList();
    }

    public static String getOreName(Holder<OreVeinDefinition> ore) {
        return ore.getKey().location().toLanguageKey("ore_vein");
    }

    public static String getFluidName(Holder<BedrockFluidDefinition> fluid) {
        return fluid.getKey().location().toLanguageKey("bedrock_fluid");
    }

    public static String getBedrockOreName(Holder<BedrockOreDefinition> bedrockOre) {
        return bedrockOre.getKey().location().toLanguageKey("bedrock_ore");
    }
}
