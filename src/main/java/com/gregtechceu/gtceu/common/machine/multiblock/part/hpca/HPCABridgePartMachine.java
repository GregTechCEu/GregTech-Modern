package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCAComponentTrait;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCABridgePartMachine extends HPCAComponentPartMachine {

    public HPCABridgePartMachine(BlockEntityCreationInfo info) {
        super(info, (m) -> new HPCAComponentTrait(m, GTValues.VA[GTValues.IV], GTValues.VA[GTValues.IV], false, true));
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return GuiTextures.HPCA_ICON_BRIDGE_COMPONENT;
    }
}
