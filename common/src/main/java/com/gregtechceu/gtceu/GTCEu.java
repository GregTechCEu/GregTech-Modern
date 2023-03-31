package com.gregtechceu.gtceu;

import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GTCEu {
    public static final String MOD_ID = "gtceu";
    public static final String MODID_KUBEJS = "kubejs";
    public static final String MODID_IRIS = "iris";
    public static final String MODID_CREATE = "create";
    public static final String NAME = "GregTechCEu";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static void init() {
        LOGGER.info("{} is initializing on platform: {}", NAME, Platform.platformName());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, FormattingUtil.toLowerCaseUnder(path));
    }

    public static boolean isKubeJSLoaded() {
        return LDLib.isModLoaded(MODID_KUBEJS);
    }

    public static boolean isCreateLoaded() {
        return LDLib.isModLoaded(MODID_CREATE);
    }

    public static boolean isIrisLoaded() {
        return LDLib.isModLoaded(MODID_IRIS);
    }
}
