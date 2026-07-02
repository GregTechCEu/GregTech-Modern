package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.RecipeElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.FluidDrillLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidDrillMachine extends RecipeElectricMultiblockMachine {

    @Getter
    private final int tier;

    public FluidDrillMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new FluidDrillLogic(this);
    }

    @Override
    public FluidDrillLogic getRecipeLogic() {
        return (FluidDrillLogic) super.getRecipeLogic();
    }

    public boolean isOverclocked() {
        return energyContainer.getEffectiveVoltage() >= 4 * GTValues.V[tier];
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            int overClockTier = isOverclocked() ? tier+1 : tier;
            long maxVoltage = GTValues.V[overClockTier];
            String voltageName = GTValues.VNF[overClockTier];
            textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));

            if (getRecipeLogic().getVeinFluid() != null) {
                // Fluid name
                Fluid drilledFluid = getRecipeLogic().getVeinFluid();
                Component fluidInfo = drilledFluid.getFluidType().getDescription().copy()
                        .withStyle(ChatFormatting.GREEN);
                textList.add(Component.translatable("gtceu.multiblock.fluid_rig.drilled_fluid", fluidInfo)
                        .withStyle(ChatFormatting.GRAY));

                // Fluid amount
                Component amountInfo = Component.literal(FormattingUtil.formatNumbers(
                        getRecipeLogic().getFluidToProduce() * 20L / FluidDrillLogic.MAX_PROGRESS) +
                        " mB/s").withStyle(ChatFormatting.BLUE);
                textList.add(Component.translatable("gtceu.multiblock.fluid_rig.fluid_amount", amountInfo)
                        .withStyle(ChatFormatting.GRAY));
            } else {
                Component noFluid = Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                        .withStyle(ChatFormatting.RED);
                textList.add(Component.translatable("gtceu.multiblock.fluid_rig.drilled_fluid", noFluid)
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

    public static int getDepletionChance(int tier) {
        if (tier == GTValues.MV)
            return 1;
        if (tier == GTValues.HV)
            return 2;
        if (tier == GTValues.EV)
            return 8;
        return 1;
    }

    public static int getRigMultiplier(int tier) {
        if (tier == GTValues.MV)
            return 1;
        if (tier == GTValues.HV)
            return 16;
        if (tier == GTValues.EV)
            return 64;
        return 1;
    }

    public static Block getCasingState(int tier) {
        if (tier == GTValues.MV)
            return GTBlocks.CASING_STEEL_SOLID.get();
        if (tier == GTValues.HV)
            return GTBlocks.CASING_TITANIUM_STABLE.get();
        if (tier == GTValues.EV)
            return GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
        return GTBlocks.CASING_STEEL_SOLID.get();
    }

    @SuppressWarnings("DataFlowIssue")
    public static Block getFrameState(int tier) {
        if (tier == GTValues.MV)
            return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Steel).get();
        if (tier == GTValues.HV)
            return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Titanium).get();
        if (tier == GTValues.EV)
            return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.TungstenSteel).get();
        return GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Steel).get();
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
