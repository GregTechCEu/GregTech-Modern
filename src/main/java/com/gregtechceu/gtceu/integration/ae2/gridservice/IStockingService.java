package com.gregtechceu.gtceu.integration.ae2.gridservice;

import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;

import appeng.api.networking.IGridService;

public interface IStockingService extends IGridService {

    void markForRefresh(IMEStockingPart part);

    void markForAutoPull(IMEStockingPart part);
}
