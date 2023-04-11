package com.gregtechceu.gtceu.common.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.CommonProxy;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote CommonProxyImpl
 */
public class CommonProxyImpl {
    public static void onKubeJSSetup() {
        CommonProxy.init();
    }

    public static void init() {
        if (!GTCEu.isKubeJSLoaded()) {
            CommonProxy.init();
        }
    }
}
