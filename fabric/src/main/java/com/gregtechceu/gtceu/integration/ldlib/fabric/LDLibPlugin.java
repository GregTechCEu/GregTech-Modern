package com.gregtechceu.gtceu.integration.ldlib.fabric;

import com.gregtechceu.gtceu.common.data.GTSyncedFieldAccessors;
import com.lowdragmc.lowdraglib.fabric.ILDLibPlugin;

/**
 * @author KilaBash
 * @date 2023/3/17
 * @implNote LDLibPlugin
 */
public class LDLibPlugin implements ILDLibPlugin {
    @Override
    public void onLoad() {
        GTSyncedFieldAccessors.init();
    }
}
