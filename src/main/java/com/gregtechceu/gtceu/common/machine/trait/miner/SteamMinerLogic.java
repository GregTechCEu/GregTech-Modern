package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine;

public class SteamMinerLogic extends MinerLogic {

    private final SteamMinerMachine steamMiner;

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
        steamMiner = (SteamMinerMachine) metaTileEntity;
    }

    @Override
    protected boolean checkCanMine() {
        return super.checkCanMine() && steamMiner.getExhaustVentTrait().checkVenting();
    }

    @Override
    protected void onMineOperation() {
        super.onMineOperation();
        steamMiner.getExhaustVentTrait().setNeedsVenting(true);
    }
}
