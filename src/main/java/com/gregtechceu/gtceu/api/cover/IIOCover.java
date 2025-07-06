package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;

public interface IIOCover {

    int getTransferRate();

    IO getIo();

    ManualIOMode getManualIOMode();
}
