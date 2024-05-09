package com.gregtechceu.gtceu.common.machines.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.guis.GuiTextures;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAEmptyPartMachine extends HPCAComponentPartMachine {

    public HPCAEmptyPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return GuiTextures.HPCA_ICON_EMPTY_COMPONENT;
    }

    @Override
    public int getUpkeepEUt() {
        return 0;
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }
}
