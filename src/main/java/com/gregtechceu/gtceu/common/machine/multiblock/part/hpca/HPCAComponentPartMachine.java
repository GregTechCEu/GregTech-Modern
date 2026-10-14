package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComputationProviderTrait;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCACoolantProviderTrait;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IDrawable;
import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAComponentPartMachine extends MultiblockPartMachine {

    @Getter
    protected final HPCAComponentTrait hpcaComponentTrait;
    @Getter
    protected final boolean isAdvanced;
    protected final IDrawable componentIcon;
    protected final IDrawable damagedComponentIcon;

    public HPCAComponentPartMachine(BlockEntityCreationInfo info, boolean isAdvanced, IDrawable hpcaComponentIcon,
                                    IDrawable hpcaComponentIconDamaged,
                                    HPCAComponentTrait hpcaTrait) {
        super(info);
        this.isAdvanced = isAdvanced;
        this.componentIcon = hpcaComponentIcon;
        this.damagedComponentIcon = hpcaComponentIconDamaged;
        this.hpcaComponentTrait = attachTrait(hpcaTrait);
    }

    public static HPCAComputationProviderTrait createHPCAComputationTrait(boolean isAdvanced) {
        int upkeepEUt = GTValues.VA[isAdvanced ? GTValues.IV : GTValues.EV];
        int maxEUt = GTValues.VA[isAdvanced ? GTValues.ZPM : GTValues.LuV];
        int cooling = isAdvanced ? 4 : 2;
        int cwu = isAdvanced ? 16 : 4;
        return new HPCAComputationProviderTrait(upkeepEUt, maxEUt, true, false, cwu, cooling);
    }

    public static HPCAComponentTrait createHPCACoolerTrait(boolean isAdvanced) {
        int upkeepEU = isAdvanced ? GTValues.VA[GTValues.IV] : 0;
        int coolingAmount = isAdvanced ? 2 : 1;
        int maxCoolant = isAdvanced ? 8 : 0;
        return new HPCACoolantProviderTrait(upkeepEU, upkeepEU, false, false, coolingAmount, maxCoolant,
                isAdvanced);
    }

    public IDrawable getComponentIcon() {
        return hpcaComponentTrait.isDamaged() ? damagedComponentIcon : componentIcon;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return false;
    }

    @Override
    public void modifyDrops(List<ItemStack> drops) {
        for (int i = 0; i < drops.size(); ++i) {
            ItemStack drop = drops.get(i);
            if (drop.getItem() == this.getDefinition().getItem()) {
                if (hpcaComponentTrait.isDamaged()) {
                    if (isAdvanced()) {
                        drops.set(i, GTBlocks.ADVANCED_COMPUTER_CASING.asStack());
                    } else {
                        drops.set(i, GTBlocks.COMPUTER_CASING.asStack());
                    }
                }
                break;
            }
        }
    }
}
