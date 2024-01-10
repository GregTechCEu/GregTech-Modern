package com.gregtechceu.gtceu.client.forge;

import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.forge.CommonProxyImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class ClientProxyImpl extends CommonProxyImpl {

    public ClientProxyImpl() {
        super();
        ClientProxy.init();
    }

}
