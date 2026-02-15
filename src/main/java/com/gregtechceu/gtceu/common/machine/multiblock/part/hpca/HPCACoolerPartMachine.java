package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCACoolantProviderTrait;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCACoolerPartMachine extends HPCAComponentPartMachine {

    @Getter
    private final boolean advanced;

    public HPCACoolerPartMachine(BlockEntityCreationInfo info, boolean advanced) {
        super(info, (machine) -> createHPCATrait(machine, advanced));
        this.advanced = advanced;
    }

    public static HPCAComponentTrait createHPCATrait(HPCAComponentPartMachine machine, boolean isAdvanced) {
        int upkeepEU = isAdvanced ? GTValues.VA[GTValues.IV] : 0;
        int coolingAmount = isAdvanced ? 2 : 1;
        int maxCoolant = isAdvanced ? 8 : 0;
        return new HPCACoolantProviderTrait(machine, upkeepEU, upkeepEU, false, false, coolingAmount, maxCoolant,
                isAdvanced);
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return advanced ? GuiTextures.HPCA_ICON_ACTIVE_COOLER_COMPONENT : GuiTextures.HPCA_ICON_HEAT_SINK_COMPONENT;
    }
}
