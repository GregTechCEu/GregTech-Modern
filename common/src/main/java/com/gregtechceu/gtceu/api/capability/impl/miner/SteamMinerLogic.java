package com.gregtechceu.gtceu.api.capability.impl.miner;

import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;

public class SteamMinerLogic extends MinerLogic {

    /**
     * Creates the logic for steam miners
     *
     * @param metaTileEntity the {@link IRecipeLogicMachine} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public SteamMinerLogic(IRecipeLogicMachine metaTileEntity, int fortune, int speed, int maximumRadius, IModelRenderer pipeTexture) {
        super(metaTileEntity, fortune, speed, maximumRadius, pipeTexture);
    }

    @Override
    protected boolean checkCanMine() {
        IExhaustVentMachine machine = (IExhaustVentMachine) this.machine;
        if (machine.checkVenting()) {
            if (machine.isVentingBlocked())
                return false;
        }

        return super.checkCanMine();
    }

    @Override
    protected void onMineOperation() {
        super.onMineOperation();
        ((IExhaustVentMachine) machine).setNeedsVenting(true);
    }
}
