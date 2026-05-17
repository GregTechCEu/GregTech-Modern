package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine;

public class SteamMinerLogic extends MinerLogic {

    /**
     * Creates the logic for steam miners
     *
     * @param fortune       the fortune amount to apply when mining ores
     * @param speed         the speed in ticks per block mined
     * @param maximumRadius the maximum radius (square shaped) the miner can mine in
     */
    public SteamMinerLogic(int fortune, int speed, int maximumRadius) {
        super(fortune, speed, maximumRadius);
    }

    @Override
    public SteamMinerMachine getMachine() {
        return (SteamMinerMachine) super.getMachine();
    }

    @Override
    protected boolean checkCanMine() {
        return super.checkCanMine() && getMachine().getExhaustVentTrait().checkVenting();
    }

    @Override
    protected void onMineOperation() {
        super.onMineOperation();
        getMachine().getExhaustVentTrait().setNeedsVenting(true);
    }
}
