package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IHPCAComputationProvider;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAComputationPartMachine extends HPCAComponentPartMachine implements IHPCAComputationProvider {

    @Getter
    private final boolean advanced;

    public HPCAComputationPartMachine(IMachineBlockEntity holder, boolean advanced) {
        super(holder);
        this.advanced = advanced;
    }

    @Override
    public ResourceTexture getComponentIcon() {
        if (isDamaged()) {
            return advanced ? GuiTextures.HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT :
                    GuiTextures.HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT;
        }
        return advanced ? GuiTextures.HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT :
                GuiTextures.HPCA_ICON_COMPUTATION_COMPONENT;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[advanced ? GTValues.IV : GTValues.EV];
    }

    @Override
    public int getMaxEUt() {
        return GTValues.VA[advanced ? GTValues.ZPM : GTValues.LuV];
    }

    @Override
    public int getCWUPerTick() {
        if (isDamaged()) return 0;
        return advanced ? 16 : 4;
    }

    @Override
    public int getCoolingPerTick() {
        return advanced ? 4 : 2;
    }

    @Override
    public boolean canBeDamaged() {
        return true;
    }
}
