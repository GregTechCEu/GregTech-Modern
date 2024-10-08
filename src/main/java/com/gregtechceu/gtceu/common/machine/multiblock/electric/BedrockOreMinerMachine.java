package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.BedrockOreMinerLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Screret
 * @date 2023/7/12
 * @implNote BedrockOreMinerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BedrockOreMinerMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    @Getter
    private final int tier;
    @Getter
    private final int depletionChance;
    @Getter
    private final int rigMultiplier;

    public BedrockOreMinerMachine(IMachineBlockEntity holder, int tier) {
        this(holder, tier, switch (tier) {
            case GTValues.MV -> 1;
            case GTValues.HV -> 2;
            case GTValues.EV -> 8;
            default -> 1;
        },
                switch (tier) {
                    case GTValues.MV -> 1;
                    case GTValues.HV -> 4;
                    case GTValues.EV -> 16;
                    default -> 1;
                });
    }

    public BedrockOreMinerMachine(IMachineBlockEntity holder, int tier, int depletionChance, int rigMultiplier) {
        super(holder);
        this.tier = tier;
        this.depletionChance = depletionChance;
        this.rigMultiplier = rigMultiplier;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new BedrockOreMinerLogic(this);
    }

    @Override
    public BedrockOreMinerLogic getRecipeLogic() {
        return (BedrockOreMinerLogic) super.getRecipeLogic();
    }

    public int getEnergyTier() {
        var energyContainer = this.getCapabilitiesProxy().get(IO.IN, EURecipeCapability.CAP);
        if (energyContainer == null) return this.tier;
        var energyCont = new EnergyContainerList(energyContainer.stream().filter(IEnergyContainer.class::isInstance)
                .map(IEnergyContainer.class::cast).toList());
        return Math.min(this.tier + 1, Math.max(this.tier, GTUtil.getFloorTierByVoltage(energyCont.getInputVoltage())));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            int energyContainer = getEnergyTier();
            long maxVoltage = GTValues.V[energyContainer];
            String voltageName = GTValues.VNF[energyContainer];
            textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));

            if (getRecipeLogic().getVeinMaterials() != null) {
                // Ore names
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ores_list")
                        .withStyle(ChatFormatting.GREEN));
                List<Map.Entry<Integer, Material>> drilledOres = getRecipeLogic().getVeinMaterials();
                for (var entry : drilledOres) {
                    Component fluidInfo = entry.getValue().getLocalizedName().withStyle(ChatFormatting.GREEN);
                    textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ore_entry", fluidInfo)
                            .withStyle(ChatFormatting.GRAY));
                }

                // Ore amount
                Component amountInfo = Component.literal(FormattingUtil.formatNumbers(
                        getRecipeLogic().getOreToProduce() * 20L / BedrockOreMinerLogic.MAX_PROGRESS) +
                        "/s").withStyle(ChatFormatting.BLUE);
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.ore_amount", amountInfo)
                        .withStyle(ChatFormatting.GRAY));
            } else {
                Component noOre = Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                        .withStyle(ChatFormatting.RED);
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ores_list")
                        .withStyle(ChatFormatting.GREEN));
                textList.add(Component.translatable("gtceu.multiblock.ore_rig.drilled_ore_entry", noOre)
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                    .withStyle(ChatFormatting.GRAY);
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        }
    }

    public static Material getMaterial(int tier) {
        if (tier == GTValues.MV) return GTMaterials.Steel;
        if (tier == GTValues.HV) return GTMaterials.Titanium;
        if (tier == GTValues.EV) return GTMaterials.TungstenSteel;
        return GTMaterials.Steel;
    }

    public static Block getCasingState(int tier) {
        return GTBlocks.MATERIALS_TO_CASINGS.get(getMaterial(tier)).get();
    }

    public static int getDepletionChanceByTier(int tier) {
        if (tier == GTValues.MV)
            return 1;
        if (tier == GTValues.HV)
            return 2;
        if (tier == GTValues.EV)
            return 8;
        return 1;
    }

    public static int getRigMultiplierByTier(int tier) {
        if (tier == GTValues.MV)
            return 1;
        if (tier == GTValues.HV)
            return 4;
        if (tier == GTValues.EV)
            return 16;
        return 1;
    }

    public static ResourceLocation getBaseTexture(int tier) {
        if (tier == GTValues.MV)
            return GTCEu.id("block/casings/solid/machine_casing_solid_steel");
        if (tier == GTValues.HV)
            return GTCEu.id("block/casings/solid/machine_casing_stable_titanium");
        if (tier == GTValues.EV)
            return GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel");
        return GTCEu.id("block/casings/solid/machine_casing_solid_steel");
    }
}
