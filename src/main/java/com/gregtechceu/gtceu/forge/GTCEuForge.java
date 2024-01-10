package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.forge.ClientProxyImpl;
import com.gregtechceu.gtceu.common.forge.CommonProxyImpl;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(GTCEu.MOD_ID)
public class GTCEuForge {
    public GTCEuForge() {
        GTCEu.init();
        DistExecutor.unsafeRunForDist(() -> ClientProxyImpl::new, () -> CommonProxyImpl::new);
    }

}
