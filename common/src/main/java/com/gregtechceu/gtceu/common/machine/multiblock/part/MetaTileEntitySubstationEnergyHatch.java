package com.gregtechceu.gtceu.common.machine.multiblock.part;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Override exists simply so that addons can still have energy hatches of
// much higher amperages, without hard-coding values in the super class.
public class MetaTileEntitySubstationEnergyHatch extends MetaTileEntityEnergyHatch {

    public MetaTileEntitySubstationEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage, boolean isExportHatch) {
        super(metaTileEntityId, tier, amperage, isExportHatch);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySubstationEnergyHatch(metaTileEntityId, getTier(), amperage, isExportHatch);
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return isExportHatch ? MultiblockAbility.SUBSTATION_OUTPUT_ENERGY : MultiblockAbility.SUBSTATION_INPUT_ENERGY;
    }

    @Override
    protected void addDescriptorTooltip(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        if (isExportHatch) {
            tooltip.add(I18n.format("gregtech.machine.substation_hatch.output.tooltip"));
        } else {
            tooltip.add(I18n.format("gregtech.machine.substation_hatch.input.tooltip"));
        }
    }
}
