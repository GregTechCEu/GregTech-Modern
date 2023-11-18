package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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

    public GTOreVeinWidget(GTOreDefinition oreDefinition) {
        super(0, 0, 120, 160);
        this.name = getOreName(oreDefinition);
        this.weight = oreDefinition.getWeight();
        this.dimensionFilter = oreDefinition.getDimensionFilter();
        this.dimensions = dimensions();
        this.range = range(oreDefinition);
        setClientSideWidget();
        setupBaseGui(oreDefinition);
        setupText();
    }

    public GTOreVeinWidget(BedrockFluidDefinition fluid) {
        super(0, 0, 120, 140);
        this.name = getFluidName(fluid);
        this.weight = fluid.getWeight();
        this.dimensionFilter = fluid.getDimensionFilter();
        this.dimensions = dimensions();
        this.range = "NULL";
        setClientSideWidget();
        setupBaseGui(fluid);
        setupText();
    }

    private String range(GTOreDefinition oreDefinition) {
        HeightProvider height = oreDefinition.getRange().height;
        int maxHeight = height instanceof UniformHeight ? ((UniformHeight) height).maxInclusive.resolveY(null) : 0;
        int minHeight = height instanceof UniformHeight ? ((UniformHeight) height).minInclusive.resolveY(null) : 0;
        return String.format("%d - %d", minHeight, maxHeight);
    }

    private void setupBaseGui(GTOreDefinition oreDefinition){
        NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
        List<BlockState> containedMaterials = oreDefinition.getVeinGenerator().getAllBlocks();
        for (BlockState material : containedMaterials) {
            containedOresAsItemStacks.add(material.getBlock().asItem().getDefaultInstance());
        }
        int n = containedOresAsItemStacks.size();
        int x = (120 - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            SlotWidget oreSlot = new SlotWidget(new ItemStackTransfer(containedOresAsItemStacks), i, x, 18, false, false);
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

    private void setupText(){
        addWidget(new LabelWidget(5, 0,
                LocalizationUtils.format("gtceu.jei.ore_vein_" + name)));
        addWidget(new LabelWidget(5, 40,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.spawn_range")));
        addWidget(new LabelWidget(5, 50, range));

        addWidget(new LabelWidget(5, 60,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.weight", weight)));
        addWidget(new LabelWidget(5, 70,
                LocalizationUtils.format("gtceu.jei.ore_vein_diagram.dimensions")));
        addWidget(new LabelWidget(5, 80, dimensions));
    }

    private String dimensions() {
        AtomicInteger counter = new AtomicInteger(0);
        if (dimensionFilter == null) return "Overworld";
        return dimensionFilter.stream()
                .map(dimension -> FormattingUtil.toEnglishName(dimension.location().getPath()))
                .map(name -> counter.getAndIncrement() % 2 == 1 ? name + "\n" : name + ", ")
                .collect(Collectors.collectingAndThen(
                        Collectors.joining(""),
                        s -> s.endsWith(", ") ? s.substring(0, s.length() - 2) : s
                ));
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
