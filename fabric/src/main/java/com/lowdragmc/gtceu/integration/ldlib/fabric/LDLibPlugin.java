package com.lowdragmc.gtceu.integration.ldlib.fabric;

import com.lowdragmc.gtceu.common.libs.GTSyncedFieldAccessors;
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
