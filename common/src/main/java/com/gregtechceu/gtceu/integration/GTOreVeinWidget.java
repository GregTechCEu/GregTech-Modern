package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Arbor
 * @implNote GTOreVeinWidget
 */
@Getter
public class GTOreVeinWidget extends WidgetGroup {
    private final String name;
    private final int weight;
    private final String range;
    private final String dimensions;
    private final Set<ResourceKey<Level>> dimensionFilter;
    public final static int width = 120;

    public GTOreVeinWidget(GTOreDefinition oreDefinition) {
        super(0, 0, width, 160);
        this.name = getOreName(oreDefinition);
        this.weight = oreDefinition.weight();
        this.dimensionFilter = oreDefinition.dimensionFilter();
        this.dimensions = dimensions();
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
        this.dimensions = dimensions();
        this.range = "NULL";
        setClientSideWidget();
        setupBaseGui(fluid);
        setupText(fluid);
    }

    private String range(GTOreDefinition oreDefinition) {
        HeightProvider height = oreDefinition.range().height;
        int minHeight = 0, maxHeight = 0;
        if (height instanceof UniformHeight uniform) {
            minHeight = uniform.minInclusive.resolveY(null);
            maxHeight = uniform.maxInclusive.resolveY(null);
        }
        return String.format("%d - %d", minHeight, maxHeight);
    }

    private void setupBaseGui(GTOreDefinition oreDefinition){
        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        List<Integer> chances = oreDefinition.veinGenerator().getAllChances();
        containedOresAsItemStacks.addAll(getContainedOresAndBlocks(oreDefinition));
        int n = containedOresAsItemStacks.size();
        int x = (width - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            SlotWidget oreSlot = new SlotWidget(new ItemStackTransfer(containedOresAsItemStacks), i, x, 18, false, false);
            int finalI = i;
            oreSlot.setOnAddedTooltips((stack, tooltips) ->
                    tooltips.add(Component.nullToEmpty(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.chance", chances.get(finalI)))));
            oreSlot.setIngredientIO(IngredientIO.OUTPUT);
            addWidget(oreSlot);
            x += 18;
        }
    }

    private void setupBaseGui(BedrockFluidDefinition fluid){
        Fluid storedFluid = fluid.getStoredFluid().get();
        TankWidget fluidSlot = new TankWidget(
                new FluidStorage(FluidStack.create(storedFluid, 1000)), 51, 18, false, false);
        fluidSlot.setIngredientIO(IngredientIO.OUTPUT);
        addWidget(fluidSlot);
    }

    private void setupText(GTOreDefinition oreDefinition){
        addWidget(new ImageWidget(5, 0, width - 10, 16,
                new TextTexture("gtceu.jei.ore_vein." + name).setType(TextTexture.TextType.LEFT_ROLL).setWidth(width - 10)));
        addWidget(new LabelWidget(5, 40,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.spawn_range")));
        addWidget(new LabelWidget(5, 50, range));

        addWidget(new LabelWidget(5, 60,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight)));
        addWidget(new LabelWidget(5, 70,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions")));
        addWidget(new LabelWidget(5, 80, dimensions));
    }

    private void setupText(BedrockFluidDefinition fluid){
        addWidget(new ImageWidget(5, 0, width - 10, 16,
                new TextTexture("gtceu.jei.bedrock_fluid." + name).setType(TextTexture.TextType.LEFT_ROLL).setWidth(width - 10)));
        addWidget(new LabelWidget(5, 40,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight)));
        addWidget(new LabelWidget(5, 50,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions")));
        addWidget(new LabelWidget(5, 60, dimensions));
    }

    private String dimensions() {
        if (dimensionFilter == null) return "Any";
        return dimensionFilter.stream()
                .map(dimension -> dimension.location().toString())
                .collect(Collectors.joining("\n"));
    }

    public static List<ItemStack> getContainedOresAndBlocks(GTOreDefinition oreDefinition) {
        return oreDefinition.veinGenerator().getAllEntries().stream()
                .map(entry -> entry.getKey().map(state -> state.getBlock().asItem().getDefaultInstance(), material -> ChemicalHelper.get(TagPrefix.rawOre, material)))
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
