package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

public class SteamMinerLogic extends MinerLogic {

    /**
     * Creates the logic for steam miners
     *
     * @param metaTileEntity the {@link IRecipeLogicMachine} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public SteamMinerLogic(IRecipeLogicMachine metaTileEntity, int fortune, int speed, int maximumRadius) {
        super(metaTileEntity, fortune, speed, maximumRadius);
    }

    @Override
    protected boolean checkCanMine() {
        IExhaustVentMachine machine = (IExhaustVentMachine) this.machine;
        return super.checkCanMine() && machine.checkVenting();
    }

    @Override
    protected void onMineOperation() {
        super.onMineOperation();
        ((IExhaustVentMachine) machine).setNeedsVenting(true);
    }
}
